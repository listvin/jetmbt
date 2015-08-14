import Boxes.Event;
import Boxes.State;
import Boxes.WebHandle;
import Common.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static Common.Selectors.*;

/**
 * Created by wimag on 7/23/15.
 */
public class Scanner {
    private Random random = new Random(Common.Settings.randomSeed);
    private Logger log = new Logger(this, Logger.Level.warning, Logger.Level.warning);
    private WebDriver driver;
    private Alphabet alphabet;
    private BlockingQueue<URL> URLQueue = null;

    /**
     * Constructs Scanner by creating a Firefox (at the moment) driver and logging at "http://localhost:8080/login" with root/root
     */
    public Scanner() {
        driver = new FirefoxDriver();
        Utils.setUpDriver(driver);
        alphabet = new PostgreSQLAlphabet();
    }

    public Scanner(BlockingQueue<URL> queue) {
        this();
        URLQueue = queue;
    }

    /**
     * Quits assigned driver
     */
    public void close() {
        driver.quit();
    }

    /**
     * Determines elementType with given handle in current Webdriver state
     *
     * @param driver
     * @param handle
     * @return ElementType. unknown if driver url didnt match handle
     * @throws NoSuchElementException when element cant be found by its xpath
     */
    public ElementType checkHandleType(WebDriver driver, WebHandle handle, Set<String> oldHashes) {
        try {



            //###### Verifying opened url
            for (int t = 0;
                 !driver.getCurrentUrl().equals(handle.url.toString())
                         && t < Common.Settings.maximumExplicitWait; ++t) {
                log.warning("opened URL: " + driver.getCurrentUrl() + "\n" +
                        "needed URL: " + handle.url.toString() + "\n" +
                        "======NOW TRYING TO SLEEP ONE SECOND======");
                Utils.sleep(1000);
            }

            if (!driver.getCurrentUrl().equals(handle.url.toString())) {
                log.error("URL opened in browser is not corresponding to URL in WebHandle\n - returning .unknown");
                return ElementType.unknown;
            }

            //###### Storing data before tries to interact


//            oldHashes.add(Utils.hashPage(driver));
            String oldWindowHandle = driver.getWindowHandle();

            //###### Performing action
            Event.perform(handle, driver, false);
            //###### Check if alert window exists #TODO if works
            try {
                driver.getTitle();
            } catch (UnhandledAlertException e) {
                // Modal dialog showed
                driver.switchTo().alert().dismiss();
                return ElementType.terminal;
            }


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
            URL newURL = Utils.createURL(driver.getCurrentUrl());

            //TODO The next check seems to be rather boring... Need flow rule for this
            if (newURL == null || !newURL.getHost().equals(handle.url.getHost()))
                return ElementType.terminal;


            //###### Checking if URL have changed
            if (!newURL.equals(handle.url))
                return ElementType.clickable;

            //###### Comparing screenshots
            if (oldHashes.contains(Utils.hashPage(driver)))
                return ElementType.noninteractive;
            else
                return ElementType.clickable;



            //###### Checking writability
            //TODO return writability check instead of simple clicker check above
            //TODO More precise check for writable type. Mb separate?
                /*Further code obviously was bounded inside some try-catches, which are lost now*/
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

        } catch (WebDriverException e) {
            log.exception(e);
            return ElementType.unknown;
        }
    }

    /**
     * @param baseState - state that is desired to be explored
     * @return - list of all interactable items
     */
    private int initialReplayFailCounter = 0;

    public List<WebHandle> scan(State baseState) {
        log.debug(String.format("scan() invoked. baseState.url : %s, baseState.sequence.size() : %d\nStarted generating xpathes...", baseState.url.toString(), baseState.sequence.size()));

        List<WebHandle>
                interactiveHandles = new ArrayList<>(),
                allHandles = new ArrayList<>();


        //###### getting all elements
        if (!baseState.reach(driver)) {
            log.error("Can't reach state in scanner to get initial value for oldHash.");
            return new ArrayList<>();
        }


        //###### queuing url for url hasher
        URL curUrl = Utils.createURL(driver.getCurrentUrl());
        if (URLQueue != null) URLQueue.add(curUrl);

        //###### collecting all xpathes
        for (String xpath : Selectors.getAllXpaths(driver)) {
            allHandles.add(new WebHandle(curUrl, xpath));
        }
        log.debug(String.format("scan() invoked.\n\tbaseState.url : %s\n\tbaseState.sequence.size() : %d\n\tFound %d elements. Started generating xpathes...\n", baseState.url.toString(), baseState.sequence.size(), allHandles.size()));

        //###### saving old page hash
        String oldHash = Utils.hashPage(driver);

        //###### checking if it is possible to trunc!??!?!?!??!?????
        //TODO move it to reach of State
        if (URLQueue != null) {
            List<String> knownHashes4thisPage = new ArrayList<>(alphabet.getHashesByURL(curUrl));
            if (baseState.sequence.size() > 0 && knownHashes4thisPage.contains(oldHash)) {
                log.debug("Trunc to url performed: " + (curUrl != null ? curUrl.toString() : "null"));
                baseState.truncToURL(curUrl);
            }
        }

        log.debug("\t " + allHandles.size() + " xpathes generated. Started testing interactivity...\n");

        //###### Testing interactivity
        Set<String> oldHashes = new HashSet<String>() {{
            add(oldHash);
        }};
        int cc = 0;

        for (WebHandle handle : allHandles) {
            if(cc % 10 == 0) {
                System.out.println("processed " + cc + " handles");
            }
            cc ++;
            log.info("XPATH for element being tested: " + handle.xpath);

            //###### Checking for cached result
            ElementType cachedEltype = null;
            if ((cachedEltype = alphabet.request(handle.url, handle.xpath)) != ElementType.unknown) {
                handle.eltype = cachedEltype;
                if (handle.eltype == ElementType.clickable || handle.eltype == ElementType.writable) {
                    WebElement elem = handle.findElement(driver);
                    if (elem == null || !elem.isDisplayed() || !elem.isEnabled()) {
                        handle.eltype = ElementType.noninteractive;
                    }
                }
                log.info("Fetched from cache as " + handle.eltype.name() + "\n");
            } else {



                //###### initial state replay
                try {
                    baseState.reach(driver);
                } catch (WebDriverException e) {
                    log.error("Smth really bad happened while initial sequence replay.");
                    log.exception(e);
                    if (++initialReplayFailCounter > Common.Settings.initialReplayFailThreshold) {
                        log.error("And this is " + initialReplayFailCounter + "th time, so returning.");
                        return interactiveHandles;
                    } else {
                        log.warning("Maybe I will be more lucky next time.");
                        continue;
                    }
                }
                //###### memorising hash of the page for future checkHandleType requests
                //TODO think of this coefficient
                if(random.nextDouble() < 0.25)
                    oldHashes.add(Utils.hashPage(driver));
                //###### now checking

                handle.eltype = checkHandleType(driver, handle, oldHashes);
                //###### caching
                alphabet.add(handle);
                log.info("Determined as " + handle.eltype.name() + "\n");

            }
            //###### preparing list of WebHandles for return
            //TODO return writables, but don't forget terminal
            switch (handle.eltype) {
                //case writable:
                case clickable:
                case terminal:
                case unknown:
                    interactiveHandles.add(handle);
                    break;
            }
        }
        log.report("Classified " + interactiveHandles.size() + " elements.\n");

        //###### finally, returning
        return interactiveHandles;
    }
}
