package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.JetURL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 8/25/15.
 */
public class GraphMLDumper extends GraphDumper{
    protected final String[] colorList = {
            "<data key=\"r\">59</data><data key=\"g\">59</data><data key=\"b\">59</data>",
            "<data key=\"r\">255</data><data key=\"g\">0</data><data key=\"b\">0</data>",
            "<data key=\"r\">16</data><data key=\"g\">120</data><data key=\"b\">255</data>",
            "<data key=\"r\">0</data><data key=\"g\">255</data><data key=\"b\">0</data>",
            "<data key=\"r\">16</data><data key=\"g\">255</data><data key=\"b\">255</data>",
            "<data key=\"r\">255</data><data key=\"g\">255</data><data key=\"b\">0</data>",
            "<data key=\"r\">240</data><data key=\"g\">0</data><data key=\"b\">240</data>",
            "<data key=\"r\">0</data><data key=\"g\">176</data><data key=\"b\">0</data>",
            "<data key=\"r\">255</data><data key=\"g\">85</data><data key=\"b\">0</data>",
            "<data key=\"r\">255</data><data key=\"g\">160</data><data key=\"b\">160</data>",
            "<data key=\"r\">177</data><data key=\"g\">177</data><data key=\"b\">177</data>",
            "<data key=\"r\">144</data><data key=\"g\">48</data><data key=\"b\">240</data>",
            "<data key=\"r\">50</data><data key=\"g\">205</data><data key=\"b\">50</data>",
            "<data key=\"r\">255</data><data key=\"g\">0</data><data key=\"b\">119</data>",
            "<data key=\"r\">120</data><data key=\"g\">160</data><data key=\"b\">255</data>",
            "<data key=\"r\">170</data><data key=\"g\">102</data><data key=\"b\">17</data>",
            "<data key=\"r\">255</data><data key=\"g\">128</data><data key=\"b\">255</data>",
            "<data key=\"r\">119</data><data key=\"g\">119</data><data key=\"b\">0</data>",
            "<data key=\"r\">129</data><data key=\"g\">129</data><data key=\"b\">129</data>",
            "<data key=\"r\">0</data><data key=\"g\">99</data><data key=\"b\">0</data>",
            "<data key=\"r\">255</data><data key=\"g\">112</data><data key=\"b\">112</data>",
            "<data key=\"r\">153</data><data key=\"g\">255</data><data key=\"b\">153</data>",
            "<data key=\"r\">17</data><data key=\"g\">17</data><data key=\"b\">255</data>",
            "<data key=\"r\">255</data><data key=\"g\">170</data><data key=\"b\">0</data>",
            "<data key=\"r\">160</data><data key=\"g\">0</data><data key=\"b\">160</data>",
            "<data key=\"r\">255</data><data key=\"g\">255</data><data key=\"b\">168</data>",
            "<data key=\"r\">255</data><data key=\"g\">255</data><data key=\"b\">255</data>",
            "<data key=\"r\">204</data><data key=\"g\">255</data><data key=\"b\">204</data>",
            "<data key=\"r\">160</data><data key=\"g\">0</data><data key=\"b\">0</data>",
            "<data key=\"r\">170</data><data key=\"g\">238</data><data key=\"b\">96</data>",
            "<data key=\"r\">208</data><data key=\"g\">208</data><data key=\"b\">255</data>",
            "<data key=\"r\">238</data><data key=\"g\">204</data><data key=\"b\">102</data>",
            "<data key=\"r\">252</data><data key=\"g\">40</data><data key=\"b\">160</data>",
            "<data key=\"r\">166</data><data key=\"g\">255</data><data key=\"b\">0</data>",
            "<data key=\"r\">97</data><data key=\"g\">97</data><data key=\"b\">97</data>",
            "<data key=\"r\">0</data><data key=\"g\">255</data><data key=\"b\">136</data>",
    };
    protected Map<String, String> colorMap = new HashMap<String, String>(){{
        put(JetURL.createOwn404().graphUrl(), colorList[0]); //#hardocode //or not?
    }};

    public void initFile() throws FileNotFoundException, UnsupportedEncodingException {
        assert writer == null : "Have not closed file";
        writer = new PrintWriter(path + generateNewName() + ".graphml", "UTF-8");
        writer.printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graphml>\n" +
                "\t\n" +
                "\t<key attr.name=\"r\"\t\t\t\tattr.type=\"int\"\t\tfor=\"node\" id=\"r\"/>\n" +
                "\t<key attr.name=\"g\"\t\t\t\tattr.type=\"int\"\t\tfor=\"node\" id=\"g\"/>\n" +
                "\t<key attr.name=\"b\"\t\t\t \tattr.type=\"int\"\t\tfor=\"node\" id=\"b\"/>\n" +
                "\t<key attr.name=\"Label\"\t\t\tattr.type=\"string\"\tfor=\"node\" id=\"l\"/>\n" +
                "\t<key attr.name=\"xpath selector\"\tattr.type=\"string\"\tfor=\"node\" id=\"s\"/>\n" +
                "\t<key attr.name=\"URL\"\t\t\tattr.type=\"string\"\tfor=\"node\" id=\"u\"/>\n" +
                "\n" +
                "  <graph id=\"G\" edgedefault=\"directed\">\n" +
                "\n");
    }

    private String generateXmlNode(String name, String url, String selector, String label){
        return String.format("\t<node id=\"%s\">\n" +
                "\t\t%s\n" +
                "\t\t<data key=\"u\">%s</data>\n" +
                "\t\t<data key=\"s\">%s</data>\n" +
                "\t\t<data key=\"l\">%s</data>\n" +
                "\t</node>", name, colorMap.get(url), Utils.htmlShield(url), Utils.htmlShield(selector), Utils.htmlShield(label));
    }

    private static int edgeCounter = 0;
    private String generateXmlEdge(String from, String to){
        return String.format("\t<edge id=\"e%d\" source=\"%s\" target=\"%s\"/>", edgeCounter++, from, to);
    }

    public void addInfoNode(Integer num, String url, String label){
        writer.println(generateXmlNode("Info"+num, url, "", label));

        //drawing edge to previous info node
        if (lastInfoNodeNum != -1)
            writer.println(generateXmlEdge("Info" + lastInfoNodeNum, "Info" + num));
        lastInfoNodeNum = num;
    }

    public void addNode(Event node){
        //checking if we have id for the node
        if (!nodeID.containsKey(node))
            nodeID.put(node, nodeID.size());

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.graphUrl())) {
            orderedUrls.add(node.handle.url.graphUrl());
            colorMap.put(node.handle.url.graphUrl(), colorList[colorMap.size() % (colorList.length-1) +1]);
        }

        writer.println(generateXmlNode("Node" + nodeID.get(node), node.handle.url.graphUrl(),
                node.handle.xpath, "$x('" + node.handle.xpath + "')"));
//        writer.print(generateHtmlNode(String.format("Node%d", nodeID.get(node)),
//                node.handle.url.graphUrl(), node.handle.xpath, node.handle.eltype, node.handle.isAssignedToUrl(),
//                node.isTicked(), node.context));
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.println(generateXmlEdge("Node" + nodeID.get(source), "Node" + nodeID.get(destination)));
    }

    public String closeFile() throws IOException, InterruptedException{
        int i = 0; lastInfoNodeNum = -1;
        for(String url : orderedUrls)
            addInfoNode(++i, url, url);

        writer.println("\n" +
                "  </graph>\n" +
                "  \n" +
                "</graphml>\n");
        writer.close();
        writer = null;
        return recoverLastName()+".graphml";
    }
}
