package Common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 8/11/15.
 */
public class Settings {
//    public static final int randomSeed = 239;
    public static final int randomSeed = Integer.valueOf(new SimpleDateFormat("HHmmssSSS").format(new Date()));
    public static final int maximumExplicitWait = 20;
    public static final int initialReplayFailThreshold = 50;
}
