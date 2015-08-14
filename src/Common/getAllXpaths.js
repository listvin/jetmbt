/**
 * This code is loaded to String in class Selector at construction.
 * Supposed to be executed in browser to collect Xpathes of all visible elements.
 * Created by wimag on 8/7/15.
 */

getElementXPath = function(element){
    var paths = [];

    // Use nodeName (instead of localName) so namespace prefix is included (if any).
    for (element; element && element.nodeType == 1; element = element.parentNode)
    {
        if (element.id){
            paths.unshift('/*[@id="' + element.id + '"]');
            break;
        }

        var index = 0;
        for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling)
        {
            // Ignore document type declaration.
            if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
                continue;

            if (sibling.nodeName == element.nodeName)
                ++index;
        }

        var tagName = element.nodeName.toLowerCase();
        var pathIndex = (index ? "[" + (index+1) + "]" : "");
        paths.unshift(tagName + pathIndex);
    }

    return paths.length ? "/" + paths.join("/") : null;
};

var nodes = [];

try {
    var result = document.evaluate("//*", document, null, XPathResult.ANY_TYPE, null);
    var item;
    while ((item = result.iterateNext()) != null)
        if(getComputedStyle( item ).visibility === 'visible'){
            nodes.push(getElementXPath(item));
        }

} catch (exc) {
    // Invalid xpath expressions make their way here sometimes.  If that happens,
    // we still want to return an empty set without an exception.
}

return nodes;