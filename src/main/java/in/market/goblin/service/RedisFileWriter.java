package in.market.goblin.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
//import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisFileWriter {

    @Value("${writer.output.dir}")
    private String outputDir;
    private final Queue<byte[]> bufferQueue = new ConcurrentLinkedQueue<>();
    private volatile int maxCapacity;
    private final int batchSize = 20;
    private final double triggerThreshold = 0.7;
    private final AtomicInteger fileCounter = new AtomicInteger(1);
    private final ThreadPoolExecutor writerPool;
    private final Thread aggregatorThread;
    private volatile boolean running = true;
    private static final int FILE_COUNT = 7;
    private final AtomicInteger fileIndex = new AtomicInteger(0);

    private final AtomicInteger droppedTicks = new AtomicInteger(0);
    private final AtomicInteger maxObservedQueueSize = new AtomicInteger(0);

    public RedisFileWriter(@Value("${writer.buffer.initial-capacity}") int initialCapacity) {
        this.maxCapacity = initialCapacity;

        writerPool = new ThreadPoolExecutor(
                1,
                Runtime.getRuntime().availableProcessors() * 2,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());

        aggregatorThread = new Thread(this::runAggregator, "BatchAggregatorThread");
        aggregatorThread.setDaemon(true);
        //aggregatorThread.start();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::adjustCapacityBasedOnMemory, 0, 10, TimeUnit.SECONDS);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::printMetrics, 0, 5, TimeUnit.SECONDS);
    }
    @PostConstruct
    public void init() {
        log.info("RedisFileWriter initialized, starting aggregator thread...");
        aggregatorThread.start(); // ← Starts after Spring context is fully ready
    }

    public void enqueue(byte[] tickData) {
        int currentSize = bufferQueue.size();
        maxObservedQueueSize.updateAndGet(prev -> Math.max(prev, currentSize));
        if (currentSize < maxCapacity) {
            bufferQueue.offer(tickData);
        } else {
            droppedTicks.incrementAndGet();
        }
    }

    private void runAggregator() {

        while (writerPool.getActiveCount() >= writerPool.getMaximumPoolSize()) {
            try {
                Thread.sleep(1); // Wait briefly before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        try {
            while (running) {
                int size = bufferQueue.size();

                if (size >= batchSize) {
                    if (size >= maxCapacity * triggerThreshold) {
                        int batches = Math.min((int) Math.ceil((double) size / batchSize), writerPool.getMaximumPoolSize());
                        for (int i = 0; i < batches; i++) {
                            writerPool.submit(this::flushToFile);
                        }
                    } else {
                        writerPool.submit(this::flushToFile);
                    }
                }else{
                        try {
                            Thread.sleep(2); // short delay for low CPU usage
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                }
            }
        }catch(Exception e){
            log.info("Aggregator thread encountered an error: {}", e.getMessage(), e);
        }
    }

    private void flushToFile() {
        List<byte[]> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            byte[] tick = bufferQueue.poll();
            if (tick == null) break;
            batch.add(tick);
        }

        if (!batch.isEmpty()) {
            int index = fileIndex.getAndUpdate(i -> (i + 1) % FILE_COUNT);
            String filename = String.format("%s\\TBToutput-%d.pb", outputDir,index);
            long start = System.nanoTime();
            writeBatchToFileAsync(batch, filename);
            long latencyMicros = (System.nanoTime() - start) / 1000;
            log.info("Queued {} ticks to {} in {} µs", batch.size(), filename, latencyMicros);
        }
    }

    private void writeBatchToFileAsync(List<byte[]> batch, String filename) {
        try {
            // Step 1: Calculate total size = sum of (4 bytes length + data length)
            int totalSize = batch.stream().mapToInt(b -> 4 + b.length).sum();
            ByteBuffer compositeBuffer = ByteBuffer.allocate(totalSize);

            // Step 2: Add each tick with 4-byte length prefix
            for (byte[] tick : batch) {
                compositeBuffer.putInt(tick.length);  // 4-byte length prefix
                compositeBuffer.put(tick);            // Protobuf message
            }
            compositeBuffer.flip();
            Path path = Paths.get(filename);
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );

            log.info("Starting async write to file: {}", filename);
            channel.write(compositeBuffer, channel.size(), null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    try {
                        channel.close();
                        log.info("Async write completed: {} bytes to {}", result, filename);
                    } catch (IOException e) {
                        log.info("Failed to close async channel: {}", e.getMessage(), e);
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    log.info("Async write failed for {}: {}", filename, exc.getMessage(), exc);
                    try {
                        channel.close();
                    } catch (IOException e) {
                        log.info("Failed to close channel after failure: {}", e.getMessage(), e);
                    }
                }
            });

        } catch (IOException e) {
            log.info("Async write setup failed for {}: {}", filename, e.getMessage(), e);
        }
    }

    private void printMetrics() {
        //if(!bufferQueue.isEmpty())
            log.info("[METRICS] Queue size: {}, Max observed: {}, Dropped ticks: {}, Active writers: {}",
                bufferQueue.size(), maxObservedQueueSize.get(), droppedTicks.get(), writerPool.getActiveCount());
    }

    private void adjustCapacityBasedOnMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        long maxHeap = heapUsage.getMax();
        long usedHeap = heapUsage.getUsed();
        double usageRatio = (double) usedHeap / maxHeap;

        if (usageRatio > 0.8 && maxCapacity > 50000) {
            maxCapacity -= 25000;
            log.warn("[MEMORY] High usage. Reducing maxCapacity to: {}", maxCapacity);
        }
        else if (usageRatio < 0.5 && maxCapacity < 200000 && bufferQueue.size() > maxCapacity * triggerThreshold) {
            maxCapacity += 25000;
            log.info("[MEMORY] Low usage. Increasing maxCapacity to: {}", maxCapacity);
        }
    }

    public void onMessage(byte[] message,  String channel) {
        enqueue(message);
    }
    @PreDestroy
    public void shutdown() {
        running = false;
        writerPool.submit(this::flushToFile);
        aggregatorThread.interrupt();
        writerPool.shutdown();
        try {
            if (!writerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Writer pool did not shut down cleanly within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Shutdown interrupted", e);
        }
    }
}
