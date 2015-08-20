import Boxes.JetURL;
import Common.Logger;
import Common.Settings;
import Common.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.*;

import static Common.Utils.hashPage;

/**
 * Created by wimag on 7/30/15.
 */
public class URLHasher implements Runnable{
    private Logger log = new Logger(this, Logger.Level.off, Logger.Level.all);
    private BlockingQueue<JetURL> urlQueue = new LinkedBlockingQueue<>();
    private WebDriver driver;

    public URLHasher(){
        Utils.setUpDriver(driver = new FirefoxDriver());
    }
    public void add(JetURL url){
        urlQueue.add(url);
    }

    public void run(){
        JetURL url;
        try {
            while(!Thread.interrupted()){
                url = urlQueue.poll();
                if (url == null) url = Invoker.alphabet.getRandomURL();
                if (url != null) {
                    driver.get(url.toFullString());
                    Invoker.alphabet.addURL(url, hashPage(driver));
                }
                Thread.sleep(Settings.urlHasherSleep);
            }
        } catch (InterruptedException e) {
            Logger.get(this).info("URLHasher was interrupted.");
        } finally {
            Logger.get(this).report("URLHasher was shutdown successfully.");
        }
    }

}
