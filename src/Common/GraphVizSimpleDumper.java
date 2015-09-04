package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.JetURL;
import Boxes.WebHandle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for convenient writing of EFG to viewable files.
 * 350% of GraphViz Java API functionality, lol
 * Created by listvin on 7/29/15.
 */
public class GraphVizSimpleDumper extends GraphDumper{
    protected final String[][] colorList = {
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
            {"cornflowerblue", "black"},
            {"maroon", "white"},
            {"cyan", "black"},
            {"gray", "white"},
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
    protected Map<String, String[]> colorMap = new HashMap<String, String[]>(){{
        put(JetURL.createOwn404().graphUrl(), colorList[0]); //#hardocode //or not?
    }};

    private String generateNode(String nodeName,
                                String url,
                                String xpath,
                                ElementType eltype,
                                Boolean assignedToUrl,
                                Boolean ticked,
                                String context){
        String shape;
        switch (eltype){
            case unknown: shape = "doubleoctagon"; break;
            case noninteractive: shape = "tripleoctagon"; break;
            case clickable: shape = "tab"; break;
            case writable: shape = "signature"; break;
            case info: shape = "note"; break; //really not ElementType
            case terminal: shape = "signature"; break;
            default: shape = "box";
        }

        return String.format(
                "\t%s [\n" +
//                        "%s" +
                        "\t\ttooltip=\"%s\"\n" +
                        "\t\tfontcolor=\"%s\" fillcolor=\"%s\"" +
                        "\t];\n",
                nodeName,
//                generateJMBTStamp(url, xpath, eltype, context, assignedToUrl, ticked),
                nodeName, colorMap.get(url)[1], colorMap.get(url)[0]);
//        return String.format(
//                "\t%s [\n" +
//                        "%s" +
//                        "\t\ttooltip=\"%s\"\n" +
//                        "\t\tfontcolor=\"%s\" fillcolor=\"%s\" shape=\"%s\"\n" +
//                        "\t\tlabel=\"%s\"\n" +
//                        "\t\tURL=\"%s\"\n" +
//                        "\t];\n",
//                nodeName, generateJMBTStamp(url, xpath, eltype, context, assignedToUrl, ticked), eltype,
//                colorMap.get(url)[1], colorMap.get(url)[0], shape, Utils.dotShield(xpath), url);
    }

    private String generateNode(String nodeName,
                                String url,
                                String xpath){
        return generateNode(nodeName, url, xpath, ElementType.info, false, false, "");
    }


    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateNewName();
        writer = new PrintWriter(path + name + ".simple.gv", "UTF-8");
        writer.printf("graph simplifiedEFG {\n" +
                "\tnode [shape=star]\n");
    }

    //TODO legend of a graph can be improved
    public void addInfoNode(Integer num, String url, String label){
        writer.print(generateNode(String.format("Info%d", num), url, label));

        //drawing edge to previous info node
        if (lastInfoNodeNum != -1)
            writer.printf("\tInfo%d -- Info%d [dir=none];\n", lastInfoNodeNum, num);
        lastInfoNodeNum = num;
    }

    public void addNode(Event node){
        //checking if we have id for the node
        if (!nodeID.containsKey(node))
            nodeID.put(node, nodeID.size());

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.graphUrl())) {
            orderedUrls.add(node.handle.url.graphUrl());
            colorMap.put(node.handle.url.graphUrl(), colorList[colorMap.size() % colorList.length]);
        }

        writer.print(generateNode(String.format("Node%d", nodeID.get(node)),
                node.handle.url.graphUrl(), node.handle.xpath, node.handle.eltype, node.handle.isAssignedToUrl(),
                node.isTicked(), node.context));
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.printf("\tNode%d -- Node%d;\n", nodeID.get(source), nodeID.get(destination));
    }

    public String closeFile() throws IOException, InterruptedException{
        int i = 0; lastInfoNodeNum = -1;
//        for(String url : orderedUrls)
//            addInfoNode(++i, url, url);

        writer.println("}\n");
        writer.close();
        writer = null;
        String name = recoverLastName();
        if (renderInstance == null || !renderInstance.isAlive()) {
            String[] args = {"dot", "-Tsvg", path + name + ".gv", "-o", path + "graph" + ".svg"}; //#hardcode
            renderInstance = runtime.exec(args); //we don't need .waitFor() in our case, do we?
        }
        return name+".simple.gv";
    }

}
