package Boxes;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

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

    @Override
    public boolean add(Event x){
        boolean temp = super.add(x);
        if (size() == 1)
            this.get(0).handle.assignToUrl();
        return temp;
    }



    /**
     * Plays stored sequence of events, throws NoSuchElementException if one of events can't be performed.
     * @param driver WebDriver in which sequence should be played.
     * @return true in case of successful play
     */
    public boolean play(WebDriver driver){
        if(size() > 0){
            driver.get(get(0).handle.url.toString());
        }
        for(Event event : this) if (!event.perform(driver)) return false;
        return true;
    }

    /**
     * tests that checker condition is satisfied after each event
     * @param driver
     * @param checker - this function MUST call event.perform and return true or false depending on further test checking
     * @return
     */
    public boolean play(WebDriver driver, BiPredicate<WebDriver, Event> checker){
                if(size() > 0){
            driver.get(get(0).handle.url.toString());
        }
        for(Event event : this){
            if(!checker.test(driver, event)) return false;
        }
        return true;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < this.size(); i ++){
            if(i == 0 || !get(i).handle.url.equals(get(i-1).handle.url)){
                stringBuilder.append(get(i).handle.url.toString() + ", ");
            }
            stringBuilder.append(get(i).handle.xpath + " ");
            if(!get(i).context.isEmpty()){
                stringBuilder.append(", " + get(i).context);
            }
            stringBuilder.append("\n ----------> \n");
        }
        return stringBuilder.toString();
    }
}
