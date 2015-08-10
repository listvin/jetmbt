import Boxes.EFG;
import Boxes.Event;
import Boxes.Sequence;
import Boxes.State;
import static Common.Utils.sleep;

import java.net.URL;
import java.util.concurrent.*;

/**
 * Created by user on 8/6/15.
 */
public class ParallelRunner {
    static EFG g = new EFG();

    public static void main(String [] args){
        assert args.length == 1 : "one argument expected - URL, to start building from";

        BlockingQueue<URL> URLQueue = new ArrayBlockingQueue<URL>(1000);
        URLHasher hasher = new URLHasher(URLQueue);
        new Thread(hasher).start();

        //scanner = new Scanner();

        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(3);

        //#TODO adequate exit condition
        while(executor.getQueue().size() > 0){
            if(executor.getQueue().size() < 5){

            }
            sleep(1000);
        }
        executor.shutdown();
        g.dump2dot();

    }
}
