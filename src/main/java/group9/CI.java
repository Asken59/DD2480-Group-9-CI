package group9;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

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
        cloneRepo("git@github.com:Asken59/DD2480-Group-9-CI.git");
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

    public static String compileProject(String projectPath){
        return "";
    }

    public static String testProject(String projectPath){
        return "";
    }

    public static void notifyGithub(String compileResult, String testResult){

    }

    public static void logToFile(String compileResult, String testResult){

    }
}
