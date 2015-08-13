package Common;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static Common.Utils.sleep;

public class Logger implements Closeable{
    public enum Level {all, info/*blue*/, debug/*yell*/, warning/*yell*/, error/*red*/, report/*green*/, off}
    private static String folderName = "", path;
    private static HashMap<Object, Logger> mem = new HashMap<>();

    private Level file, console;
    private PrintStream printer;
    private String name;

    private static String getJavaDefaultName(Object obj){
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }

    private static boolean getFolderTimeNameLocked = false;
    public static String getFolderTimeName(){
        while (getFolderTimeNameLocked) sleep((long)100);
        if (folderName.length() == 0 && (getFolderTimeNameLocked = true)){
            folderName = (new SimpleDateFormat("ddMMMyyyy_EEE_HH%1mm%2ss.SSS%3"))
                .format(new Date()).toLowerCase()
                .replace("%1", "h").replace("%2", "m").replace("%3", "s");
            path = "log/" + folderName + "/";
            String[] args = {"mkdir", path};
            try {
                Runtime.getRuntime().exec(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getFolderTimeNameLocked = false;
        }
        return folderName;
    }

    public Logger(Object owner, Level console, Level file){
        this.file = file;
        this.console = console;
        path = "log/" + getFolderTimeName() + "/";
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
            System.out.println(
                "\u001B[" + color + ";1m" + LVL + stamp + "\u001B[0m" +
                "\u001B[" + color + "m" + "(by " + name + ") " + "\u001B[0m" +
                text +
                "\u001B[" + color + ";1m\n^^^^^^^^^^^^^^^^^^\u001B[0m");
        if (threshold.compareTo(file) >= 0)
            printer.println(
                LVL + stamp +
                text +
                "\n~~~~~~~~~~~~~~~~~~\n");
    }
    public void info(String msg){ printMessage("34", "INF", msg, Level.info); }
    public void debug(String msg){ printMessage("33", "DBG", msg, Level.debug); }
    public void warning(String msg){ printMessage("33", "WRN", msg, Level.warning); }
    public void error(String msg){ printMessage("31", "ERR", msg, Level.error); }
    public <T extends Exception> void exception(T e){ printMessage("31", "EXC", ExceptionUtils.getStackTrace(e), Level.error); }
    public void report(String msg){ printMessage("32", "REP", msg, Level.report); }

    public void close(){
        printer.close();
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
