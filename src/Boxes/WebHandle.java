package Boxes;

import Common.ElementType;
import Common.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.NoSuchElementException;

/**
 * Class expected to provide possibility of absolute
 * identification of any element given in the web.
 * .hashCode overrided to return the same value
 * for different instances pointing to the same element.
 * .equals(obj) is also overrided
 * Created by user on 7/23/15.
 */
public class WebHandle {
    public final URL url;
    public final String xpath;
    private final int hash;

    public ElementType eltype = ElementType.unknown;

    public WebHandle(URL url, String xpath) {
        this.url = url;
        this.xpath = xpath;
        this.hash = Utils.hashString(url.toString() + xpath);
    }

    public WebHandle(URL url, String xpath, ElementType eltype) {
        this(url, xpath);
        this.eltype = eltype;
    }

    @Override
    public int hashCode(){ return hash; }
    @Override
    public boolean equals(Object obj){
        return obj instanceof WebHandle
                && url.toString().equals(((WebHandle)obj).url.toString())
                && xpath.equals(((WebHandle)obj).xpath);
    }


    /**
     * Theoretically we are not stricted in using any concrete
     * identification mechanism.. This choice, imho, should be
     * localized in WebHandle. So WebHandle.findElement performs
     * WebDriver's findElement by preferred method.
     * @return WebElement ready to perform actions
     */
    public WebElement findElement(WebDriver driver) throws NoSuchElementException{
        assert driver.getCurrentUrl().equals(url.toString()) : "request of element not from this page occurred";
        return driver.findElement(By.xpath(xpath));
    }

    //TODO: shortestparent + depth
}
