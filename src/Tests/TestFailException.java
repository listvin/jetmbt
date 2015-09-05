package Tests;

import java.lang.reflect.Method;

/**
 * Created by user on 9/4/15.
 */
public class TestFailException extends Exception {
    private Method test = null;
    //Parameterless Constructor
    public TestFailException() {}

    //Constructor that accepts a message
    public TestFailException(String message)
    {
        super(message);
    }
    //Constructor that accepts a message and sets TestMethod that cause fail
    public TestFailException(String message, Method test)
    {
        super(message);
        this.test = test;
    }

    /**
     * get failed test name
     * @return
     */
    public String getTestName(){
        return test.getName();
    }
}
