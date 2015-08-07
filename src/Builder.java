import Boxes.EFG;
import Boxes.Event;
import Boxes.Sequence;
import Boxes.State;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Separate class for building EFG.
 * Created by listvin on 7/23/15.
 */
public class Builder {
    private static Scanner scanner;
    static EFG g;

    /**@param args Use --url url_to_parse or --file file_to_load_graph_from.*/
    public static void main(String [] args) throws MalformedURLException{
        BlockingQueue<URL> URLQueue = new ArrayBlockingQueue<>(1000);
        URLHasher hasher = new URLHasher(URLQueue);
        new Thread(hasher).start();
        scanner = new Scanner(URLQueue);

        //TODO embed commons CLI
        switch (args[0]){
            case "--url":
                g = new EFG();
                dfs(Event.createTerminal("BUILDINGROOT"), new State(new URL(args[1]), new Sequence()), 0); //#hardcode
                break;
            case "--file":
                g = new EFG(args[1]);
                /*Event start = g.pickStart();
                if (start == null) assert false : "this graph is finished";*/
                //lol, null here seems to be really dangerous guy!
                    dfs(Event.createTerminal("BUILDINGROOT"), new State(null, new Sequence()), 0); //#hardcode
                break;
            default:
                System.err.print("Use --url <url to parse> or --file <file to load graph from>");
                System.exit(0);
        }

        scanner.close();
        g.dump2dot();
    }

    static final int depthLimit = 50; //#hardcode
    /**
     * @param prev - this called "prev" because in browser this event was already performed. For simple dfs 
     */
    private static void dfs(Event prev, State cur, int depth){
        prev.setTicked(); //for now (for building) let's make it classic, with touring by nodes instead of edges.

        System.out.print("DFS:"); for (int i = 0; i < depth; ++i) System.out.printf("_%2d_", i); System.out.printf("Last event: %s | %s\n", prev.handle.url, prev.handle.xpath);

        if (g.isScannedOnce(prev)) {
            if (prev.handle.isAssignedToUrl())
                cur = new State(prev.handle.url).createAppended(prev);
        } else
            g.addEdges(prev, Event.generateTestEvents(scanner.scan(cur)));


        if (depth < depthLimit)
            while (true){
                Event next = g.pickEventToGoFrom(prev);
                if (next != null) {
                    g.dump2dot();
                    dfs(next, cur.createAppended(next), depth + 1);
                } else break;
            }
    }
}
