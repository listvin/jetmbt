package Common;

import java.net.URL;

/**
 * Created by user on 7/23/15.
 *
 */
public class Handle {
    public final URL url;

    public final String xpath;

    public ElementType eltype;

    public Handle(URL url, String xpath, ElementType eltype) {
        this.url = url;
        this.xpath = xpath;
        this.eltype = eltype;
    }

    public Handle(URL url, String xpath) {
        this.url = url;
        this.xpath = xpath;
        this.eltype = ElementType.unknown;
    }



    //TODO: shortestparent + depth
}
