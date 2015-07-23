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

    public boolean isExplored(){
        return explored;
    }

    public void setExplored(boolean f){
        explored = f;
    }
}
