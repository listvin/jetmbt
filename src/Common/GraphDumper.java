package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.WebHandle;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for convenient writing of EFG to viewable files.
 * 350% of GraphViz Java API functionality, lol
 * Created by listvin on 7/29/15.
 */
public class GraphDumper {
    private Runtime runtime = Runtime.getRuntime();
    public final String folderName, path;
    private Integer dumpNum = 0;
    public GraphDumper() {
        folderName = Logger.getFolderTimeName();
        path = "graphs/" + folderName + "/";
        String[] args = {"mkdir", path}; //#hardcode
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
        writer = new PrintWriter(path + name + ".gv", "UTF-8");
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
            writer.printf("\tInfo%d -> Info%d [dir=none];\n", lastInfoNodeNum, num);
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


    private ArrayList<Event> rNodeID = new ArrayList<>();
    Pattern pEdge = Pattern.compile("\\s*Node(\\d+):common\\s*->\\s*Node(\\d+):common;");//1,2
    Pattern pNode = Pattern.compile("\\s*Node(\\d+)\\s*\\[");//1
    Pattern pJMBThead = Pattern.compile("\\s*/\\*JMBT");
    Pattern pUrl = Pattern.compile("\\s*url:(\\p{all}*)"); //1
    Pattern pXpath = Pattern.compile("\\s*xpath:(\\p{all}*)"); //1
    Pattern pEltype = Pattern.compile("\\s*eltype:(\\p{all}*)"); //1
    Pattern pContext = Pattern.compile("\\s*context:(\\p{all}*)"); //1
    Pattern pAssigned = Pattern.compile("\\s*assignedToUrl:(\\p{all}*)"); //1
    Pattern pJMBTtail = Pattern.compile("\\s*JMBT\\*/");
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
                URL url = new URL(m.group(1));
                if (!(m = pXpath.matcher(reader.readLine())).matches()) throw new IOException("not found xpath: record");
                String xpath = m.group(1);
                if (!(m = pEltype.matcher(reader.readLine())).matches()) throw new IOException("not found eltype: record");
                ElementType eltype = ElementType.valueOf(m.group(1));
                if (!(m = pContext.matcher(reader.readLine())).matches()) throw new IOException("not found context: record");
                String context = m.group(1);
                if (!(m = pAssigned.matcher(reader.readLine())).matches()) throw new IOException("not found assignedToUrl: record");
                boolean assigned = Boolean.valueOf(m.group(1));
                if (!(m = pJMBTtail.matcher(reader.readLine())).matches()) throw new IOException("not found JMBT*/ record");
                Event event = Event.create(new WebHandle(url, xpath, eltype, assigned), context);
                g.addEventUnchecked(event);
                System.err.println("id: " + id);
                while (rNodeID.size() < id+1) rNodeID.add(null);
                rNodeID.set(id, event);
            }
        }
    }
}
