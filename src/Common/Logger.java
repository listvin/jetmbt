package Common;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Logger implements Closeable, Runnable{
    public enum Level {all, info/*blue*/, debug/*yell*/, warning/*yell*/, error/*red*/, report/*green*/, off}

    public static final String folderName;
    private static final String path;
    private static HashMap<Object, Logger> mem = new HashMap<>();
    private static BlockingQueue<Map.Entry<PrintStream, String>> msgQueue = new LinkedBlockingQueue<>();
    private static PrintStream stopwatch;
    static {
        folderName = (new SimpleDateFormat("ddMMMyyyy_EEE_HH%1mm%2ss.SSS%3"))
                .format(new Date()).toLowerCase()
                .replace("%1", "h").replace("%2", "m").replace("%3", "s");
        path = "log/" + folderName + "/";
        String[] args = {"mkdir", path};
        try {
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
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

    private Level file, console;
    private PrintStream printer;
    private String name;

    private static String getJavaDefaultName(Object obj){
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }

    public Logger(Object owner, Level console, Level file){
        this.file = file;
        this.console = console;
        name = getJavaDefaultName(owner);
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
        
    private SimpleDateFormat hhmmsssss = new SimpleDateFormat("[HH:mm:ss.SSS]: ");
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

    /**Should be used for real time investigations
     * Use carefully, only for develop, cause itself it's rather slow*/
//    private static HashMap <String, TreeMap<String, Integer>> met2cp2id = new HashMap<>();
//    public static void checkpoint(String filter, String description){
//        StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
//        String codePoint = ste.getFileName() + ":" + ste.getLineNumber();
//
//        if (!cp2id.containsKey(codePoint)) cp2id.put(codePoint, cp2id.size());
//        int id = cp2id.get(codePoint);
//    }

    public void close(){
        printer.close();
    }

    public void run() {
        Map.Entry<PrintStream, String> entry;
        try {
            while (!Thread.interrupted()) {
                if ((entry = msgQueue.poll(125, TimeUnit.MILLISECONDS)) != null) {
                    entry.getKey().print(entry.getValue());
                }
            }
        } catch (InterruptedException iex) {
            System.err.println("Logger was interrupted.");
        }
        System.err.print("Logger shut down");
    }

    //for testing only
    public static void main(String args[]){
        Integer i = 0;
        Logger log = new Logger(i, Level.all);
        log.debug("Some debug message...");
        log.error("simple error sample");
        log.warning("Warning expected to be the same.");
        log.info("INFO, AHAHAHAHA. =/");
        log.exception(new Exception("your advertisement here"));
        log = new Logger(i);
        log.report("smth");
    }
}
