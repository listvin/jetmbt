package Boxes;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.NoSuchElementException;

/**
 * Immutable class to store in reach states of browsing experience.
 * Created by user on 7/23/15.
 */
public class State {
    public URL url;
    public Sequence sequence = new Sequence();

    /**Makes state based on given url.*/
    public State(URL url){
        this.url = url;
    }

    /**Makes state based on given url and COPY of sent sequence.*/
    public State(URL url, Sequence sequence){
        this(url);
        this.sequence = new Sequence(sequence);
    }

    /**
     * This is factory method to make a step to new State
     * @param event - step is introduced by this Event
     * @return requested State
     */
    public State createAppended(Event event){
        return new State(url, sequence.createAppended(event));
    }

    /**
     * This reaches state stored inside. First goes to the URL, then plays sequence
     * @param driver WebDriver to come to state in.
     * @return true in case of success
     */
    public boolean reach(WebDriver driver){
        driver.get(url.toString());
        if (!sequence.play(driver)) return false;
        return true;
    }

    public void truncToURL(URL url){
        this.url = url;
        sequence = new Sequence();
    }
}
