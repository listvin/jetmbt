import Common.Logger;
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
 * Created by wimag on 7/30/15.
 */
public class URLHasher implements Runnable{
    private Logger log = new Logger(this, Logger.Level.off, Logger.Level.all);
    protected BlockingQueue<URL> URLQueue = null;
    WebDriver driver;
    Alphabet alphabet;
    public URLHasher(BlockingQueue queue){
        this.URLQueue = queue;
        Utils.setUpDriver(driver = new FirefoxDriver());
        alphabet = new PostgreSQLAlphabet();
    }
    
    public void run(){
        try {
            while(true){
                URL url = URLQueue.poll(1000, TimeUnit.MILLISECONDS);
                if(url != null){
                    driver.get(url.toString());
                    log.report("Acquired " + url.toString() + " as a Hash url parameter.");
                    String hash = hashPage(driver);
                    alphabet.addURL(url, hash);
                }else{
                    URL update = alphabet.getRandomURL();
                    if(update == null){
                        sleep(200);
                        continue;
                    }
                    log.debug("Chosen " + update.toString() + " as a Hash url parameter.");
                    driver.get(update.toString());
                    String hash = hashPage(driver);
                    alphabet.addURL(update, hash);
                }
                sleep(3000);
            }
        }catch (InterruptedException e){
            log.exception(e);
        }
    }

}
