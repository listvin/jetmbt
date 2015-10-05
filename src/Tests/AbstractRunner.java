package Tests;

import Boxes.EFG;
import Boxes.Event;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 9/5/15.
 */
public abstract class AbstractRunner implements RunnerInterface {
    /*
     * processors - contains instances of processes and reference classes
     */
    protected final Map<Class<?>, Object> processors = new HashMap<>();
    protected EFG graph;
    protected WebDriver driver;

    /**
     * You can create testing environment based on graph dump
     * @param filePath - path to graph Dump
     */
    public AbstractRunner(String filePath){
        graph = new EFG(filePath);
        driver = new FirefoxDriver();
    }

    /**
     * Or you can reuse driver
     * @param filePath - path to graph dump
     * @param driver - webdriver to use
     */
    public AbstractRunner(String filePath, WebDriver driver){
        graph = new EFG(filePath);
        this.driver = driver;
    }


    @Override
    public void setEFG(String path) {
        graph = new EFG(path);
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


    protected void addProcessor(Object processor){
        processors.put(processor.getClass(), processor);
    }

    /**
     * creates instances of missing processors
     */
    protected void instantiateMissingProcessors() {
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
}
