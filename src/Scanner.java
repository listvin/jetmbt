import Boxes.JetURL;
import Boxes.State;
import Boxes.WebHandle;
import Common.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wimag on 7/23/15.
 */
public class Scanner {
    private Random random = new Random(Common.Settings.randomSeed);
    private Logger log = new Logger(this, Logger.Level.debug, Logger.Level.debug);
    private WebDriver driver;

    /**
     * Constructs Scanner by creating a Firefox (at the moment) driver and logging at "http://localhost:8080/login" with root/root
     */
    public Scanner() {
        Utils.setUpDriver(driver = new FirefoxDriver());
    }

    /**Quits from assigned driver*/
    public void close() {
        driver.quit();
    }

    /**
     * Determines elementType with given handle in current WebDriver state
     * explicitWaitLeft supposed to be set
     * @param driver
     * @param handle
     * @return ElementType. unknown if driver url didnt match handle
     * @throws NoSuchElementException when element cant be found by its xpath
     */
    private int explicitWaitLeft;
    private ElementType checkHandleType(WebDriver driver, WebHandle handle, Set<String> oldHashes) { Logger.init();
        try {
            //###### Sleeping in case of problems with JetURL
            for (int t = 0;
                 !JetURL.compare(handle.url, driver.getCurrentUrl())
                        && t < Common.Settings.maximumSingleExplicitWait
                        && explicitWaitLeft > 0; ++t) {
                log.warning("opened JetURL: " + driver.getCurrentUrl() + "\n" +
                        "needed JetURL: " + handle.url.toFullString() + "\n" +
                        "======NOW TRYING TO SLEEP ONE SECOND======");
                Utils.sleep(1000);
                --explicitWaitLeft;
            }
            Logger.cpd("got url");

            //###### Verifying opened url
            if (JetURL.compare(handle.url, driver.getCurrentUrl())) {
                explicitWaitLeft = Settings.maximumExplicitWaitInARow;
            } else {
                if (JetURL.weaklyCompare(handle.url, driver.getCurrentUrl())){
                    log.warning("Opened and needed links seems to be similar, but not as expected:\n" +
                            "opened JetURL: " + driver.getCurrentUrl() + "\n" +
                            "needed JetURL: " + handle.url.toFullString() + "\n" +
                            "Trying to go ahead.");
                } else {
                    log.error("URL opened in browser is not corresponding to URL in WebHandle\n - returning .unknown");
                    return ElementType.unknown;
                }
            }
            Logger.cpd("verified opened url");

            //###### Storing data before tries to interact


//            oldHashes.add(Utils.hashPage(driver));
            String oldWindowHandle = driver.getWindowHandle();
            Logger.cpd("captured initial screenshot");

            //###### Performing action
            WebElement element2check = handle.findElement(driver);
            if (element2check != null)
                try{
                    element2check.click();
                } catch (WebDriverException wde) {
                    return ElementType.noninteractive;
                }
            else {
                System.out.println("wolololo");

                return ElementType.unknown; //smth bad happened and we lost him
            }
            Logger.cpd("performed action (click for now)");

            //###### Check if alert window exists
            try {
                driver.getTitle();
            } catch (UnhandledAlertException e) {
                // Modal dialog showed
                driver.switchTo().alert().dismiss();
                return ElementType.terminal;
            }
            Logger.cpd("checked alert window");


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
            Logger.cpd("Have checked if new windows opened.");

            //###### Collecting data after interaction
            JetURL newURL = new JetURL(driver.getCurrentUrl());

            //TODO The next check seems to be rather boring... Need flow rule for this
            if (newURL == null || !newURL.getHost().equals(handle.url.getHost()))
                return ElementType.terminal;


            //###### Checking if URL have changed
            if (!newURL.equals(handle.url))
                return ElementType.clickable;

            //###### Taking second screenshot
            String newHash = Utils.hashPage(driver);
            Logger.cpd("2nd screen captured and WILL be compared with 1st");

            //###### returning
            if (oldHashes.contains(newHash))
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

    public List<WebHandle> scan(State baseState) { Logger.init();
        driver.manage().deleteCookieNamed("YTSESSIONID");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Utils.login(driver);

        explicitWaitLeft = Settings.maximumExplicitWaitInARow;
        log.debug(String.format("scan() invoked. baseState.url : %s, baseState.sequence.size() : %d\nStarted generating xpathes...", baseState.url.graphUrl(), baseState.sequence.size()));

        List<WebHandle>
                interactiveHandles = new ArrayList<>(),
                allHandles = new ArrayList<>();


        //###### reaching desired state
        if (!baseState.reach(driver)) {
            log.error("Can't reach state in scanner to get initial value for oldHash.");
            return new ArrayList<>();
        }
        Logger.cpd("have reached initial state");


        //###### queuing url for url hasher
        JetURL curUrl = new JetURL(driver.getCurrentUrl());
        Invoker.urlHasher.add(curUrl);

        //###### collecting all xpathes

        //TODO: remove #HARDCODE wait
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String xpath : Selectors.getAllXpaths(driver)) {
            allHandles.add(new WebHandle(curUrl, xpath));
        }
        Logger.cpd("Xpaths generated");
        log.debug("\t " + allHandles.size() + " Xpaths generated. Started testing interactivity...\n");

        //###### saving old page hash
        String oldHash = Utils.hashPage(driver);
        Logger.cpd("hashed old page");

        //###### checking if it is possible to trunc!??!?!?!??!?????
        //TODO move it to reach of State
        List<String> knownHashes4thisPage = new ArrayList<>(Invoker.alphabet.getHashesByURL(curUrl));
        if (baseState.sequence.size() > 0 && knownHashes4thisPage.contains(oldHash)) {
            log.debug("Trunc to url performed: " + (curUrl != null ? curUrl.toFullString() : "null"));
            baseState.truncToURL(curUrl);
        }
        Logger.cpd("checked whether its possible to trunc sequence.");

        //###### Testing interactivity
        Set<String> oldHashes = new HashSet<String>() {{
            add(oldHash);
        }};

        int cc = 0;
        for (WebHandle handle : allHandles) { Logger.init("for");
            if(cc++ % 10 == 0) log.debug("processed " + cc + " handles");
            log.info("XPATH for element being tested: " + handle.xpath);

            //###### Checking for cached result
            ElementType cachedEltype = null;
            if ((cachedEltype = Invoker.alphabet.request(handle.url, handle.xpath)) != ElementType.unknown) {
                handle.eltype = cachedEltype;
                if (handle.eltype == ElementType.clickable || handle.eltype == ElementType.writable) {
                    WebElement elem = handle.findElement(driver);
                    if (elem == null || !elem.isDisplayed() || !elem.isEnabled()) {
                        handle.eltype = ElementType.noninteractive;
                    }
                }
                log.info("Fetched from SQL as " + handle.eltype.name() + "\n");
                Logger.cp("for", "fetched from SQL");
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
                Invoker.alphabet.add(handle);
                log.info("Determined as " + handle.eltype.name() + "\n");
                Logger.cp("for", "resolved with help of checker and pushed to SQL");
            }
            //###### preparing list of WebHandles for return
            //TODO return writables, but don't forget terminal
            switch (handle.eltype) {
                //case writable:
                case clickable:
                case terminal:
                //case unknown:
                    interactiveHandles.add(handle);
                    break;
            }
        }
        log.report("Classified " + interactiveHandles.size() + " elements.\n");

        //###### finally, returning
        Logger.cpd("returning");
        return interactiveHandles;
    }
}
