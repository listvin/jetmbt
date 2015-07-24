import Common.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by user on 7/23/15.
 */
public class Scanner {
    private static String baseURL;


    public static void login(WebDriver driver){
        driver.get("http://unit-775:8080/login");
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



    /**
     * Determines elementType of geven handle in current Webdriver state
     *
     * @param driver
     * @param handle
     * @return ElementType. unknown if driver url didnt match handle
     * @throws NoSuchElementException when element cant be found by its xpath
     */
    //TODO More precise check for writable type. Mb separate?

    public static ElementType checkHandleType(WebDriver driver, Handle handle) throws NoSuchElementException{
        if(!driver.getCurrentUrl().equals(handle.url.toString())){
            return ElementType.unknown;
        }
        String oldHash = Utils.hashPage(driver);
        String originalHandle = driver.getWindowHandle();
        driver.findElement(By.xpath(handle.xpath)).click();
        try
        {
            driver.switchTo().alert().dismiss();

            return ElementType.terminal;
        }   // try
        catch (NoAlertPresentException ignored)
        {

        }   // catch
        if(driver.getWindowHandles().size() != 1){
            for(String handleName : driver.getWindowHandles()) {
                if (!handleName.equals(originalHandle)) {
                    driver.switchTo().window(handleName);
                    driver.close();
                }
                //Just in case somehow tab wont have sabe handle as before
                if(driver.getWindowHandles().size() == 1){
                    break;
                }
            }
            return ElementType.terminal;
        }
        String newHash = Utils.hashPage(driver);
        if (oldHash.equals(newHash)){
            return ElementType.noninteractive;
        }

        try {
            URL currentURL = new URL(driver.getCurrentUrl());
            if(currentURL.getHost().equals(handle.url.getHost())){
                if(driver.findElements(By.xpath(handle.xpath)).size() > 0){
                    driver.findElement(By.xpath(handle.xpath)).sendKeys("aba");
                    if(Utils.hashPage(driver).equals(newHash)){
                        return ElementType.clickable;
                    }else{
                        return ElementType.writable;
                    }
                }else{
                    return ElementType.clickable;
                }
            }else{
                return ElementType.terminal;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return ElementType.unknown;
    }



    public static void main(String[] args) {

        WebDriver driver = new FirefoxDriver();
        login(driver);
        baseURL = "unit-775:8080/issue/fsefs-1";

        driver.get(baseURL);
        try {
            Thread.sleep(5000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        List<WebElement> el = driver.findElements(By.cssSelector("*"));
        System.out.println(el.size());
        List<Handle> intercatableHandles = new ArrayList<Handle>();


        try {
            Handle BaseHandle = new Handle(new URL(baseURL), "");
            State baseState = new State(new URL(baseURL), new ArrayList<Event>(Arrays.asList(new Event(BaseHandle, ""))));
            for(WebElement element: el){

                System.out.println("Getting XPATH for element: " + el.toString());
                String xpath = Selectors.formXPATH(driver, element);
                System.out.println(xpath);

                Handle handle = new Handle(new URL(baseURL), xpath);
                handle.eltype = checkHandleType(driver, handle);
                System.out.println("Determined as " + handle.eltype.name() + "\n");
                if(handle.eltype.equals(ElementType.clickable) || handle.eltype.equals(ElementType.writable)){
                    intercatableHandles.add(handle);
                }



            }
        }catch (MalformedURLException ignored){
            System.out.print("WTF JUST HAPPANED?!");

        }


        driver.close();
    }
}
