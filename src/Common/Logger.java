package Common;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static Common.Utils.sleep;

public class Logger implements Closeable{
    public enum Level {all, info/*blue*/, debug/*yell*/, warning/*yell*/, error/*red*/, report/*green*/, off};
    private static String folderName = "", path;
    private Level file, console;
    private PrintWriter writer;
    
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
        try {
            writer = new PrintWriter(path + owner.getClass().getSimpleName() + "-" + System.identityHashCode(owner) + ".log", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        report("Owner object created.");
    }
    public Logger(Object owner, Level console) { this(owner, console, Level.warning); }

    /**
     * Creates new Logger, assigned to instance with default verbosity levels:
     * - error for console ()
     * @param owner class which supposed to send logs about himself
     */
    public Logger(Object owner){ this(owner, Level.error, Level.warning); }
        
    private SimpleDateFormat hhmmsssss = new SimpleDateFormat("[HH:mm:ss.SSS]: ");

    private void printMessage(String color, String LVL, String text, Level threshold){
        String stamp = hhmmsssss.format(new Date());
        if (threshold.compareTo(console) >= 0) System.out.println("\u001B[" + color + ";1m" + LVL + stamp + "\u001B[0m" + text);
        if (threshold.compareTo(file) >= 0) writer.println(text);
    }
    public void info(String msg){ printMessage("34", "INF", msg, Level.info); }
    public void debug(String msg){ printMessage("33", "DBG", msg, Level.debug); }
    public void warning(String msg){ printMessage("33", "WRN", msg, Level.warning); }
    public void error(String msg){ printMessage("31", "ERR", msg, Level.error); }
    public void report(String msg){ printMessage("32", "REP", msg, Level.report); }

    public void close(){
        report("Owner object was destroyed.");
        writer.close();
    }

    public static void main(String args[]){
        Integer i = 0;
        Logger log = new Logger(i);
    }
}
