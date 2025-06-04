
   package in.market.goblin.service;
   
   import com.fasterxml.jackson.databind.ObjectMapper;
   import com.upstox.ApiClient;
   import com.upstox.api.MarketData;
   import com.upstox.feeder.MarketDataStreamerV3;
   import com.upstox.feeder.MarketUpdateV3;
   import com.upstox.feeder.constants.Mode;
   import com.upstox.feeder.listener.OnMarketUpdateV3Listener;
   //import in.market.goblin.entity.MarketDepthData;
   //import in.market.goblin.entity.LiveData;
   //import in.market.goblin.repository.MarketDepthDataRepository;
   import io.swagger.client.api.WebsocketApi;

   //import org.springframework.kafka.core.KafkaTemplate;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;
   
   import java.time.LocalDateTime;
   import java.util.*;
   import java.util.stream.Collectors;

   import static com.upstox.feeder.constants.Mode.FULL_D30;

   //@Slf4j
   @Service
   public class MarketDepthService {
      @Autowired
      private Properties credentials;
      @Autowired
      private RedisPublisher publisher;
      // Initialize SmartAPI
      private ApiClient apiClient = new ApiClient();
      private Set<String> instrumentKeys;
      private MarketDataStreamerV3 feed;
      private static Map<MarketDataStreamerV3, Set<String>> feeds = new HashMap<>();;
      public void startMarketDepthStream(Set<String> instruments) {
         instrumentKeys = instruments; //Set.of("NSE_EQ|INE002A01018");//, "NSE_EQ|INE090A01021", "NSE_EQ|INE040A01034");
         feed = new MarketDataStreamerV3(apiClient, instrumentKeys, Mode.FULL_D30);
         feeds.put(feed,instrumentKeys);
         startTBTStream();
      }
      public void disconnectMarketDepthStream(Set<String> instruments) {
         MarketDataStreamerV3 feedStream = null;
         for (Map.Entry<MarketDataStreamerV3, Set<String>> entry : feeds.entrySet()) {
            Set<String> streamerInstrument = entry.getValue();
            Set<String> intersection = new HashSet<>(streamerInstrument);
            intersection.retainAll(instruments);
            if (!intersection.isEmpty()) {
               feedStream = entry.getKey();
            }
         }
         try {
            if(feedStream != null)
               feedStream.disconnect();
            else
               System.out.println("Disconnect failed as instruments are not subscribed");
         } catch (Exception e) {
               throw new RuntimeException(e);
         }

      }
      public void startTBTStream() {
         try {
            OnMarketUpdateV3Listener onMarketUpdateListener = (bytes, receivedTime) -> {
               publisher.publish(bytes, receivedTime);
            };
            feed.setOnMarketUpdateListener(onMarketUpdateListener);
            feed.connect();

         } catch (Exception e) {
             throw new RuntimeException(e);
         }
      }
   }