package Boxes;

/** Holder for dfs's parameters (use it for construction Builder
 * class and sending tasks back to Invoker for other Builders)
 * Created by listvin on 8/18/15.
 */
public class BuilderRequest {
    public Event prev;
    public State start;
    public int depth;

    public BuilderRequest(Event prev, State start, int depth) {
        this.prev = prev;
        this.start = start;
        this.depth = depth;
    }
}
