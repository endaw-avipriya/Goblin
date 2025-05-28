package in.market.goblin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"in.market.goblin", "com.upstox.feeder"})
public class GoblinApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoblinApplication.class, args);
	}

}