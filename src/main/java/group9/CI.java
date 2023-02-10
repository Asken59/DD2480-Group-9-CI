package group9;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.nio.Buffer;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import org.eclipse.jetty.client.HttpClient;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
    private static HttpClient apiClient;
    private static String accessToken;

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        long threadID = Thread.currentThread().getId();
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

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

            System.out.println(threadID + " Recieved new push from " + repoName + "/" + branch);
            System.out.println(threadID + " Cloning...");

            // Clone
            String projectPath;
            try {
                projectPath = cloneRepo("https://github.com/" + repoName + ".git", commitID, threadID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println(threadID + " Cloned! Compiling...");

            // Compile
            String compileResult;
            try {
                compileResult = compileProject(projectPath);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(threadID + " Compiled! Testing...");

            // Test
            ArrayList<String> testResult;
            try {
                testResult = testProject(projectPath);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(threadID + " Tested! Writing to log file...");

            // Write log
            logToFile(repoName, branch, commitID, commitMessage, testResult, compileResult, threadID);
            System.out.println(threadID + " Wrote to log file!");

            // change index.html
            generateIndexFile();

            // Send notification
            try {
                commitStatus(repoName, commitID, compileResult, testResult, threadID);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            System.out.println("--- " + threadID + " Push handled ---");
            System.out.println();
            baseRequest.setHandled(true);

            //Clean up repo folder
            try {
                cleanUp(threadID);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        //Read the github access token
        System.out.println("Please enter your GitHub access token (used for commit status):");
        Scanner input = new Scanner(System.in);
        accessToken = input.nextLine();
        input.close();

        Server server = new Server(8080);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(".");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new CI() });
        server.setHandler(handlers);

        // Run once before the server starts to make sure there is an index.html file
        generateIndexFile();

        //Start the API client
        apiClient = new HttpClient();
        apiClient.setFollowRedirects(false);
        apiClient.start();

        // Start server
        server.start();
        System.out.println("--Server started--");
        System.out.println();
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
    public static String cloneRepo(String repoURL, String commitID, long threadID)
            throws IOException, InterruptedException, GitAPIException {

        // Remove the old clone of the repo (if it exists)
        String repo_dir_name = "repository" + threadID;
        File repo_dir = new File(repo_dir_name);
        if(repo_dir.exists()) {

            // Remove the old clone
            ProcessBuilder pb = new ProcessBuilder("rm", "-r", repo_dir_name);
            Process p = pb.start();
            p.waitFor();
            p.destroy();
        }

        // Clone the repo branch
        Git git = Git.cloneRepository().setDirectory(repo_dir).setURI(repoURL).call();
        git.checkout().setName(commitID).call();

        // Return the absolute path to the cloned repository
        return repo_dir.getAbsolutePath();
    }

    /** compileProject
     * The method will attempt to compile the project at the given path.
     * Compilation is preformed with mvn compile and the result of the command
     * is parsed and returned.
     * @param projectPath path to the project that should be compiled.
     * @return Returns either "BUILD SUCCESS" or "BUILD FAILED" depending on compile result.
     * @throws  IOException
     * @throws  InterruptedException
    */
    public static String compileProject(String projectPath) throws IOException, InterruptedException {


        // Go into directory and launch mvn compile
        ProcessBuilder pb = new ProcessBuilder("mvn", "compile");
        pb.directory(new File(projectPath));

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
        String parsedResult = "BUILD FAILED";
        // Check if it succeeded
        if (result.contains("BUILD SUCCESS"))
            parsedResult = "BUILD SUCCESS";

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
        pb.directory(new File(projectPath));
        pb.command("mvn", "test");

        // Start process
        Process process = pb.start();
        // Initialize bufferreader to read output from process
        BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
        StringBuilder builder = new StringBuilder();
        String line = null;
        ArrayList<String> testResult = new ArrayList<String>();

        // Iterate all lines and add to builder
        while ( (line = reader.readLine()) != null ) {
            if (line.contains("CITests.") && !line.contains("at group9.")) {
                line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                line = line.replaceAll("^\\[ERROR\\]\\s*", "");
                testResult.add(line);
            }
            if (line.contains("BUILD")) {
                line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                line = line.replaceAll("^\\[INFO\\]\\s*", "");
                testResult.add(line);
            }
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        // Wait and kill process
        process.waitFor();
        process.destroy();

        String result = builder.toString();

        // Return results
        return testResult;
    }

    /** logToFile
     * The method will create and write to the build log file
     * A time stamp for the build is created when this method is run
     * writes the new json file to the build-logs directory
     * @param repository the repository of the project.
     * @param branch the current branch being worked on
     * @param commitId the commit ID of the current commit
     * @param commitMessage the message of the current commit
     * @param compileResult the result of the compilation
     * @param testResult an ArrayList of the tests failed, the last element is the build result
     * @throws IOException
     */

    public static void logToFile(String repository, String branch, String commitId, String commitMessage,
                                 ArrayList<String> testResult, String compileResult, long threadID) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date();
        String buildDate = formatter.format(date);
        JSONObject obj = new JSONObject();

        System.out.println(threadID + " Log file name: " + buildDate + ".json");

        String tests ="";

        for(int i = 0; i < testResult.size()-1; i++){
            if(i == 0) {
                tests = "Tests failed "  + testResult.get(i);
            }
            else {
                tests = tests + " " + testResult.get(i);
            }
        }

        if(tests.length() == 0){
            tests = "All tests passed";
        }

        obj.put("repository", repository);
        obj.put("branch", branch);
        obj.put("commitId", commitId);
        obj.put("commitMessage", commitMessage);
        obj.put("buildDate", buildDate);
        obj.put("compileResult", compileResult);
        obj.put("testResult", tests);

        File json_dir = new File("build-logs");
        String file_name = "build-" + buildDate + ".json";

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(json_dir, file_name), false));
            bw.write(obj.toString(4));
            bw.close();
    }

    /** commitStatus
     * The method will try to send a POST request to github and updates commit status with
     * the github API
     * @param repo Name of the gitrepo
     * @param sha Commit ID
     * @param compileStatus If the project compiled or not
     * @param testStatus If the tests succeded or not
     * @param threadID Identical ID of current thread
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static void commitStatus(String repo, String sha, String compileStatus, ArrayList<String> testStatus, long threadID)
            throws InterruptedException, ExecutionException, TimeoutException {
        String url = "https://api.github.com/repos/" + repo + "/statuses/" + sha;

        StringBuilder jsonString = new StringBuilder("{\"state\":\"");
        if(compileStatus == "BUILD FAILED") {
            jsonString.append("failure\",\"description\":\"Compilation failed");
        }
        else if(testStatus.size() < 2) { //Success
            jsonString.append("success\",\"description\":\"Compilation possible and all tests passes");
        } else {
            jsonString.append("failure\",\"description\":\"Tests failed:");
            for(int i = 0; i < testStatus.size() - 1; i++){ //Print all test failures
                jsonString.append(" " + testStatus.get(i));
            }
        }
        jsonString.append("\",\"context\":\"ci-server\"}");
        String jsonPayload = jsonString.toString();

        org.eclipse.jetty.client.api.Request apiRequest = apiClient.POST(url);

        //Per the GitHub api docs
        apiRequest.header(HttpHeader.ACCEPT, "application/vnd.github+json");
        apiRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        apiRequest.header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        apiRequest.body(new StringRequestContent(jsonPayload));

        System.out.println(threadID + " Updating commit status of commit " + sha);
        ContentResponse response = apiRequest.send();
        System.out.println(threadID + " Reply: " + response.getContentAsString());
    }

    /**
     * The method will generate an index.html file in the root directory.
     * The file created will contain an unordered list of links to all build logs
     * so that they can be visited and viewed in JSON format in a web browser.
     */
    public static void generateIndexFile() throws IOException {

        // Find the "build-logs" directory
        File json_dir = new File("build-logs");

        // Write to a new file "index.html" and place it in the root directory.
        // An existing "index.html" file will be overwritten.
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("index.html"), false));
        bw.write("<html><body>");
        bw.write("<ul>");
        File[] logs = json_dir.listFiles();
        Arrays.sort(logs);

        // Dynamically write HTML links to "index.html" for all build logs in the "build-logs" directory
        for(File log : logs){
            bw.write("<li>");
            bw.write("<a href='/build-logs/");
            bw.write(log.getName());
            bw.write("'>");
            bw.write(log.getName());
            bw.write("</a>");
            bw.write("</li>");
        }

        // Wrap up and close the writer
        bw.write("</ul>");
        bw.write("</body></html>");
        bw.close();
    }

    /** cleanUp
     * The method removes the repositories which was created
     * @param threadID Identical ID of current thread
     * @throws  IOException
     * @throws  InterruptedException
     */
    private void cleanUp(long threadID) throws IOException, InterruptedException {
        String repo_dir_name = "repository" + threadID;
        File repo_dir = new File(repo_dir_name);
        if(repo_dir.exists()) {
            // Cleanup the repo
            ProcessBuilder pb = new ProcessBuilder("rm", "-r", repo_dir_name);
            Process p = pb.start();
            p.waitFor();
            p.destroy();
        }
    }
}
