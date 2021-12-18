package com.agentdid127.CBScript;

import com.agentdid127.CBScript.impl.ClassParser;
import com.agentdid127.CBScript.impl.PomCompiler;
import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Project {

    private Path projectPath;
    private Path mavenHome;

    /**
     * Project Class, grabs the project you are compiling
     * @param projectPath Path to project or file
     * @param mavenHome Directory where Maven is installed.
     */
    public Project(Path projectPath, Path mavenHome) {
        this.projectPath = projectPath;
        this.mavenHome = mavenHome;
    }

    /**
     * Builds the project
     * @throws IOException
     * @throws MavenInvocationException
     */
    public void build() throws IOException, MavenInvocationException {
        //Check if Project path is a directory.
        if (projectPath.toFile().isDirectory()) {
            //Get some information on the project
            File project = projectPath.toFile();
            String name = project.getName();
            Path working = Paths.get("./" +  name + "_working");

            //Checks if working directory already exists, if so, deletes it and creates a fresh one
            if (working.toFile().exists()) Util.deleteDirectoryAndContents(working);
            Util.copyDir(projectPath, working);

            //Runs PomCompiler Class
            PomCompiler pomCompiler = new PomCompiler(working);
            pomCompiler.run();

            //Moves CBScript folder to Java folder
            Path newPath = working.resolve("src" + File.separator + "main" + File.separator +  "java");
            Files.move(working.resolve("src" + File.separator + "main" + File.separator +  "cbscript"), newPath);

            //Parses and translates CBScript classes as Java ones.
            ClassParser classParser = new ClassParser(newPath);
            classParser.run();

            //If build exists, remove it.
            if (projectPath.resolve("target").toFile().exists()) Util.deleteDirectoryAndContents(projectPath.resolve("target"));

            //Creates a Maven Build
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile( working.resolve("pom.xml").toFile() );
            List<String> goals = new ArrayList<>();
            goals.add("-B");
            request.setGoals(goals);

            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(mavenHome.toFile());
            invoker.execute( request );

            //Copies project output from maven Directory to CBScript directory
            Util.copyDir(working.resolve("target"), projectPath.resolve("target"));

            //Deletes working directory
            if (working.toFile().exists()) Util.deleteDirectoryAndContents(working);
        }
        //If Project is not a directory, compile just that one.
        else {
            //Copy file to temporary position
            Files.copy(projectPath, projectPath.getParent().resolve(projectPath.getFileName() + "_copy"));
            //Parse the original file
            new ClassParser(projectPath).run();
            //Move the copy to the original location.
            Files.move(projectPath.getParent().resolve(projectPath.getFileName() + "_copy"), projectPath);
        }
    }
}
