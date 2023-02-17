package group9;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit test for compileProject and testProject methods
 */
public class CITests
{
    // Paths to the subdirectories used for testing methods
    String compileFailSubProject =  "mvn-tests/mvn-fail-compile";
    String compileSuccessSubProject = "mvn-tests/mvn-fail-tests";
    String testFailSubProject = "mvn-tests/mvn-fail-tests";
    String testSuccessSubProject = "mvn-tests/mvn-succeed-tests";

    // compileProject failure test
    // Calls compileProject with the path to a subproject
    // that should not compile.
    // Expected result from method call: "BUILD FAILED"
    @Test
    public void compileFailure() throws IOException, InterruptedException {
        File f = new File(compileFailSubProject);
        String p = f.getAbsolutePath();
        String result = CI.compileProject(p);
        assertFalse("BUILD SUCCESS".equals(result));
    }

    // compileProject success test
    // Calls compileProject with the path to a subproject
    // that should compile.
    // Expected result from method call: "BUILD SUCCESS"
    @Test
    public void compileSuccess() throws IOException, InterruptedException {
        File f = new File(compileSuccessSubProject);
        String p = f.getAbsolutePath();
        String result = CI.compileProject(p);
        assertFalse("BUILD SUCCESS".equals(result));
    }

    // testProject failure test
    // Calls testProject with the path to a subproject
    // with tests that should fail.
    // Expected result from method call: "BUILD FAILURE"
    @Test
    public void testFailure() throws IOException, InterruptedException {
        File f = new File(testFailSubProject);
        String p = f.getAbsolutePath();
        ArrayList result = CI.testProject(p);
        assertTrue(result.get(0).equals("BUILD FAILURE"));

    }

    // testProject success test
    // Calls testProject with the path to a subproject
    // with tests that should pass.
    // Expected result from method call: "BUILD SUCCESS"
    @Test
    public void testSuccess() throws IOException, InterruptedException {
        File f = new File(testSuccessSubProject);
        String p = f.getAbsolutePath();
        ArrayList result = CI.testProject(p);
        assertTrue(result.get(0).equals("BUILD SUCCESS"));

    }
}
