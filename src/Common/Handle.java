package Common;

import java.net.URL;

/**
 * Created by user on 7/23/15.
 *
 */
public class Handle {
    public final URL url;

    public final String XPATH;

    public int elType;

    public Handle(URL iurl, String xpath, int eltype) {
        url = iurl;
        XPATH = xpath;
        elType = eltype;
    }

    //TODO: shortestparent + depth
}
