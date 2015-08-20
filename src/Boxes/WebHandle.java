package Boxes;

import Common.ElementType;
import Common.Logger;
import Common.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Class expected to provide possibility of absolute
 * identification of any element given in the web.
 * .hashCode overrided to return the same value
 * for different instances pointing to the same element.
 * .equals(obj) is also overrided
 * Created by user on 7/23/15.
 */
public class WebHandle {
    private static Logger log = Logger.get(new WebHandle(JetURL.createOwn404(), ""));
    public final JetURL url;
    public final String xpath;
    private final int hash;

    private boolean assignedToUrl = false;

    public ElementType eltype = ElementType.unknown;

    public WebHandle(JetURL url, String xpath) {
        this.url = url;
        this.xpath = xpath;
        this.hash = Utils.hashString(url.graphUrl() + xpath);
    }

    public WebHandle(JetURL url, String xpath, ElementType eltype) {
        this(url, xpath);
        this.eltype = eltype;
    }

    public WebHandle(JetURL url, String xpath, ElementType eltype, boolean assignedToUrl) {
        this(url, xpath, eltype);
        this.assignedToUrl = assignedToUrl;
    }

    @Override
    public int hashCode(){ return hash; }
    @Override
    public boolean equals(Object obj){
        return obj instanceof WebHandle
                && url.equals(((WebHandle)obj).url)
                && xpath.equals(((WebHandle)obj).xpath);
    }
    @Override
    public String toString(){
        return url.graphUrl() + " | " + xpath;
    }


    /**
     * Theoretically we are not stricted in using any concrete
     * identification mechanism.. This choice, imho, should be
     * localized in WebHandle. So WebHandle.findElement performs
     * WebDriver's findElement by preferred method.
     * @return WebElement ready to perform actions
     */
    public WebElement findElement(WebDriver driver){
        if(!JetURL.weaklyCompare(url, driver.getCurrentUrl())) {
            log.error("I'am: " + toString());
            log.error("My driver shows: " + driver.getCurrentUrl());
            log.error("JetURL, opened in driver, given to .findElement(WebDriver) is not corresponds to stored url");
            return null;
        }
        try {
            return driver.findElement(By.xpath(xpath));
        } catch (NoSuchElementException e){
//            log.exception(e);
            return null;
        }
    }

    public void assignToUrl(){ assignedToUrl = true; }
    public boolean isAssignedToUrl(){ return assignedToUrl; }
    //TODO: shortestparent + depth
}
