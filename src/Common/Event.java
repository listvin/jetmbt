package Common;

import com.google.common.primitives.UnsignedInteger;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Box for determining an element (by it's WebHandle) and what exactly to do with it. Aka node in EFG.
 * Created by user on 7/23/15.
 */
public class Event{
    private final int hash; //should be stored until all affecting values (context and handle) are final
    /**To do smth with element we have to find it. WebHandle helps to find=)*/
    public final WebHandle handle;
    /**If element is writable, this string contains, what we are going to type in.*/
    public final String context;

    /**Constructor for events with non-writable elements.*/
    public Event(WebHandle handle){
        this.handle = handle;
        context = "";
        hash = handle.hashCode();
    }

    /**Constructor for events with writable elements.*/
    public Event(WebHandle handle, String context) {
        this.handle = handle;
        this.context = context;
        hash = handle.hashCode() + Utils.hashString(context)*239;
    }

    /**Factory method for convenient creating of fake element.*/
    public static Event createFakeTerminal(){
        try{
            return new Event(new WebHandle(new URL("http://localhost:8080"),"",ElementType.terminal)); //#hardcode
        }catch (MalformedURLException ignored){
            //Empty URL can't be malformed.. I think..
            return null;
        }
    }

    /**
     * @param list handles to operate with in Event-s
     * @return List of Event-s, consisting of operations with web-elements
     * specified by handles. E.g. "click" or "write abacaba"
     */
    public static List<Event> generateTestEvents(List<WebHandle> list){
        //TODO here parameters of generated events can be specified
        //TODO writeable are determined as clickables for a while
        List <Event> result = new ArrayList<>();
        for (WebHandle handle : list) result.add(new Event(handle));
        return result;
    }

    /**Performs event in specified WebDriver
     * @param driver WebDriver to perform in
     */
    public void perform(WebDriver driver){
        switch (handle.eltype){
            case clickable: handle.findElement(driver).click(); break;
            case writable: handle.findElement(driver).sendKeys(context); break;
            case terminal: assert false : "terminal elements was touched"; break;
            default: assert false : "this shouldn't have happened"; break;
        }
    }

    @Override
    public int hashCode(){ return hash; }
}
