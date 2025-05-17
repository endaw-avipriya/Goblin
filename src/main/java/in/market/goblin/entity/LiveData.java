package in.market.goblin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id; import jakarta.persistence.Table; 
import lombok.Data;
import java.time.LocalDateTime;
//import in.angelbroking.smartapi.smartstream.models.TokenID;

@Entity
@Table(name = "live_data")
@Data
public class LiveData {
		
		@Id 
	 	private long sequenceNumber;
	 	private String token;
		private long exchangeFeedTimeEpochMillis;
		private long lastTradedPrice;
		private long lastTradedQty;
		private long avgTradedPrice;
		private long volumeTradedToday;
		private double totalBuyQty;
		private double totalSellQty;
		private long openPrice;
		private long highPrice;
		private long lowPrice;
		private long closePrice;
		private long lastTradedTimestamp = 0;
		private long openInterest = 0;
		private LocalDateTime recStartTime;
		
		public long getSequenceNumber() {
			return sequenceNumber;
		}
		public void setSequenceNumber(long sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public long getExchangeFeedTimeEpochMillis() {
			return exchangeFeedTimeEpochMillis;
		}
		public void setExchangeFeedTimeEpochMillis(long exchangeFeedTimeEpochMillis) {
			this.exchangeFeedTimeEpochMillis = exchangeFeedTimeEpochMillis;
		}
		public long getLastTradedPrice() {
			return lastTradedPrice;
		}
		public void setLastTradedPrice(long lastTradedPrice) {
			this.lastTradedPrice = lastTradedPrice;
		}
		public long getLastTradedQty() {
			return lastTradedQty;
		}
		public void setLastTradedQty(long lastTradedQty) {
			this.lastTradedQty = lastTradedQty;
		}
		public long getAvgTradedPrice() {
			return avgTradedPrice;
		}
		public void setAvgTradedPrice(long avgTradedPrice) {
			this.avgTradedPrice = avgTradedPrice;
		}
		public long getVolumeTradedToday() {
			return volumeTradedToday;
		}
		public void setVolumeTradedToday(long volumeTradedToday) {
			this.volumeTradedToday = volumeTradedToday;
		}
		public double getTotalBuyQty() {
			return totalBuyQty;
		}
		public void setTotalBuyQty(double totalBuyQty) {
			this.totalBuyQty = totalBuyQty;
		}
		public double getTotalSellQty() {
			return totalSellQty;
		}
		public void setTotalSellQty(double totalSellQty) {
			this.totalSellQty = totalSellQty;
		}
		public long getOpenPrice() {
			return openPrice;
		}
		public void setOpenPrice(long openPrice) {
			this.openPrice = openPrice;
		}
		public long getHighPrice() {
			return highPrice;
		}
		public void setHighPrice(long highPrice) {
			this.highPrice = highPrice;
		}
		public long getLowPrice() {
			return lowPrice;
		}
		public void setLowPrice(long lowPrice) {
			this.lowPrice = lowPrice;
		}
		public long getClosePrice() {
			return closePrice;
		}
		public void setClosePrice(long closePrice) {
			this.closePrice = closePrice;
		}
		public long getLastTradedTimestamp() {
			return lastTradedTimestamp;
		}
		public void setLastTradedTimestamp(long lastTradedTimestamp) {
			this.lastTradedTimestamp = lastTradedTimestamp;
		}
		public long getOpenInterest() {
			return openInterest;
		}
		public void setOpenInterest(long openInterest) {
			this.openInterest = openInterest;
		}
		public LocalDateTime getRecStartTime() {
			return recStartTime;
		}
		public void setRecStartTime(LocalDateTime recStartTime) {
			this.recStartTime = recStartTime;
		}
}
