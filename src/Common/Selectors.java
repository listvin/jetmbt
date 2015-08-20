package Common;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 7/23/15.
 */
public class Selectors {
    private static Logger log = Logger.get(new Selectors());
    private static String ECMA_getAllXpaths;
    static {
        try {
            ECMA_getAllXpaths = new String(Files.readAllBytes(Paths.get("src/Common/getAllXpaths.js")), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllXpaths(WebDriver driver){
        Object response = (((JavascriptExecutor) driver).executeScript(ECMA_getAllXpaths));
        return (ArrayList<String>) response;
    }
}
