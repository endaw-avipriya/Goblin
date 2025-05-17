package in.market.goblin.controller;

//import in.market.goblin.entity.HistoricalData;
//import in.market.goblin.repository.HistoricalDataRepository;
//import jakarta.transaction.Transactional;
import in.market.goblin.service.AccessTokenService;
import in.market.goblin.service.HistoricalDataService;

//import java.util.List;

//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//import in.market.goblin.service.MarketDepthService;

//@Transactional
@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

	
	@Autowired
	private HistoricalDataService historicalService;
	@Autowired
	private AccessTokenService accessTokenService;

		/*
		 * @Autowired private MarketDepthService marketDepthService;
		 */
	  @PostMapping("/historical") 
	  public String fetchHistoricalData() {
	  historicalService.fetchAndStoreHistoricalData(); 
	  return "Historical data fetch initiated"; 
	  }
	 /*@GetMapping("/accessToken")
	 public String fetchAccessToken() {
		accessTokenService.fetchAndStoreAccessTokenForADay();
		return "Access Token data fetch initiated";
	 }*/
		/*
		 * @PostMapping("/market-depth") public String startMarketDepthStream() {
		 * marketDepthService.startMarketDepthStream(); return
		 * "Market depth stream initiated"; }
		 * 
		 * @PostMapping("/disconnect") public String disconnectWebSocket() { return
		 * marketDepthService.disconnectWebSocket(); }
		 */
		/*
		 * @Autowired private final HistoricalDataRepository instrumentRepository=null;
		 * 
		 * 
		 * @PostMapping("/saveMarketData") public void saveMarketData(@RequestBody
		 * HistoricalData instrument) throws Exception {
		 * instrumentRepository.save(instrument);
		 * System.out.println("Market data fetched and stored successfully"); }
		 */
	 
}