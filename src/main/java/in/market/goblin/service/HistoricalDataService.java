package in.market.goblin.service;

import com.upstox.ApiClient;
import com.upstox.ApiResponse;
import com.upstox.api.GetHistoricalCandleResponse;

import com.upstox.api.HistoricalCandleData;
import io.swagger.client.api.HistoryApi;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import in.market.goblin.entity.HistoricalData;
import in.market.goblin.repository.HistoricalDataRepository;
import com.upstox.ApiException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


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
			//String accessToken = AccessTokenService.getAccessToken();
			//ApiClient.setAccessToken(accessToken);
			GetHistoricalCandleResponse response = apiInstance.getHistoricalCandleData("NSE_EQ|INE090A01021","minutes","1","2025-05-09","2025-05-22");
			HistoricalCandleData rawdata = response.getData();
			Object[][] candles =rawdata.getCandles();
			List<HistoricalData> batch = new ArrayList<>();
			for(int i = 0; i < candles.length; i++) {
				 Object[] candle = candles[i];
				 HistoricalData data = new HistoricalData();
				 data.setSymbolToken("4963");//2885
				 data.setTradingSymbol("ICICIBANK");//RELIANCE-EQ
				 OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(candle[0]),DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				 data.setTimestamp(offsetDateTime.toLocalDateTime());
				 //LocalDateTime.parse(candle.getString(0),DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
				 data.setOpen(((Number)candle[1]).doubleValue());
				 data.setHigh(((Number)candle[2]).doubleValue());
				 data.setLow(((Number)candle[3]).doubleValue());
				 data.setClose(((Number)candle[4]).doubleValue());
				 data.setVolume(((Number)candle[5]).longValue());
				 data.setOi(((Number)candle[6]).longValue());
				 /*if(i==0 && data.getTimestamp().isBefore(to))
					 to = data.getTimestamp();*/
				 batch.add(data);
				if (i % 100 == 0) {
					repository.saveAll(batch);
					repository.flush();
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				repository.saveAll(batch);
				repository.flush();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}