package Boxes;

import Common.Logger;
import Common.Settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Created by user on 8/19/15.
 */
public class JetURL {
    private static Logger log = new Logger(new JetURL(), Logger.Level.all, Logger.Level.all);

    private URL url;
    public JetURL(String str_url){
        url = createURL(str_url);
    }
    public JetURL(URL url){
        this.url = url;
    }
    public JetURL(){
        url = createOwn404().url;
    }

    private static URL createURL(String url_str){
        try {
            return new URL(url_str);
        } catch (MalformedURLException e){
            log.error("url_str = " + url_str);
            log.exception(e);
            return null;
        }
    }

    /**Factory for JetURL with suppressed MalformedURLException (in most cases in this project, we are sure that JetURL isn't malformed)*/
    public static JetURL createJetURL(String url){
        try {
            return new JetURL(new URL(url));
        } catch (MalformedURLException e){
            log.exception(e);
            return null;
        }
    }

    public static JetURL createOwn404(){
        return new JetURL(Settings.Own404);
    }

    /**
     * Extracts from given JetURL: protocol + authority part + path,
     *     /**
     * Extract from given JetURL protocol + authority part + path,
     * according to https://en.wikipedia.org/wiki/URI_scheme#Generic_syntax
     */
    public String shrunk(){
        if (url == null) return "";
        return url.getProtocol() + "://" + url.getAuthority() + url.getPath();
    }

    /**
     * Extracts from given JetURL: protocol + hierarchical part + reference (if it has one),
     * //according https://en.wikipedia.org/wiki/URI_scheme#Generic_syntax
     */
    public String graphUrl(){
        if (url == null) return "";
        if (url.getRef() == null || url.getRef().length() == 0)
            return shrunk();
        else
            return shrunk() + "#" + url.getRef();
    }

    public String getHost(){
        if (url == null) return "";
        return url.getHost();
    }

    @Override
    public String toString(){
        log.exception(new Exception("THIS SHIT IS FORBIDDEN FOR NOW"));
        System.exit(-1);
        return null;
    }

    public String toFullString(){
        return url.toString();
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof JetURL && graphUrl().equals(((JetURL) obj).graphUrl());
    }

    public static boolean compare(JetURL a, JetURL b){
        return a.equals(b);
    }

    public static boolean compare(JetURL a, String b){
        return new JetURL(b).equals(a);
    }

    public static boolean weaklyCompare(JetURL a, JetURL b){
        return a.shrunk().equals(b.shrunk());
    }

    public static boolean weaklyCompare(JetURL a, String b){
        return weaklyCompare(new JetURL(b), a);
    }
}
