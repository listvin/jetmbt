package Common;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 7/24/15.
 */
public class Utils {
    static Logger log = Logger.get(new Utils());

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to calculate a polynomial hash of given string:
     * hashString(s)  =  s[0]
     *                +  s[1]*base
     *                +  s[2]*base^2
     *                +  s[3]*base^3
     *                +  ...
     *                +  s[s.size()-1]*base^(s.size()-1)
     * As you can see above overflow is used instead of taking modulo...
     * Base here is 257.
     * @param s string for hashing
     * @return returned int is ok to override native .hsahCode()
     */
    public static int hashString(String s){
        final int base = 257;
        int powed = 1, result = 0;
        for (Character c : s.toCharArray()) {
            result += c*powed;
            powed *= base;
        }
        return result;
    }

    /**
     *
     * @param image - image to be converted to grayscale
     */
    public static void convertImageToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
                Color c = new Color(image.getRGB(j, i));
                int red = (int)(c.getRed() * 0.21);
                int green = (int)(c.getGreen() * 0.72);
                int blue = (int)(c.getBlue() *0.07);
                int sum = red + green + blue;
                Color newColor = new Color(sum,sum,sum);
                image.setRGB(j,i,newColor.getRGB());
            }
        }
    }

    /**
     * Method for taking hash of a graphical screenshot of a browser assigned to specific WebDriver
     * @param driver - WebDriver whose state will be hashed
     * @return hash of the page which was opened in driver
     */
    public static String hashPage(WebDriver driver){
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        String hexString = "";

        try {
            BufferedImage buffImg = ImageIO.read(scrFile);
            convertImageToGrayscale(buffImg);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(buffImg, "png", outputStream);
            byte[] data = outputStream.toByteArray();

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] hash = md.digest();
            for (int i=0; i < hash.length; i++) { //for loop ID:1
                hexString +=
                        Integer.toString( ( hash[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            FileUtils.copyFile(scrFile, new File("screenshots/" + hexString + ".png"));

        } catch (FileNotFoundException e) {
            log.error("Couldn't calculate state hash");
            log.exception(e);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return hexString;

    }

    /**
     * perform login and parameter setup un specified driver
     * @param driver
     */
    public static void setUpDriver(WebDriver driver){
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);


        //TODO ACHTUNG!!! THIS (hardcoded login) SHOULD NOT EXIST!!!!
        driver.get("http://localhost:8080/login"); //#hardcode
        log.report("Have logged in with root/root at\n\tlocalhost:8080/login\n");

        driver.findElement(By.id("id_l.L.login")).sendKeys("root");
        driver.findElement(By.id("id_l.L.password")).sendKeys("root");
        driver.findElement(By.id("id_l.L.loginButton")).click();
    }
}
