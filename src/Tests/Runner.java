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
 */
public class Runner{
    private final Map<Class<?>, Object> processors = new HashMap<>();
    private final EFG graph;
    private Event A;
    private Event B;
    private WebDriver driver;
    public Runner(String filePath){
        graph = new EFG(filePath);
        driver = new FirefoxDriver();
    }
    public Runner(String filePath, WebDriver driver){
        graph = new EFG(filePath);
        this.driver = driver;
    }
    public void addProcessors(Object... processors){
        for(Object obj: processors){
            addProcessor(obj);
        }
    }


    public void addProcessor(Object processor){
        processors.put(processor.getClass(), processor);
    }

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

    public void testSingleEvent(Event event) throws Exception {
        instantiateMissingProcessors();
        A = event;
        Sequence path = graph.generateSinglePathTroughEvent(A, 30);
        Utils.setUpDriver(driver);
        while(! path.play(driver, (a, b) -> {
            try {
                return singleEventChecker(a, b);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        })){
            Utils.resetSession(driver);
            System.out.println("Next attempt");
            path = graph.generateSinglePathTroughEvent(A, 100);
        }
    }

    public void testSingleEvent(String URL, String XPath) throws Exception {
        testSingleEvent(URL, XPath, "");
    }

    public void testSingleEvent(String URL, String XPath, String context) throws Exception {
        testSingleEvent(URL, XPath, ElementType.clickable, context);
    }

    public void testSingleEvent(String URL, String XPath, ElementType type, String context) throws Exception {
        testSingleEvent(Event.create(new WebHandle(JetURL.createJetURL(URL), XPath, type), context));
    }

    private boolean singleEventChecker(WebDriver driver, Event event) throws Exception {
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
                        System.out.println(method.invoke(entry.getValue()));
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
