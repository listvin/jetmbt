package Tests;

import Boxes.Event;
import Boxes.JetURL;
import Boxes.Sequence;
import Boxes.WebHandle;
import Common.ElementType;
import Common.Utils;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 9/19/15.
 */
public class DualEventRunner extends AbstractRunner {
    private Event A, B;
    private int len;
    public DualEventRunner(String filePath) {
        super(filePath);
        len = 10;
    }

    public DualEventRunner(String filePath, int len) {
        super(filePath);
        this.len = len;
    }

    public DualEventRunner(String filePath, WebDriver driver) {
        super(filePath, driver);
        len = 10;
    }

    public DualEventRunner(String filePath, WebDriver driver, int len) {
        super(filePath, driver);
        this.len = len;
    }

    /**
     * sets length of paths
     * @param a
     */
    public void setLen(int a){
        len = a;
    }

    /**
     * generates paths through tow events and performs checks
     * @param event1 - intermidiate point of paths
     * @param event2 - end point of paths
     * @throws Exception
     */
    public void test(Event event1, Event event2) throws Exception {
        instantiateMissingProcessors();
        A = event1;
        B = event2;
        Sequence path = graph.generateSinglePathBetweenEvents(A, B, len);
        Utils.setUpDriver(driver);
        // TODO - remove dirty hacks. THIS CODE IS EXCREMELY DIRTY HACK
        // Current architecture: if replay failes, but it is not considered
        // as test fail = just return false in checker
        // iftest fails = raise exception, add it to failException list and use it as flag.
        //Yes dirty - I know it...
        final List<String> failExceptions = new ArrayList<>();
        while(! path.play(driver, (driver1, event0) -> {
            try {
                return dualEventPerformer(driver1, event0);
            } catch (TestFailException e) {
                failExceptions.add(e.getMessage() + e.getTestName());
            }
            return false;
        })){
            if(!failExceptions.isEmpty()){
                break;
            }
            Utils.resetSession(driver);
            System.out.println("Next attempt");
            path = graph.generateSinglePathTroughEvent(A, 20);
        }
        if(failExceptions.isEmpty()) {
            System.out.println("Finished testing");
        }else{
            System.out.println(failExceptions.get(0));
            System.out.println("Test path:");
            System.out.println(path);
        }


    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @throws Exception
     */
    public void test(String URL, String XPath, String URL1, String XPath1) throws Exception {
        test(URL, XPath, "", URL1, XPath1, "");
    }

    /**
     * generates paths through single event and performs checks
     * @param URL's - URL strings of desired tested events
     * @param XPath - Xpaths of tested elements
     * @param context - context of tested elements
     * @throws Exception
     */
    public void test(String URL, String XPath, String context, String URL1, String XPath1, String context1) throws Exception {
        test(URL, XPath, ElementType.clickable, context, URL1, XPath1, ElementType.clickable, context1);
    }

    /**
     * generates paths through two events event and performs checks
     * @param URL's - URL strings of desired tested events
     * @param XPath - XPATH strings of desired tested events
     * @param type - types of events
     * @param context -context of events
     * @throws Exception
     */
    public void test(String URL, String XPath, ElementType type, String context, String URL1, String XPath1, ElementType type1, String context1) throws Exception {
        test(Event.create(new WebHandle(JetURL.createJetURL(URL), XPath, type), context), Event.create(new WebHandle(JetURL.createJetURL(URL1), XPath1, type1), context1));
    }


    /**
     * function desined to perform tests. This function must call event.perform inside of it
     * @param driver
     * @param event
     * @return
     * @throws Exception
     */
    private boolean dualEventPerformer(WebDriver driver, Event event) throws TestFailException {
        if(!event.perform(driver)) return false;

        if(!A.equals(event) && !B.equals(event)){
            return true;
        }
        for (Map.Entry<Class<?>, Object> entry : processors.entrySet()) {
            for(Method method: entry.getValue().getClass().getDeclaredMethods()){
                if(A.equals(event) && method.isAnnotationPresent(Tests.OnFirstElementTest.class)){
                    if(!method.getAnnotation(Tests.OnFirstElementTest.class).enabled()){
                        continue;
                    }
                    if(!method.getReturnType().equals(boolean.class)){
                        throw new TestFailException("All @OnElementTest annotated methods should have boolean return type: ", method);
                    }
                    try {
                        //TODO - check all methods on driver
                        if(!(boolean)method.invoke(entry.getValue(), driver)){

                            throw new TestFailException("Failed test", method);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else if(B.equals(event) && method.isAnnotationPresent(Tests.OnSecondElementTest.class)){
                    if(!method.getAnnotation(Tests.OnSecondElementTest.class).enabled()){
                        continue;
                    }
                    if(!method.getReturnType().equals(boolean.class)){
                        throw new TestFailException("All @OnElementTest annotated methods should have boolean return type: ", method);
                    }
                    try {
                        //TODO - check all methods on driver
                        if(!(boolean)method.invoke(entry.getValue(), driver)){

                            throw new TestFailException("Failed test", method);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    public void close(){
        driver.close();
    }
}
