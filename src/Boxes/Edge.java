package Boxes;

/**
 * Created by user on 7/29/15.
 */
class Edge {
    public final Event destination;
    /**
     * @param destination This is target node.
     */
    Edge(Event destination) {
        this.destination = destination;
    }

    private static int tickValue = 1;
    private int tick0 = 0, tick1 = 0;
    /** This invalidates all tick-marks on all edges.*/
    public void invalidateTicks(){ ++tickValue; }
    void setTicked() {
        if (isTicked())
            tick1 = tickValue;
        else
            tick0 = tickValue;
    }
    boolean isTicked() {
        return (tick0 == tickValue);
    }
    boolean isTickedTwice() {
        return (isTicked() && tick1 == tickValue);
    }
    int getTicksCount() { return (isTicked() ? (isTickedTwice() ? 2 : 1) : 0); }
}
