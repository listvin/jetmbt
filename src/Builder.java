import Boxes.EFG;
import Boxes.Event;
import Boxes.Sequence;
import Boxes.State;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Separate class for building EFG.
 * Created by listvin on 7/23/15.
 */
public class Builder {
    private static Scanner scanner;
    static EFG g = new EFG();

    /**@param arg_url For now only one argument expected - URL, to start building from.*/
    public static void main(String [] arg_url) throws MalformedURLException{
        assert arg_url.length == 1 : "one argument expected - URL, to start building from";

//        WebDriver driver = new FirefoxDriver();
//        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
//        driver.get(arg_url[0]);

        scanner = new Scanner();

        dfs(Event.createFakeTerminal(), new State(new URL(arg_url[0]), new Sequence()), 0);

        g.dump2dot();
    }

    static final int depthLimit = 20; //#hardcode
    /**
     * @param prev - this called "prev" because in browser this event was already performed. For simple dfs 
     */
    private static void dfs(Event prev, State cur, int depth){
        prev.setTicked(); //for now let's make it classic, with touring by nodes instead of edges.
        System.out.print("DFS:"); for (int i = 0; i < depth; ++i) System.out.printf("_%2d_", i); System.out.printf("Last event: %s | %s\n", prev.handle.url, prev.handle.xpath);
        g.addEdges(prev, Event.generateTestEvents(scanner.scan(cur)));
        if (depth < depthLimit)
            while (true){
                Event next = g.pickEventToGoFrom(prev);
                if (next != null) {
                    g.dump2dot();
                    dfs(next, cur.createAppended(next), depth + 1);
                }
                else break;
            }
    }
}
