package Boxes;

import Common.*;

import java.io.IOException;
import java.util.*;

/**
 * This is implementation of a holder for event flow graph.
 * It controls duplicating of nodes (Event-s) but doesn't control
 * duplicating of edges coming from the node, be careful with this.
 * Created by listvin on 7/23/15.
 */
public class EFG {
    private Logger log = new Logger(this, Logger.Level.debug);
    private GraphDumper dumper = new GraphVizSimpleDumper();
    private Random random = new Random(Common.Settings.randomSeed);
    private Map<Event, EdgeList> adjList = new HashMap<>();
    private Map<Event, EdgeList> revList = new HashMap<>();
    public EFG(){}
    public EFG(String path){
        try {
            dumper.parseFile(this, path);
        } catch (IOException e) {
            log.exception(e);
            System.exit(-1);
        }
    }

    //TODO turn this in use instead of builder's inside search with check through isScannedOnce(Event)
    public Event pickStart(){
        for (Event node : adjList.keySet())
            if (!node.isTicked()){
                node.setTicked();
                return node;
            }
        return null;
    }

    public boolean isScannedOnce(Event event){ return adjList.containsKey(event) && adjList.get(event).size() > 0; }

    public void addEventUnchecked(Event node){
        adjList.put(node, new EdgeList());
        revList.put(node, new EdgeList());
    }

    /**Be careful here, look both after adjList and revList*/
    private void addEdge2ListUnchecked(EdgeList edgeList, Event destination){
        edgeList.add(new Edge(destination));
    }

    public void addEdgeUnchecked(Event source, Event destination){
        addEdge2ListUnchecked(adjList.get(source), destination);
        addEdge2ListUnchecked(revList.get(destination), source);
    }

    /**
     * Adds one edge. See {@link #addEdges(Event, List)} to add edges in batch.
     * @param edgeList Adding edge to this list... Should exist.
     * @param destination ...and comes to this one.
     */
    private void addEdge(EdgeList edgeList, Event destination){
        //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
        if (!adjList.containsKey(destination))
            addEventUnchecked(destination);
        //2. Adding edge to the certain list of outgoing paths
        addEdge2ListUnchecked(edgeList, destination);
    }

    /**
     * Should be used (typically after scanning) to add edges in batch. See {@link #addEdge(EdgeList, Event)} to add one edge.
     * @param source All adding edges come out from this Event-node. Being add
     * @param destList This is list of destination events to add edges to. ArrayList recommended. Will be shuffled!
     */
    public void addEdges(Event source, List <Event> destList){
        if (!adjList.containsKey(source))
            addEventUnchecked(source);
        //So, shuffling. Here it is.
            Collections.shuffle(destList);
        EdgeList sourcesEdgeList = adjList.get(source);
        for (Event destination : destList){
            addEdge(sourcesEdgeList, destination);
            addEdge(revList.get(destination), source);
        }
    }

    /**
     * This performs choice of next node in graph to go from source among unticked edges only.
     * Also unticked edge should lead to unticked node.
     * @param source Event to determinate the beginning of set of edges to select from.
     * @return If there are some described edges, returns one and sets it as explored. Null otherwise.
     */
    public Event pickEventToGoFrom(Event source){
        EdgeList list = adjList.get(source);
        while (list.firstToExplore < list.size()
                && (list.get(list.firstToExplore).destination.isTicked()
                || !Utils.interactive(list.get(list.firstToExplore).destination.handle.eltype)))
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
        try {
            dumper.initFile();
            for (Event node : adjList.keySet()) dumper.presentNode(node);
            for (Event node : adjList.keySet()) dumper.addNode(node);
            for (Event node : adjList.keySet())
                for (Edge edge : adjList.get(node))
                    dumper.addEdgeFromTo(node, edge.destination);
            log.report(dumper.closeFile() + " have been wrote to graphs folder.");
            return true;
        } catch (Exception e){
            log.exception(e);
            return false;
        }
    }

}


