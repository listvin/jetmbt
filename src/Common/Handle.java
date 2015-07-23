package Common;

/**
 * Created by user on 7/23/15.
 *
 */
public class Handle {
    public final String URL;

    public final String XPATH;

    public int elType;

    public Handle(String url, String xpath, int eltype) {
        URL = url;
        XPATH = xpath;
        elType = eltype;
    }
}
