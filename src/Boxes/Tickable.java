package Boxes;

/**
 * Abstract class which can for anything that could be marked with
 * a yes/no flag. Can store up to two flags(ticks) in the moment.
 * {@link Tickable#invalidateTicks()} is a key method. It removes all
 * ticks immediately.
 * Created by listvin on 30jul2015
 */
public abstract class Tickable{
    private static int tickValue = 1;
    private int tick0 = 0, tick1 = 0;
    
    /**
     * This invalidates all tick-marks on all edges.
     * Expected to be implemented by increasing inner
     * counter of an integer flag, showing the
     * instance is ticked. So, this is O(1) */
    public static void invalidateTicks(){ ++tickValue; }
    
    /**
     * First call sets first tick, the second -
     * second. Only two ticks supported at the moment. */
    public void setTicked() {
        if (isTicked()) tick1 = tickValue;
        else tick0 = tickValue;
    }
    
    /** Says if it is ticked at least once */
    public boolean isTicked() { return (tick0 == tickValue); }
    
    /** Says if it is ticked twice */
    public boolean isTickedTwice() { return (tick1 == tickValue); }
    
    /** 
     * @return 0 if not ticked, 1 if ticked once, 2 - otherwise */
    public int getTicksCount() { return (isTicked() ? (isTickedTwice() ? 2 : 1) : 0); }
}