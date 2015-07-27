import Common.*;

import java.net.URL;

/**
 * Interface for database able to cache tested web elements
 * on clickableness, writableness, etc...
 * Created by listvin on 7/27/15.
 */
public interface Alphabet {
    class HandleTypePredefinedException extends Exception{
        public HandleTypePredefinedException(){ super(); }
    }

    class ConflictingHandleStored extends Exception{
        public ConflictingHandleStored(){ super(); }
    }

    /**
     * Method to look whether type of element specified by is
     * url and xpath is already known.
     * @param handle WebHandle containing URL and XPath to search
     *               for. eltype field MUST be set to unknown.
     * @return New WebHandle, based on old one with modified field
     * eltype if it is known. Otherwise Old WebHandle returned.
     * @throws HandleTypePredefinedException in case eltype field is already
     * set to smth not equal to unknown.
     */
    default WebHandle request(WebHandle handle) throws HandleTypePredefinedException {
        if (handle.eltype != ElementType.unknown) throw new HandleTypePredefinedException();
        ElementType eltype = request(handle.url, handle.xpath);
        if (eltype != ElementType.unknown)
            return new WebHandle(handle.url, handle.xpath, eltype);
        else
            return handle;
    }

    /**
     * Purpose is the same as {@link #request(WebHandle)}, but with lower-level arguments.
     * @param url - URL to search in database for
     * @param xpath - Xpath to search for
     * @return ElementType.unknown if not cached, otherwise - cached value.
     */
    ElementType request(URL url, String xpath);

    /**
     * Method for caching new testing results
     * @param handle - eltype field should be properly set
     */
    default void add(WebHandle handle) throws ConflictingHandleStored{
        add(handle.url, handle.xpath, handle.eltype);
    }

    /**
     * Lower method for caching new testing results {@link #add(WebHandle)}
     * @param url - URL is not verified
     * @param xpath - Xpath also not verified
     * @param eltype - value to cache.
     */
    void add(URL url, String xpath, ElementType eltype) throws ConflictingHandleStored;
}
