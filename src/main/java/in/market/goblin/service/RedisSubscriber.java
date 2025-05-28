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
    private double totalBidVolume = 0.0;
    private double totalAskVolume = 0.0;
    private OnMarketUpdateV3Listener onMarketUpdateListener;
    public void setOnMarketUpdateListener(OnMarketUpdateV3Listener onMarketUpdateListener) {
        this.onMarketUpdateListener = onMarketUpdateListener;
    }
    public void onMessage(byte[] message, String channel) {
        try {
            // Parse byte array to Trade
            ByteBuffer buffer = ByteBuffer.wrap(message);
            MarketDataFeedV3.FeedResponse feedResponse = MarketDataFeedV3.FeedResponse.parseFrom(buffer);
            String jsonFormat = JsonFormat.printer()
                    .print(feedResponse);
            Gson gson = new Gson();
            MarketUpdateV3 marketUpdate = gson.fromJson(jsonFormat, MarketUpdateV3.class);

            if (onMarketUpdateListener != null) {
                onMarketUpdateListener.onUpdate(marketUpdate);
            }
        } catch (Exception e) {
            System.err.println("Error processing trade: " + e.getMessage());
            e.printStackTrace();
        }
    }
}