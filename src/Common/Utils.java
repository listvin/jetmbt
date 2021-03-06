package Common;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

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
    static Logger log = new Logger(new Utils(), Logger.Level.all, Logger.Level.all);

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
     * @return returned int is ok to override native .hashCode()
     */
    public static int hashString(String s){
        if (s == null) return 0;
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

    public static void login(WebDriver driver){ //#hardcode
        if (Settings.yt525mode) {login525(driver); return;}
        log.info("NewTrack mode selected by ?default.");
        driver.get("http://localhost:8080");
        sleep(2000);
        driver.findElement(By.xpath("/html/body/div[4]/div[1]/div/div[1]/div[1]/div/div[3]/div/div[2]/button")).click();
        driver.findElement(By.xpath("/html/body/div/div[1]/div/form/div[2]/div[2]/span/a/span")).click();
//        //logging in as root
//        driver.findElement(By.id("username")).sendKeys("root");
//        driver.findElement(By.id("password")).sendKeys("root");
//        driver.findElement(By.id("password")).sendKeys(Keys.RETURN);
//        driver.findElement(By.id("id_l.L.loginButton")).click();
        log.report("Have logged in as guest at\n\tlocalhost:8080\n");
    }
    public static void login525(WebDriver driver) { //#hardcode
        log.warning("YouTrack 5.2.5 mode is active.");
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("id_l.L.login")).sendKeys("root");
        driver.findElement(By.id("id_l.L.password")).sendKeys("root");
        driver.findElement(By.id("id_l.L.loginButton")).click();
        log.report("Have logged in with root/root at\n\tlocalhost:8080/login\n");
    }

    /**
     * perform login and parameter setup un specified driver
     * @param driver
     */
    public static void setUpDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        //driver.manage().timeouts().setScriptTimeout(15, TimeUnit.SECONDS);
        //TODO remove hardcoded logging in
        login(driver);
    }

    public static boolean interactive(ElementType eltype){
        //TODO add writables here
        return eltype == ElementType.clickable;
    }

    public static String getJavaDefaultName(Object obj){
        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }

    public static String htmlShield(String s){
        //TODO check it for graphviz
        return s/*.replace("\"","&#34;")*/.replace("&", "&#38;").replace("<","&lt;").replace("'","&apos;");
    }

    public static void resetSession(WebDriver driver){ //#HARDCODE
        driver.manage().deleteCookieNamed("YTSESSIONID");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Utils.login(driver);

    }

    public static String dotShield(String s){
        return s.replace("\"","'");
    }
    
    public static String base58(int x){
		final String alpha = "123456789abcdefghijkmnopqrstuvwxyz";
		StringBuilder sb = new StringBuilder();
		while (x > 0){
			sb.insert(0, alpha.charAt(x%alpha.length()));
			x /= alpha.length();
		}
        if (sb.length() == 0) sb.append(alpha.charAt(0));
        return sb.toString();
	}
}
