/*
 * package in.market.goblin.service;
 * 
 * import in.market.goblin.entity.MarketDepthData; import
 * in.market.goblin.entity.LiveData; import
 * in.market.goblin.repository.MarketDepthDataRepository; import
 * in.market.goblin.repository.LiveDataRepository; import
 * in.angelbroking.smartapi.smartstream.models.SmartApiBBSInfo; import
 * in.angelbroking.smartapi.smartstream.models.SnapQuote; import
 * lombok.extern.slf4j.Slf4j; import
 * org.springframework.kafka.annotation.KafkaListener; import
 * org.springframework.stereotype.Service; import
 * org.springframework.transaction.annotation.Transactional;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper;
 * 
 * import java.time.LocalDateTime; import java.util.ArrayList; import
 * java.util.List; import java.util.concurrent.CompletableFuture; import
 * java.util.concurrent.ExecutorService; import java.util.concurrent.Executors;
 * 
 * 
 * // Kafka consumer to process SnapQuote messages from the 'snapquote' topic.
 * // Saves liveData, bids, and asks to database concurrently using
 * multithreading.
 * 
 * @Service
 * 
 * @Slf4j public class SnapQuoteConsumer {
 * 
 * private LiveDataRepository liveDataRepository=null; private
 * MarketDepthDataRepository marketDepthDataRepository=null; private final
 * ExecutorService executorService = Executors.newFixedThreadPool(4); // Thread
 * pool for parallel tasks
 * 
 * 
 * 
 * 
 * // Kafka listener to consume SnapQuote messages. // @param snapQuote
 * SnapQuote object
 * 
 * @KafkaListener(topics = "snapquote", groupId = "snapquote-group") public void
 * consumeSnapQuote(SnapQuote snapQuote) { long startTime = System.nanoTime();
 * //ObjectMapper mapper = new ObjectMapper(); //SnapQuote snapQuote =
 * mapper.readValue(snapQuoteJson, SnapQuote.class);
 * log.debug("Consumed SnapQuote for token: {}, sequenceNumber: {}",
 * snapQuote.getToken(), snapQuote.getSequenceNumber());
 * 
 * // Parallel saving with CompletableFuture CompletableFuture<Void>
 * liveDataFuture = CompletableFuture.runAsync( () -> saveLiveData(snapQuote),
 * executorService );
 * 
 * CompletableFuture<Void> bidsFuture = CompletableFuture.runAsync( () ->
 * saveBids(snapQuote), executorService );
 * 
 * CompletableFuture<Void> asksFuture = CompletableFuture.runAsync( () ->
 * saveAsks(snapQuote), executorService );
 * 
 * // Ensure all tasks complete or log partial failure
 * CompletableFuture.allOf(liveDataFuture, bidsFuture, asksFuture)
 * .whenComplete((result, throwable) -> { if (throwable != null) {
 * log.error("Error saving SnapQuote data for sequenceNumber: {}: {}",
 * snapQuote.getSequenceNumber(), throwable.getMessage()); } else {
 * log.debug("Completed saving SnapQuote for sequenceNumber: {}, time: {}ms",
 * snapQuote.getSequenceNumber(), (System.nanoTime() - startTime) /
 * 1_000_000.0); } }) .exceptionally(throwable -> {
 * log.error("Unexpected error for sequenceNumber: {}: {}",
 * snapQuote.getSequenceNumber(), throwable.getMessage()); return null; }); }
 * 
 * 
 * // Saves liveData to the database. // @param snapQuote SnapQuote object
 * // @return Saved liveData entity
 * 
 * @Transactional private void saveLiveData(SnapQuote snapQuote) { // Save to
 * parent table (live_data) try { LiveData liveData = new LiveData();
 * liveData.setSequenceNumber(snapQuote.getSequenceNumber());
 * liveData.setToken(snapQuote.getToken().toString());
 * liveData.setExchangeFeedTimeEpochMillis(snapQuote.
 * getExchangeFeedTimeEpochMillis());
 * liveData.setLastTradedPrice(snapQuote.getLastTradedPrice());
 * liveData.setLastTradedQty(snapQuote.getLastTradedQty());
 * liveData.setAvgTradedPrice(snapQuote.getAvgTradedPrice());
 * liveData.setVolumeTradedToday(snapQuote.getVolumeTradedToday());
 * liveData.setTotalBuyQty(snapQuote.getTotalBuyQty());
 * liveData.setTotalSellQty(snapQuote.getTotalSellQty());
 * liveData.setOpenPrice(snapQuote.getOpenPrice());
 * liveData.setHighPrice(snapQuote.getHighPrice());
 * liveData.setLowPrice(snapQuote.getLowPrice());
 * liveData.setClosePrice(snapQuote.getClosePrice());
 * liveData.setLastTradedTimestamp(snapQuote.getLastTradedTimestamp());
 * liveData.setOpenInterest(snapQuote.getOpenInterest());
 * liveData.setRecStartTime(LocalDateTime.now());
 * liveDataRepository.save(liveData); } catch (Exception e) {
 * log.error("Failed to save liveData for sequenceNumber: {}: {}",
 * snapQuote.getSequenceNumber(), e.getMessage()); throw e; }
 * 
 * }
 * 
 * 
 * // Saves MarketDepth bid entries to the database. // @param snapQuote
 * SnapQuote object //
 * 
 * @Transactional private void saveBids(SnapQuote snapQuote) { try {
 * List<MarketDepthData> bidEntries = new ArrayList<>(); long sequenceNumber =
 * snapQuote.getSequenceNumber(); long startTime = System.nanoTime(); // Save to
 * child table (market_depth) SmartApiBBSInfo[] bids =
 * snapQuote.getBestFiveBuy(); for (int i = 0; i < 5; i++) { MarketDepthData
 * depth = new MarketDepthData(); depth.setSequenceNumber(sequenceNumber);
 * depth.setBuySellFlag(bids[i].getBuySellFlag());
 * depth.setPrice(bids[i].getPrice()); depth.setQuantity(bids[i].getQuantity());
 * depth.setNumberOfOrders(bids[i].getNumberOfOrders());
 * depth.setRecStartTime(LocalDateTime.now()); bidEntries.add(depth); }
 * 
 * if (!bidEntries.isEmpty()) { marketDepthDataRepository.saveAll(bidEntries);
 * log.debug("Saved {} bids for sequenceNumber: {}, time: {}ms",
 * bidEntries.size(), sequenceNumber, (System.nanoTime() - startTime) /
 * 1_000_000.0);
 * 
 * } }catch (Exception e) {
 * log.error("Failed to save bids for sequenceNumber: {}: {}",
 * snapQuote.getSequenceNumber(), e.getMessage()); throw e; } }
 * 
 *//**
	 * Saves MarketDepth ask entries to the database.
	 * 
	 * @param snapQuote SnapQuote object
	 *//*
		 * @Transactional private void saveAsks(SnapQuote snapQuote) { try {
		 * List<MarketDepthData> askEntries = new ArrayList<>(); long startTime =
		 * System.nanoTime(); long sequenceNumber = snapQuote.getSequenceNumber();
		 * SmartApiBBSInfo[] asks = snapQuote.getBestFiveSell(); for (int i = 0; i < 5;
		 * i++) { MarketDepthData depth = new MarketDepthData();
		 * depth.setSequenceNumber(sequenceNumber);
		 * depth.setBuySellFlag(asks[i].getBuySellFlag());
		 * depth.setPrice(asks[i].getPrice()); depth.setQuantity(asks[i].getQuantity());
		 * depth.setNumberOfOrders(asks[i].getNumberOfOrders());
		 * depth.setRecStartTime(LocalDateTime.now()); askEntries.add(depth); }
		 * 
		 * if (!askEntries.isEmpty()) { marketDepthDataRepository.saveAll(askEntries);
		 * log.debug("Saved {} bids for sequenceNumber: {}, time: {}ms",
		 * askEntries.size(), sequenceNumber, (System.nanoTime() - startTime) /
		 * 1_000_000.0); } } catch (Exception e) {
		 * log.error("Failed to save bids for sequenceNumber: {}: {}",
		 * snapQuote.getSequenceNumber(), e.getMessage()); throw e; } }
		 * 
		 * }
		 */