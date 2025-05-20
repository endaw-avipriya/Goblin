package in.market.goblin.service;

import com.upstox.ApiException;
import com.upstox.api.TokenResponse;
import io.swagger.client.api.LoginApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {
    @Value("${credentials.file}")
    private String credentialsFile;
    @Value("${access.file}")
    private String accessFile;
    @Autowired
    private Properties credentials;
    private static String accessToken;
    // Initialize API
    LoginApi apiInstance = new LoginApi();
    public void fetchAndStoreAccessTokenForADay() {

            // Authenticate
            try {
                String accessCode = SeleniumAuthService.getAuthorizationCode(credentials);
                TokenResponse token = apiInstance.token("2.0", accessCode, credentials.getProperty("api.key"), credentials.getProperty("api.secret"), credentials.getProperty("redirectUrl"),"authorization_code");
                accessToken = token.getAccessToken();
                writeAccessToken(accessToken);
            } catch (Exception e) {
                System.err.println("Exception when calling LoginApi#authorize");
                e.printStackTrace();
            }
    }
    public void writeAccessToken(String accessToken) {
        try (FileWriter fileWriter = new FileWriter(accessFile)) {
            fileWriter.write(accessToken);
            System.out.println("Content overwritten successfully to " + accessFile);
        } catch (IOException e) {
            System.err.println("An error occurred while appending to the file: " + e.getMessage());
        }
    }
    public String getAccessToken() {
        if(accessToken==null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(accessFile))) {
                return reader.readLine().trim();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else
            return accessToken;
    }
    public void logout() {
        try {
            apiInstance.logout(getAccessToken());
        }catch (ApiException e) {
            System.err.println("An error occurred during logout: " + e.getMessage());
        }
    }
}