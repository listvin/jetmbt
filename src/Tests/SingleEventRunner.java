package Tests;

import Boxes.*;
import Common.ElementType;
import Common.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by user on 9/1/15.
 * This class is desined for easy test construction
 */
public class SingleEventRunner extends AbstractRunner {
    private Event A;
    private int len;
    public SingleEventRunner(String filePath) {
        super(filePath);
        len = 10;
    }

    public SingleEventRunner(String filePath, int len) {
        super(filePath);
        this.len = len;
    }

    public SingleEventRunner(String filePath, WebDriver driver) {
        super(filePath, driver);
        len = 10;
    }

    public SingleEventRunner(String filePath, WebDriver driver, int len) {
        super(filePath, driver);
        this.len = len;
    }

    public void setLen(int a){
        len = a;
    }
    /**
     * generates paths through single event and performs checks
     * @param event - event to check on
     * @throws Exception
     */
    public void test(Event event) throws Exception {
        instantiateMissingProcessors();
        A = event;
        Sequence path = graph.generateSinglePathTroughEvent(A, 20);
        Utils.setUpDriver(driver);
        // TODO - remove dirty hacks. THIS CODE IS EXCREMELY DIRTY HACK
        // Current architecture: if replay failes, but it is not considered
        // as test fail = just return false in checker
        // iftest fails = raise exception, add it to failException list and use it as flag.
        //Yes dirty - I know it...
        final List<String> failExceptions = new ArrayList<>();
        while(! path.play(driver, (driver1, event1) -> {
            try {
                return singleEventPerformer(driver1, event1);
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
    public void test(String URL, String XPath) throws Exception {
        test(URL, XPath, "");
    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @param context
     * @throws Exception
     */
    public void test(String URL, String XPath, String context) throws Exception {
        test(URL, XPath, ElementType.clickable, context);
    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @param type
     * @param context
     * @throws Exception
     */
    public void test(String URL, String XPath, ElementType type, String context) throws Exception {
        test(Event.create(new WebHandle(JetURL.createJetURL(URL), XPath, type), context));
    }


    /**
     * function desined to perform tests. This function must call event.perform inside of it
     * @param driver
     * @param event
     * @return
     * @throws Exception
     */
    private boolean singleEventPerformer(WebDriver driver, Event event) throws TestFailException {
        if(!event.perform(driver)) return false;

        if(!A.equals(event)){
           return true;
        }
        for (Map.Entry<Class<?>, Object> entry : processors.entrySet()) {
            for(Method method: entry.getValue().getClass().getDeclaredMethods()){
                if(method.isAnnotationPresent(Tests.OnElementTest.class)){
                    if(!method.getAnnotation(Tests.OnElementTest.class).enabled()){
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
