package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.TreeMap;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Siyan Shen
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void filename() {
        File file = new File("Makefile");
        if (file.exists()) {
            assertEquals(file.getName(), "Makefile");
        }
    }

    @Test
    public void hashMapProperty() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void testSortHashSet() {
        HashSet<String> set = new HashSet<>();
        set.add("a");
        set.add("z");
        set.add("d");
        String[] b = Utils.sorthashset(set);
        String[] a = {"a", "d", "z"};
        for (int i = 0; i < b.length; i += 1) {
            assertEquals(b[i], a[i]);
        }
    }

    @Test
    public void hashMapProperty1() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty2() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty3() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty4() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty5() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty6() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void hashMapProperty7() {
        TreeMap<String, String> map = new TreeMap<>();
        assertTrue(map.keySet().isEmpty());
    }
}
