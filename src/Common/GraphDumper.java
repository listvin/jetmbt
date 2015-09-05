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
 * Created by user on 8/25/15.
 */
public class GraphDumper {
    private Runtime runtime = Runtime.getRuntime();
    private Logger log = new Logger(this, Logger.Level.debug);
    private Process renderInstance;
    private Integer dumpNum = 0;
    private static String customName, path;
    public static void setCustomPath(String path, String name){
        GraphDumper.path = path;
        GraphDumper.customName = name;
    }
    public GraphDumper() {
        path = "graphs/" + Logger.folderName + "/";
        String[] args = {"mkdir", path}; //#hardcode
        try {
            runtime.exec(args).waitFor();
        } catch (InterruptedException | IOException e) {
            log.exception(e);
        }
    }
    private String generateName(Integer num){ return "dump#" + num.toString(); }
    private String generateNewName(){ return GraphDumper.customName == null ? generateName(++dumpNum) : GraphDumper.customName; }
    private String recoverLastName(){ return GraphDumper.customName == null ? generateName(dumpNum) : GraphDumper.customName; }

    private PrintWriter dotFile = null;
    private PrintWriter infoFile = null;
    private PrintWriter jmbtFile = null; //todo implement this

    private Map<Event, Integer> nodeID = new HashMap<>();

    private void writeDOTNode(Event node){
        dotFile.printf("node_%d [tooltip=\"@%s\"; fillcolor=\"%s\"];\n", nodeID.get(node), Utils.base58(nodeID.get(node)), colorMap.get(node.handle.url.graphUrl()));
    }
    private void writeDOTEdge(Event src, Event dst){
        dotFile.printf("node_%d -> node_%d [color=\"%s90:%s90;0.5\"]; \n", nodeID.get(src), nodeID.get(dst), colorMap.get(dst.handle.url.graphUrl()), colorMap.get(src.handle.url.graphUrl()));
    }
    private void writeInfoRow(Event node){
        infoFile.println("<TR>");
        infoFile.println("\t<TD bgcolor=" + colorMap.get(node.handle.url.graphUrl()) + "><FONT color=black> @" + Utils.base58(nodeID.get(node)) + " </FONT></TD>");
        infoFile.println("\t<TD><FONT color=white> " + Utils.htmlShield(node.handle.url.graphUrl()) + " </FONT></TD>");
        infoFile.println("\t<TD><FONT color=white> " + Utils.htmlShield(node.handle.xpath) + " </FONT></TD>");
        infoFile.println("\t<TD><FONT color=white> " + node.handle.eltype.name() + " </FONT></TD>");
        infoFile.println("\t<TD><FONT color=white> " + node.handle.isAssignedToUrl() + " </FONT></TD>");
        infoFile.println("\t<TD><FONT color=white> " + Utils.htmlShield(node.context) + " </FONT></TD>");
        infoFile.println("</TR>");
    }
    private String genJMBTStamp(Event node){
        if (node.handle.eltype == ElementType.info)
            return "";
        else
            return String.format(
                    "\t\t/*JMBT\n" +
                            "\t\turl:%s\n" +
                            "\t\txpath:%s\n" +
                            "\t\teltype:%s\n" +
                            "\t\tcontext:%s\n" +
                            "\t\tassignedToUrl:%s\n" +
                            "\t\tticked:%s\n" +
                            "\t\tJMBT*/\n",
                    node.handle.url.graphUrl(), node.handle.xpath, node.handle.eltype.name(), node.context == null ? "" : node.context,
                    node.handle.isAssignedToUrl() ? "true" : "false", node.isTicked() ? "true" : "false");
    }


    private List<JetURL> orderedUrls = new ArrayList<>();
    private void writePalette() throws FileNotFoundException{
        //writing palette
        PrintWriter printer = new PrintWriter(path + recoverLastName() + ".palette.html");
        printer.println("<body bgcolor=black> <TABLE border=0 cellspacing=2>");
        for (JetURL url : orderedUrls)
            printer.println("<TR> <TD bgcolor=" + colorMap.get(url.graphUrl()) + "><FONT color=black> " + url.graphUrl() + " </FONT></TD> </TR>");
        printer.println("</TABLE> </body>");
        printer.close();
    }

