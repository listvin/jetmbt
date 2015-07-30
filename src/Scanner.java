import Boxes.Sequence;
import Boxes.State;
import Boxes.WebHandle;
import Common.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wimag on 7/23/15.
 */
public class Scanner {
    private WebDriver driver;
    private Alphabet alphabet;
    private Alphabet alphabet_testing;

    /** Constructs Scanner by creating a Firefox (at the moment) driver and logging at "http://localhost:8080/login" with root/root*/
    public Scanner(){
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);

        alphabet = new MapAlphabet();
        alphabet_testing = new PostgreSQLAlphabet();

        //TODO ACHTUNG!!! THIS (hardcoded login) SHOULD NOT EXIST!!!!
        driver.get("http://localhost:8080/login"); //#hardcode
        System.err.printf("Have logged in with root/root at\n\tlocalhost:8080/login\n");

        driver.findElement(By.id("id_l.L.login")).sendKeys("root");
        driver.findElement(By.id("id_l.L.password")).sendKeys("root");
        driver.findElement(By.id("id_l.L.loginButton")).click();
    }

    /** Terminates assigned driver*/
    public void close(){
        driver.quit();
    }

    /**
     * Determines elementType with given handle in current Webdriver state
     * @param driver
     * @param handle
     * @return ElementType. unknown if driver url didnt match handle
     * @throws NoSuchElementException when element cant be found by its xpath
     */
    public ElementType checkHandleType(WebDriver driver, WebHandle handle) throws NoSuchElementException{
        try {
            //###### Verifying opened url
            //TODO remove this crutch
            if (!driver.getCurrentUrl().equals(handle.url.toString())){
                do {
                    System.err.println("opened URL: " + driver.getCurrentUrl());
                    System.err.println("handle URL: " + handle.url.toString());
                    System.err.println("=======================TRYING TO SLEEP NOW=======================");
                    Utils.sleep(1000);
                }while(!driver.getCurrentUrl().equals(handle.url.toString()));
            }
            //Supposed to used this instead in the future:
//                if (!driver.getCurrentUrl().equals(handle.url.toString())){
//                    throw new WebDriverException("URL opened in browser is not corresponding to URL in WebHandle");
//                }

            //###### Storing data before tries to interact
            String oldHash = Utils.hashPage(driver);
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

            /* The following seems to be redundant... Just comparing URLs as strings seems to be OK?
            //This gonna test whether clicking changes "reference" of the URL (after the hash-sign)
            if (!StringUtils.equals(newURL.getRef(), handle.url.getRef()))
                return ElementType.clickable;*/

            //###### Checking if URL have changed
            if (!newURL.equals(handle.url))
                return ElementType.clickable;

            //###### Comparing screenshots
            if (oldHash.equals(Utils.hashPage(driver)))
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

        baseState.reach(driver);

        List<WebElement> elementList = driver.findElements(By.cssSelector("*"));
        System.err.printf("scan() of Scanner invoked. baseState.\n" +
                        "\t.url : %s\n" +
                        "\tsequence.size() : %d\n" +
                        "\tFound %d elements. Started generating xpathes...\n",
                baseState.url.toString(), baseState.sequence.size(), elementList.size());

        //TODO remove threshold for count of generated xpathes per scan session
        int i = 0, xpathsThreshold = 80; //#hardcode
        URL curUrl = null;
        try {
            curUrl = new URL(driver.getCurrentUrl());
        } catch (MalformedURLException e) {
            //This MalformedURLException is rather annoying.
            //Why at all we still use the URL class...
            e.printStackTrace();
        }
        for(WebElement element: elementList){
            if(!element.isDisplayed() || !element.isEnabled()) continue;
            String xpath = Selectors.formXPATH(driver, element);
            WebHandle handle = new WebHandle(curUrl, xpath);
            allHandles.add(handle);
            if (++i > xpathsThreshold) break;
        }

        for (WebHandle handle: allHandles){
            baseState.reach(driver);
//            System.err.println("XPATH for element being tested: " + handle.xpath);
            try {
                //###### Checking for cached result
                ElementType cachedEltype = null;
                //TODO remove "stress" testing of SQL
                /**/    try {
                /**/        cachedEltype = alphabet_testing.request(handle.url, handle.xpath);
                /**/    } catch (Exception e) {
                /**/        System.err.println("PostgreSQLAlphabet failed with: ");
                /**/        e.printStackTrace(System.err);
                /**/        System.err.println("    trying to Request deprecated MapAlphabet instead...");
                /**/        try {
                /**/            cachedEltype = alphabet.request(handle.url, handle.xpath);
                /**/        } catch (SQLException ignored) {
                /**/            ignored.printStackTrace(System.err);
                /**/        }
                /**/    }
                if (cachedEltype != ElementType.unknown)
                    handle.eltype = cachedEltype;
                else {
                    handle.eltype = checkHandleType(driver, handle);
                /**/        try {
                /**/         alphabet.add(handle);
                /**/        alphabet_testing.add(handle);
                /**/        } catch (Exception e) {
                /**/           System.err.println("One of the Alphabet's failed with: ");
                /**/          e.printStackTrace(System.err);
                /**/        }
                }
            }catch (NoSuchElementException e){
                //TODO: after hovering over button tooltip appears changing page structure(Extra div) => couldn't handle element.
                continue;
            }

//            System.err.println("Determined as " + handle.eltype.name() + "\n");
            //TODO return writables
//                if(handle.eltype.equals(ElementType.clickable) || handle.eltype.equals(ElementType.writable)) {
            if(handle.eltype.equals(ElementType.clickable)) {
                interactiveHandles.add(handle);
                //TODO remove intaractive elements count limit
                if (interactiveHandles.size() >= 20) return interactiveHandles; //#hardcode
            }
        }
        return interactiveHandles;
    }

    /**Left for testing at the moment*/
    public static void main(String args[]) throws MalformedURLException {
        Scanner scanner = new Scanner();
        State baseState = new State(new URL("http://localhost:8080/dashboard"), new Sequence());
        ArrayList<WebHandle> ints = new ArrayList<>(scanner.scan(baseState));
        for(WebHandle handle: ints){
            System.out.println(handle.xpath + " " + handle.eltype.name());
        }
        scanner.close();
    }

}
