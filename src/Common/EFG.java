package Common;

import java.util.*;

/**
 * This is implemntation of holder for event flow graph.
 * It controls duplicating of nodes (Event-s) but doesn't control
 * duplicating of edges coming from the node, be careful with this.
 * Created by listvin on 7/23/15.
 */
public class EFG {
    private class EdgeList extends ArrayList<Edge>{
        int countOfUnexplored = 0;
        EdgeList(){ super(); }
    }
    private Random random = new Random(239);
    private Map<Event, EdgeList> adjList = new HashMap<Event, EdgeList>();

    /**
     * Adds one edge. See {@link #addEdges(Event, List)} to add edges in batch.
     * @param source Adding edges comes out from this Event-node...
     * @param destination ...and comes to this one.
     */
    public void addEdge(Event source, Event destination){
        //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
        if (!adjList.containsKey(destination))
            adjList.put(destination, new EdgeList());
        //2. Adding edge to the certain list of outgoing paths
        ++adjList.get(source).countOfUnexplored;
        adjList.get(source).add(new Edge(destination));
    }

    /**
     * Should be used (typically after scanning) to add edges in batch. See {@link #addEdge(Event, Event)} to add one edge.
     * @param source All adding edges come out from this Event-node.
     * @param destList This is list of destination events to add edges to.
     */
    public void addEdges(Event source, List <Event> destList){
        for (Event destination : destList) addEdge(source, destination);
    }

    /**
     * @param source Event to determinate the beginning of set of edges to select from.
     * @return If there are some unexplored edges outgoing from source, this returns it, and marks as explored.
     */
    public Edge pickUpAnyUnexploredEdge(Event source){
        //TODO: optimize selection of random edge
        EdgeList list = adjList.get(source);
        if (list.countOfUnexplored == 0) return null;
        int num = random.nextInt() % list.size();
        while (list.get(num).isExplored())
            num = num+1 == list.size() ? 0 : num+1;
        //Setting here edge as explored in Edge itself and in EdgeList counter
        list.get(num).setExplored();
        --list.countOfUnexplored;
        return list.get(num);
    }

    /**
     * This generates .dot file with currently stored EFG
     * @param filename Can be specified.
     * @return True in case of success.
     */
    public boolean dump2xdot(String filename){
        //TODO
        return false;
    }
}
