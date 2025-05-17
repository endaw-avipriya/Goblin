
  package in.market.goblin.repository;
  
  import org.springframework.data.jpa.repository.JpaRepository;
  
  import in.market.goblin.entity.MarketDepthData;
  
  public interface MarketDepthDataRepository extends JpaRepository<MarketDepthData, Long> { }
 