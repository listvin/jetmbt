package Common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * This is implemntation of holder for event flow graph.
 * It controls duplicating of nodes (Event-s) but doesn't control
 * duplicating of edges coming from the node, be careful with this.
 * Created by listvin on 7/23/15.
 */
public class EFG {
    private int edgeTickValue = 1;
    /** This invalidates all tick-marks on all edges.*/
    public void invalidateTicks(){ ++edgeTickValue; }

    private class Edge {
        public final Event destination;
        private int tick0 = 0, tick1 = 0;

        /**@param destination This is target node.*/
        private Edge(Event destination) {
            this.destination = destination;
        }

        boolean isTicked(){ return (tick0 == edgeTickValue);}
        boolean isTickedTwice(){ return (isTicked() && tick1 == edgeTickValue);}
        int getTicksCount(){ return (isTicked() ? (isTickedTwice() ? 2 : 1) : 0); }
        void setTicked(){
            if (isTicked())
                tick1 = edgeTickValue;
            else
                tick0 = edgeTickValue;
        }
    }
    private class EdgeList extends ArrayList<Edge>{
        int countOfUnexplored = 0;
        EdgeList(){ super(); }
    }

    private Random random = new Random(239);
    private Map<Event, EdgeList> adjList = new HashMap<Event, EdgeList>();

    /**
     * Adds one edge. See {@link #addEdges(Event, List)} to add edges in batch.
     * @param edgeList Adding edge to this list... Should exist.
     * @param destination ...and comes to this one.
     */
    private void addEdge(EdgeList edgeList, Event destination){
        //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
        if (!adjList.containsKey(destination))
            adjList.put(destination, new EdgeList());
        //2. Adding edge to the certain list of outgoing paths
        ++edgeList.countOfUnexplored;
        edgeList.add(new Edge(destination));
    }

    /**
     * Should be used (typically after scanning) to add edges in batch. See {@link #addEdge(EdgeList, Event)} to add one edge.
     * @param source All adding edges come out from this Event-node. Being add
     * @param destList This is list of destination events to add edges to.
     */
    public void addEdges(Event source, List <Event> destList){
        if (!adjList.containsKey(source))
            adjList.put(source, new EdgeList());
        for (Event destination : destList) addEdge(adjList.get(source), destination);
    }

    /**
     * This performs random choice of next node in graph to go from source among unticked edges only.
     * @param source Event to determinate the beginning of set of edges to select from.
     * @return If there are some unexplored edges outgoing from source, this returns it, and marks as explored.
     */
    public Event pickEventToGoFrom(Event source){
        //TODO: optimize selection of random edge
        EdgeList list = adjList.get(source);
        if (list.countOfUnexplored == 0) return null;
        int num = random.nextInt() % list.size();
        while (list.get(num).isTicked())
            num = num+1 == list.size() ? 0 : num+1;
        //Setting here edge as explored in Edge itself and in EdgeList counter
        list.get(num).setTicked();
        --list.countOfUnexplored;
        return list.get(num).destination;
    }

    /**
     * This generates .gv file with currently stored EFG in DOT format. Overwrites files.
     * @param name Can be specified.
     * @return True in case of success.
     */
    public boolean dump2dot(String name){
        //TODO make it more readable
        try {
            PrintWriter writer = new PrintWriter(name + ".gv", "UTF-8");
            writer.println("digraph EFG {");
            writer.printf("\t//first nodes");
            for (Event node : adjList.keySet())
                writer.printf("\t\tN%d [label=\"%s\"];", node.hashCode(), node.handle.xpath);
            writer.printf("\t//now edges");
            for (Event node : adjList.keySet())
                for (Edge edge : adjList.get(node))
                    writer.printf("\t\tN%d -- N%d;", node.hashCode(), edge.destination.hashCode());
            writer.println("}");
            writer.close();
            return true;
        } catch (Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }

}
