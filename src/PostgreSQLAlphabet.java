import Boxes.JetURL;
import Common.ElementType;
import Boxes.WebHandle;
import Common.Logger;
import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;
import java.util.List;
/**
 * Class for storing ElementTypes of corresponding WebElements in PostqreSQL db
 * Created by wimag on 7/29/15.
 */
public class PostgreSQLAlphabet implements Alphabet {
    private Logger log = new Logger(this, Logger.Level.debug, Logger.Level.all);
    private Connection c;

    public PostgreSQLAlphabet(){
        Properties defaultProps = new Properties();
        String DBNAME = null;
        String DBUSER = null;
        String DBPASS = null;
        try {
            defaultProps.load(new FileInputStream(new File("DB.properties")));
            DBNAME = defaultProps.getProperty("alphabetDB");
            DBUSER = defaultProps.getProperty("DBUser");
            DBPASS = defaultProps.getProperty("DBUserPassword");

        } catch (IOException e) {
            log.error("Couldn't fine properties settings file");
            log.exception(e);
        }

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/"+DBNAME,
                            DBUSER, DBPASS);
            log.report("Opened database successfully.");
        } catch (Exception e) {
            log.exception(e);
            log.error("System.exit(-1) here was commented by listvin. (Ask wimag for details)");
//            System.exit(-1);
        }
    }

    /**
     * Adds given triplet of url-xpath-eltype to db of elemtnts (aka AlphabetDB)
     */
    public void add(JetURL url, String xpath, ElementType eltype) {
        try {
            Statement stmt = c.createStatement();
            //SQL

            ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url.graphUrl() +
                    "' AND xpath = '" + xpath + "'");
            if (!rs.isBeforeFirst()) {
                stmt.executeUpdate("INSERT INTO handles (url, xpath, eltype) " +
                        "VALUES ('" + url.graphUrl() + "','" + xpath + "','" + eltype.name() + "')");
            } else {
                Logger.get(this).info("UPDATE handles SET url='" + url.graphUrl() + "',xpath = '" + xpath + "', eltype='" + eltype.name() + "'" +
                        "WHERE url = '" + url.graphUrl() + "' AND xpath = '" + xpath + "'");
                stmt.executeUpdate("UPDATE handles SET url='" + url.graphUrl() + "',xpath = '" + xpath + "', eltype='" + eltype.name() + "'" +
                        "WHERE url = '" + url.graphUrl() + "' AND xpath = '" + xpath + "'");
            }
            stmt.close();
        } catch (SQLException e){
            log.exception(e);
        }
    }

    /**
     * Closes connection with DB
     */
    public void close(){
        try {
            c.close();
            Logger.get(this).report("Closed DB successfully.");
        } catch (SQLException e) {
            Logger.get(this).report("Failed to close connection with db.");
            Logger.get(this).exception(e);
        }
    }


    /**
     * Performs search in db by url, xpath
     * @param url - url to search for database
     * @param xpath - xpath to search for in database
     * @return - ElementType of this handle. Unknown if not found in db.
     */
    @Override
    public ElementType request(JetURL url, String xpath){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url.graphUrl() +
                    "' AND xpath = '" + xpath + "'");
            while (rs.next()) {
                String type = rs.getString("eltype");
                stmt.close();
                return ElementType.valueOf(type);
            }
            stmt.close();
        } catch (SQLException e){
            log.exception(e);
        }
        return ElementType.unknown;
    }


    /**
     * Get all hashes associated with url
     * @param url
     * @return List of Strings, each String is hash of the page layout loaded by the url
     * @throws SQLException
     */
    public List<String> getHashesByURL(JetURL url){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM urls WHERE url = '" + url.graphUrl() + "'");
            List<String> hashes = new ArrayList<String>();
            while (rs.next()) hashes.add(rs.getString("hash"));
            stmt.close();
            return hashes;
        } catch (SQLException e){
            log.exception(e);
            return new ArrayList<>();
        }
    }

    /**
     * Adds JetURL/Hash pair to the DB.
     * @param url
     * @param hash - value "MyLittleDefaultHash" is used to marke unparseg pages
     * @throws SQLException
     */
    public void addURL(JetURL url, String hash){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from urls WHERE url = '" + url.graphUrl() +
                    "' AND hash = '" + hash + "'");
            if (!rs.isBeforeFirst()) {
                stmt.executeUpdate("INSERT INTO urls (url, hash) " +
                        "VALUES ('" + url.graphUrl() + "','" + hash + "')");
            }
            stmt.close();
        } catch (SQLException e){
            log.exception(e);
        }
    }

    @Nullable
    public JetURL getRandomURL() {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT url FROM urls");
            List<String> urls = new ArrayList<>();
            if (!rs.isBeforeFirst()) {
                return null;
            }
            while (rs.next()) {
                urls.add(rs.getString("url"));
            }
            Random random = new Random(Common.Settings.randomSeed);
            return new JetURL(urls.get(random.nextInt(urls.size())));
        } catch (SQLException e){
            log.exception(e);
        }
        return null;
    }

    @Override
    public void setURLAcessable(WebHandle handle) {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            stmt.executeUpdate("UPDATE handles SET URL_access=TRUE WHERE url = '" + handle.url.graphUrl() + "' AND xpath = '" + handle.xpath + "'");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isURLAcessable(WebHandle handle){
        return false;
    }

    //for testing purposes only
    public static void main(String args[]) throws MalformedURLException, SQLException {
        PostgreSQLAlphabet alphabet = new PostgreSQLAlphabet();
        WebHandle handle = new WebHandle(new JetURL("http://vk.com"), "//html[1]", ElementType.clickable);
        alphabet.add(handle);
        alphabet.close();
    }
}
