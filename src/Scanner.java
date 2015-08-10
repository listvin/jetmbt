import Boxes.State;
import Boxes.WebHandle;
import Common.*;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static Common.Selectors.*;

/**
 * Created by wimag on 7/23/15.
 */
public class Scanner {
    private Logger log = new Logger(this, Logger.Level.debug, Logger.Level.all);
    private WebDriver driver;
    private Alphabet alphabet_testing;
    private BlockingQueue<URL> URLQueue = null;
    /** Constructs Scanner by creating a Firefox (at the moment) driver and logging at "http://localhost:8080/login" with root/root*/
    public Scanner(){
        driver = new FirefoxDriver();
        Utils.setUpDriver(driver);
        alphabet_testing = new PostgreSQLAlphabet();
    }

    public Scanner(BlockingQueue<URL> queue){
        this();
        URLQueue = queue;
    }
    /** Terminates assigned driver*/
    public void close(){
        driver.quit();
    }



    public ElementType checkHandleType(WebDriver driver, WebHandle handle, String oldhash){
        ArrayList<String> hashes = new ArrayList<>(Collections.singletonList(oldhash));
        return checkHandleType(driver, handle, hashes);
    }
    /**
     * Determines elementType with given handle in current Webdriver state
     * @param driver
     * @param handle
     * @return ElementType. unknown if driver url didnt match handle
     * @throws NoSuchElementException when element cant be found by its xpath
     */
    public ElementType checkHandleType(WebDriver driver, WebHandle handle, List<String> oldHash) throws NoSuchElementException{
        try {
            //###### Verifying opened url
            //TODO remove this crutch
            if (!driver.getCurrentUrl().equals(handle.url.toString())){
                do {
                    log.debug("opened URL: " + driver.getCurrentUrl() + "\n" +
                            "needed URL: " + handle.url.toString() + "\n" +
                            "======NOW TRYING TO SLEEP ONE SECOND======");
                    Utils.sleep(1000);
                }while(!driver.getCurrentUrl().equals(handle.url.toString()));
            }
            //Supposed to used this instead in the future:
//                if (!driver.getCurrentUrl().equals(handle.url.toString())){
//                    throw new WebDriverException("URL opened in browser is not corresponding to URL in WebHandle");
//                }

            //###### Storing data before tries to interact
            if(oldHash == null) {
                oldHash = new ArrayList<>();
                oldHash.add(Utils.hashPage(driver));
            }
            String oldWindowHandle = driver.getWindowHandle();
            driver.findElement(By.xpath(handle.xpath)).click();

            //###### Check if alert window exists
            if (ExpectedConditions.alertIsPresent().apply(driver) != null){
                driver.switchTo().alert().dismiss();
                //TODO allow alerted actions
                return ElementType.terminal;
            }
            /* Old alert test: try {
                driver.switchTo().alert().dismiss();
                return ElementType.terminal;
            } catch (NoAlertPresentException ignored) {} */

            //###### This checks if new windows/tabs were opened
            if (driver.getWindowHandles().size() > 1) {
                for (String windowHandle : driver.getWindowHandles())
                    if (!windowHandle.equals(oldWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        //TODO allow opening new tabs/windows
                        driver.close();
                    }
                driver.switchTo().window(oldWindowHandle);
                return ElementType.terminal;
            }

            //###### Collecting data after interaction
            URL newURL = new URL(driver.getCurrentUrl());

            //TODO The next check seems to be rather boring... Need flow rule for this
            if (!newURL.getHost().equals(handle.url.getHost()))
                return ElementType.terminal;


            //###### Checking if URL have changed
            if (!newURL.equals(handle.url))
                return ElementType.clickable;

            //###### Comparing screenshots
            if (oldHash.contains(Utils.hashPage(driver)))
                return ElementType.noninteractive;
            else
                return ElementType.clickable;

            //###### Checking writability
            //TODO return writability check instead of simple clicker check above
            //TODO More precise check for writable type. Mb separate?
            /*if (driver.findElements(By.xpath(handle.xpath)).size() > 0) {
                String oldValue = handle.findElement(driver).getAttribute("value");
                handle.findElement(driver).sendKeys("aba");
                driver.switchTo().defaultContent();
                String newValue = null;

                if (!driver.findElements(By.xpath(handle.xpath)).isEmpty()) {
                    newValue = driver.findElement(By.xpath(handle.xpath)).getAttribute("value");
                }
                if ((newValue != null) && (!newValue.equals(oldValue))) {
                    return ElementType.writable;
                } else {
                    if (oldHash.equals(newHash)) {
                        return ElementType.noninteractive;
                    }
                    return ElementType.clickable;
                }
            } else {
                return ElementType.clickable;
            }*/

        } catch (ElementNotVisibleException e){
            /*This happens sometimes right after trying to click on found
            element. This is strange cause all handles provided to this method
            are from the list of visible at that moment elements.*/
            return ElementType.unknown;
        } catch (MalformedURLException e){
            /*This is never going to happen because our URL is being constructed
            from the one which is already opened*/
            e.printStackTrace();
            return ElementType.unknown;
        }
    }

    /**
     * @param baseState - state that is desired to be explored
     * @return - list of all interactable items
     */
    public List<WebHandle> scan(State baseState){
        List<WebHandle> interactiveHandles = new ArrayList<>();
        List<WebHandle> allHandles = new ArrayList<>();

        try {
            baseState.reach(driver);
        } catch (ElementNotVisibleException | ElementNotFoundException | NoSuchElementException e){
            log.error("Can't reach state in scanner to get initial value for oldHash.");
            log.exception(e);
            return new ArrayList<>();
        }
        String oldHash = Utils.hashPage(driver);

        List<WebElement> elementList = driver.findElements(By.cssSelector("*"));
        log.debug(String.format("scan() invoked.\n" +
                        "\tbaseState.url : %s\n" +
                        "\tbaseState.sequence.size() : %d\n" +
                        "\tFound %d elements. Started generating xpathes...\n",
                baseState.url.toString(), baseState.sequence.size(), elementList.size()));

        //TODO remove threshold for count of generated xpathes per scan session
        //int i = 0, xpathsThreshold = 80; //#hardcode
        URL curUrl = null;
        try {
            curUrl = new URL(driver.getCurrentUrl());
        } catch (MalformedURLException e) {
            //This MalformedURLException is rather annoying.
            //Why at all we still use the URL class...
            log.exception(e);
        }
        if(URLQueue != null){
            URLQueue.add(curUrl);
        }

        //Testing faster ways
//        for(WebElement element: elementList){
//            if(!element.isDisplayed() || !element.isEnabled()) continue;
//            String xpath = formXPATH(driver, element);
//            WebHandle handle = new WebHandle(curUrl, xpath);
//            allHandles.add(handle);
//            if (++i > xpathsThreshold) break;
//        }

        for(String xpath: Selectors.getAllXPATHs(driver)){
            allHandles.add(new WebHandle(curUrl, xpath));
        }

        List<String> hashes = new ArrayList<>(Collections.singletonList(oldHash));
        Random random = new Random(239);
        if(URLQueue != null){
            List<String> knownHashes = new ArrayList<>(alphabet_testing.getHashesByURL(curUrl));
            if(!baseState.url.equals(curUrl) && !knownHashes.isEmpty() && knownHashes.contains(oldHash)){
                log.debug("Trunc to url performed: " + (curUrl != null ? curUrl.toString() : "null"));
                baseState.truncToURL(curUrl);
            }
        }

        log.debug("\tXpathes generated. Started generating testing interactivity...\n");
        for (WebHandle handle: allHandles){
            log.info("XPATH for element being tested: " + handle.xpath);
            try {
                baseState.reach(driver);
                //###### Checking for cached result
                ElementType cachedEltype = null;
                if ((cachedEltype = alphabet_testing.request(handle.url, handle.xpath)) != ElementType.unknown)
                    handle.eltype = cachedEltype;
                else {
                    if(random.nextDouble() < 0.25) {
                        hashes.add(Utils.hashPage(driver));
                    }
                    handle.eltype = checkHandleType(driver, handle, hashes);
                    alphabet_testing.add(handle);
                }
            }catch (ElementNotVisibleException | ElementNotFoundException | NoSuchElementException e){
                //TODO: after hovering over button tooltip appears changing page structure(Extra div) => couldn't handle element.
                log.error("failed while trying to get new hash");
                log.exception(e);
                continue;
            }

          log.info("Determined as " + handle.eltype.name() + "\n");
            //TODO return writables, but don't forget terminal
//                if(handle.eltype.equals(ElementType.clickable) || handle.eltype.equals(ElementType.writable)) {
            if (handle.eltype == ElementType.clickable || handle.eltype == ElementType.terminal || handle.eltype == ElementType.unknown) {
                interactiveHandles.add(handle);
                //TODO remove interactive elements count limit
                if (interactiveHandles.size() >= 20) return interactiveHandles; //#hardcode
            }
        }
        log.debug("\tFound %d interactive elements.\n" + interactiveHandles.size());
        return interactiveHandles;
    }


    /**Further is left for testing at the moment*/
    public void testXpathSelector(){
        driver.get("http://localhost:8080/issues");
        log.report(Integer.toString(getAllXPATHs(driver).size()));
    }
    public static void main(String args[]) throws MalformedURLException {
        Scanner scanner = new Scanner();
        scanner.testXpathSelector();
        scanner.close();
    }
}
