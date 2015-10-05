package Boxes;

import Common.Utils;
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
    public JetURL url;
    public Sequence sequence = new Sequence();

    /**Makes state based on given url.*/
    public State(JetURL url){
        this.url = url;
        this.sequence = new Sequence();
    }

    /**Makes state based on given url and COPY of sent sequence.*/
    public State(JetURL url, Sequence sequence){
        this.url = url;
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
     * This reaches state stored inside. First goes to the JetURL, then plays sequence
     * @param driver WebDriver to come to state in.
     * @return true in case of success
     */
    public boolean reach(WebDriver driver){
        driver.get(url.graphUrl());
        boolean result = sequence.play(driver);
        //TODO place trunc check here
        return result;
    }

    public void truncToURL(JetURL url){
        this.url = url;
        sequence = new Sequence();
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Event event : sequence) {
            result.append(++i);
            result.append(')');
            result.append(event);
            result.append('\n');
        }
        if (sequence.size() >= 1) result.deleteCharAt(result.length()-1);
        return result.toString();
    }
}
