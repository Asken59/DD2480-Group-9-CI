package group9;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;

//Generated by example maven project

/**
 * Unit test for simple App.
 */
public class CITests
{
    // Paths to the subdirectories used for testing methods
    String compileFailSubProject =  "mvn-tests/mvn-fail-compile";
    String compileSuccessSubProject = "mvn-tests/mvn-fail-tests";
    String testFailSubProject = "mvn-tests/mvn-fail-tests";
    String testSuccessSubProject = "mvn-tests/mvn-succeed-tests";

    // Compile negative test
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

    // Compile positive test
    // Calls compileProject with the path to a subproject
    // that should compile.
    // Expected result from method call: "BUILD SUCCESS"
    @Test
    public void compileSuccess() throws IOException, InterruptedException {
        File f = new File(compileSuccessSubProject);
        String p = f.getAbsolutePath();
        String result = CI.compileProject(p);
        assertTrue("BUILD SUCCESS".equals(result));
    }
}
