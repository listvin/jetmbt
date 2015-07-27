import Common.*;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 7/23/15.
 */
public class Scanner {
    private WebDriver driver;

    /** Constructs Scanner by creating a Firefox (at the moment) driver and logging at "http://localhost:8080/login" with root/root*/
    public Scanner(){
        this.driver = new FirefoxDriver();
        //TODO ACHTUNG THIS SHOULD NOT EXIST!!!!
        driver.get("http://localhost:8080/login");
        try {
            Thread.sleep(500);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        System.out.print(By.cssSelector("input"));
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
    //TODO More precise check for writable type. Mb separate?
    public static ElementType checkHandleType(WebDriver driver, WebHandle handle) throws NoSuchElementException{
        try {
            //###### Verifying opened url
            if (!driver.getCurrentUrl().equals(handle.url.toString()))
                throw new WebDriverException("URL opened in browser is not corresponding to base URL in state");

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
        baseState.reach(driver);
        try {
            Thread.sleep(5000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        List<WebHandle> interactiveHandles = new ArrayList<>();
        List<WebHandle> allHandles = new ArrayList<>();

            baseState.reach(driver);

            List<WebElement> el = driver.findElements(By.cssSelector("*"));
            System.out.println(el.size());

            for(WebElement element: el){
                if(!element.isDisplayed() || !element.isEnabled()){
                    continue;
                }
                String xpath = Selectors.formXPATH(driver, element);
                WebHandle handle = new WebHandle(baseState.url, xpath);
                allHandles.add(handle);
            }

            for(WebHandle handle: allHandles){
                baseState.reach(driver);
                System.out.println("XPATH for element: " + handle.xpath);
                try {
                    handle.eltype = checkHandleType(driver, handle);
                }catch (NoSuchElementException e){
                    //TODO: after hovering over button tolltip appears chainging page structure(Extra div) => couldn't handle element.
                    continue;
                }

                System.out.println("Determined as " + handle.eltype.name() + "\n");
                //TODO return writables
//                if(handle.eltype.equals(ElementType.clickable) || handle.eltype.equals(ElementType.writable)) {
                if(handle.eltype.equals(ElementType.clickable)) {
                    interactiveHandles.add(handle);
                    //TODO remove intaractive elements count limit
                    if (interactiveHandles.size() >= 20) return interactiveHandles;
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
