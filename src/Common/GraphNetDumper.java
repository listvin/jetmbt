package Common;

import Boxes.Event;
import Boxes.JetURL;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class for convenient writing of EFG to viewable files.
 * 350% of GraphViz Java API functionality, lol
 * Created by listvin on 7/29/15.
 */
public class GraphNetDumper extends GraphDumper{
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

    private String generateNode(int num){
        return Integer.toString(num);
    }


    private boolean writingNodes = false;
    private int nodesCount = 0;
    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateNewName();
        writer = new PrintWriter(path + name + ".net", "UTF-8");
        writer.print("*Vertices ");
        writingNodes = false;
        nodesCount = 0;
    }

    //TODO legend of a graph can be improved
    public void addInfoNode(Integer num, String url, String label){
//        writer.print(generateNode(String.format("Info%d", num), url, label));
//
//        //drawing edge to previous info node
//        if (lastInfoNodeNum != -1)
//            writer.printf("\tInfo%d -> Info%d [dir=none];\n", lastInfoNodeNum, num);
//        lastInfoNodeNum = num;
    }

    public void presentNode(Event node){ ++nodesCount;}

    public void addNode(Event node){
        //checking if we have id for the node
        if (!nodeID.containsKey(node))
            nodeID.put(node, nodeID.size()+1);

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.graphUrl())) {
            orderedUrls.add(node.handle.url.graphUrl());
            colorMap.put(node.handle.url.graphUrl(), colorList[colorMap.size() % colorList.length]);
        }

        if (!writingNodes && nodesCount > 0){
            writer.println(nodesCount);
            writingNodes = true;
            nodesCount = 0;
        }

        writer.println(generateNode(nodeID.get(node)));
    }

    public void addEdgeFromTo(Event source, Event destination){
        if (writingNodes){
            writer.println("*Edges");
            writingNodes = false;
        }
        writer.println(nodeID.get(source).toString() + " " + nodeID.get(destination).toString());
    }

    public String closeFile() throws IOException, InterruptedException{
//        int i = 0; lastInfoNodeNum = -1;
//        for(String url : orderedUrls)
//            addInfoNode(++i, url, url);

        writer.close();
        writer = null;
        String name = recoverLastName();
//        if (renderInstance == null || !renderInstance.isAlive()) {
//            String[] args = {"dot", "-Tsvg", path + name + ".gv", "-o", path + "graph" + ".svg"}; //#hardcode
//            renderInstance = runtime.exec(args); //we don't need .waitFor() in our case, do we?
//        }
        return name+".net";
    }

    private Pattern pEdge = Pattern.compile("\\s*Node(\\d+):common\\s*->\\s*Node(\\d+):common;");//1,2
    private Pattern pNode = Pattern.compile("\\s*Node(\\d+)\\s*\\[");//1
    private Pattern pJMBThead = Pattern.compile("\\s*/\\*JMBT");
    private Pattern pJMBTtail = Pattern.compile("\\s*JMBT\\*/");

}
