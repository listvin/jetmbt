package Common;

/**
 * Created by user on 8/11/15.
 */
public class Settings {
    public static final int randomSeed = 239;
//    public static final int randomSeed = Integer.valueOf(new SimpleDateFormat("HHmmssSSS").format(new Date()));
    static { Logger.get(0).report(Integer.toString(randomSeed)); }

    public static final int maximumSingleExplicitWait = 3;
    public static final int maximumExplicitWaitInARow = 10;
    public static final int initialReplayFailThreshold = 50;
    public static final int urlHasherSleep = 3000;
    public static final int buildersCount = 15;
    public static final int depthLimit = 10;
    public static final int dumpsLimit = 10; //#hardcode
    public static final boolean dumpLimitEnabled = false;
    public static final String Own404 = "https://github.com/404";

}
