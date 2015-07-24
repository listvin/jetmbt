package Common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Wrapping for List of Event-s with possibility of replay.
 * Created by listvin on 7/24/15.
 */
public class Sequence extends ArrayList<Event>{
    public Sequence(){
        super();
    }
    public Sequence(Sequence sequence){
        super(sequence);
    }

    /**
     * Plays stored sequence of events, throws NoSuchElementException if one of events can't be performed.
     * @param driver WebDriver in which sequence should be played.
     */
    public void play(WebDriver driver) throws NoSuchElementException{
        for(Event event: this)
            switch (event.handle.eltype){
                case clickable: driver.findElement(By.xpath(event.handle.xpath)).click(); break;
                case writable: driver.findElement(By.xpath(event.handle.xpath)).sendKeys(event.context); break;
            }
        //TODO: IRL replay of Sequence should throw something more informative or at least return bool in case of success
    }
}
