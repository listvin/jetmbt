package Boxes;

import Common.ElementType;
import Common.GraphDumper;

import java.util.*;

/**
 * This is implementation of a holder for event flow graph.
 * It controls duplicating of nodes (Event-s) but doesn't control
 * duplicating of edges coming from the node, be careful with this.
 * Created by listvin on 7/23/15.
 */
public class EFG {
    private GraphDumper dumper = new GraphDumper();
    private Random random = new Random(239);
    private Map<Event, EdgeList> adjList = new HashMap<>();

    /**
     * Adds one edge. See {@link #addEdges(Event, List)} to add edges in batch.
     * @param edgeList Adding edge to this list... Should exist.
     * @param destination ...and comes to this one.
     */
    private void addEdge(EdgeList edgeList, Event destination){
        //TODO remove this crutch. Terminal and others should be drawen, but not in this way!
        if (destination.handle.eltype != ElementType.clickable && destination.handle.eltype != ElementType.writable)
            destination.setTicked(); //#hardcode
        //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
        if (!adjList.containsKey(destination))
            adjList.put(destination, new EdgeList());
        //2. Adding edge to the certain list of outgoing paths
        edgeList.add(new Edge(destination));
    }

    /**
     * Should be used (typically after scanning) to add edges in batch. See {@link #addEdge(EdgeList, Event)} to add one edge.
     * @param source All adding edges come out from this Event-node. Being add
     * @param destList This is list of destination events to add edges to. ArrayList recommended. Will be shuffled!
     */
    public void addEdges(Event source, List <Event> destList){
        if (!adjList.containsKey(source))
            adjList.put(source, new EdgeList());
        //So, shuffling. Here it is.
            Collections.shuffle(destList);
        for (Event destination : destList) addEdge(adjList.get(source), destination);
    }

    /**
     * This performs choice of next node in graph to go from source among unticked edges only.
     * Also unticked edge should lead to unticked node.
     * @param source Event to determinate the beginning of set of edges to select from.
     * @return If there are some described edges, returns one and sets it as explored. Null otherwise.
     */
    public Event pickEventToGoFrom(Event source){
        EdgeList list = adjList.get(source);
        while (list.firstToExplore < list.size() && list.get(list.firstToExplore).destination.isTicked())
            ++list.firstToExplore;
        if (list.firstToExplore == list.size()) return null;

        //Setting destination event as explored
            //dfs is doing this now
//            list.get(list.firstToExplore).destination.setTicked();

        //Setting here edge as explored in Edge itself and in EdgeList counter
            //not considering exploration of edges at all at the moment
//            list.get(list.firstToExplore).setTicked();

            //at the moment dfs does this: list.get(list.firstToExplore).destination.setTicked(); //marking destination node
        return list.get(list.firstToExplore++).destination; //++ here is extremely important =/
    }

    /**
     * This generates .gv file named by current date and time with stored EFG in DOT format.
     * Connected GraphDumper supposed to generate svg automatically
     * @return true in case of success
     */
    public boolean dump2dot(){
        //TODO make it more readable
        try {
            dumper.initFile();
            for (Event node : adjList.keySet()) dumper.addNode(node);
            for (Event node : adjList.keySet())
                for (Edge edge : adjList.get(node))
                    dumper.addEdgeFromTo(node, edge.destination);
            System.out.print("\u001B[32;1m\"" + dumper.closeFile() + ".gv\"\u001B[0m\u001B[32m have been wrote to graphs folder.\n" + "\u001B[0m");
            return true;
        } catch (Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }
}


