package Common;

import javafx.util.Pair;
import java.util.*;
//import java.util.Map.Entry;

/**
 * Created by user on 7/23/15.
 */
public class EFG {
    private Random random = new Random(239);
    private Map<Event, Pair<Integer, List<Edge>>> adjList = new HashMap<Event, Pair<Integer, List<Edge>>>();
    private void inc(Integer i){ i = i + 1; }
    private void dec(Integer i){ i = i - 1; }

    // Should be used (typically by dfs) after scanning.
    // @param source All adding edges should come out from this Event-node
    // @param list This is list of edges to add. All edges supposed to be marked as unexplored.
    public void addEdges(Event source, List <Edge> list){
        for (Edge edge : list){
            //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
            if (!adjList.containsKey(edge.destination))
                adjList.put(edge.destination, new Pair<Integer, List<Edge>>(0, new ArrayList<Edge>()));
            //2. Adding edge to the certain list of outgoing paths
            inc(adjList.get(source).getKey());
            adjList.get(source).getValue().add(edge);
        }
    }

    //@return If there are some unexplored edges outgoing from source, this returns it, and marks as explored.
    //@param source Event to determinate the beginning of set of edges to select from.
    public Edge pickUpAnyUnexploredEdge(Event source){
        //TODO: optimize selection of random edge
        Pair<Integer, List <Edge>> pair = adjList.get(source);
        if (pair.getKey() == 0) return null;
        int num = random.nextInt() % pair.getValue().size();
        while (pair.getValue().get(num).isExplored())
            num = num+1 == pair.getValue().size() ? num+1 : 0;
        pair.getValue().get(num).setExplored();
        dec(pair.getKey());
        return pair.getValue().get(num);
    }

    //This generates .dot file with currently stored EFG
    //@param filename Can be specified.
    //@return True in case of success.
    public void dump2xdot(String filename){
        //TODO
    }
}
