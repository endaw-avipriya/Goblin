package in.market.goblin.controller;

import in.market.goblin.service.AccessTokenService;
import in.market.goblin.service.HistoricalDataService;

//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import in.market.goblin.service.MarketDepthService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


//import in.market.goblin.service.MarketDepthService;

//@Transactional
@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private MarketDepthService marketDepthService;

	@GetMapping("/login")
	public String fetchAccessToken() {
		accessTokenService.fetchAndStoreAccessTokenForADay();
		return "Access Token fetch initiated";
	}
    @DeleteMapping("/logout")
	public String logout() {
		accessTokenService.logout();
		return "Logout successful";
	}

	@PostMapping("/market-feed")
	public String startMarketDepthStream(@RequestBody Set<String> instruments) {
		marketDepthService.startMarketDepthStream(instruments);
		return "Market depth stream initiated";
	}

	@PostMapping("/disconnect") 
	public String disconnectWebSocket(@RequestBody Set<String> instruments) {
		marketDepthService.disconnectMarketDepthStream(instruments);
		return "Market depth stream disconnected";
	}
}