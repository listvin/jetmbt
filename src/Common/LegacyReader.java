package Common;

import Boxes.EFG;
import Boxes.Event;
import Boxes.JetURL;
import Boxes.WebHandle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 9/12/15.
 */
public class LegacyReader {
    private static Logger log = Logger.get(new LegacyReader());
    private static ArrayList<Event> rNodeID = new ArrayList<>();
    private static Pattern pEdge = Pattern.compile("\\s*Node(\\d+):common\\s*->\\s*Node(\\d+):common;");//1,2
    private static Pattern pNode = Pattern.compile("\\s*Node(\\d+)\\s*\\[");//1
    private static Pattern pJMBThead = Pattern.compile("\\s*/\\*JMBT");
    private static Pattern pJMBTtail = Pattern.compile("\\s*JMBT\\*/");
    private static Pattern pUrl = Pattern.compile("\\s*url:(\\p{all}*)"); //1
    private static Pattern pXpath = Pattern.compile("\\s*xpath:(\\p{all}*)"); //1
    private static Pattern pEltype = Pattern.compile("\\s*eltype:(\\p{all}*)"); //1
    private static Pattern pContext = Pattern.compile("\\s*context:(\\p{all}*)"); //1
    private static Pattern pAssigned = Pattern.compile("\\s*assignedToUrl:(\\p{all}*)"); //1
    private static Pattern pTicked = Pattern.compile("\\s*ticked:(\\p{all}*)"); //1
    public static void parseFile(EFG g, String path) throws IOException {
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

    public static void main(String args[]){
        Logger log = new Logger(new LegacyReader(), Logger.Level.all, Logger.Level.all);
        Thread logThread = new Thread(log);
        logThread.start(); logThread.setPriority(Thread.MIN_PRIORITY);

        String num;
        num = "3093";
//        num = "100";
        EFG g = new EFG();
        try {
            parseFile(g, "/home/user/Dropbox/jb/graphs_workshop/dump#"+num+".html.gv");
        } catch (IOException e) {
            log.exception(e);
        }
        GraphDumper.setCustomPath("/home/user/Dropbox/jb/graphs_workshop/new/", "dump#"+num);
        g.dump2dot();
        log.report("read'n'dump is finishing...");

        logThread.interrupt();
    }

}
