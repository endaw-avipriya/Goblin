package in.market.goblin.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Table(name = "TBT_historical_data")
@Data
public class HistoricalData {
    
	@Id
	@GeneratedValue
	private long id;
    private String symbolToken;
    private String tradingSymbol;
    private LocalDateTime timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private long oi;
    public HistoricalData() {
    }

    // Parameterized constructor to initialize all fields
    public HistoricalData(String symbolToken, String tradingSymbol, LocalDateTime timestamp, double open, double high, double low,double close, long volume, long oi) {
        this.symbolToken = symbolToken;
        this.tradingSymbol = tradingSymbol;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
		this.oi = oi;
    }
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSymbolToken() {
		return symbolToken;
	}

	public void setSymbolToken(String symbolToken) {
		this.symbolToken = symbolToken;
	}

	public String getTradingSymbol() {
		return tradingSymbol;
	}

	public void setTradingSymbol(String tradingSymbol) {
		this.tradingSymbol = tradingSymbol;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getOi() {return oi;}

	public void setOi(long oi) {
		this.oi = volume;
	}

}