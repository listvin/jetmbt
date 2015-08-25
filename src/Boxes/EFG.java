package Boxes;

import Common.ElementType;
import Common.GraphDumper;
import Common.Logger;
import Common.Utils;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

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
    private Map<Event, EdgeList> revList = new HashMap<>();

    public EFG() {
    }

    public EFG(String path) {
        try {
            dumper.parseFile(this, path);
        } catch (IOException e) {
            log.exception(e);
            System.exit(-1);
        }
    }

    //TODO turn this in use instead of builder's inside search with check through isScannedOnce(Event)
    public Event pickStart() {
        for (Event node : adjList.keySet())
            if (!node.isTicked()) {
                node.setTicked();
                return node;
            }
        return null;
    }

    public boolean isScannedOnce(Event event) {
        return adjList.containsKey(event) && adjList.get(event).size() > 0;
    }

    public void addEventUnchecked(Event node) {
        adjList.put(node, new EdgeList());//BIDLOCODE!!
        revList.put(node, new EdgeList());
    }

    /**
     * Be careful here, look both after adjList and revList
     */
    private void addEdge2ListUnchecked(EdgeList edgeList, Event destination) {
        edgeList.add(new Edge(destination));
    }

    public void addEdgeUnchecked(Event source, Event destination) {
        addEdge2ListUnchecked(adjList.get(source), destination);
        addEdge2ListUnchecked(revList.get(destination), source);
    }

    /**
     * Adds one edge. See {@link #addEdges(Event, List)} to add edges in batch.
     *
     * @param edgeList    Adding edge to this list... Should exist.
     * @param destination ...and comes to this one.
     */
    private void addEdge(EdgeList edgeList, Event destination) {
        //1. Destination event of edge which is being added may be new one. Let's check whether it exists and add if needed
        if (!adjList.containsKey(destination))
            addEventUnchecked(destination);
        //2. Adding edge to the certain list of outgoing paths
        addEdge2ListUnchecked(edgeList, destination);
    }

    /**
     * Should be used (typically after scanning) to add edges in batch. See {@link #addEdge(EdgeList, Event)} to add one edge.
     *
     * @param source   All adding edges come out from this Event-node. Being add
     * @param destList This is list of destination events to add edges to. ArrayList recommended. Will be shuffled!
     */
    public void addEdges(Event source, List<Event> destList) {
        if (!adjList.containsKey(source))
            addEventUnchecked(source);
        //So, shuffling. Here it is.
        Collections.shuffle(destList);
        EdgeList sourcesEdgeList = adjList.get(source);
        for (Event destination : destList) {
            addEdge(sourcesEdgeList, destination);
            addEdge(revList.get(destination), source);
        }
    }

    /**
     * This performs choice of next node in graph to go from source among unticked edges only.
     * Also unticked edge should lead to unticked node.
     *
     * @param source Event to determinate the beginning of set of edges to select from.
     * @return If there are some described edges, returns one and sets it as explored. Null otherwise.
     */
    public Event pickEventToGoFrom(Event source) {
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
     *
     * @return true in case of success
     */
    public boolean dump2dot() {
        //TODO make it more readable
        try {
            dumper.initFile();
            for (Event node : adjList.keySet()) dumper.addNode(node);
            for (Event node : adjList.keySet())
                for (Edge edge : adjList.get(node))
                    dumper.addEdgeFromTo(node, edge.destination);
            log.report(dumper.closeFile() + ".gv have been wrote to graphs folder.");
            return true;
        } catch (Exception e) {
            log.exception(e);
            return false;
        }
    }

    class PathGenerator {
        public Sequence generateSinglePathBetweenEvents(Event a, Event b, int length) {
            return generatePathsBetweenEvents(a, b, 1, length).get(0);
        }


        /**
         * Generates paths of given length containing two events
         *
         * @param a      - this event will be encountered inside of path
         * @param b      - this event will be paths last event
         * @param n      - number of events to be generated
         * @param length - length of each paths
         * @return
         */
        public List<Sequence> generatePathsBetweenEvents(Event a, Event b, int n, int length) {
            //TODO
            Event root = null;
            for (Event ev : adjList.keySet()) {
                if (ev.handle.eltype == ElementType.terminal && adjList.get(ev).size() > 0) {
                    root = ev;
                }
            }
            if (root == null) {
                return null;
            }
            Map<Event, Vertex> rootdepths = bfs(adjList, root);
            Map<Event, Vertex> bdepths = bfs(revList, b);
            ArrayList<Sequence> paths = new ArrayList<>();
            while (paths.size() < n) {
                Event tmp = a;
                Sequence path = new Sequence();
                while (rootdepths.get(tmp).parent != null) {
                    path.add(tmp);
                    tmp = rootdepths.get(tmp).parent;
                }
                Collections.reverse(path);
                List<Event> fromA = propagateForward(a, (v) -> (v.depth + rootdepths.get(a).depth + bdepths.get(v.event).depth >= length), (v) -> !bdepths.containsKey(v.event));
                for (int j = fromA.size() - 2; j >= 1; j--) {
                    path.add(fromA.get(j));
                }
                tmp = fromA.get(0);
                while (bdepths.get(tmp).parent != null) {
                    path.add(tmp);
                    tmp = bdepths.get(tmp).parent;
                }
                paths.add(path);
            }
            return paths;
        }


        /**
         * creates single path throug one event. Takes almost the same time as generating multiple paths. Use this when only one path is required
         *
         * @param event
         * @param leghth
         * @return
         */
        public Sequence generateSinglePathTroughEvent(Event event, int leghth) {
            return generatePathsTroughEvent(event, 1, leghth).get(0);
        }

        /**
         * Generates radom paths ending with event
         * stores copies of adjlist and revlist
         *
         * @param event  - event to go through
         * @param n      - number of paths to generate
         * @param leghth - legth of paths
         */
        public List<Sequence> generatePathsTroughEvent(Event event, int n, int leghth) {
            Event root = null;
            for (Event ev : adjList.keySet()) {
                if (ev.handle.eltype == ElementType.terminal && adjList.get(ev).size() > 0) {
                    root = ev;
                }
            }
            if (root == null) {
                return null;
            }
            Map<Event, Vertex> depths = bfs(adjList, root);
            for (Vertex v : depths.values()) {
                System.out.println(v);
            }
            //System.out.println(propagateForward(event, 5));
            List<Sequence> paths = new ArrayList<>();
            while (paths.size() < n) {
                //steps after event
                int forward = 0;
                if (depths.get(event).depth < 2) {
                    forward = n - depths.get(event).depth;
                } else if (adjList.get(event).size() != 0) {
                    forward = random.nextInt(leghth - depths.get(event).depth);
                }
                final int temp = forward;
                List<Event> suffix = propagateForward(event, forward);
                List<Event> prefix = propagateForward(revList, event, (v) -> v.depth + depths.get(v.event).depth + temp >= leghth);
                System.out.println(forward);
                System.out.println(suffix);
                System.out.println(prefix);
                Sequence path = new Sequence();
                Event tmp;
                if (prefix != null && prefix.size() > 0) {
                    tmp = prefix.get(0);
                } else {
                    tmp = event;
                }
                while (depths.get(tmp).parent != null) {
                    path.add(tmp);
                    tmp = depths.get(tmp).parent;
                }
                Collections.reverse(path);
                for (int i = 1; i < prefix.size(); i++) {
                    path.add(prefix.get(i));
                }

                for (int i = suffix.size() - 1; i > 0; i--) {
                    path.add(suffix.get(i));
                }

                paths.add(path);
            }
            for (Sequence path : paths) {
                System.out.println(path);
            }
            return paths;
        }


        /**
         * generate path of given length
         *
         * @param root
         * @param len
         * @return
         */
        private List<Event> propagateForward(Event root, int len) {
            return propagateForward(root, (v) -> v.depth >= len);
        }

        /**
         * generate path untill condition is satisfied
         *
         * @param root
         * @param condition
         * @return
         */
        private List<Event> propagateForward(Event root, Predicate<Vertex> condition) {
            return propagateForward(adjList, new Vertex(0, root, null), condition,(v) -> false);
        }

        private List<Event> propagateForward(Map<Event, EdgeList> graph, Event root, Predicate<Vertex> condition) {
            return propagateForward(graph, new Vertex(0, root, null), condition, (v) -> false);
        }

        private List<Event> propagateForward(Event root, Predicate<Vertex> OKcondition, Predicate<Vertex> returnCondition) {
            return propagateForward(adjList, new Vertex(0, root, null), OKcondition,returnCondition);
        }

        private List<Event> propagateForward(Map<Event, EdgeList> graph, Event root, Predicate<Vertex> condition, Predicate<Vertex> returnCondition) {
            return propagateForward(graph, new Vertex(0, root, null), condition, returnCondition);
        }



        /**
         * Generate path in graph until condition is satisfied
         *
         * @param graph
         * @param root
         * @param OKcondition
         * @return
         */
        private List<Event> propagateForward(Map<Event, EdgeList> graph, Vertex root, Predicate<Vertex> OKcondition, Predicate<Vertex> returnCondition) {
            if(returnCondition.test(root)){
                return null;
            }
            if (OKcondition.test(root)) {
                return new ArrayList<>(Arrays.asList(root.event));
            }
            List<Integer> permutuation = new ArrayList<>();
            for (int i = 0; i < graph.get(root.event).size(); i++) {
                permutuation.add(i);
            }
            Collections.shuffle(permutuation);
            List<Event> result;
            for (int x : permutuation) {
                result = propagateForward(graph, new Vertex(root.depth + 1, graph.get(root.event).get(x).destination, root.event), OKcondition, returnCondition);
                if (result != null) {
                    result.add(root.event);
                    return result;
                }
            }
            return null;
        }


        /**
         * Container for storing results of BFS, DFS and Other algorithms
         */
        private class Vertex {
            public int depth; //
            public Event event;
            public Event parent;


            public Vertex(int depth, Event event, Event parent) {
                this.depth = depth;
                this.parent = parent;
                this.event = event;
            }

            public String toString() {
                return event.toString() + "\n Current depth: " + String.valueOf(depth);
            }
        }


        /**
         * Simple bfs implementation - performs BFS on given Graph from given root.
         *
         * @param graph
         * @param root
         * @return returns List of Vertex objects - tuple of depth, parent and event. If parent is set to null - it is root
         */
        private Map<Event, Vertex> bfs(Map<Event, EdgeList> graph, Event root) {
            Map<Event, Vertex> ans = new HashMap<>();
            Event.invalidateTicks();
            Queue<Vertex> queue = new ArrayDeque<>();
            root.setTicked();
            queue.add(new Vertex(0, root, null));
            ans.put(root, new Vertex(0, root, null));

            while (!queue.isEmpty()) {
                Vertex cur = queue.poll();

                for (Edge next : graph.get(cur.event)) {
                    if (!next.destination.isTicked()) {
                        ans.put(next.destination, new Vertex(cur.depth + 1, next.destination, cur.event));
                        next.destination.setTicked();
                        queue.add(new Vertex(cur.depth + 1, next.destination, cur.event));
                    }
                }
            }
            return ans;
        }
    }

    public void run() {
        PathGenerator tmp = new PathGenerator();
        //List<Sequence> paths = tmp.generatePathsTroughEvent(Event.create(new WebHandle(new JetURL("url:http://localhost:8080/dashboard"), "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]"), ""), 5, 10);
        List<Sequence> paths = tmp.generatePathsBetweenEvents(Event.create(new WebHandle(new JetURL("url:http://localhost:8080/dashboard"), "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]"), ""), Event.create(new WebHandle(new JetURL("url:http://localhost:8080/issues"), "//*[@id=\"filterLi_toggler_reporter\"]/div[2]"), ""), 5, 15);
        WebDriver driver = new FirefoxDriver();
//        Utils.login(driver);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        for (Sequence path : paths) {
            Utils.resetSession(driver);
            path.play(driver);
        }
    }

    public void cnt() {
        int ct = 0;
        int vs = 0;
        for (Event ev : adjList.keySet()) {
            if (adjList.get(ev).size() > 0) {
                ct++;
                System.out.println(ev.handle.url.toString());
            }
            if (ev.isTicked()) {
                vs++;
            }
            if (ev.isTicked() && adjList.get(ev).size() == 0) {
                System.out.println(ev);
            }
        }
        System.out.println("Non empty edge lists " + ct);
        System.out.println("Ticked events " + vs);
        System.out.println("Total events " + adjList.size());
    }

    public static void main(String[] args) {
        EFG g = new EFG(args[0]);
        g.run();
        //g.cnt();
    }

}


