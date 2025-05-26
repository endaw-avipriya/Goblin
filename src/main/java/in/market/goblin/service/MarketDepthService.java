
   package in.market.goblin.service;
   
   import com.fasterxml.jackson.databind.ObjectMapper;
   import com.upstox.ApiClient;
   import com.upstox.api.MarketData;
   import com.upstox.feeder.MarketDataStreamerV3;
   import com.upstox.feeder.MarketUpdateV3;
   import com.upstox.feeder.constants.Mode;
   import com.upstox.feeder.listener.OnMarketUpdateV3Listener;
   import in.market.goblin.entity.MarketDepthData;
   import in.market.goblin.entity.LiveData;
   import in.market.goblin.repository.MarketDepthDataRepository;
   import io.swagger.client.api.WebsocketApi;
   import jakarta.transaction.Transactional;
   import in.market.goblin.repository.LiveDataRepository;
   //import jakarta.annotation.PostConstruct;
   import org.springframework.kafka.core.KafkaTemplate;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;
   
   import java.time.LocalDateTime;
   import java.util.ArrayList;
   import java.util.Arrays; import java.util.Collections;
   import java.util.HashSet;
   import java.util.List;
   import java.util.Properties;
   import java.util.Set;
   import java.util.concurrent.CompletableFuture;
   import java.util.concurrent.ExecutorService;
   import java.util.concurrent.Executors;
   import java.util.stream.Collectors;

   import static com.upstox.feeder.constants.Mode.FULL_D30;

   //@Slf4j
   @Service
   public class MarketDepthService {
      //private KafkaTemplate<String, MarketUpdateV3> kafkaTemplate;
      private LiveDataRepository liveDataRepository;
      private MarketDepthDataRepository marketDepthDataRepository;
      @Autowired
      private Properties credentials;
      // Initialize SmartAPI
      private ApiClient apiClient = new ApiClient();
      private Set<String> instrumentKeys = Set.of("NSE_EQ|INE002A01018");//, "NSE_EQ|INE090A01021", "NSE_EQ|INE040A01034");
      private MarketDataStreamerV3 feed = new MarketDataStreamerV3(apiClient, instrumentKeys, Mode.FULL);

      public MarketDepthService(LiveDataRepository liveDataRepository, MarketDepthDataRepository marketDepthDataRepository) {
         this.liveDataRepository = liveDataRepository;
         this.marketDepthDataRepository = marketDepthDataRepository;
      }
      public void disconnectMarketDepthStream() {
         try {
            feed.disconnect();
         }catch(Exception e){
            throw new RuntimeException(e);
         }
      }
      public void startMarketDepthStream() {
         try {
            OnMarketUpdateV3Listener onMarketUpdateListener = (marketUpdate) -> {
               System.out.println(marketUpdate.toString());
               /*try { ObjectMapper mapper = new ObjectMapper();
                  String json = mapper.writeValueAsString(marketUpdate);
                  kafkaTemplate.send("snapquote", String.valueOf(marketUpdate.getCurrentTs()), marketUpdate);
                  //log.debug("Published SnapQuote to Kafka for sequenceNumber: {}", marketUpdate.getCurrentTs());
               } catch (Exception e) {
                  System.err.println(e.getMessage());
               }*/
            };
            feed.setOnMarketUpdateListener(onMarketUpdateListener);
            feed.connect();

         } catch (Exception e) {
             throw new RuntimeException(e);
         }
      }
   }