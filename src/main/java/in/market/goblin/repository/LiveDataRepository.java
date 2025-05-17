
package in.market.goblin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.market.goblin.entity.LiveData;

public interface LiveDataRepository extends JpaRepository<LiveData, Long> { }
