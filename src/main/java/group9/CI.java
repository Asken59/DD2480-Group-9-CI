package group9;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.nio.Buffer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        if (request.getMethod().equals("POST")){
            // Handle CI

            // Read from request
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            String data = buffer.toString();

            // Convert POST request body to json
            JSONObject json = new JSONObject(data);

            // Retrieve url
            String repoURL = json.getJSONObject("repository").getString("html_url");

            // Retrieve repo
            String repoName = json.getJSONObject("repository").getString("full_name");

            // Retrieve branch
            String branch = json.getString("ref");

            // Retrieve commitID
            String commitID = json.getJSONObject("head_commit").getString("id");

            // Retrieve commit message
            String commitMessage = json.getJSONObject("head_commit").getString("message");

//            // Clone
//            String projectPath;
//            try {
//                projectPath = cloneRepo("");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            // Compile
//            String compileResult;
//            try {
//                compileResult = compileProject(projectPath);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // Test
//            String testResult;
//            try {
//                testResult = testProject(projectPath);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // Write log
//            logToFile("repo", "branch", "commitID",
//                compileResult, testResult);
//
//            // change index.html
//            generateIndexFile();
//
//            // Send notification
//

        }
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(".");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new CI() });
        server.setHandler(handlers);

        // Run once before the server starts to make sure there is an index.html file
        generateIndexFile();

        // Start server
        server.start();
        server.join();
    }

    /**
     * The method will attempt to clone a specified repository at a fixed path.
     * If there exists a previously cloned repository at the location, the method will remove it.
     * JGit is used to clone the repository.
     *
     * @param   repoURL     Git repository cloning URL
     * @return              An absolute path to the cloned repository
     */
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

    /** testProject
     * The method will attempt to run all tests in the project at the given path.
     * Test is preformed with mvn test and the result of the command
     * is parsed and returned.
     * @param projectPath path to the project that should be compiled.
     * @return Returns either "BUILD SUCCESS" or "BUILD FAILED" with the
     * corresponding tests which failed, depending on compile result.
     */
    public static ArrayList<String> testProject(String projectPath) throws IOException, InterruptedException {

        // Initialize a processbuilder
        ProcessBuilder pb = new ProcessBuilder();

        // Go into directory and launch mvn test
        pb.command("/bin/bash", "-c", "mvn test");
        // pb.command("cd", projectPath, ";", "mvn test");

        // Start process
        Process process = pb.start();

        // Initialize bufferreader to read output from process
        BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
        String line = null;
        ArrayList<String> testResult = new ArrayList<String>();

        // Iterate all lines and add to builder
        while ( (line = reader.readLine()) != null ) {
            if (line.contains("CITests.") && !line.contains("at group9.")) {
                line = line.replaceAll("^\\[ERROR\\]\\s*", "");
                testResult.add(line);
            }
            if (line.contains("BUILD")) {
                line = line.replaceAll("^\\[INFO\\]\\s*", "");
                testResult.add(line);
            }
        }

        // Wait and kill process
        process.waitFor();
        process.destroy();


        // Return results
        return testResult;
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
        obj.put("compileResult", compileResult);
        obj.put("testResult", testResult);

        File json_dir = new File("build-logs");
        String file_name = "build-" + buildDate + ".json";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(json_dir, file_name), false));) {
            bw.write(obj.toString(4));
            bw.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    public static void generateIndexFile() throws IOException {

        File json_dir = new File("build-logs");

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("index.html"), false));
        bw.write("<html><body>");
        bw.write("<ul>");

        for(File log : json_dir.listFiles()){
            bw.write("<li>");
            bw.write("<a href='/build-logs/");
            bw.write(log.getName());
            bw.write("'>");
            bw.write(log.getName());
            bw.write("</a>");
            bw.write("</li>");
        }

        bw.write("</ul>");
        bw.write("</body></html>");
        bw.close();

    }
}
