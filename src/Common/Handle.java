package Common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.NoSuchElementException;

/**
 * Created by user on 7/23/15.
 *
 */
public class Handle {
    public final URL url;

    public final String xpath;

    public ElementType eltype;

    public Handle(URL url, String xpath, ElementType eltype) {
        this.url = url;
        this.xpath = xpath;
        this.eltype = eltype;
    }

    public Handle(URL url, String xpath) {
        this.url = url;
        this.xpath = xpath;
        this.eltype = ElementType.unknown;
    }

    /**
     * Theoretically we are not stricted in using any concrete
     * identification mechanism.. This choice, imho, should be
     * localized in Handle. So Handle.findElement performs
     * WebDriver's findElement by preferred method.
     * @return WebElement ready to perform actions
     */
    public WebElement findElement(WebDriver driver) throws NoSuchElementException{
        assert driver.getCurrentUrl().equals(url.toString()) : "request of element not from this page occurred";
        return driver.findElement(By.xpath(xpath));
    }

    //TODO: shortestparent + depth
}
