package Common;

/**
 * Created by user on 7/23/15.
 */
public class Edge {
    public final Event destination;
    private boolean explored;

    public Edge(Event destination) {
        this.destination = destination;
        explored = false;
    }

    //@return Returns true if edge was marked as explored (dfs has investigated it), else - false.
    public boolean isExplored(){
        return explored;
    }
    //This marks edge as explored. Supposed to be invoked when dfs takes decision to investigate in this direction
    public void setExplored(){
        explored = true;
    }
}
