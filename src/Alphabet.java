import Boxes.JetURL;
import Boxes.WebHandle;
import Common.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for database able to cache tested web elements
 * on clickableness, writableness, etc...
 * Created by listvin on 7/27/15.
 */
public interface Alphabet {

    /**
     * Method to look whether type of element specified by is
     * url and xpath is already known.
     * @param handle WebHandle containing JetURL and XPath to search
     *               for. eltype field MUST be set to unknown.
     * @return New WebHandle, based on old one with modified field
     * eltype if it is known. Otherwise Old WebHandle returned.
     */
    default WebHandle request(WebHandle handle) {
        assert handle.eltype != ElementType.unknown : "handle type predefined";
        ElementType eltype = request(handle.url, handle.xpath);
        if (eltype != ElementType.unknown)
            return new WebHandle(handle.url, handle.xpath, eltype);
        else
            return handle;
    }

    /**
     * Purpose is the same as {@link #request(WebHandle)}, but with lower-level arguments.
     * @param url - JetURL to search in database for
     * @param xpath - Xpath to search for
     * @return ElementType.unknown if not cached, otherwise - cached value.
     */
    ElementType request(JetURL url, String xpath);

    /**
     * Method for caching new testing results
     * @param handle - eltype field should be properly set
     */
    default void add(WebHandle handle) {
        add(handle.url, handle.xpath, handle.eltype);
    }

    /**
     * Lower method for caching new testing results {@link #add(WebHandle)}
     * Changes element type if similar element present in DB. Otherwise - creates it.
     * @param url - JetURL is not verified
     * @param xpath - Xpath also not verified
     * @param eltype - value to cache.
     */
    void add(JetURL url, String xpath, ElementType eltype);

    void close();


    /**
     * Adds JetURL with specified hash, if entry already exists - replaces with new data
     * @param url
     * @param hash
     * @throws SQLException
     */
    void addURL(JetURL url, String hash);

    /**
     * Returns all hashes of url
     * @param url
     * @return - Arraylist of all hashes
     * @throws SQLException
     */
    List<String> getHashesByURL(JetURL url);

    /**
     * Get random JetURL from encountered.
     * @return
     * @throws SQLException
     */
    JetURL getRandomURL();
}
