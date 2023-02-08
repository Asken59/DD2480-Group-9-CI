package group9;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 SOURCED FROM: https://github.com/KTH-DD2480/smallest-java-ci
 */

public class CI extends AbstractHandler
{
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
        Server server = new Server(8080);
        server.setHandler(new CI());
        server.start();
        server.join();
    }

    public static String cloneRepo(String repoURL){
        return "";
    }

    public static String compileProject(String projectPath){
        return "";
    }

    public static String testProject(String projectPath){
        return "";
    }

    public static void notifyGithub(String compileResult, String testResult){

    }

    public static void logToFile(String repository, String branch, String commitId, String compileResult, String testResult) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String buildDate = formatter.format(date);
        JSONObject obj = new JSONObject();

        obj.put("repository", repository);
        obj.put("branch", branch);
        obj.put("commitId", commitId);
        obj.put("buildDate", buildDate);
        obj.put("compileResult ", compileResult);
        obj.put("testResult", testResult);

        String jsonFile = "build:" + buildDate;

        try (FileWriter file = new FileWriter(jsonFile)) {
            file.write(obj.toJSONString());
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}
