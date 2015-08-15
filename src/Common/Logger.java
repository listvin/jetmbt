package Common;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**Expected to be executed in separate non-daemon thread, but only once*/
public class Logger implements Runnable{
    public enum Level {all, info/*blue*/, debug/*yell*/, warning/*yell*/, error/*red*/, report/*green*/, off}

    //Static fields
    public static final String folderName;
    private static final String path;
    private static HashMap<Object, Logger> mem = new HashMap<>();
    private static BlockingQueue<Map.Entry<PrintStream, String>> msgQueue = new LinkedBlockingQueue<>();
    private static SimpleDateFormat hhmmsssss = new SimpleDateFormat("[HH:mm:ss.SSS]: ");
    static {
        folderName = (new SimpleDateFormat("ddMMMyyyy_EEE_HH%1mm%2ss.SSS%3"))
                .format(new Date()).toLowerCase()
                .replace("%1", "h").replace("%2", "m").replace("%3", "s");
        path = "log/" + folderName + "/";
        String[] args = {"mkdir", path};
        try {
            Runtime.getRuntime().exec(args).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
            System.err.println("CRITICAL ERROR: Logger can't create folder. Check access and OS settings.");
        }
        try {
            stopwatch = new PrintStream(path + "STOPWATCH" + ".CSV", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
            stopwatch = System.out;
        }
    }

    //Instance fields
    private Level file, console;
    private PrintStream printer;
    private String name;

    //Constructors and factories
    public Logger(Object owner, Level console, Level file){
        this.file = file;
        this.console = console;
        name = Utils.getJavaDefaultName(owner);
        try {
            printer = new PrintStream(path + name + ".log", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }
        if (!mem.containsKey(owner)) info("Personal Logger created.");
        mem.put(owner, this);
    }
    public Logger(Object owner, Level console) { this(owner, console, Level.info); }
    /**
     * Creates new Logger, assigned to instance with default verbosity levels:
     * - error for console ()
     * @param owner class which supposed to send logs about himself
     */
    public Logger(Object owner){ this(owner, Level.error, Level.info); }
    public static Logger get(Object obj){
        if (mem.containsKey(obj))
            return mem.get(obj);
        else
            return new Logger(obj);
    }
        
    //Instance methods
    private void printMessage(String color, String LVL, String text, Level threshold){
        String stamp = hhmmsssss.format(new Date());
        if (threshold.compareTo(console) >= 0)
            msgQueue.add(new AbstractMap.SimpleEntry<>(System.out,
                    "\u001B[" + color + ";1m" + LVL + stamp + "\u001B[0m" +
                            "\u001B[" + color + "m" + "(by " + name + ") " + "\u001B[0m" +
                            text +
                            "\u001B[" + color + ";1m\n^^^^^^^^^^^^^^^^^^\u001B[0m\n"));
        if (threshold.compareTo(file) >= 0)
            msgQueue.add(new AbstractMap.SimpleEntry<>(printer,
                    LVL + stamp +
                            text +
                            "\n~~~~~~~~~~~~~~~~~~\n\n"));
    }
    public void info(String msg){
        printMessage("34", "INF", msg, Level.info);
    }
    public void debug(String msg){
        printMessage("33", "DBG", msg, Level.debug);
    }
    public void warning(String msg){
        printMessage("33", "WRN", msg, Level.warning);
    }
    public void error(String msg){
        printMessage("31", "ERR", msg, Level.error);
    }
    public <T extends Exception> void exception(T e){
        printMessage("31", "EXC", ExceptionUtils.getStackTrace(e), Level.error);
    }
    public void report(String msg){
        printMessage("32", "REP", msg, Level.report);
    }

    //Further contains operating with checkpoints
    private static PrintStream stopwatch;
    private static class CheckPoint{
        final String prefix, suffix;
        long m = Long.MAX_VALUE, av = 0, M = -1;
        long hits = 0, lastHit = 0;
        final CheckPoint prev;
        CheckPoint(){ prev = null; prefix = suffix = null; }
        CheckPoint(CheckPoint prev, String codePoint, Integer number, String description){
            this.prev = prev;
            prefix = codePoint +"\t"+ number;
            suffix = description;
        }
        void hit(long millis){
            ++hits;
            lastHit = millis;
            if (prev != null) {
                long delta = lastHit - prev.lastHit;
                m = Math.min(m, delta);
                M = Math.max(M, delta);
                av = (av * (hits-1) + delta)/hits;
            }
        }
    }
    private static class Track{
        TreeMap<String, CheckPoint> cp2cp = new TreeMap<>();
        CheckPoint last = null;
        void pushHit(String codePoint, long millis){ (last = cp2cp.get(codePoint)).hit(millis); }
        void hit(String codePoint, long millis){
            if (!cp2cp.containsKey(codePoint))
                cp2cp.put(codePoint, new CheckPoint());
            pushHit(codePoint, millis);
        }
        void hit(String codePoint, String description, long millis){
            if (!cp2cp.containsKey(codePoint))
                cp2cp.put(codePoint, new CheckPoint(last, codePoint, cp2cp.size(), description));
            pushHit(codePoint, millis);
        }
        String getRows(){
            if (last == null) return "";
            StringBuilder sb = new StringBuilder();
            for (CheckPoint cp : cp2cp.values()) if (cp.prev != null){
                sb.insert(0, '\n'); sb.insert(0, cp.suffix);
                sb.insert(0, '\t'); sb.insert(0, cp.M);
                sb.insert(0, '\t'); sb.insert(0, cp.av);
                sb.insert(0, '\t'); sb.insert(0, cp.m);
                sb.insert(0, '\t'); sb.insert(0, cp.hits);
                sb.insert(0, '\t'); sb.insert(0, cp.prefix);
            }
            return sb.toString();
        }
    }
    private static HashMap <String, Track> cp2track = new HashMap<>();
    private static void checkpoint(String label, String description, boolean initial){
        long millis = System.currentTimeMillis();
        //prepearing codePoint to search for track and cp
            StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
            String codePoint = ste.getClassName() + "." + ste.getMethodName() + (label != null ? "(~)#" + label : "(~)");
        
        //creating new track, if needed
            if (!cp2track.containsKey(codePoint)) cp2track.put(codePoint, new Track());

        //appending codePoint
            if (initial)
                cp2track.get(codePoint).hit(codePoint + "\t:" + ste.getLineNumber(), millis);
            else
                cp2track.get(codePoint).hit(codePoint + "\t:" + ste.getLineNumber(), description, millis);
    }
    /**
     * May be used for real time investigations.
     * Use {@link #init()} in the start of investigated method.
     * Will write collected data to "STOPWATCH.CSV" file in corresponding logs folder.
     * Use judiciously, only in develop needs, cause itself it's rather slow.
     * See {@link #cpd(String)} - to attach descritpion
     */
    public static void cp(){ checkpoint(null, "nope", false); }
    /**
     * May be used for real time investigations.
     * Use {@link #init()} in the start of investigated method.
     * Will write collected data to "STOPWATCH.CSV" file in corresponding logs folder.
     * Use judiciously, only in develop needs, cause itself it's rather slow.
     * @param description - see also {@link #cp()}
     */
    public static void cpd(String description){ checkpoint(null, description, false); }
    public static void cpl(String label){ checkpoint(label, "nope", false); }
    public static void cp(String label, String description){ checkpoint(label, description, false); }
    /**
     * Should be called from the very beginning of the method under investigation or cycle body.
     * See also {@link #cp()}, {@link #cpd(String), {@link #cpl(String) and {@link #cp(String, String)
     */
    public static void init(){ checkpoint(null, null, true); }
    public static void init(String label){ checkpoint(label, null, true); }

    //This is non-daemon daemon, which will print queued text to files
    public void run() {
        Map.Entry<PrintStream, String> entry;
        try {
            while (!Thread.interrupted())
                if ((entry = msgQueue.poll(125, TimeUnit.MILLISECONDS)) != null)
                    entry.getKey().print(entry.getValue());
        } catch (InterruptedException iex) {
            System.err.println("Logger was interrupted.");
        } finally {
            //output of stopwatches
            if (cp2track.size() != 0) {
                stopwatch.print("Class.method(~)\t:line\t#\thits\tm\t~\tM\tdescription\n");
                for (Track track : cp2track.values()) stopwatch.print(track.getRows());
                stopwatch.close();
            }

            //finalizing all files
                for (Logger logger : mem.values()){
                    while ((entry = msgQueue.poll()) != null)
                        entry.getKey().print(entry.getValue());
                    logger.printer.close();
                }

            System.out.print("\u001B[32;1mLogger was shutdown successfully.\u001B[0m\n");
        }
    }

    //Further is for testing only
    private void ga(){
        Logger.init();
        Object obj = new Object();
        Logger log = new Logger(obj, Level.all, Level.all);
        Logger.cp();
        for (int i = 0; i < 4000; i++) {
            Logger.init("cycle");
            log.debug("Some debug message...");
            log.error("simple error sample");
            log.warning("Warning expected to be the same.");
            log.info("INFO, AHAHAHAHA. =/");
            Logger.cp("cycle", "before exception");
            log.exception(new Exception("your advertisement here"));
            Logger.cpl("cycle");
            log = new Logger(obj);
            log.report("smth");
            Logger.cp("cycle", "cycle ended");
        }
        Logger.cpd("after cycle, ending of the ga()");
    }
    public static void main(String args[]){
        System.err.println("main started");
        Logger.init();
        Logger.cp();
        Logger.cp();
        Logger.cp();

        Logger log = new Logger(0);
        Thread thread = new Thread(log);
        thread.start();

        Logger.cpl("bef ga");
        log.ga();
        Logger.cpl("afterga");

        thread.interrupt();
        System.err.println("main finished");
    }
}
