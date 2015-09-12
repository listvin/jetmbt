package Common;

import Boxes.*;

public class ReadNdump{
    static EFG g;

    //private resources
    private static Logger log = new Logger(new ReadNdump(), Logger.Level.all, Logger.Level.all);

    //service-thread control
    private static Thread logThread = new Thread(log);
    static {
        logThread.start(); logThread.setPriority(Thread.MIN_PRIORITY);
    }

    public static void main(String [] args){

        String num;
        num = "3093";
//        num = "100";
//        g = new EFG("/home/user/Downloads/Telegram Desktop/dump#" + num + ".gv");
        g = new EFG("/home/user/Dropbox/jb/graphs_workshop/new/dump#"+num+".jgraph");
        GraphDumper.setCustomPath("/home/user/Dropbox/jb/graphs_workshop/testread/", "dump#"+num);
        g.dump2dot();
        log.report("read'n'dump is finishing...");

        logThread.interrupt();
    }
}