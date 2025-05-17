package in.market.goblin.service;

import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;

@Service
public class SeleniumAuthService {
 
    public static String getAuthorizationCode(Properties credentials) {
        System.setProperty("webdriver.chrome.driver", credentials.getProperty("driverPath"));
        WebDriver driver = new ChromeDriver(new ChromeOptions().addArguments("--headless"));
        try {
            String loginUrl = credentials.getProperty("loginUrl");
            /*"https://api.upstox.com/v2/login/authorization/dialog?client_id=07c90c0a-a4ae-431f-9100-d80ceee7b92f&redirect_uri=https%3A%2F%2Fwww.google.com%2F&state=&scope=";
            "https://api.upstox.com/v2/login/authorization/dialog?client_id=07c90c0a-a4ae-431f-9100-d80ceee7b92f&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback";*/
            driver.get(loginUrl);
            Thread.sleep(2000);
            String redirectUrl1 = driver.getCurrentUrl();
            driver.findElement(By.id("mobileNum")).sendKeys(credentials.getProperty("user"));
            driver.findElement(By.id("getOtp")).click();
            Thread.sleep(1000);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            int totpCode = gAuth.getTotpPassword(credentials.getProperty("totp.secret"));
            Thread.sleep(1000);
            CharSequence otp = String.valueOf(totpCode);
            driver.findElement(By.id("otpNum")).sendKeys(otp);
            Thread.sleep(2000);
            driver.findElement(By.id("continueBtn")).click();
            Thread.sleep(2000);
            CharSequence pin = credentials.getProperty("pin");
            driver.findElement(By.id("pinCode")).sendKeys(pin);
            Thread.sleep(2000);
            driver.findElement(By.id("pinContinueBtn")).click();
            Thread.sleep(2000);
            String redirectUrl = driver.getCurrentUrl();
             return redirectUrl.split("code=")[1].split("&")[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to get authorization code", e);
        } finally {
            driver.quit();
        }
    }
}