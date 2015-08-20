import Boxes.BuilderRequest;
import Boxes.Event;
import Boxes.State;
import Common.Logger;
import Common.Settings;

/**
 * Separate class for building EFG.
 * Created by listvin on 7/23/15.
 */
public class Builder implements Runnable{
    private BuilderRequest request;
    private Scanner scanner;
    public Builder(BuilderRequest request, Scanner scanner){

        this.request = request;
        this.scanner = scanner;
    }

    public void run(){
        dfs(request.prev, request.start, request.depth);
    }


    //further is private
    private Logger log = new Logger(this, Logger.Level.all, Logger.Level.all);
    private static int dumps = 0;
    /**
     * @param prev - this called "prev" because in browser this event was already performed. For usual dfs this called "cur"
     * @param cur - State characterises shortest way to the state, corresponding to current dfs invocation
     * @param depth - length of the route made by dfs (not one stored in cur.sequence, but real one which is greater or equal)
     */
    private void dfs(Event prev, State cur, int depth) {
        Logger.init();
        prev.setTicked(); //for now (for building) let's make it classic, with touring by nodes instead of edges.
        log.info("dfs at depth " + depth + "\nEvent prev = " + prev + "\n State cur:\n" + cur);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; ++i) sb.append(String.format("_%2d_", i));
        log.debug(String.format("DFS: %s (%s | %s)\n", sb.toString(), prev.handle.url.graphUrl(), prev.handle.xpath));

        boolean needDump = true;
        if (Invoker.g.isScannedOnce(prev)) {
            needDump = false;
            if (prev.handle.isAssignedToUrl())
                cur = new State(prev.handle.url).createAppended(prev);
        } else {
            Invoker.g.addEdges(prev, Event.generateTestEvents(scanner.scan(cur)));
            Logger.cpd("scan performed");
        }

        if (needDump) {
            Invoker.g.dump2dot();
            if (Settings.dumpLimitEnabled && ++dumps >= Settings.dumpsLimit) { //#hardcode #WILD
                Invoker.shutdown = true;
            }
        } else log.report("Skipping dump, that seems to be equal to previous.");

        if (depth < Settings.depthLimit){
            for (Event next = Invoker.g.pickEventToGoFrom(prev); next != null; next = Invoker.g.pickEventToGoFrom(prev)){
                State nextState = cur.createAppended(next);
                if (!Invoker.ifExpecting(next, nextState, depth + 1)){
                    log.info("invoking same dfs");
                    dfs(next, nextState, depth + 1);
                } else {
                    log.info("asking for another builder");
                }
            }
        }
    }
}
