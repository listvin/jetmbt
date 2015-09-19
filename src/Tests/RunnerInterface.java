package Tests;

import Boxes.Event;

import java.util.Objects;

/**
 * Created by user on 9/5/15.
 */
public interface RunnerInterface {
    void setEFG(String path);

    void addProcessors(Object... processors);

    void close();
}
