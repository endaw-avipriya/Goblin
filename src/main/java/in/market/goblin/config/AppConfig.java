package in.market.goblin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
/*import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;*/
import java.util.Properties;

@Configuration
public class AppConfig {
	
	@Value("${credentials.file}") 
	private String credentialsFile;

	@Bean
    public Properties credentials() throws Exception {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))) {
            props.setProperty("api.key", reader.readLine().trim());
            props.setProperty("api.secret", reader.readLine().trim());
            props.setProperty("totp.secret", reader.readLine().trim());
            props.setProperty("user", reader.readLine().trim());
            props.setProperty("pin", reader.readLine().trim());
            props.setProperty("redirectUrl", reader.readLine().trim());
            props.setProperty("driverPath", reader.readLine().trim());
            props.setProperty("loginUrl", reader.readLine().trim());
        }
        return props;
    }
    /*public List<String> fetchKey() {
    	List<String> keys=null;
    	try {
            // Read all lines from the file
            keys = Files.readAllLines(Paths.get(credentialsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    	return keys;
    }*/
}