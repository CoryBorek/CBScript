package com.agentdid127.CBScript.impl;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;


public class PomCompiler {

    private Path workingDir;

    /**
     * PomCompiler class.
     * @param workingDir Directory for project.
     */
    public PomCompiler (Path workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Compiles .cbpom file as a pom.xml
     * @throws IOException
     */
    public void run() throws IOException {

        //Read .cbpom as a YAML File
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(workingDir.resolve("project.cbpom").toFile());
        Map<String,Object> pom = yaml.load(inputStream);

        //Checks if pom is legit, if so makes it a project
        if (pom.containsKey("project")) {

            //Output the project tag
            String out = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "xmlns:xsi=\"http://w3.org/2001/XMLSchema-instance\"\n" +
                    "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "<modelVersion>4.0.0</modelVersion>\n";

            //Get Project as object
            Map<String,Object> project = (Map<String, Object>) pom.get("project");

            //Group, Artifact, and Version
            if (project.containsKey("groupId")) out += "<groupId>" + project.get("groupId") + "</groupId>\n";
            if (project.containsKey("artifactId")) out += "<artifactId>" + project.get("artifactId") + "</artifactId>\n";
            if (project.containsKey("version")) out += "<version>" + project.get("version") + "</version>\n";

            //Packages as a Jar (no poms yet.)
            out += "<packaging>jar</packaging>\n";

            //Gets Properties
            if (project.containsKey("properties")) {
                out += "<properties>\n";
                ArrayList<Map<String, Object>> properties = (ArrayList<Map<String, Object>>) project.get("properties");

                //Grabs Properties
                for (Map<String, Object> property : properties) {
                    //Adds Property
                    if (property.containsKey("item") && property.containsKey("info")) out += "<" + property.get("item") + ">" + property.get("info") + "</" + property.get("item") + ">\n";
                }
                out += "</properties>\n";

            }

            //Repositories
            if (project.containsKey("repositories")) {
                out += "<repositories>\n";

                ArrayList<Map<String, Object>> repositories = (ArrayList<Map<String, Object>>) project.get("repositories");
                //Adds Each Repo
                for (Map<String, Object> repository : repositories) {
                    out += "<repository>\n";
                    //Id and URLs
                    if (repository.containsKey("id")) out += "<id>" + repository.get("id") + "</id>\n";
                    if (repository.containsKey("url")) out += "<url>" + repository.get("url") + "</url>\n";
                    out += "</repository>\n";
                }
                out += "</repositories>\n";
            }

            if (project.containsKey("dependencies")) {
                out += "<dependencies>\n";
                ArrayList<Map<String, Object>> dependencies = (ArrayList<Map<String, Object>>) project.get("dependencies");
                for (Map<String, Object> dependency : dependencies) {
                    out += "<dependency>\n";
                    //Dependency information
                    if (dependency.containsKey("groupId")) out += "<groupId>" + dependency.get("groupId") + "</groupId>\n";
                    if (dependency.containsKey("artifactId")) out += "<artifactId>" + dependency.get("artifactId") + "</artifactId>\n";
                    if (dependency.containsKey("version")) out += "<version>" + dependency.get("version") + "</version>\n";
                    if (dependency.containsKey("scope")) out += "<scope>" + dependency.get("scope") + "</scope>\n";
                    out += "</dependency>\n";
                }
                out += "</dependencies>\n";
            }

            //Build information
            if (project.containsKey("build")) {
                out += "<build>\n";
                Map<String, Object> build = (Map<String, Object>) project.get("build");
                //Gets goal, and name
                if (build.containsKey("defaultGoal")) out += "<defaultGoal>" + build.get("defaultGoal") + "</defaultGoal>\n";
                if (build.containsKey("finalName")) out += "<finalName>" + build.get("finalName") + "</finalName>\n";
                //Main Class determines plugins
                if (build.containsKey("mainClass"))
                    out += "<plugins>\n" +
                        "<plugin>\n" +
                        "<groupId>org.apache.maven.plugins</groupId>\n" +
                        "<artifactId>maven-compiler-plugin</artifactId>\n" +
                        "<version>2.3.2</version>\n" +
                        "<configuration>\n" +
                        "<source>8</source>\n" +
                        "<target>8</target>\n" +
                        "</configuration>\n" +
                        "</plugin>\n" +
                        "<plugin>\n" +
                        "<groupId>org.apache.maven.plugins</groupId>\n" +
                        "<artifactId>maven-shade-plugin</artifactId>\n" +
                        "<version>2.4</version>\n" +
                        "<executions>\n" +
                        "<execution>\n" +
                        "<phase>package</phase>\n" +
                        "<goals>\n" +
                        "<goal>shade</goal>\n" +
                        "</goals>\n" +
                        "<configuration>\n" +
                        "<minimizeJar>false</minimizeJar>\n" +
                        "<filters>\n" +
                        "<filter>\n" +
                        "<artifact>*:*</artifact>\n" +
                        "<excludes>\n" +
                        "<exclude>META-INF/*.SF</exclude>\n" +
                        "<exclude>META-INF/*.DSA</exclude>\n" +
                        "<exclude>META-INF/*.RSA</exclude>\n" +
                        "</excludes>\n" +
                        "</filter>\n" +
                        "</filters>\n" +
                        "</configuration>\n" +
                        "</execution>\n" +
                        "</executions>\n" +
                        "</plugin>\n" +
                        "<plugin>\n" +
                        "<groupId>org.apache.maven.plugins</groupId>\n" +
                        "<artifactId>maven-jar-plugin</artifactId>\n" +
                        "<version>2.4</version>\n" +
                        "<configuration>\n" +
                        "<archive>\n" +
                        "<manifest>\n" +
                        "<mainClass>" + build.get("mainClass") + "</mainClass>\n" +
                        "</manifest>\n" +
                        "</archive>\n" +
                        "</configuration>\n" +
                        "</plugin>\n" +
                        "</plugins>";
                out += "</build>\n";
            }
            out += "</project>";


            //Saves the file.
            byte[] outbytes = out.getBytes(StandardCharsets.UTF_8);
            inputStream.close();
            FileOutputStream fos = new FileOutputStream(workingDir.resolve("pom.xml").toFile());
            fos.write(outbytes);
            Files.delete(workingDir.resolve("project.cbpom"));
        }
        else System.out.println("File did not find project. Please Check your Syntax");
    }
}
