import Boxes.EFG;
import Boxes.Event;
import Boxes.Sequence;
import Boxes.State;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 8/6/15.
 */
public class ParallelBuilder implements Runnable{
    private static Scanner scanner;

    static final int depthLimit = 20; //#hardcode

    //State to start with
    private State baseState;

    /**
     * @param prev - this called "prev" because in browser this event was already performed. For simple dfs
     */
    private static void dfs(Event prev, State cur, int depth){
        prev.setTicked(); //for now (for building) let's make it classic, with touring by nodes instead of edges.
//        System.out.print("DFS:"); for (int i = 0; i < depth; ++i) System.out.printf("_%2d_", i); System.out.printf("Last event: %s | %s\n", prev.handle.url, prev.handle.xpath);
//        //g.addEdges(prev, Event.generateTestEvents(scanner.scan(cur)));
//        if (depth < depthLimit)
//            while (true){
//                //Event next = g.pickEventToGoFrom(prev);
//                if (next != null) {
//                    //g.dump2dot();
//                    dfs(next, cur.createAppended(next), depth + 1);
//                } else break;
//            }
    }

    public void run(){

    }
}
