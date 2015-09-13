import Boxes.*;
import Common.Logger;
import Common.Settings;

import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Common.Utils.sleep;

/**
 * Created by listvin on 8/12/15.
 */

public class Invoker {
    //public for access from different threads
    public static Alphabet alphabet = new PostgreSQLAlphabet();
    public static final URLHasher urlHasher = new URLHasher();
    public static EFG g;

    //private resources
    private static Logger log = new Logger(new Invoker(), Logger.Level.all, Logger.Level.all);

    //service-thread control
    private static Thread logThread = new Thread(log), urlThread = new Thread(urlHasher);
    static {
        urlThread.start();
        logThread.start(); logThread.setPriority(Thread.MIN_PRIORITY);
    }

    //builders management
    private static class WrappedBuilder{
        public static int aliveCount = 0;
        private Scanner scanner = new Scanner();
        private Builder builder;
        private Thread thread;
        /**
         * @return true if inner builder have nothing to do*/
        public boolean stillNothingToDo(){ Logger.init();
            Logger.cpd("only started");
            if (thread != null && thread.isAlive()) return false;
            Logger.cpd("passed aliveness check");
            BuilderRequest br = requests.poll();
            Logger.cpd("polled request");
            if (br == null) return true;
            Logger.cpd("poll successful");
            (thread = new Thread(builder = new Builder(br, scanner))).start();
            Logger.cpd("started thread");
            if (isAlive()){
                ++aliveCount;
                Logger.cpd("and has it really started");
                return false;
            } else {
                Logger.cpd("but it has given a shit");
                return true;
            }
        }
        boolean isAlive(){
            return thread.isAlive();
        }
        void close(){
            if (isAlive()) thread.interrupt();
            try { thread.join(); }
            catch (InterruptedException e) {
                log.error("interrupted while closing builder");
            }
            scanner.close();
        }
    }
    public static boolean shutdown = false;
    private static int expectingRequest = 0;
    /**@return true and uses parameters if there is available slots for Builders, false otherwise*/
    public static boolean ifExpecting(Event prev, State start, int depth){
        if (expectingRequest > 0){
            --expectingRequest;
            requests.add(new BuilderRequest(prev, start, depth));
            return true;
        } else return false;
    }
    private static BlockingQueue<BuilderRequest> requests = new LinkedBlockingQueue<>();
    private static WrappedBuilder[] builders = new WrappedBuilder[Settings.buildersCount];
    static { for (int i = 0; i < builders.length; ++i) builders[i] = new WrappedBuilder(); }

    /**@param args Use --url url_to_parse or --file file_to_load_graph_from.*/
    public static void main(String [] args) throws MalformedURLException {

        State startState;
        //TODO embed commons CLI here
        switch (args[0]){
            case "--url":
                g = new EFG();
                startState = new State(new JetURL(args[1]), new Sequence());
                break;
            case "--file":
                g = new EFG(args[1]);
                //TODO we need search of start point to continue building graph
                startState = null; //lol, null here seems to be really dangerous guy!)
                break;
            default:
                System.err.print("Use --url <url to parse> or --file <file to load graph from>");
                return;
        }
        requests.add(new BuilderRequest(Event.createTerminal("BUILDINGROOT"), startState, 0)); //#hardcode


        while (!Thread.interrupted()){
            for (WrappedBuilder wBuilder : builders) {
                int temp = 0;
                if (wBuilder.stillNothingToDo())
                    ++temp;
                expectingRequest = temp;
            }
            if (shutdown || Event.getGlobalTicksCount() == g.getNodesCount()) break;
            sleep(300);
        }

        for (WrappedBuilder wBuilder : builders)
            wBuilder.close();

        g.dump2dot();
        log.report("Invoker is finishing...");

        logThread.interrupt();
        urlThread.interrupt();
    }
}

