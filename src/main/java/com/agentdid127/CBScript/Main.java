package com.agentdid127.CBScript;

import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, MavenInvocationException {

        System.out.println("Welcome to the CBScript Compiler");

        if (args.length > 2) new Project(Paths.get(args[0]), Paths.get(args[1])).build();
        else if (args.length == 1) System.out.println("Missing Argument: Maven Directory");
        else System.out.println("Missing Argument: Run Path");

        System.out.println("Finished!");
    }
}
