import Common.Selectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;

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
        for(WebElement element: el){

            System.out.println("Getting XPATH for element: " + el.toString());
            System.out.println(Selectors.formXPATH(driver, element) + "\n");
        }



        driver.close();
    }
}
