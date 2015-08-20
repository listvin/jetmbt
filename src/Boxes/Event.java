package Boxes;

import Common.ElementType;
import Common.Logger;
import Common.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static Logger log = Logger.get(new Event(new WebHandle(JetURL.createOwn404(), ""), null));
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
        return creationFinalizer(new Event(new WebHandle(JetURL.createOwn404(), name, ElementType.terminal), "")); //#hardcode
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
     * @return true in case of success (Element existed and was visible)
     */
    public boolean perform(WebDriver driver){ return perform(handle, driver); }

    /**
     * This static one is pretty useful for using from inside of checker in Scanner, cause Event can't be
     * constructed in that moment. For other usage refer to {@link #perform(WebDriver)}
     * @param driver WebDriver to perform in
     * @param handle handle with which event will be performed
     * @return true in case of success (Element existed and was visible)
     */
    //TODO this requires redesign for writables
    public static boolean perform(WebHandle handle, WebDriver driver){
        WebElement we = handle.findElement(driver);
        if (we == null){
            //TODO this place strongly interferes with driver's implicitly wait. So, I think, personal fails counter per handle needed.
            log.error(handle.url.graphUrl() + " | " + handle.xpath + "\n" +
                    "Unable to .perform(WebDriver), cause search of element by stored selector is failed.");
            return false;
        }
        if (!we.isDisplayed() || !we.isEnabled()){
            log.error(handle.url.graphUrl() + " | " + handle.xpath + "\n" +
                    "Failed to .perform(WebDriver), cause element not displayed or not enabled.");
            return false;
        }
        switch (handle.eltype){
            case clickable: we.click(); break;
            //case writable: we.sendKeys(context); break;
            case terminal: assert false : "terminal elements was touched"; break;
            default: assert false : "this shouldn't have happened???"; break;
        }
        return true;
    }

    @Override
    public int hashCode(){ return hash; }
    @Override
    public boolean equals(Object obj){
        return obj instanceof Event
                && context.equals(((Event)obj).context)
                && handle.equals(((Event)obj).handle);
    }
    @Override
    public String toString(){
        return handle + (context.length() == 0 ? "" : " (" + context + ")");
    }
}
