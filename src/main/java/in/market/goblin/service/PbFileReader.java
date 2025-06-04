package in.market.goblin.service;

import com.google.gson.Gson;
import com.google.protobuf.util.JsonFormat;
import com.upstox.feeder.MarketUpdateV3;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PbFileReader {

    /**
     * Reads a .pb file where each tick is prefixed with 4 bytes indicating its length.
     * Returns a list of byte arrays, each representing a single tick.
     */
    public static List<byte[]> readLengthPrefixedPbMessages(String filePath) throws IOException {
        List<byte[]> messages = new ArrayList<>();

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] lengthBuffer = new byte[4]; // Buffer to hold the 4-byte length prefix

            while (inputStream.read(lengthBuffer) == 4) { // Read the 4-byte length prefix
                int length = ByteBuffer.wrap(lengthBuffer).getInt(); // Convert to integer
                byte[] message = new byte[length];
                int bytesRead = inputStream.read(message); // Read the tick data
                if (bytesRead != length) {
                    throw new IOException("Unexpected end of file while reading tick data.");
                }
                messages.add(message);
            }
        }

        return messages;
    }

    public static void main(String[] args) {
        String pathToPbFile = "C:\\Users\\avipr\\Workspaces\\Project_1\\TBTdata\\TBToutput-0.pb"; // Change this to your .pb file path

        try {
            List<byte[]> messages = readLengthPrefixedPbMessages(pathToPbFile);
            System.out.println("Read " + messages.size() + " messages.");
            for (int i = 0; i < messages.size(); i++) {
                System.out.println("Message " + (i + 1) + " size: " + messages.get(i).length + " bytes");
                ByteBuffer buffer = ByteBuffer.wrap(messages.get(i));
                MarketDataFeedV3.FeedResponse feedResponse = MarketDataFeedV3.FeedResponse.parseFrom(buffer);
                String jsonFormat = JsonFormat.printer()
                        .print(feedResponse);
                Gson gson = new Gson();
                MarketUpdateV3 marketUpdate = gson.fromJson(jsonFormat, MarketUpdateV3.class);
                System.out.println(marketUpdate.toString());
            }
        } catch (IOException e) {
            System.err.println("Failed to read .pb file: " + e.getMessage());
        }
    }
}