package Common;

import org.openqa.selenium.WebDriver;

import java.net.URL;

/**
 * Immutable class to store in reach states of browsing experience.
 * Created by user on 7/23/15.
 */
public class State {
    public final URL url;
    public final Sequence sequence;

    /**Makes state based on copy of sent sequence.*/
    public State(URL url, Sequence sequence){
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
     * This reaches state stored inside. First goes to the URL, then plays sequence
     * @param driver WebDriver to come to state in.
     */
    public void reach(WebDriver driver){
        driver.get(url.toString());
        try {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        sequence.play(driver);
    }
}
