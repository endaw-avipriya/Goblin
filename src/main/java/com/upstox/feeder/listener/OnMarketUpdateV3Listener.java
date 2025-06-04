package com.upstox.feeder.listener;
//import com.upstox.feeder.MarketUpdateV3;
import java.nio.ByteBuffer;

public interface OnMarketUpdateV3Listener {
    void onUpdate(ByteBuffer bytes, long receivedTime);//(MarketUpdateV3 marketUpdate);
}
