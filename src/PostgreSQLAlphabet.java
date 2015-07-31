
import Boxes.State;
import Common.ElementType;
import Boxes.WebHandle;
import com.sun.istack.internal.Nullable;

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
            System.out.print("Couldn't fine properties settings file");
        }

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/"+DBNAME,
                            DBUSER, DBPASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    /**
     *
     * @param handles - add all elements from this list to alphabet DB
     */
    public void addHandles(List<WebHandle> handles) throws SQLException, ConflictingHandleStored {
        for(WebHandle handle: handles){
            add(handle);
        }
    }

    /**
     * Changes element type if similar element present in DB. Otherwise - creates it.
     * @param handle - add handle to DB
     */
    public void addOrModify(WebHandle handle) throws SQLException {
        Statement stmt = c.createStatement();
        //SQL

        ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + handle.url.toString() +
                "' AND xpath = '" + handle.xpath + "'");
        if(!rs.isBeforeFirst()){
            stmt.executeUpdate("INSERT INTO handles (url, xpath, eltype) " +
                    "VALUES ('" + handle.url.toString() + "','" + handle.xpath + "','" + handle.eltype.name() + "')");
        }else{
            System.out.println("UPDATE handles SET url='" + handle.url.toString() + "',xpath = '" + handle.xpath + "', eltype='" + handle.eltype.name() + "'" +
                    "WHERE url = '" + handle.url.toString() + "' AND xpath = '" + handle.xpath + "'");
            stmt.executeUpdate("UPDATE handles SET url='" + handle.url.toString() + "',xpath = '" + handle.xpath + "', eltype='" + handle.eltype.name() + "'" +
                    "WHERE url = '" + handle.url.toString() + "' AND xpath = '" + handle.xpath + "'");
        }
        stmt.close();
    }

    public void add(URL url, String xpath, ElementType eltype) throws SQLException, ConflictingHandleStored {
        //TODO remove this copypaste
        Statement stmt = c.createStatement();
        //SQL

        ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url +
                "' AND xpath = '" + xpath + "'");
        if(!rs.isBeforeFirst()){
            stmt.executeUpdate("INSERT INTO handles (url, xpath, eltype) " +
                    "VALUES ('" + url + "','" + xpath + "','" + eltype.name() + "')");
        }else{
            rs.next();
            if(!rs.getString("eltype").equals(eltype.name())){
                throw new ConflictingHandleStored();
            }
        }
        stmt.close();

    }

    /**
     * abort connection with DB
     */
    public void close(){
        try {
            c.close();
            System.out.println("closed DB sucessfully");
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
    public ElementType request(URL url, String xpath) throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * from handles WHERE url = '" + url +
                "' AND xpath = '" + xpath + "'");
        while (rs.next()){
            String type = rs.getString("eltype");
            stmt.close();
            return ElementType.valueOf(type);
        }
        stmt.close();
        return ElementType.unknown;
    }


    /**
     * get all hashes associated with url
     * @param url
     * @return
     * @throws SQLException
     */
    public List<String> getHashesByURL(URL url) throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM urls WHERE url = '" + url.toString() + "'");
        List<String> hashes = new ArrayList<String>();
        while(rs.next()){
            hashes.add(rs.getString("hash"));
        }
        stmt.close();
        return hashes;

    }

    /**
     * Adds URL/Hash pair to the DB.
     * @param url
     * @param hash - value "MyLittleDefaultHash" is used to marke unparseg pages
     * @throws SQLException
     */
    public void addURL(URL url, String hash) throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * from urls WHERE url = '" + url.toString() +
                "' AND hash = '" + hash + "'");
        if(!rs.isBeforeFirst()){
            stmt.executeUpdate("INSERT INTO urls (url, hash) " +
                    "VALUES ('" + url.toString() + "','" + hash + "')");
        }
        stmt.close();
    }

    @Nullable
    public URL getRandomURL() throws SQLException, MalformedURLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT url FROM urls");
        List<String> urls = new ArrayList<>();
        if(!rs.isBeforeFirst()){
            return null;
        }
        while (rs.next()){
            urls.add(rs.getString("url"));
        }
        Random random = new Random();
        return new URL(urls.get(random.nextInt(urls.size())));
    }
    //for testing purposes only
    public static void main(String args[]) throws MalformedURLException, SQLException {
        PostgreSQLAlphabet alphabet = new PostgreSQLAlphabet();
        WebHandle handle = new WebHandle(new URL("http://vk.com"), "//html[1]", ElementType.clickable);
        try {
            alphabet.add(handle);
        } catch (ConflictingHandleStored conflictingHandleStored) {
            conflictingHandleStored.printStackTrace();
        }
        alphabet.close();
    }
}
