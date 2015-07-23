package Common;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 7/23/15.
 */
public class State {
    final URL url;
    List<Event> sequence ;

    State(URL url, List<Event> sequence){
        this.url = url;
        this.sequence = new ArrayList<Event>(sequence);
    }
}
