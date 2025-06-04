package in.market.goblin.service;

import com.google.gson.Gson;
import com.google.protobuf.util.JsonFormat;
import com.upstox.feeder.MarketUpdateV3;
import com.upstox.feeder.listener.OnMarketUpdateV3Listener;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class RedisSubscriber {
    private long totalBidVolume = 0;
    private long totalAskVolume = 0;
    private long totalMissedTrades = 0;;
    private boolean lastAddedToAsk = true;
    private double lastLtp = 0.0;
    private long lastVtt = 0;

    public void onMessage(byte[] message, String channel) {
        long startProcessing = System.nanoTime();
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message);
            MarketDataFeedV3.FeedResponse feedResponse = MarketDataFeedV3.FeedResponse.parseFrom(buffer);
            String jsonFormat = JsonFormat.printer()
                    .print(feedResponse);
            Gson gson = new Gson();
            MarketUpdateV3 marketUpdate = gson.fromJson(jsonFormat, MarketUpdateV3.class);

            //System.out.println(marketUpdate.toString());
        // Write calculation logic
            if (marketUpdate != null && marketUpdate.getFeeds() != null) {
                marketUpdate.getFeeds().forEach((key, feed) -> {
                    if (feed != null && feed.getFullFeed() != null && feed.getFullFeed().getMarketFF() != null) {
                        MarketUpdateV3.MarketFullFeed marketFullFeed = feed.getFullFeed().getMarketFF();
                        if (marketFullFeed.getLtpc() != null) {
                            double ltp = marketFullFeed.getLtpc().getLtp();
                            long ltq = marketFullFeed.getLtpc().getLtq();
                            long vtt = marketFullFeed.getVtt();
                            // Calculate the difference between vtt and last vtt
                            long vttDifference=0;
                            if(lastVtt != 0) {
                                vttDifference = vtt - lastVtt;

                            }
                            if(vttDifference != 0) {
                                totalMissedTrades += (vttDifference - ltq);
                                // Add ltq based on ltp comparison
                                if (ltp > lastLtp) {
                                    totalAskVolume += ltq;
                                    lastAddedToAsk = true;
                                } else if (ltp < lastLtp) {
                                    totalBidVolume += ltq;
                                    lastAddedToAsk = false;
                                } else {
                                    // Add to the same variable as the last one
                                    if (lastAddedToAsk) {
                                        totalAskVolume += ltq;
                                    } else {
                                        totalBidVolume += ltq;
                                    }
                                }
                            }
                            System.out.println("LTP: " + ltp + ", LTQ: " + ltq + ", VTT: " + vtt + ", Volume: "+ vttDifference + ", totalMissedTrades: "+ totalMissedTrades + ", MissedTrades: " + (vttDifference - ltq));

                            // Update lastLtp and lastVtt
                            lastLtp = ltp;
                            lastVtt = vtt;

                        }
                        if (marketFullFeed.getMarketLevel() != null && marketFullFeed.getMarketLevel().getBidAskQuote() != null) {
                            double totalbearCapital = 0.0;
                            double totalbullCapital = 0.0;
                            double bidP = 0;
                            double askP = 0;
                            for (MarketUpdateV3.Quote quote : marketFullFeed.getMarketLevel().getBidAskQuote()) {
                                 totalbearCapital +=(quote.getBidP() * quote.getBidQ());
                                 totalbullCapital +=(quote.getAskP() * quote.getAskQ());
                                 if(bidP == 0 && askP == 0) {
                                    bidP = quote.getBidP();
                                    askP = quote.getAskP();
                                 }
                                 if(quote.getBidP() < bidP){
                                     bidP = quote.getBidP();
                                 }
                                 if(quote.getAskP() > askP){
                                     askP = quote.getAskP();
                                 }
                            }
                            System.out.println("totalAskVolume: " + totalAskVolume + ", totalBidVolume: " + totalBidVolume + ", Bear Capital: " + totalbearCapital/100000 + " to " + bidP +", Bull Capital: " + totalbullCapital/100000 + " to " + askP);
                        }
                    }
                });
            }



        } catch (Exception e) {
            System.err.println("Error processing trade: " + e.getMessage());
            e.printStackTrace();
        }finally {
            long endProcessing = System.nanoTime();
            System.out.println("Subscriber processing time: " + ((endProcessing - startProcessing) / 1_000_000.0) + " ms");
        }
    }
}