package Common;

import Boxes.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by listvin on 7/29/15.
 */
public class GraphDumper {
    public final String namePrefix;
    private Integer dumpNum = 0;
    public GraphDumper() {
        namePrefix = (new SimpleDateFormat("EEEddMMMyyyy_1HH_2mm_3ss.SSS"))
                .format(new Date()).toLowerCase()
                .replace("_1", "h").replace("_2", "m").replace("_3", "s");

    }

    private String generateName(Integer num){ return namePrefix + "_dump#" + num.toString(); }
    private String generateName(){ return generateName(++dumpNum); }
    private String recoverLastName(){ return generateName(dumpNum); }

    private Map<String, String> colorMap = new HashMap<>();
    private final String[] colorList = {
            "fillcolor=black fontcolor=white",
            "fillcolor=white fontcolor=black",
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
    };

    private PrintWriter writer = null;
    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateName();
        writer = new PrintWriter("graphs/" + name + ".gv", "UTF-8");
        writer.printf("digraph EFG {\n" +
                      "\tnode [style=filled]\n");
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
        if (!colorMap.containsKey(node.handle.url.toString()))
            colorMap.put(node.handle.url.toString(), colorList[colorMap.size() % colorList.length]);

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
        writer.printf("\t\t];\n");
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.printf("\tNode%d -> Node%d;\n", nodeID.get(source), nodeID.get(destination));
    }

    private Runtime runtime = Runtime.getRuntime();
    public String closeFile() throws IOException, InterruptedException{
        writer.println("}\n");
        writer.close();
        writer = null;
        String name = recoverLastName();
        runtime.exec("dot -Tsvg \"graphs/" + name + ".gv\" -o \"graphs/" + name + ".svg\"");
        return name;
    }
}