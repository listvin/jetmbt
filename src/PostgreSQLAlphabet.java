
import Boxes.State;
import Common.ElementType;
import Boxes.WebHandle;
import Common.Logger;
import com.sun.istack.internal.Nullable;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
/**
 * Class for storing ElementTypes of corresponding WebElements in PostqreSQL db
 * Created by wimag on 7/29/15.
 */
public class PostgreSQLAlphabet implements Alphabet {
    private Logger log = new Logger(this, Logger.Level.debug, Logger.Level.all);
    private static String CONFIG = "DB.properties";
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
        } catch (Exception e) {
            log.exception(e);
            log.error("Invoking System.exit(1) now...");
            System.exit(-1);
        }
        log.report("Opened database successfully.");
    }

    public void add(URL url, String xpath, ElementType eltype) {
        try {
            Statement stmt = c.createStatement();
            //SQL

            ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url.toString() +
                    "' AND xpath = '" + xpath + "'");
            if (!rs.isBeforeFirst()) {
                stmt.executeUpdate("INSERT INTO handles (url, xpath, eltype) " +
                        "VALUES ('" + url.toString() + "','" + xpath + "','" + eltype.name() + "')");
            } else {
                Logger.get(this).info("UPDATE handles SET url='" + url.toString() + "',xpath = '" + xpath + "', eltype='" + eltype.name() + "'" +
                        "WHERE url = '" + url.toString() + "' AND xpath = '" + xpath + "'");
                stmt.executeUpdate("UPDATE handles SET url='" + url.toString() + "',xpath = '" + xpath + "', eltype='" + eltype.name() + "'" +
                        "WHERE url = '" + url.toString() + "' AND xpath = '" + xpath + "'");
            }
            stmt.close();
        } catch (SQLException e){
            log.exception(e);
        }
    }

//    public void add(URL url, String xpath, ElementType eltype) throws SQLException, ConflictingHandleStored {
//        Statement stmt = c.createStatement();
//        //SQL
//
//        ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url +
//                "' AND xpath = '" + xpath + "'");
//        if(!rs.isBeforeFirst()){
//            stmt.executeUpdate("INSERT INTO handles (url, xpath, eltype) " +
//                    "VALUES ('" + url + "','" + xpath + "','" + eltype.name() + "')");
//        }else{
//            rs.next();
//            if(!rs.getString("eltype").equals(eltype.name())){
//                throw new ConflictingHandleStored();
//            }
//        }
//        stmt.close();
//
//    }

    /**
     * abort connection with DB
     */
    public void close(){
        try {
            c.close();
            Logger.get(this).report("Closed DB successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param url - url to be found in database
     * @param xpath - xpath to be found in database
     * @return - element type of this handle. unknown if never encountered
     * @throws SQLException
     */
    @Override
    public ElementType request(URL url, String xpath){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url +
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
     * get all hashes associated with url
     * @param url
     * @return
     * @throws SQLException
     */
    public List<String> getHashesByURL(URL url){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM urls WHERE url = '" + url.toString() + "'");
            List<String> hashes = new ArrayList<String>();
            while (rs.next()) {
                hashes.add(rs.getString("hash"));
            }
            stmt.close();
            return hashes;
        } catch (SQLException e){
            log.exception(e);
        }
        return new ArrayList<>();
    }

    /**
     * Adds URL/Hash pair to the DB.
     * @param url
     * @param hash - value "MyLittleDefaultHash" is used to marke unparseg pages
     * @throws SQLException
     */
    public void addURL(URL url, String hash){
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from urls WHERE url = '" + url.toString() +
                    "' AND hash = '" + hash + "'");
            if (!rs.isBeforeFirst()) {
                stmt.executeUpdate("INSERT INTO urls (url, hash) " +
                        "VALUES ('" + url.toString() + "','" + hash + "')");
            }
            stmt.close();
        } catch (SQLException e){
            log.exception(e);
        }
    }

    @Nullable
    public URL getRandomURL() {
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
            Random random = new Random();
            return new URL(urls.get(random.nextInt(urls.size())));
        } catch (SQLException | MalformedURLException e){
            log.exception(e);
        }
        return null;
    }
    //for testing purposes only
    public static void main(String args[]) throws MalformedURLException, SQLException {
        PostgreSQLAlphabet alphabet = new PostgreSQLAlphabet();
        WebHandle handle = new WebHandle(new URL("http://vk.com"), "//html[1]", ElementType.clickable);
        alphabet.add(handle);
        alphabet.close();
    }
}
