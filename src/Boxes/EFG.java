package Boxes;

import Common.ElementType;
import Common.GraphDumper;
import Common.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This is implementation of a holder for event flow graph.
 * It controls duplicating of nodes (Event-s) but doesn't control
 * duplicating of edges coming from the node, be careful with this.
 * Created by listvin on 7/23/15.
 */
public class EFG {
    private Logger log = new Logger(this, Logger.Level.debug);
    private GraphDumper dumper = new GraphDumper();
    private Random random = new Random(Common.Settings.randomSeed);
    private Map<Event, EdgeList> adjList = new HashMap<>();
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
		//1. Search for all unticked nodes
		//2. Search for all assigned nodes
		//3a. Dijkstrate from one assigned to all
		//3b. Dijkstrate from one unticked to all
		//4. Choose shortest route
		//5. Create (and return) State with this route inside
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
    }

    private void addEdgeUnchecked(EdgeList edgeList, Event destination){
        edgeList.add(new Edge(destination));
    }

    public void addEdgeUnchecked(Event source, Event destination){
        //TODO remove this crutch. Terminal and others should be drawen, but not in this way!
        if (destination.handle.eltype != ElementType.clickable && destination.handle.eltype != ElementType.writable)
            destination.setTicked(); //#hardcode
        addEdgeUnchecked(adjList.get(source), destination);
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
        addEdgeUnchecked(edgeList, destination);
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
            log.report(dumper.closeFile() + ".gv have been wrote to graphs folder.");
            return true;
        } catch (Exception e){
            log.exception(e);
            return false;
        }
    }
}


