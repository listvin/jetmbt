import Common.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static Common.Utils.hashPage;
import static Common.Utils.sleep;

/**
 * Created by user on 7/30/15.
 */
public class URLHasher implements Runnable{
    protected BlockingQueue<URL> URLQueue = null;
    WebDriver driver = null;
    Alphabet alphabet = null;
    public URLHasher(BlockingQueue queue){
        this.URLQueue = queue;
        driver = new FirefoxDriver();
        Utils.setUpDriver(driver);
        alphabet = new PostgreSQLAlphabet();
    }

    public void run(){

        try {
            while(true){
                URL url = URLQueue.poll(1000, TimeUnit.MILLISECONDS);
                driver.get("http://vk.com");
                if(url != null){
                    driver.get(url.toString());
                    System.out.println("Aquired " + url.toString() + " as a Hash url parameter");
                    String hash = hashPage(driver);
                    alphabet.addURL(url, hash);
                }else{
                    URL update = alphabet.getRandomURL();
                    if(update == null){
                        sleep(200);
                        continue;
                    }
                    System.out.println("Chosen " + update.toString() + " as a Hash url parameter");
                    driver.get(update.toString());
                    String hash = hashPage(driver);
                    alphabet.addURL(update, hash);
                }
                sleep(2000);
            }
        }catch (InterruptedException | SQLException | MalformedURLException e){
            e.printStackTrace();
        }
    }

}
