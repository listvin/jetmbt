package Common;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 7/23/15.
 */
public class Selectors {

    public static String formXPATH(WebDriver driver, WebElement element){
        return (String) ((JavascriptExecutor) driver).executeScript("function absoluteXPath(element) {"+
                        "var comp, comps = [];"+
                        "var parent = null;"+
                        "var xpath = '';"+
                        "var getPos = function(element) {"+
                        "var position = 1, curNode;"+
                        "if (element.nodeType == Node.ATTRIBUTE_NODE) {"+
                        "return null;"+
                        "}"+
                        "for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"+
                        "if (curNode.nodeName == element.nodeName) {"+
                        "++position;"+
                        "}"+
                        "}"+
                        "return position;"+
                        "};"+

                        "if (element instanceof Document) {"+
                        "return '/';"+
                        "}"+

                        "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"+
                        "comp = comps[comps.length] = {};"+
                        "switch (element.nodeType) {"+
                        "case Node.TEXT_NODE:"+
                        "comp.name = 'text()';"+
                        "break;"+
                        "case Node.ATTRIBUTE_NODE:"+
                        "comp.name = '@' + element.nodeName;"+
                        "break;"+
                        "case Node.PROCESSING_INSTRUCTION_NODE:"+
                        "comp.name = 'processing-instruction()';"+
                        "break;"+
                        "case Node.COMMENT_NODE:"+
                        "comp.name = 'comment()';"+
                        "break;"+
                        "case Node.ELEMENT_NODE:"+
                        "comp.name = element.nodeName;"+
                        "break;"+
                        "}"+
                        "comp.position = getPos(element);"+
                        "}"+

                        "for (var i = comps.length - 1; i >= 0; i--) {"+
                        "comp = comps[i];"+
                        "xpath += '/' + comp.name.toLowerCase();"+
                        "if (comp.position !== null) {"+
                        "xpath += '[' + comp.position + ']';"+
                        "}"+
                        "}"+

                        "return xpath;"+

                        "} return absoluteXPath(arguments[0]);", element);

    }

    public static List<String> getAllXPATHs(WebDriver driver){
        Object response = (((JavascriptExecutor) driver).executeScript("/**\n" +
                        " * Created by user on 8/7/15.\n" +
                        " */\n" +
                        "\n" +
                        "getElementTreeXPath = function(element)\n" +
                        "{\n" +
                        "    var paths = [];\n" +
                        "\n" +
                        "    // Use nodeName (instead of localName) so namespace prefix is included (if any).\n" +
                        "    for (; element && element.nodeType == 1; element = element.parentNode)\n" +
                        "    {\n" +
                        "        var index = 0;\n" +
                        "        for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling)\n" +
                        "        {\n" +
                        "            // Ignore document type declaration.\n" +
                        "            if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)\n" +
                        "                continue;\n" +
                        "\n" +
                        "            if (sibling.nodeName == element.nodeName)\n" +
                        "                ++index;\n" +
                        "        }\n" +
                        "\n" +
                        "        var tagName = element.nodeName.toLowerCase();\n" +
                        "        var pathIndex = (index ? \"[\" + (index+1) + \"]\" : \"\");\n" +
                        "        paths.splice(0, 0, tagName + pathIndex);\n" +
                        "    }\n" +
                        "\n" +
                        "    return paths.length ? \"/\" + paths.join(\"/\") : null;\n" +
                        "};\n" +
                        "\n" +
                        "getElementXPath = function(element)\n" +
                        "{\n" +
                        "    if (element && element.id)\n" +
                        "        return '//*[@id=\"' + element.id + '\"]';\n" +
                        "    else\n" +
                        "        return this.getElementTreeXPath(element);\n" +
                        "};\n" +
                        "\n" +
                        "\n" +
                        "var nodes = [];\n" +
                        "\n" +
                        "try {\n" +
                        "    var result = document.evaluate(\"//*\", document, null, XPathResult.ANY_TYPE, null);\n" +
                        "    var item;\n" +
                        "    while ((item = result.iterateNext()) != null)\n" +
                        "        nodes.push(getElementXPath(item));\n" +
                        "}\n" +
                        "catch (exc)\n" +
                        "{\n" +
                        "    // Invalid xpath expressions make their way here sometimes.  If that happens,\n" +
                        "    // we still want to return an empty set without an exception.\n" +
                        "}\n" +
                        "    return nodes;\n" +
                        "\n"
        ));
        return (ArrayList<String>) response;
    }
}
