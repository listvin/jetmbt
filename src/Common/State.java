package Common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 7/23/15.
 */
public class State {
    final URL url;
    List<Event> sequence ;

    public State(URL url, List<Event> sequence){
        this.url = url;
        this.sequence = new ArrayList<Event>(sequence);
    }

    public void replaySequence(WebDriver driver){
        driver.get(url.toString());
        for(Event e: sequence){
            if(e.handle.eltype.equals(ElementType.clickable)){
                driver.findElement(By.xpath(e.handle.xpath)).click();
            }
            if(e.handle.eltype.equals(ElementType.writable)){
                driver.findElement(By.xpath(e.handle.xpath)).sendKeys(e.context);
            }
        }
    }
}
