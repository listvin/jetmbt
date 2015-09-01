package Tests;

/**
 * Created by user on 9/1/15.
 */
public class TestExample {
    @OnElementTest
    boolean TestA(){
        System.out.println("wololo");
        return true;
    }
    //tmp.generatePathsBetweenEvents(Event.create(new WebHandle(new JetURL("url:http://localhost:8080/dashboard"), "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]"), ""), Event.create(new WebHandle(new JetURL("url:http://localhost:8080/issues"), "//*[@id=\"filterLi_toggler_reporter\"]/div[2]"), ""), 5, 15);
    public void run(String[] args){
        Runner runner = new Runner(args[0]);
        runner.addProcessors(this);
        try {
            runner.testSingleEvent("url:http://localhost:8080/dashboard", "//*[@id=\"dashboard\"]/div[2]/div/div[2]/rg-tabs-pane/div/div/div/ul/li[3]/widget-youtrack-issues/div/div[2]/div[3]/div[2]/div/div[2]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        runner.close();
    }

    public static void main(String[] args){
        new TestExample().run(args);
    }
}