    private boolean closed = true;
    public void initFile() throws FileNotFoundException{
        String name = generateNewName();
        dotFile = new PrintWriter(path + name + ".gv");
        dotFile.println("digraph EFG {\n" +
                "\tgraph [\n" +
                "\t\tlayout=sfdp\n" +
                "\t\toverlap=prism1500\n" +
                "\t\tbgcolor=black\n" +
                "\t\toutputorder=edgesfirst\n" +
                "\t]\n" +
                "\tnode [\n" +
                "\t\tshape=pentagon\n" +
                "\t\tstyle=filled\n" +
                "\t\tlabel=\"\"\n" +
                "\t\twidth=0.2\n" +
                "\t\theight=0.2\n" +
                "\t\tpenwidth=0.5\n" +
                "\t]");
        infoFile = new PrintWriter(path + name + ".list.html");
        infoFile.println("<body bgcolor=black> <TABLE border=0 cellspacing=2>");
    }
    private boolean writingNodes = false;
    public void addNode(Event node){
        //checking if we have id for the node
        if (!nodeID.containsKey(node))
            nodeID.put(node, nodeID.size());

        //checking if we have a color assigned to event's url
        if (!colorMap.containsKey(node.handle.url.graphUrl())) {
            orderedUrls.add(node.handle.url);
            colorMap.put(node.handle.url.graphUrl(), colorList[colorMap.size() % colorList.length]);
        }

        writeDOTNode(node);
        writeInfoRow(node);
        //writeJMBTStamp(node);
    }
    private boolean writingEdges = false;
    public void addEdge(Event source, Event destination){
        writeDOTEdge(source, destination);
    }
    public String closeFile() throws FileNotFoundException{
        dotFile.println("}\n");
        dotFile.close();
        dotFile = null;
        infoFile.println("</TABLE> </body>");
        infoFile.close();
        infoFile = null;
        writePalette();
        String name = recoverLastName();
        if (renderInstance == null || !renderInstance.isAlive()) {
            String[] args = {"dot", "-Tsvg", path + name + ".gv", "-o", path + "graph" + ".svg"}; //#hardcode
            try {
                renderInstance = runtime.exec(args); //we don't need .waitFor() in our case, do we?
            } catch (IOException e){
                log.exception(e);
            }
        }
        return name;
    }

    private ArrayList<Event> rNodeID = new ArrayList<>();
    private Pattern pEdge = Pattern.compile("\\s*Node(\\d+):common\\s*->\\s*Node(\\d+):common;");//1,2
    private Pattern pNode = Pattern.compile("\\s*Node(\\d+)\\s*\\[");//1
    private Pattern pJMBThead = Pattern.compile("\\s*/\\*JMBT");
    private Pattern pJMBTtail = Pattern.compile("\\s*JMBT\\*/");
    private Pattern pUrl = Pattern.compile("\\s*url:(\\p{all}*)"); //1
    private Pattern pXpath = Pattern.compile("\\s*xpath:(\\p{all}*)"); //1
    private Pattern pEltype = Pattern.compile("\\s*eltype:(\\p{all}*)"); //1
    private Pattern pContext = Pattern.compile("\\s*context:(\\p{all}*)"); //1
    private Pattern pAssigned = Pattern.compile("\\s*assignedToUrl:(\\p{all}*)"); //1
    private Pattern pTicked = Pattern.compile("\\s*ticked:(\\p{all}*)"); //1
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

    private String[] colorList = {
            "#3B3B3B",  //service
            "#FF0000",  //red pure
            "#1078FF",  //blue pure
            "#00FF00",  //green pure
            "#10FFFF",  //blue pure-cyan
            "#FFFF00",  //yellow pure
            "#F000F0",  //violet pure-fuchsia
            "#00B000",  //green dark
            "#FF5500",  //orange foxy
            "#FFA0A0",  //red light
            "#B1B1B1",  //grey light
            "#9030F0",  //violet pure
            "#32CD32",  //green salad
            "#FF0077",  //red fuchsia
            "#78A0FF",  //blue mariner
            "#AA6611",  //orange brown
            "#FF80FF",  //violet light-pink
            "#777700",  //yellow dark
            "#818181",  //grey middle
            "#006300",  //green darkest
            "#FF7070",  //red warm-pink
            "#99FF99",  //green light
            "#1111FF",  //blue darkest
            "#FFAA00",  //orange squirel
            "#A000A0",  //violet dark
            "#FFFFA8",  //yellow light
            "#FFFFFF",  //grey white
            "#CCFFCC",  //green lightest
            "#A00000",  //red dark
            "#AAEE60",  //green olive
            "#D0D0FF",  //blue light
            "#EECC66",  //orange golden
            "#FC28A0",  //violet cold-pink
            "#A6FF00",  //yellow green
            "#616161",  //grey dark
            "#00FF88",  //green coldest
    };
    protected Map<String, String> colorMap = new HashMap<String, String>(){{
        put(JetURL.createOwn404().graphUrl(), colorList[0]); //#hardocode //or not?
    }};
}
