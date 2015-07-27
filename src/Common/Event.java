package Common;

import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Box for determining an element (by it's Handle) and what exactly to do with it. Aka node in EFG.
 * Created by user on 7/23/15.
 */
public class Event {
    /**To do smth with element we have to find it. Handle helps to find=)*/
    public final Handle handle;
    /**If element is writable, this string contains, what we are going to type in.*/
    public final String context;

    /**Constructor for events with non-writable elements.*/
    public Event(Handle handle){
        this.handle = handle;
        context = "";
    }

    /**Constructor for events with writable elements.*/
    public Event(Handle handle, String context) {
        this.handle = handle;
        this.context = context;
    }

    /**Factory method for convenient creating of fake element.*/
    public static Event createFakeTerminal(){
        try{
            return new Event(new Handle(new URL(""),"",ElementType.terminal));
        }catch (MalformedURLException ignored){
            //Empty URL can't be malformed.. I think..
            return null;
        }
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
}
