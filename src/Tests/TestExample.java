package Tests;

import org.openqa.selenium.WebDriver;

/**
 * Created by user on 9/1/15.
 * All tests should return true if test is passed
 */
public class TestExample {
    @OnElementTest
    boolean TestA(WebDriver driver){
        System.out.println("wololo");
        return true;
    }

    @OnElementTest
    boolean Detect404(WebDriver driver){
        return !driver.getTitle().contains("404");
    }
    //tmp.generatePathsBetweenEvents(Event.create(new WebHandle(new JetURL("url:http://localhost:8080/dashboard"), "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]"), ""), Event.create(new WebHandle(new JetURL("url:http://localhost:8080/issues"), "//*[@id=\"filterLi_toggler_reporter\"]/div[2]"), ""), 5, 15);
    public void run(String[] args){
        SingleEventRunner singleEventRunner = new SingleEventRunner(args[0]);
        singleEventRunner.addProcessors(this);
        try {
            singleEventRunner.test("url:http://localhost:8080/dashboard", "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        singleEventRunner.close();
    }

    public static void main(String[] args){
        new TestExample().run(args);
    }
}
