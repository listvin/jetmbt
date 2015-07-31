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

/**
 * Created by user on 7/30/15.
 */
public class URLHasher implements Runnable{
    protected BlockingQueue<URL> URLQueue = null;
    WebDriver driver = null;
    Alphabet alphabet = null;
    public URLHasher(BlockingQueue queue){
        this.URLQueue = queue;
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
        Alphabet alphabet = new PostgreSQLAlphabet();
    }

    public void run(){

        try {
            while(true){
                URL url = URLQueue.poll(1000, TimeUnit.MILLISECONDS);
                if(url != null){
                    driver.get(url.toString());
                    String hash = hashPage(driver);
                    alphabet.addURL(url, hash);
                }else{
                    URL update = alphabet.getRandomURL();
                    driver.get(update.toString());
                    String hash = hashPage(driver);
                    alphabet.addURL(update, hash);
                }
            }
        }catch (InterruptedException | SQLException | MalformedURLException e){
            e.printStackTrace();
        }
    }

}
