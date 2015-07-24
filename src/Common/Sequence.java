package Common;

import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
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
    public Sequence(List<Event> list) {
        super(list);
    }

    /**
     * Factory method to produce new Sequence on one Event longer than parent.
     * @param increment Event to append.
     * @return Requested sequence.
     */
    public Sequence createAppended(Event increment){
        Sequence result = new Sequence(this);
        result.add(increment);
        return result;
    }

    /**
     * Plays stored sequence of events, throws NoSuchElementException if one of events can't be performed.
     * @param driver WebDriver in which sequence should be played.
     */
    public void play(WebDriver driver) throws NoSuchElementException{
        for(Event event: this) event.perform(driver);
        //TODO: IRL replay of Sequence should throw something more informative or at least return bool in case of success
    }
}
