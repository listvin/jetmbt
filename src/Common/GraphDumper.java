package Common;

import Boxes.Event;
import Boxes.WebHandle;

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

    private Map<String, String[]> colorMap = new HashMap<>();
    private final String[][] colorList = {
            {"black", "white"},
            {"firebrick1", "black"},
            {"chocolate1", "black"},
            {"yellow", "black"},
            {"lawngreen", "black"},
            {"forestgreen", "white"},
            {"cadetblue1", "black"},
            {"dodgerblue", "white"},
            {"blue", "white"},
            {"indigo", "white"},
            {"deeppink", "black"},
            {"lightpink", "black"},
            {"orangered4", "white"},
            {"cornflowerblue", "black"},
            {"maroon", "white"},
            {"cyan", "black"},
            {"gray", "black"},
            {"gray29", "white"},
            {"antiquewhite", "black"},
            {"aquamarine", "black"},
            {"darkolivegreen1", "black"},
            {"limegreen", "white"},
            {"brown", "white"},
            {"aliceblue", "black"},
            {"burlywood1", "black"},
            {"white", "black"},
    };

    private String generateJMBTStamp(String url, String xpath, ElementType eltype, String context, Boolean assignedToUrl){
        if (eltype == ElementType.info)
            return "";
        else
            return String.format(
                    "\t\t/*JMBT\n" +
                    "\t\turl:%s\n" +
                    "\t\txpath:%s\n" +
                    "\t\teltype:%s\n" +
                    "\t\tcontext:%s\n" +
                    "\t\tassignedToUrl:%s\n" +
                    "\t\tJMBT*/\n",
                    url, xpath, eltype.name(), context == null ? "" : context, assignedToUrl ? "true" : "false");
    }

    private String generateHtmlNode(String nodeName,
                                    String url,
                                    String xpath,
                                    ElementType eltype,
                                    Boolean assignedToUrl,
                                    String context){
        String ch;
        switch (eltype){
            case unknown: ch = "&#9072;"; break;
            case noninteractive: ch = "&#128683"; break;
            case clickable: ch = "&#128432;"; break;
            case writable: ch = "&#128430;"; break;
            case info: ch = "&#128456;"; break; //really not ElementType
            case terminal: ch = "&#9940;"; break;
            default: ch = "&#9762;";
        }

        return String.format(
                "\t%s [\n" +
                "%s" +
                "\t\ttooltip=\"%s\"\n" +
                "\t\tfontcolor=\"%s\"\n" +
                "\t\tlabel=<<TABLE PORT=\"common\" border=\"0\" cellborder=\"1\" cellspacing=\"0\" bgcolor=\"%s\"><TR>\n" +
                "\t\t\t<TD cellpadding=\"0\"><FONT point-size=\"5\">%s</FONT></TD>\n" +
                "\t\t\t<TD cellpadding=\"1\"><FONT point-size=\"10\">$x(\"%s\")</FONT></TD>\n" +
                "\t\t\t<TD cellpadding=\"0\" href=\"%s\" tooltip=\"%s\"><FONT point-size=\"5\">%s</FONT></TD>\n" +
                "\t\t</TR></TABLE>>\n" +
                "\t];\n",
                nodeName, generateJMBTStamp(url, xpath, eltype, context, assignedToUrl), eltype,
                colorMap.get(url)[1], colorMap.get(url)[0],
                ch, xpath, url, url,
                assignedToUrl ? "<U><B>&#128279;</B></U>" : "&#128279;");
    }


    private PrintWriter writer = null;
    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateNewName();
        writer = new PrintWriter(folderName + name + ".gv", "UTF-8");
        writer.printf("digraph EFG {\n" +
                "\tnode [shape=plaintext]\n");
    }

    //TODO legend of a graph should be improved
    private List<String> orderedUrls = new ArrayList<>();
    private int lastInfoNodeNum = -1;
    public void addInfoNode(Integer num, String url, String label){
        writer.print(generateHtmlNode(String.format("Info%d", num), url, label, ElementType.info, false, ""));

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

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.toString())) {
            orderedUrls.add(node.handle.url.toString());
            colorMap.put(node.handle.url.toString(), colorList[colorMap.size() % colorList.length]);
        }

        writer.print(generateHtmlNode(String.format("Node%d", nodeID.get(node)),
                node.handle.url.toString(), node.handle.xpath, node.handle.eltype, node.handle.isAssignedToUrl(), node.context));
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.printf("\tNode%d:common -> Node%d:common;\n", nodeID.get(source), nodeID.get(destination));
    }

    private Process graphVizInstance;
    public String closeFile() throws IOException, InterruptedException{
        int i = 0; lastInfoNodeNum = -1;
        for(String url : orderedUrls)
            addInfoNode(++i, url, url);

        writer.println("}\n");
        writer.close();
        writer = null;
        String name = recoverLastName();
        if (graphVizInstance == null || !graphVizInstance.isAlive()) {
            String[] args = {"dot", "-Tsvg", folderName + name + ".gv", "-o", folderName + "graph" + ".svg"}; //#hardcode
            graphVizInstance = runtime.exec(args); //we don't need .waitFor() in our case, do we?
        }
        return name;
    }
}