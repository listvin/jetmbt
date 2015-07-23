package Common;

import java.net.URL;

/**
 * Created by user on 7/23/15.
 *
 */
public class Handle {
    public final URL url;

    public final String xpath;

    public int eltype;

    public Handle(URL url, String xpath, int eltype) {
        this.url = url;
        this.xpath = xpath;
        this.eltype = eltype;
    }

    //TODO: shortestparent + depth
}
