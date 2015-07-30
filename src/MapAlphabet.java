import Common.ElementType;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation based on Map, supposed to be used for
 * testing purposes only... But who knows=)...
 * Created by listvin on 7/27/15.
 */
public class MapAlphabet implements Alphabet{
    private Map<String, ElementType> map = new HashMap<>();
    private String crutch(URL url, String xpath){
        return url.toString() + "|" + xpath;
    }

    public ElementType request(URL url, String xpath) {
        ElementType result = map.get(crutch(url, xpath));
        return result == null ? ElementType.unknown : result;
    }

    public void add(URL url, String xpath, ElementType eltype) throws ConflictingHandleStored {
        String id = crutch(url, xpath);
        ElementType temp = map.get(id);
        if (temp == null) map.put(id, eltype);
        else if (temp != eltype) throw new ConflictingHandleStored();
    }

    @Override
    public void close() {
        //TODO add storage in simple csv
    }
}
