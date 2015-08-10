package Boxes;

import Common.ElementType;
import Common.Utils;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * WARNING! All factories of instances of this class guarantee, that there won't be generated
 * two different instances with equal handles and equal contexts. Actually all factories are
 * lazy, and class contains it's own implementation of memory management above native one. Also
 * all constructors are private.
 *
 * Box for determining an element (by it's WebHandle) and what exactly to do with it. Aka node in EFG.
 * .equals(obj) and .hashCode() are overrided
 * Created by user on 7/23/15.
 */
public class Event extends Tickable{
    /**This implements "memory-management" claimed above.*/
    private static HashMap<Event, Event> memory = new HashMap<>();

    private final int hash; //should be stored until all affecting values (context and handle) are final
    /**To do smth with element we have to find it. WebHandle helps to find=)*/
    public final WebHandle handle;
    /**If element is writable, this string contains, what we are going to type in.*/
    public final String context;

    /**Should be called in the end of every public factory*/
    private static Event creationFinalizer(Event event){
        if (memory.containsKey(event))
            return memory.get(event);
        memory.put(event, event);
        return event;
    }

    /**Just a constructor. Fills hash field.*/
    private Event(WebHandle handle, String context) {
        this.handle = handle;
        this.context = context;
        hash = handle.hashCode() + Utils.hashString(context)*239;
    }

    /**Factory method for convenient creating of fake element. Will be terminal*/
    public static Event createTerminal(String name){
        try{
            return creationFinalizer(new Event(new WebHandle(new URL("http://github.com/404"), name, ElementType.terminal), "")); //#hardcode
        }catch (MalformedURLException ignored){
            //Hardcoded URL can't be malformed.. I think..
            return null;
        }
    }

    public static Event create(WebHandle handle, String context){
        return creationFinalizer(new Event(handle, context));
    }

    /**
     * @param list handles to operate with in Event-s
     * @return List of Event-s, consisting of operations with web-elements
     * specified by handles. E.g. "click" or "write abacaba"
     */
    public static List<Event> generateTestEvents(List<WebHandle> list){
        //TODO here parameters of generated events can be specified
        //TODO writable are determined as clickable for a while
        List <Event> result = new ArrayList<>();
        for (WebHandle handle : list) result.add(creationFinalizer(new Event(handle, "")));
        return result;
    }

    /**Performs event in specified WebDriver
     * @param driver WebDriver to perform in
     */
    public void perform(WebDriver driver) throws ElementNotVisibleException, NoSuchElementException, InvalidSelectorException{
        switch (handle.eltype){
            case clickable: handle.findElement(driver).click(); break;
            case writable: handle.findElement(driver).sendKeys(context); break;
            case terminal: assert false : "terminal elements was touched"; break;
            default: assert false : "this shouldn't have happened"; break;
        }
    }

    @Override
    public int hashCode(){ return hash; }
    @Override
    public boolean equals(Object obj){
        return obj instanceof Event
                && context.equals(((Event)obj).context)
                && handle.equals(((Event)obj).handle);
    }
}
