
  package in.market.goblin.entity;
  
  import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id; import jakarta.persistence.Table; 
  import lombok.Data;
  
 //import java.time.LocalDateTime;
//import java.util.Arrays;
  
  @Entity
  @Table(name = "market_depth_data")
  @Data 
  public class MarketDepthData {
  
  @Id
  @GeneratedValue
  private long id;
  private long sequenceNumber; 
  //siBbBuySellFlag = 1 buy
  // siBbBuySellFlag = 0 sell
	private short buySellFlag = -1;
	private long quantity = -1;
	private long price = -1;
	private short numberOfOrders = -1;
	private LocalDateTime recStartTime;
	
	public long getId() {
		return id;
	}
	public long getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(long SequenceNumber) {
		this.sequenceNumber = SequenceNumber;
	}
	public short getBuySellFlag() {
		return buySellFlag;
	}
	public void setBuySellFlag(short buySellFlag) {
		this.buySellFlag = buySellFlag;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public long getPrice() {
		return price;
	}
	public void setPrice(long price) {
		this.price = price;
	}
	public short getNumberOfOrders() {
		return numberOfOrders;
	}
	public void setNumberOfOrders(short numberOfOrders) {
		this.numberOfOrders = numberOfOrders;
	}
	public LocalDateTime getRecStartTime() {
		return recStartTime;
	}
	public void setRecStartTime(LocalDateTime recStartTime) {
		this.recStartTime = recStartTime;
	}
 }
 