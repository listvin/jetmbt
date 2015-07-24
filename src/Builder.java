import Common.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Separate class for building EFG.
 * Created by listvin on 7/23/15.
 */
public class Builder {
    private static WebDriver driver = new FirefoxDriver();
    private static Scanner scanner = new Scanner();
    static EFG g = new EFG();

    /**@param arg_url For now only one argument expected - URL, to start building from.*/
    public static void main(String [] arg_url) throws MalformedURLException{
        assert arg_url.length == 1 : "one argument expected - URL, to start building from";
        driver.get(arg_url[0]);
        dfs(Event.createFakeTerminal(), new State(new URL(arg_url[0]), new Sequence()), 0);
    }

    static final int depthLimit = 20;
    private static void dfs(Event prev, State cur, int depth){
        g.addEdges(prev, scanner.scan(cur));
        if (depth < depthLimit)
            while (true){
                Event next = g.pickEventToGoFrom(prev);
                if (next != null)
                    dfs(next, cur.createAppended(next), depth+1);
                else break;
            }
    }
}
