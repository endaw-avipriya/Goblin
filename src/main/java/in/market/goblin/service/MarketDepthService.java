
   package in.market.goblin.service;
   
   import in.angelbroking.smartapi.SmartConnect; import
   in.angelbroking.smartapi.http.exceptions.SmartAPIException; import
   in.angelbroking.smartapi.smartstream.models.Depth; import
   in.angelbroking.smartapi.smartstream.models.ExchangeType; import
   in.angelbroking.smartapi.smartstream.models.SmartApiBBSInfo; import
   in.angelbroking.smartapi.smartstream.models.SmartStreamError; import
   in.angelbroking.smartapi.smartstream.models.SmartStreamSubsMode; import
   in.angelbroking.smartapi.smartstream.models.TokenID; import
   in.angelbroking.smartapi.models.User; import
   in.angelbroking.smartapi.smartstream.ticker.SmartStreamListener;
   //import in.angelbroking.smartapi.smartTicker.SmartWSOnTicks;
   import in.angelbroking.smartapi.smartstream.models.SnapQuote;
   import in.angelbroking.smartapi.smartstream.ticker.SmartStreamTicker; import
   in.market.goblin.entity.MarketDepthData; import
   in.market.goblin.entity.LiveData; import
   in.market.goblin.repository.MarketDepthDataRepository;
   import io.swagger.client.api.WebsocketApi;
   import
   jakarta.transaction.Transactional; import
   in.market.goblin.repository.LiveDataRepository;
   //import jakarta.annotation.PostConstruct;
   import lombok.extern.slf4j.Slf4j;
   //import org.springframework.kafka.core.KafkaTemplate;
   
   import com.fasterxml.jackson.databind.ObjectMapper; import
   com.warrenstrange.googleauth.GoogleAuthenticator; import
   org.springframework.beans.factory.annotation.Autowired; import
   org.springframework.stereotype.Service;
   
   import java.time.LocalDateTime; import java.util.ArrayList; import
   java.util.Arrays; import java.util.Collections; import java.util.HashSet;
   import java.util.List; import java.util.Properties; import java.util.Set;
   import java.util.concurrent.CompletableFuture; import
   java.util.concurrent.ExecutorService; import java.util.concurrent.Executors;
   import java.util.stream.Collectors;
   

   //@Slf4j
   @Service
   public class MarketDepthService {
   
   //@Autowired private MarketDepthDataRepository repository;
   
   @Autowired
   private Properties credentials;
   // Initialize SmartAPI
       WebsocketApi webSocketApi = new WebsocketApi();
       webSocketApi.getMarketDataFeed("3.0");
   SmartConnect smartConnect = new SmartConnect(); //private final
   KafkaTemplate<String, SnapQuote> kafkaTemplate; private final Set<String>
   SYMBOL_TOKENS = new HashSet<>(Arrays.asList("1333", "4963", "2885")); String
   exchange = "NSE_CM"; private SmartStreamTicker smartStreamTicker; boolean
   isSubscribed = false; private final LiveDataRepository liveDataRepository;
   private final MarketDepthDataRepository marketDepthDataRepository; private
   final ExecutorService executorService = Executors.newFixedThreadPool(4); //
   Thread pool for parallel tasks public MarketDepthService(LiveDataRepository
   liveDataRepository, MarketDepthDataRepository marketDepthDataRepository) {
   log.
   info("Initializing MarketDataService with tickDataRepository={}, marketDepthRepository={}"
   , liveDataRepository, marketDepthDataRepository); this.liveDataRepository =
   liveDataRepository; this.marketDepthDataRepository =
   marketDepthDataRepository; } public void startMarketDepthStream() { try {
   
   
   String apiKey = credentials.getProperty("api.key");
   smartConnect.setApiKey(apiKey);
   
   // Generate TOTP GoogleAuthenticator gAuth = new GoogleAuthenticator(); int
   totpCode = gAuth.getTotpPassword(credentials.getProperty("totp.secret"));
   
   // Authenticate String clientCode = credentials.getProperty("client.code");
   User user = smartConnect.generateSession( clientCode,
   credentials.getProperty("password"), String.format("%06d", totpCode) );
   
   // Initialize WebSocket
   log.info("Initializing SmartStreamTicker for market depth");
   // symbol_token:{}, exchange: {}", symbolToken, exchange); " +"smartStreamTicker = new SmartStreamTicker(user.getAccessToken(), apiKey,clientCode, user.getFeedToken(), new SmartStreamListenerImpl());
   smartStreamTicker.connect();
   
   //SmartStreamTicker ticker = new SmartStreamTicker(user.getAccessToken(),
   apiKey, clientCode, user.getFeedToken());
   
   ticker.setOnDepthUpdate(new OnDepthUpdate() {
   
   @Override public void onDepthUpdate(MarketData marketData) { Depth depth =
   marketData.getDepth(); MarketDepthData data = new MarketDepthData();
   data.setSymbolToken(String.valueOf(marketData.getToken()));
   data.setTradingSymbol(marketData.getTradingSymbol());
   data.setTimestamp(LocalDateTime.now()); data.setId(data.getSymbolToken() +
   "_" + data.getTimestamp().toString());
   
   // Store top 2 bid/ask levels List<Order> bids = depth.getBuy(); List<Order>
   asks = depth.getSell(); if (bids.size() >= 2 && asks.size() >= 2) {
   datsetBidPrice1(bids.get(0).getPrice());
   data.setBidQuantity1(bids.get(0).getQuantity());
   data.setAskPrice1(asks.get(0).getPrice());
   data.setAskQuantity1(asks.get(0).getQuantity());
   data.setBidPrice2(bids.get(1).getPrice());
   data.setBidQuantity2(bids.get(1).getQuantity());
   data.setAskPrice2(asks.get(1).getPrice());
   data.setAskQuantity2(asks.get(1).getQuantity()); }
   
   repository.save(data); } });
   
   
   // Subscribe to market depth for SBIN-EQ
   
   // Keep the connection alive (simplified; in production, manage lifecycle)
   //Thread.sleep(60000); // Run for 1 minute for demo
   //smartStreamTicker.disconnect(); } catch (Exception e) {
   log.error("Failed to initialize SmartStreamTicker: {}", e.getMessage(), e);
   e.printStackTrace(); } }
   
   @Transactional private void saveTickData(SnapQuote snapQuote) { // Save to
   parent table (tick_data) LiveData liveData = new LiveData();
   liveData.setSequenceNumber(snapQuote.getSequenceNumber());
   liveData.setToken(snapQuote.getToken().toString());
   liveData.setExchangeFeedTimeEpochMillis(snapQuote.
   getExchangeFeedTimeEpochMillis());
   liveData.setLastTradedPrice(snapQuote.getLastTradedPrice());
   liveData.setLastTradedQty(snapQuote.getLastTradedQty());
   liveData.setAvgTradedPrice(snapQuote.getAvgTradedPrice());
   liveData.setVolumeTradedToday(snapQuote.getVolumeTradedToday());
   liveData.setTotalBuyQty(snapQuote.getTotalBuyQty());
   liveData.setTotalSellQty(snapQuote.getTotalSellQty());
   liveData.setOpenPrice(snapQuote.getOpenPrice());
   liveData.setHighPrice(snapQuote.getHighPrice());
   liveData.setLowPrice(snapQuote.getLowPrice());
   liveData.setClosePrice(snapQuote.getClosePrice());
   liveData.setLastTradedTimestamp(snapQuote.getLastTradedTimestamp());
   liveData.setOpenInterest(snapQuote.getOpenInterest());
   liveData.setRecStartTime(LocalDateTime.now());
   
   liveDataRepository.save(liveData); }
   
  
	   Saves MarketDepth bid entries to the database.
	   
	   @param snapQuote SnapQuote object


   @Transactional private void saveBids(SnapQuote snapQuote) {
   List<MarketDepthData> bidEntries = new ArrayList<>(); long sequenceNumber =
   snapQuote.getSequenceNumber(); // Save to child table (market_depth)
   SmartApiBBSInfo[] bids = snapQuote.getBestFiveBuy(); for (int i = 0; i < 5;
   i++) { MarketDepthData depth = new MarketDepthData();
   depth.setSequenceNumber(sequenceNumber);
   depth.setBuySellFlag(bids[i].getBuySellFlag());
   depth.setPrice(bids[i].getPrice()); depth.setQuantity(bids[i].getQuantity());
   depth.setNumberOfOrders(bids[i].getNumberOfOrders());
   depth.setRecStartTime(LocalDateTime.now()); bidEntries.add(depth); }
   
   if (!bidEntries.isEmpty()) { marketDepthDataRepository.saveAll(bidEntries); }
   }
   
  
	   Saves MarketDepth ask entries to the database.
	   
	   @param snapQuote SnapQuote object

		   @Transactional private void saveAsks(SnapQuote snapQuote) {
		   List<MarketDepthData> askEntries = new ArrayList<>(); long sequenceNumber =
		   snapQuote.getSequenceNumber(); SmartApiBBSInfo[] asks =
		   snapQuote.getBestFiveSell(); for (int i = 0; i < 5; i++) { MarketDepthData
		   depth = new MarketDepthData(); depth.setSequenceNumber(sequenceNumber);
		   depth.setBuySellFlag(asks[i].getBuySellFlag());
		   depth.setPrice(asks[i].getPrice()); depth.setQuantity(asks[i].getQuantity());
		   depth.setNumberOfOrders(asks[i].getNumberOfOrders());
		   depth.setRecStartTime(LocalDateTime.now()); askEntries.add(depth); }
		   
		   if (!askEntries.isEmpty()) { marketDepthDataRepository.saveAll(askEntries); }
		   } private class SmartStreamListenerImpl implements SmartStreamListener {
		   
		   @Override public void onConnected() {
		   log.info("SmartStreamTicker connected for market depth");
		   
		   if(!isSubscribed) { try { //List<String> tokens = new ArrayList<>();
		   Set<TokenID> tokenIDs = SYMBOL_TOKENS.stream() .map(token -> new
		   TokenID(ExchangeType.valueOf(exchange), token)) .collect(Collectors.toSet());
		   //String symbolToken= "2";
		   
		   //TokenID tokenID = new TokenID(ExchangeType.valueOf(exchange), symbolToken);
		   smartStreamTicker.subscribe(SmartStreamSubsMode.SNAP_QUOTE, tokenIDs);
		   isSubscribed = true;
		   log.info("Subscribed to SnapQuote for symbol_token: {}, exchange: {}",
		   SYMBOL_TOKENS, exchange); } catch (Exception e) {
		   log.error("Failed to subscribe on connection: {}", e.getMessage(), e); } } }
		   
		   @Override public void onDisconnected() {
		   log.warn("SmartStreamTicker disconnected"); isSubscribed = false; }
		   
		   @Override public void onError(SmartStreamError error) {
		   log.error("SmartStreamTicker error: {}", error.getException().getMessage(),
		   error.getException()); }
		   
		   @Override public SmartStreamError onErrorCustom() { SmartStreamError error =
		   new SmartStreamError(); error.setException(new
		   SmartAPIException("Custom WebSocket error occurred"));
		   log.error("SmartStreamTicker custom error"); return error; }
		   
		   @Override public void onPong() {
		   log.debug("Pong received from SmartStreamTicker"); }
		   
		   @Override public void
		   onLTPArrival(in.angelbroking.smartapi.smartstream.models.LTP ltp) { // Not
		   used }
		   
		   @Override public void
		   onQuoteArrival(in.angelbroking.smartapi.smartstream.models.Quote quote) { //
		   Not used }
		   
		   @Override public void onSnapQuoteArrival(SnapQuote snapQuote) {
		   log.debug("Received SnapQuote for token: {}, sequenceNumber: {}",
		   snapQuote.getToken(), snapQuote.getSequenceNumber()); // Publish to Kafka
		   
		   try { //ObjectMapper mapper = new ObjectMapper(); //String json =
		   mapper.writeValueAsString(snapQuote); KafkaTemplate.send("snapquote",
		   String.valueOf(snapQuote.getSequenceNumber()), snapQuote);
		   log.debug("Published SnapQuote to Kafka for sequenceNumber: {}",
		   snapQuote.getSequenceNumber()); } catch (Exception e) {
		   log.error("Failed to publish SnapQuote to Kafka for sequenceNumber: {}: {}",
		   snapQuote.getSequenceNumber(), e.getMessage()); }
		   
		   
		   
		   
		   // Parallel saving with CompletableFuture CompletableFuture<Void>
		   liveDataFuture = CompletableFuture.runAsync( () -> saveTickData(snapQuote),
		   executorService );
		   
		   CompletableFuture<Void> bidsFuture = CompletableFuture.runAsync( () ->
		   saveBids(snapQuote), executorService );
		   
		   CompletableFuture<Void> asksFuture = CompletableFuture.runAsync( () ->
		   saveAsks(snapQuote), executorService );
		   
		   // Handle exceptions CompletableFuture.allOf(liveDataFuture,
		   bidsFuture,asksFuture).exceptionally(throwable -> {
		   log.error("Error saving SnapQuote data: {}", throwable.getMessage(),
		   throwable); return null; });
		   
		   //System.out.println(snapQuote.toString());
		   
		   }
		   
		   @Override public void onDepthArrival(Depth depth) {
		   log.info("Received market depth for symbol_token: {}, exchange: {}",
		   depth.getToken(), depth.getExchangeType()); //processDepth(depth); } } public
		   String disconnectWebSocket() { if (smartStreamTicker != null) { try {
		   smartStreamTicker.disconnect(); isSubscribed = false;
		   log.info("WebSocket disconnected"); return "WebSocket disconnected"; } catch
		   (Exception e) { log.error("Failed to disconnect WebSocket: {}",
		   e.getMessage(), e); return "Failed to disconnect WebSocket"; } } else {
		   log.warn("WebSocket not initialized"); return "WebSocket not initialized"; }
		   } }
