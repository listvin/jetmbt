import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 7/28/15.
 */
public class FedorsExperiments {
//    public static void main(String arg[]){
//        String name = (new SimpleDateFormat("EEE_ddMMMyyyy_HH:mm:ss.SSS_")).format(new Date()).toLowerCase() + "at_my_clock";
//        System.out.println(name);
//
//        Map<String, String> atts = new HashMap<>();
//
//        try {
//            PrintWriter writer = new PrintWriter("graphs/" + "colors" + ".gv", "UTF-8");
//            FileReader reader = new FileReader("colors.in");
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
////        reader
//
//    }

        public static final void main(final String[] args) throws Throwable {
            System.out.println(
                    FedorsExperiments
                            .class
                            .getClassLoader()
                            .getResource(FedorsExperiments.class.getName().replace('.', '/') + ".class")
            );

            PrintStream ps = new PrintStream("test.txt");
            ps.println("ga\n");
        }
}
