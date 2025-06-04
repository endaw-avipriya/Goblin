package com.upstox.feeder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.upstox.ApiClient;
import com.upstox.feeder.constants.Mode;
import com.upstox.feeder.exception.StreamerException;
import com.upstox.feeder.listener.*;
//import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3.FeedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class MarketDataStreamerV3 extends Streamer {
    //private String TBTdata="C:\\Users\\avipr\\Workspaces\\Project_1\\tbt_data.pb";
    //private static final ByteBuffer NEWLINE = ByteBuffer.wrap(new byte[] { 0x0A });
    private OnMarketUpdateV3Listener onMarketUpdateListener;

    public void setOnMarketUpdateListener(OnMarketUpdateV3Listener onMarketUpdateListener) {
        this.onMarketUpdateListener = onMarketUpdateListener;
    }
    private static final String SOCKET_NOT_OPEN_ERROR = "WebSocket is not open.";
    private static final String INVALID_VALUES_ERROR = "Values provided are invalid.";

    private Map<Mode, Set<String>> subscriptions;

    public MarketDataStreamerV3(ApiClient apiClient) {

        if (apiClient == null) {
            throw new StreamerException(INVALID_VALUES_ERROR);
        }

        this.apiClient = apiClient;
        subscriptions = new HashMap<>();
        subscriptions.put(Mode.LTPC, new HashSet<>());
        subscriptions.put(Mode.FULL, new HashSet<>());
        subscriptions.put(Mode.OPTION_GREEKS, new HashSet<>());
        subscriptions.put(Mode.FULL_D30, new HashSet<>());
    }

    public MarketDataStreamerV3(ApiClient apiClient, Set<String> instrumentKeys, Mode mode) {

        if (apiClient == null || instrumentKeys == null || mode == null) {
            throw new StreamerException(INVALID_VALUES_ERROR);
        }

        this.apiClient = apiClient;
        subscriptions = new HashMap<>();
        subscriptions.put(Mode.LTPC, new HashSet<>());
        subscriptions.put(Mode.FULL, new HashSet<>());
        subscriptions.put(Mode.OPTION_GREEKS, new HashSet<>());
        subscriptions.put(Mode.FULL_D30, new HashSet<>());

        // Populate initial subscriptions if provided
        this.subscriptions.get(mode)
                .addAll(instrumentKeys);
    }

    @Override
    public void connect() {
        feeder = new MarketDataFeederV3(apiClient, new OnOpenListener() {

            @Override
            public void onOpen() {
                handleOpen();

            }
        }, new OnMessageListener() {

            @Override
            public void onMessageAsBytes(ByteBuffer bytes) {
                handleMessage(bytes);

            }

            @Override
            public void onMessageAsString(String message) {

            }
        }, new OnErrorListener() {

            @Override
            public void onError(Throwable error) {
                handleError(error);

            }
        }, new OnCloseListener() {

            @Override
            public void onClose(int statusCode, String reason) {
                handleClose(statusCode, reason);

            }
        });

        feeder.connect();
    }

    public void disconnect() {

        if (feeder != null) {
            disconnectValid = true;
            feeder.disconnect();
            clearSubscriptions();
        } else {
            throw new StreamerException(SOCKET_NOT_OPEN_ERROR);
        }
    }

    public void subscribe(Set<String> instrumentKeys, Mode mode) {

        if (instrumentKeys == null || mode == null) {
            this.handleError(new StreamerException(INVALID_VALUES_ERROR));
            return;
        }

        MarketDataFeederV3 marketDataFeeder = (MarketDataFeederV3) feeder;

        if (marketDataFeeder == null) {
            throw new StreamerException(SOCKET_NOT_OPEN_ERROR);
        }

        subscriptions.get(mode)
                .addAll(instrumentKeys);

        marketDataFeeder.subscribe(instrumentKeys, mode);
    }

    public void unsubscribe(Set<String> instrumentKeys) {

        if (instrumentKeys == null) {
            this.handleError(new StreamerException(INVALID_VALUES_ERROR));
            return;
        }

        MarketDataFeederV3 marketDataFeeder = (MarketDataFeederV3) feeder;

        if (marketDataFeeder == null) {
            throw new StreamerException(SOCKET_NOT_OPEN_ERROR);
        }

        for (Set<String> modeKeys : subscriptions.values()) {
            modeKeys.removeAll(instrumentKeys);
        }

        marketDataFeeder.unsubscribe(instrumentKeys);
    }

    public void changeMode(Set<String> instrumentKeys, Mode newMode) {

        if (instrumentKeys == null || newMode == null) {
            this.handleError(new StreamerException(INVALID_VALUES_ERROR));
            return;
        }

        MarketDataFeederV3 marketDataFeeder = (MarketDataFeederV3) feeder;

        if (marketDataFeeder == null) {
            throw new StreamerException(SOCKET_NOT_OPEN_ERROR);
        }

        for(Set<String> modeKeys : subscriptions.values()) {
            modeKeys.removeAll(instrumentKeys);
        }

        subscriptions.get(newMode)
                .addAll(instrumentKeys);

        marketDataFeeder.changeMode(instrumentKeys, newMode);
    }

    private void subscribeToInitialKeys() {

        MarketDataFeederV3 marketDataFeeder = (MarketDataFeederV3) feeder;

        for (Map.Entry<Mode, Set<String>> entry : subscriptions.entrySet()) {
            Mode mode = entry.getKey();
            Set<String> keys = entry.getValue();

            if (!keys.isEmpty()) {
                marketDataFeeder.subscribe(keys, mode);
            }
        }
    }

    private void clearSubscriptions() {

        for (Set<String> modeKeys : this.subscriptions.values()) {
            modeKeys.clear();
        }
    }

    protected void handleOpen() {
        disconnectValid = false;
        reconnectInProgress = false;
        reconnectAttempts = 0;

        if (onOpenListener != null) {
            onOpenListener.onOpen();
        }

        subscribeToInitialKeys();

    }

    protected void handleMessage(ByteBuffer bytes) {
        long receivedTime = System.nanoTime();
        try {
            // Convert ByteBuffer to byte[] for file output
            /*byte[] input_byte = new byte[bytes.remaining()];
            bytes.get(input_byte);
            File file = new File(TBTdata);
            boolean append = file.exists();
            try (FileOutputStream fos = new FileOutputStream(file, append); FileChannel channel = fos.getChannel()) {
                // Write ByteBuffer directly
                channel.write(bytes);
                fos.write(input_byte);
                // Append newline delimiter
                ByteBuffer newlineBuffer = NEWLINE.duplicate();
                channel.write(newlineBuffer);
                //System.out.println("ByteBuffer protobuf data saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }*/
                if (onMarketUpdateListener != null) {
                    onMarketUpdateListener.onUpdate(bytes, receivedTime);
                }
        } catch (Exception e) {
            handleError(e);
        }
    }
}

