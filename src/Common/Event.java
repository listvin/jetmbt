package Common;

/**
 * Created by user on 7/23/15.
 */
public class Event {
    public final Handle handle;
    public final String context;

    public Event(Handle handle, String context) {
        this.handle = handle;
        this.context = context;
    }
}
