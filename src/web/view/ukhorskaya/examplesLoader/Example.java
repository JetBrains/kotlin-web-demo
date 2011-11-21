package web.view.ukhorskaya.examplesLoader;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/17/11
 * Time: 11:02 AM
 */

public class Example {
    private ExampleDirectory parent;
    private String name;

    public Example(ExampleDirectory parent, String name) {
        this.parent = parent;
        this.name = name;
    }
}
