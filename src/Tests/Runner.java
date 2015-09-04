package Tests;

import Boxes.*;
import Common.ElementType;
import Common.Utils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.annotation.processing.AbstractProcessor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by user on 9/1/15.
 * This class is desined for easy test construction
 */
public class Runner{
    /*
     * processors - contains instances of processes and reference classes
     */
    private final Map<Class<?>, Object> processors = new HashMap<>();
    private final EFG graph;
    private Event A;
    private Event B;
    private WebDriver driver;

    /**
     * You can create testing environment based on graph dump
     * @param filePath - path to graph Dump
     */
    public Runner(String filePath){
        graph = new EFG(filePath);
        driver = new FirefoxDriver();
    }

    /**
     * Or you can reuse driver
     * @param filePath - path to graph dump
     * @param driver - webdriver to use
     */
    public Runner(String filePath, WebDriver driver){
        graph = new EFG(filePath);
        this.driver = driver;
    }


    /**
     * provide processor classes for tests
     * @param processors
     */
    public void addProcessors(Object... processors){
        for(Object obj: processors){
            addProcessor(obj);
        }
    }


    private void addProcessor(Object processor){
        processors.put(processor.getClass(), processor);
    }

    /**
     * creates instances of missing processors
     */
    private void instantiateMissingProcessors() {
        for (Map.Entry<Class<?>, Object> entry : processors.entrySet()) {
            if (entry.getValue() == null) {
                try {
                    entry.setValue(entry.getKey().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * generates paths through single event and performs checks
     * @param event - event to check on
     * @throws Exception
     */
    public void testSingleEvent(Event event) throws Exception {
        instantiateMissingProcessors();
        A = event;
        Sequence path = graph.generateSinglePathTroughEvent(A, 20);
        Utils.setUpDriver(driver);
        // TODO - mb throw exception from play?
        while(! path.play(driver, (a, b) -> {
            try {
                return singleEventPerformer(a, b);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        })){
            Utils.resetSession(driver);
            System.out.println("Next attempt");
            path = graph.generateSinglePathTroughEvent(A, 20);
        }
        System.out.println("Finished testing");
    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @throws Exception
     */
    public void testSingleEvent(String URL, String XPath) throws Exception {
        testSingleEvent(URL, XPath, "");
    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @param context
     * @throws Exception
     */
    public void testSingleEvent(String URL, String XPath, String context) throws Exception {
        testSingleEvent(URL, XPath, ElementType.clickable, context);
    }

    /**
     * generates paths through single event and performs checks
     * @param URL
     * @param XPath
     * @param type
     * @param context
     * @throws Exception
     */
    public void testSingleEvent(String URL, String XPath, ElementType type, String context) throws Exception {
        testSingleEvent(Event.create(new WebHandle(JetURL.createJetURL(URL), XPath, type), context));
    }


    /**
     * function desined to perform tests. This function must call event.perform inside of it
     * @param driver
     * @param event
     * @return
     * @throws Exception
     */
    private boolean singleEventPerformer(WebDriver driver, Event event) throws Exception {
        if(!event.perform(driver)) return false;

        if(!A.equals(event)){
           return true;
        }
        for (Map.Entry<Class<?>, Object> entry : processors.entrySet()) {
            for(Method method: entry.getValue().getClass().getDeclaredMethods()){
                if(method.isAnnotationPresent(Tests.OnElementTest.class)){
                    if(!method.getReturnType().equals(boolean.class)){
                        throw new Exception("All @OnElementTest annotated methods should have boolean return type");
                    }
                    try {
                        //TODO - check all methods on driver
                        if(!(boolean)method.invoke(entry.getValue(), driver)){

                            throw new Exception("Failed test: "  + method.getName());
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
