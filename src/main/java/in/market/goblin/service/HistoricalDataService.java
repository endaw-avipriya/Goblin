package in.market.goblin.service;

import com.upstox.ApiResponse;
import com.upstox.api.GetHistoricalCandleResponse;
import io.swagger.client.api.HistoryApi;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import in.market.goblin.entity.HistoricalData;
import in.market.goblin.service.SeleniumAuthService;
import in.market.goblin.repository.HistoricalDataRepository;
import com.upstox.api.TokenResponse;
import com.upstox.ApiException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.Properties;

import io.swagger.client.api.LoginApi;

@Service
public class HistoricalDataService {
	/*
	 * @PostConstruct 
	 * public void init() { 
	 * smartapi = new SmartConnect(apiKey);
	 * smartapi.setAccessToken(accessToken); 
	 * }
	 */
	
	@Autowired
    private HistoricalDataRepository repository;

    @Autowired
    private Properties credentials;

    public void fetchAndStoreHistoricalData() {
        try {
            // Initialize API
			HistoryApi apiInstance = new HistoryApi();
			//GetHistoricalCandleResponse data = apiInstance.getHistoricalCandleData("NSE_EQ|INE090A01021","minutes","1","2025-05-16","2025-05-16");
            //LocalDateTime to = LocalDateTime.of(2025, 5, 2, 15, 30);
            //LocalDateTime from = LocalDateTime.of(2025, 1, 1, 9, 15);

            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            //while(from.isBefore(to)) {
	            
	      
	
	            //JSONArray candles = smartConnect.candleData(params);
	            //JSONArray candles = response.getJSONObject("data").getJSONArray("data");
	
				/*
				 * for (int i = 0; i < candles.length(); i++) {
				 * 
				 * JSONArray candle = candles.getJSONArray(i); HistoricalData data = new
				 * HistoricalData(); data.setSymbolToken("2885");
				 * data.setTradingSymbol("RELIANCE-EQ"); OffsetDateTime offsetDateTime =
				 * OffsetDateTime.parse(candle.getString(0),
				 * DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				 * data.setTimestamp(offsetDateTime.toLocalDateTime());
				 * //LocalDateTime.parse(candle.getString(0),
				 * DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
				 * data.setOpen(candle.getDouble(1)); data.setHigh(candle.getDouble(2));
				 * data.setLow(candle.getDouble(3)); data.setClose(candle.getDouble(4));
				 * data.setVolume(candle.getLong(5)); if(i==0 &&
				 * data.getTimestamp().isBefore(to)) { to = data.getTimestamp(); }
				 * repository.save(data); }
				 */
            
            // Logout
            //JSONObject response = smartConnect.logout();
            //System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}