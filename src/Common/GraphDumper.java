package Common;

import Boxes.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class for convenient writing of EFG to viewable files.
 * 350% of GraphViz Java API functionality, lol
 * Created by listvin on 7/29/15.
 */
public class GraphDumper {
    private Runtime runtime = Runtime.getRuntime();
    public final String folderName;
    private Integer dumpNum = 0;
    public GraphDumper() {
        folderName = "graphs/" + (new SimpleDateFormat("ddMMMyyyy_EEE_HH%1mm%2ss.SSS%3"))
                .format(new Date()).toLowerCase()
                .replace("%1", "h").replace("%2", "m").replace("%3", "s") + "/"; //#hardcode
        String[] args = {"mkdir", folderName}; //#hardcode
        try {
            runtime.exec(args).waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace(System.err);
        }
        colorMap.put("http://github.com/404", colorList[0]); //#hardcode
    }

    private String generateName(Integer num){ return "dump#" + num.toString(); }
    private String generateNewName(){ return generateName(++dumpNum); }
    private String recoverLastName(){ return generateName(dumpNum); }

    private Map<String, String> colorMap = new HashMap<>();
    private final String[] colorList = {
            "fillcolor=black fontcolor=white",
            "fillcolor=firebrick1 fontcolor=black",
            "fillcolor=chocolate1 fontcolor=black",
            "fillcolor=yellow fontcolor=black",
            "fillcolor=lawngreen fontcolor=black",
            "fillcolor=forestgreen fontcolor=white",
            "fillcolor=cadetblue1 fontcolor=black",
            "fillcolor=dodgerblue fontcolor=white",
            "fillcolor=blue fontcolor=white",
            "fillcolor=indigo fontcolor=white",
            "fillcolor=deeppink fontcolor=black",
            "fillcolor=lightpink fontcolor=black",
            "fillcolor=orangered4 fontcolor=white",
            "fillcolor=cornflowerblue fontcolor=black",
            "fillcolor=maroon fontcolor=white",
            "fillcolor=cyan fontcolor=black",
            "fillcolor=gray fontcolor=black",
            "fillcolor=gray29 fontcolor=white",
            "fillcolor=antiquewhite fontcolor=black",
            "fillcolor=aquamarine fontcolor=black",
            "fillcolor=darkolivegreen1 fontcolor=black",
            "fillcolor=limegreen fontcolor=white",
            "fillcolor=brown fontcolor=white",
            "fillcolor=aliceblue fontcolor=black",
            "fillcolor=burlywood1 fontcolor=black",
            "fillcolor=white fontcolor=black",
    };

    private PrintWriter writer = null;
    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateNewName();
        writer = new PrintWriter(folderName + name + ".gv", "UTF-8");
        writer.printf("digraph EFG {\n" +
                "\tnode [style=filled]\n");

        int i = 0; lastInfoNodeNum = -1;
        for(String url : orderedUrls)
            addInfoNode(++i, url, url, colorMap.get(url));
    }

    //TODO this should be improved
    private List<String> orderedUrls = new ArrayList<>();
    private int lastInfoNodeNum = -1;
    public void addInfoNode(Integer num, String url, String label, String color){
        //writing info-node entry
        writer.printf("\tInfo%d [\n", num);

        //shaping node, corresponding to the fact it is InfoNode
        writer.printf("\t\tshape=%s\n", "note");

        //writing node's label
        if (label != null) writer.printf("\t\tlabel=\"%s\"\n", label);

        //writing node's url
        if (url != null) writer.printf("\t\tURL=\"%s\"\n", url);

        //painting node with color, corresponding to URL
        writer.printf("\t\t%s\n", color);

        //closing node definition
        writer.printf("\t];\n");

        //drawing edge to previous info node
        if (lastInfoNodeNum != -1)
            writer.printf("\tInfo%d -> Info%d;\n", lastInfoNodeNum, num);
        lastInfoNodeNum = num;
    }

    private Map<Event, Integer> nodeID = new HashMap<>();
    public void addNode(Event node){
        //checking if we have id for the node
        if (!nodeID.containsKey(node))
            nodeID.put(node, nodeID.size());

        //writing node's name in file
        writer.printf("\tNode%d [\n", nodeID.get(node));

        //writing node's URL and xpath as label
        writer.printf("\t\tlabel=\"%s\"\n" +
                      "\t\tURL=\"%s\"\n", node.handle.xpath, node.handle.url.toString());

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.toString())) {
            orderedUrls.add(node.handle.url.toString());
            colorMap.put(node.handle.url.toString(), colorList[colorMap.size() % colorList.length]);
        }

        //painting node with color, corresponding to URL
        writer.printf("\t\t%s\n", colorMap.get(node.handle.url.toString()));

        //shaping node, corresponding to it's ElementType
        switch (node.handle.eltype) {
            case terminal: writer.printf("\t\tshape=%s\n", "signature"); break;
            case clickable: writer.printf("\t\tshape=%s\n", "rectangle"); break;
            //TODO case clickable-baseable: writer.printf("\t\t\tshape=%s\n", "rectangle"); break;
            //TODO case writable: ??context
            default: writer.printf("\t\tshape=%s\n", "tripleoctagon");
        }

        //closing node definition
        writer.printf("\t];\n");
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.printf("\tNode%d -> Node%d;\n", nodeID.get(source), nodeID.get(destination));
    }

    public String closeFile() throws IOException, InterruptedException{
        writer.println("}\n");
        writer.close();
        writer = null;
        String name = recoverLastName();
        String[] args = {"dot", "-Tsvg", folderName + name + ".gv", "-o", folderName + "graph" + ".svg"}; //#hardcode
        runtime.exec(args); //we don't need .waitFor() in our case, do we?
        return name;
    }
}