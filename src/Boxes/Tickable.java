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
    private static int ticks = 0;
    private int tick0 = 0, tick1 = 0;

    /**
     * This invalidates all tick-marks on all edges.
     * Expected to be implemented by increasing inner
     * counter of an integer flag, showing the
     * instance is ticked. So, this is O(1) */
    public static void invalidateTicks(){ ticks = 0; ++tickValue; }

    /**
     * @return how much times {@link #setTicked()}
     * was invoked since last invalidation
     */
    public static int getGlobalTicksCount(){ return ticks; }
    
    /**
     * First call sets first tick, the second -
     * second. Only two ticks supported at the moment. */
    public void setTicked() {
        ++ticks;
        if (isTicked()) tick1 = tickValue;
        else tick0 = tickValue;
    }
    
    /** Says if it is ticked at least once */
    public boolean isTicked() { return (tick0 == tickValue); }
    
    /** Says if it is ticked twice */
    public boolean isTickedTwice() { return (tick1 == tickValue); }
    
    /** 
     * @return 0 if not ticked, 1 if ticked once, 2 - otherwise */
    public int getTimesTicked() { return (isTicked() ? (isTickedTwice() ? 2 : 1) : 0); }
}