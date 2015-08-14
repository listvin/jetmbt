import Boxes.EFG;
import Boxes.Event;
import Boxes.Sequence;
import Boxes.State;
import Common.Logger;
import Common.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Separate class for building EFG.
 * Created by listvin on 7/23/15.
 */
public class Builder {
    private static Builder me = new Builder();
    private static Scanner scanner;
    private Logger log = new Logger(this, Logger.Level.debug);
    static EFG g;

    /**@param args Use --url url_to_parse or --file file_to_load_graph_from.*/
    public static void main(String [] args) throws MalformedURLException{
        BlockingQueue<URL> URLQueue = new ArrayBlockingQueue<>(1000);
        URLHasher hasher = new URLHasher(URLQueue);
        Thread thread = new Thread(hasher);
        thread.start();
        Thread logThread = new Thread(me.log);
        logThread.start(); logThread.setPriority(Thread.MIN_PRIORITY);
        scanner = new Scanner(URLQueue);

        switch (args[0]){
            case "--url":
                g = new EFG();
                me.dfs(Event.createTerminal("BUILDINGROOT"), new State(Utils.createURL(args[1]), new Sequence()), 0); //#hardcode
                break;
            case "--file":
                g = new EFG(args[1]);
                /*Event start = g.pickStart();
                if (start == null) assert false : "this graph is finished";*/
                //lol, null here seems to be really dangerous guy!)
                    me.dfs(Event.createTerminal("BUILDINGROOT"), new State(null, new Sequence()), 0); //#hardcode
                break;
            default:
                System.err.print("Use --url <url to parse> or --file <file to load graph from>");
                System.exit(0);
        }

        scanner.close();
        me.log.report("Builder finished successfully.");
        g.dump2dot();
    }

    static final int depthLimit = 50; //#hardcode
    /**
     * @param prev - this called "prev" because in browser this event was already performed. For simple dfs
     */
    private void dfs(Event prev, State cur, int depth){
        prev.setTicked(); //for now (for building) let's make it classic, with touring by nodes instead of edges.

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; ++i) sb.append(String.format("_%2d_", i));
        log.debug(String.format("DFS: %s (%s | %s)\n", sb.toString(), prev.handle.url, prev.handle.xpath));

        boolean needDump = true;
        if (g.isScannedOnce(prev)) {
            needDump = false;
            if (prev.handle.isAssignedToUrl())
                cur = new State(prev.handle.url).createAppended(prev);
        } else
            g.addEdges(prev, Event.generateTestEvents(scanner.scan(cur)));

        if (needDump) g.dump2dot();
        else log.report("Skipping dump, that seems to be equal to previous.");

        if (depth < depthLimit)
            while (true){
                Event next = g.pickEventToGoFrom(prev);
                if (next != null) {
                    dfs(next, cur.createAppended(next), depth + 1);
                } else break;
            }
    }
}
