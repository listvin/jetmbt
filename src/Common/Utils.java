package Common;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 7/24/15.
 */
public class Utils {

    /**
     *
     * @param driver - WebDriver whose state will be hashed
     * @return
     */
    public static String hashPage(WebDriver driver){
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        String hexString = "";

        try {
            BufferedImage buffImg = ImageIO.read(scrFile);
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
            System.out.print("Couldn't calculate state hash");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hexString;

    }
}
