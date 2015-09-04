package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.JetURL;
import Boxes.WebHandle;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for convenient writing of EFG to viewable files.
 * 350% of GraphViz Java API functionality, lol
 * Created by listvin on 7/29/15.
 */
public class GraphVizHtmlDumper extends GraphDumper{
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

    private String generateHtmlNode(String nodeName,
                                    String url,
                                    String xpath,
                                    ElementType eltype,
                                    Boolean assignedToUrl,
                                    Boolean ticked,
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
                nodeName, generateJMBTStamp(url, xpath, eltype, context, assignedToUrl, ticked), eltype,
                colorMap.get(url)[1], colorMap.get(url)[0],
                ch, Utils.htmlShield(xpath), Utils.htmlShield(url), Utils.htmlShield(url),
                assignedToUrl ? "<U><B>&#128279;</B></U>" : "&#128279;");
    }

    private String generateHtmlNode(String nodeName,
                                    String url,
                                    String xpath){
        return generateHtmlNode(nodeName, url, xpath, ElementType.info, false, false, "");
    }


    public void initFile() throws FileNotFoundException, UnsupportedEncodingException{
        assert writer == null : "Have not closed file";
        String name = generateNewName();
        writer = new PrintWriter(path + name + ".html.gv", "UTF-8");
        writer.printf("digraph EFG {\n" +
                "\tnode [shape=plaintext]\n");
    }

    //TODO legend of a graph can be improved
    public void addInfoNode(Integer num, String url, String label){
        writer.print(generateHtmlNode(String.format("Info%d", num), url, label));

        //drawing edge to previous info node
        if (lastInfoNodeNum != -1)
            writer.printf("\tInfo%d -> Info%d [dir=none];\n", lastInfoNodeNum, num);
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

        writer.print(generateHtmlNode(String.format("Node%d", nodeID.get(node)),
                node.handle.url.graphUrl(), node.handle.xpath, node.handle.eltype, node.handle.isAssignedToUrl(),
                node.isTicked(), node.context));
    }

    public void addEdgeFromTo(Event source, Event destination){
        writer.printf("\tNode%d:common -> Node%d:common;\n", nodeID.get(source), nodeID.get(destination));
    }

    public String closeFile() throws IOException, InterruptedException{
        int i = 0; lastInfoNodeNum = -1;
        for(String url : orderedUrls)
            addInfoNode(++i, url, url);

        writer.println("}\n");
        writer.close();
        writer = null;
        String name = recoverLastName();
        if (renderInstance == null || !renderInstance.isAlive()) {
            String[] args = {"dot", "-Tsvg", path + name + ".gv", "-o", path + "graph" + ".svg"}; //#hardcode
            renderInstance = runtime.exec(args); //we don't need .waitFor() in our case, do we?
        }
        return name+".html.gv";
    }

    private Pattern pEdge = Pattern.compile("\\s*Node(\\d+):common\\s*->\\s*Node(\\d+):common;");//1,2
    private Pattern pNode = Pattern.compile("\\s*Node(\\d+)\\s*\\[");//1
    private Pattern pJMBThead = Pattern.compile("\\s*/\\*JMBT");
    private Pattern pJMBTtail = Pattern.compile("\\s*JMBT\\*/");

    public void parseFile(EFG g, String path) throws IOException{
        String s;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        Matcher m;
        while ((s = reader.readLine()) != null){
            if ((m = pEdge.matcher(s)).matches())
                g.addEdgeUnchecked(rNodeID.get(Integer.valueOf(m.group(1))), rNodeID.get(Integer.valueOf(m.group(2))));
            else if ((m = pNode.matcher(s)).matches()) {
                int id = Integer.valueOf(m.group(1));
                if (!pJMBThead.matcher(reader.readLine()).matches()) throw new IOException("not found /*JMBT record");
                if (!(m = pUrl.matcher(reader.readLine())).matches()) throw new IOException("not found url: record");
                JetURL url = new JetURL(m.group(1));
                if (!(m = pXpath.matcher(reader.readLine())).matches()) throw new IOException("not found xpath: record");
                String xpath = m.group(1);
                if (!(m = pEltype.matcher(reader.readLine())).matches()) throw new IOException("not found eltype: record");
                ElementType eltype = ElementType.valueOf(m.group(1));
                if (!(m = pContext.matcher(reader.readLine())).matches()) throw new IOException("not found context: record");
                String context = m.group(1);
                if (!(m = pAssigned.matcher(reader.readLine())).matches()) throw new IOException("not found assignedToUrl: record");
                boolean assigned = Boolean.valueOf(m.group(1));
                if (!(m = pTicked.matcher(reader.readLine())).matches()) throw new IOException("not found ticked: record");
                boolean ticked = Boolean.valueOf(m.group(1));
                if (!pJMBTtail.matcher(reader.readLine()).matches()) throw new IOException("not found JMBT*/ record");
                Event event = Event.create(new WebHandle(url, xpath, eltype, assigned), context);
                if (ticked) event.setTicked();
                g.addEventUnchecked(event);
                log.info("Have successfully read Node" + id + " from file.");
                while (rNodeID.size() < id+1) rNodeID.add(null);
                rNodeID.set(id, event);
            }
        }
    }
}
