package group9;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.nio.Buffer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;


/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 SOURCED FROM: https://github.com/KTH-DD2480/smallest-java-ci
 */

public class CI extends AbstractHandler
{
    static String repo_name = "DD2480-Group-9-CI";
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code

        response.getWriter().println("CI job done");
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        logToFile("repo", "branch", "commitID",
                "compileResult", "testResult");
//        cloneRepo("git@github.com:Asken59/DD2480-Group-9-CI.git");
        //compileProject("DD2480-Group-9-CI");
        //testProject("DD2480-Group-9-CI");
//        Server server = new Server(8080);
//        server.setHandler(new CI());
//        server.start();
//        server.join();
    }

    public static String cloneRepo(String repoURL) throws IOException, InterruptedException, GitAPIException {

        // Remove the old clone of the repo (if it exists)
        File repo_dir = new File(repo_name);
        if(repo_dir.exists()) {

            // Remove the old clone
            ProcessBuilder pb = new ProcessBuilder("rm", "-r", repo_name);
            Process p = pb.start();
            p.waitFor();
            p.destroy();
        }

        // Clone the repo
        Git git = Git.cloneRepository().setURI(repoURL).call();

        return "";
    }

    //TODO: Add cd functionality. Need absolute path
    /** compileProject
     * The method will attempt to compile the project at the given path.
     * Compilation is preformed with mvn compile and the result of the command
     * is parsed and returned.
     * @param projectPath path to the project that should be compiled.
     * @return Returns either "BUILD SUCCESS" or "BUILD FAILED" depending on compile result.
    */
    public static String compileProject(String projectPath) throws IOException, InterruptedException {


        // Go into directory and launch mvn compile
        ProcessBuilder pb = new ProcessBuilder("mvn", "compile");

        // Start process
        Process process = pb.start();

        // Initialize bufferReader to read output from process
        BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
        StringBuilder builder = new StringBuilder();
        String line = null;

        // Iterate all lines and add to builder
        while ( (line = reader.readLine()) != null ) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        // Get string
        String result = builder.toString();

        // Wait and kill process
        process.waitFor();
        process.destroy();

        // Parse result
        // Retrieve relevant line and remove unnecessary tokens
        String lines[]  = result.split("\n");
        String parsedResult = lines[13].substring(7);

        // Check if it failed
        if (!parsedResult.equals("BUILD SUCCESS"))
            parsedResult = "BUILD FAILED";

        // Return parsedResults
        return parsedResult;
    }

    public static String testProject(String projectPath) throws IOException, InterruptedException {

        // Initialize a processbuilder
        ProcessBuilder pb = new ProcessBuilder();

        // Go into directory and launch mvn test
        pb.command("cd", projectPath, ";", "mvn test");

        // Start process
        Process process = pb.start();

        // Initialize bufferreader to read output from process
        BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
        StringBuilder builder = new StringBuilder();
        String line = null;

        // Iterate all lines and add to builder
        while ( (line = reader.readLine()) != null ) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        // Get string
        String result = builder.toString();

        // Wait and kill process
        process.waitFor();
        process.destroy();

        // Return results
        return result;
    }

    public static void notifyGithub(String compileResult, String testResult){

    }

    public static void logToFile(String repository, String branch, String commitId, String compileResult, String testResult) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date();
        String buildDate = formatter.format(date);
        JSONObject obj = new JSONObject();

        obj.put("repository", repository);
        obj.put("branch", branch);
        obj.put("commitId", commitId);
        obj.put("buildDate", buildDate);
        obj.put("compileResult ", compileResult);
        obj.put("testResult", testResult);

        File json_dir = new File("build-logs");
        String file_name = "build-" + buildDate + ".json";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file_name), false));) {
            bw.write(obj.toString(4));
            bw.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}
