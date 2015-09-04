package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.JetURL;
import Boxes.WebHandle;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 8/25/15.
 */
public abstract class GraphDumper {
    protected Runtime runtime = Runtime.getRuntime();
    protected Logger log = new Logger(this, Logger.Level.debug);
    protected Integer dumpNum = 0;
    protected Integer lastInfoNodeNum = -1;
    protected PrintWriter writer = null;
    protected List<String> orderedUrls = new ArrayList<>();
    protected Map<Event, Integer> nodeID = new HashMap<>();
    protected ArrayList<Event> rNodeID = new ArrayList<>();
    protected Process renderInstance;

    public static String folderName, path;
    public GraphDumper() {
        folderName = Logger.folderName;
        path = "graphs/" + folderName + "/";
        String[] args = {"mkdir", path}; //#hardcode
        try {
            runtime.exec(args).waitFor();
        } catch (InterruptedException | IOException e) {
            log.exception(e);
        }
    }

    public static String customName;
    public static void setCustomPath(String path, String name){
        GraphDumper.path = path;
        GraphDumper.customName = name;
    }

    protected String generateName(Integer num){ return "dump#" + num.toString(); }
    protected String generateNewName(){ return GraphDumper.customName == null ? generateName(++dumpNum) : GraphDumper.customName; }
    protected String recoverLastName(){ return GraphDumper.customName == null ? generateName(dumpNum) : GraphDumper.customName; }

    protected String generateJMBTStamp(String url, String xpath, ElementType eltype, String context, Boolean assignedToUrl, Boolean ticked){
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
                            "\t\tticked:%s\n" +
                            "\t\tJMBT*/\n",
                    url, xpath, eltype.name(), context == null ? "" : context,
                    assignedToUrl ? "true" : "false", ticked ? "true" : "false");
    }
    public abstract void initFile() throws FileNotFoundException, UnsupportedEncodingException;
    public abstract void addInfoNode(Integer num, String url, String label);
    public void presentNode(Event node){

    };
    public abstract void addNode(Event node);
    public abstract void addEdgeFromTo(Event source, Event destination);
    public abstract String closeFile() throws IOException, InterruptedException;

    protected Pattern pUrl = Pattern.compile("\\s*url:(\\p{all}*)"); //1
    protected Pattern pXpath = Pattern.compile("\\s*xpath:(\\p{all}*)"); //1
    protected Pattern pEltype = Pattern.compile("\\s*eltype:(\\p{all}*)"); //1
    protected Pattern pContext = Pattern.compile("\\s*context:(\\p{all}*)"); //1
    protected Pattern pAssigned = Pattern.compile("\\s*assignedToUrl:(\\p{all}*)"); //1
    protected Pattern pTicked = Pattern.compile("\\s*ticked:(\\p{all}*)"); //1

    public void parseFile(EFG g, String path) throws IOException {
        log.error("can't read all graph files yet, trying to read as gv");
        GraphVizHtmlDumper gvd = new GraphVizHtmlDumper();
        gvd.parseFile(g, path);
        log.report("gv-reading seems to be finished...");
    }
}
