package Common;

import org.openqa.selenium.WebDriver;

import java.net.URL;

/**
 * Immutable class to store in reach states of browsing experience.
 * Created by user on 7/23/15.
 */
public class State {
    final URL url;
    final Sequence sequence;

    /**Makes state based on copy of sent sequence.*/
    public State(URL url, Sequence sequence){
        this.url = url;
        this.sequence = new Sequence(sequence);
    }

    /**
     * This reaches state stored inside. First goes to the URL, then plays sequence
     * @param driver WebDriver to come to state in.
     */
    public void reach(WebDriver driver){
        driver.get(url.toString());
        sequence.play(driver);
    }
}
