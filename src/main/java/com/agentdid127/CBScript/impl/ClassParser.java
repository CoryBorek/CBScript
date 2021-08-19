package com.agentdid127.CBScript.impl;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class ClassParser {

    private final Path dir;

    /**
     * ClassParser class, Turns .cbscript files to .java ones
     * @param dir Directory or file to parse.
     */
    public ClassParser(Path dir) {
        this.dir = dir;
    }

    /**
     * Runs the thing
     * @throws IOException
     */
    public void run() throws IOException {
        //If it's a directory, check all files in that,
        if (dir.toFile().isDirectory())
        findFiles(dir);
        //Else just compile one file.
        else compileFile(dir);
    }

    /**
     * Finds Files within a directory, and compiles them
     * @param find Directory to check
     * @throws IOException
     */
    public void findFiles(Path find) throws IOException {
        //Directory to search
        File directory = find.toFile();

        //If it's a directory, search, and run.
        if (directory.isDirectory()) {
            //Get list of files in directory
            File[] fList = directory.listFiles();

            //Search through all files
            assert fList != null;
            for (File file : fList) {
                //If it's a directory, search through that too
                if (file.isDirectory()) {
                    findFiles(file.toPath());
                }
                //If it's a regular file, compile it.
                else {
                    compileFile(file.toPath());
                }
            }
        }
    }

    /**
     * Compiles a file
     * @param path Path to file
     * @throws IOException
     */
    private void compileFile(Path path) throws IOException {
        //If it's a CBScript file, compile
        if (path.toFile().getName().endsWith(".cbscript")) {
            //Read CBScript as YAML
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path.toFile());
            Map<String,Object> file = yaml.load(inputStream);

            //Final text
            String out = "";


            //If it's in a package,
            if (file.containsKey("package")) out += "package " + file.get("package") + ";\n";
            //Get imports
            if (file.containsKey("import")) {
                ArrayList<Map<String, Object>> imports = (ArrayList<Map<String, Object>>) file.get("import");
                for (Map<String, Object> item : imports) {
                    if (item.containsKey("item")) out += "import " + item.get("item") + ";\n";
                }
            }

            //Actual Class file
            if (file.containsKey("class")) {
                Map<String, Object> classn = (Map<String, Object>) file.get("class");
                //Gets type of class and name.
                if (classn.containsKey("setup")) {
                    String[] setup = classn.get("setup").toString().split(";;");
                    out += setup[0] + "class" + setup[1] + " {\n";
                }
                else return;

                //Gets instance variables
                if (classn.containsKey("vars")) {
                    ArrayList<Map<String, Object>> vars = (ArrayList<Map<String, Object>>) classn.get("vars");

                    //Runs per variable
                    for (Map<String, Object> var : vars) {

                        //If it's a variable
                        if (var.containsKey("var")) {
                            //Declare Variable
                            out += var.get("var").toString().replace(" ;; ", " ");
                            //If if has data
                            if (var.containsKey("data")) {
                                String varType = var.get("var").toString().split(" ;; ")[0].split(" ")[var.get("var").toString().split(" ;; ")[0].split(" ").length-1];
                                out += " = ";
                                System.out.println(varType);
                                //Get type of Data, and output it as such
                                if (var.get("data").toString().startsWith(";;")) out += var.get("data").toString().replace(";;", "");
                                else if (isBoolean(var.get("data").toString()) || isNumber(var.get("data").toString())) out += var.get("data");
                                else if (var.get("data").toString().startsWith("new ")) out += var.get("data");
                                else if (varType.equals("char")) out += "\'" + var.get("data") + "\'";
                                else out += "\"" + var.get("data") + "\"";
                            }
                            out += ";\n";
                        }
                    }
                }
                //Run functions, the actual code of the program
                if (classn.containsKey("functions")) {
                    //Get list of functions and run the,,
                    ArrayList<Map<String, Object>> functions = (ArrayList<Map<String, Object>>) classn.get("functions");
                    for (Map<String,Object> function : functions) {
                        //If it actually is a function,run
                        if (function.containsKey("function")) {
                            //Declare function and arguments
                            out += function.get("function") + "(";
                            if (function.containsKey("args")) out += function.get("args");
                            out += ") {\n";
                            //Write the juicy parts of the function
                            if (function.containsKey("items")) {
                                ArrayList<Map<String, Object>> items = (ArrayList<Map<String, Object>>) function.get("items");
                                //Write the juice in another area, as it can be reused
                                out = runFunction(out, items);
                            }
                            out += "}\n";
                        }
                    }
                }
                out += "}\n";
                //Save file
                byte[] outbytes = out.getBytes(StandardCharsets.UTF_8);
                inputStream.close();
                FileOutputStream fos = new FileOutputStream(path.getParent().resolve(path.toFile().getName().replace(".cbscript", ".java")).toFile());
                fos.write(outbytes);
                Files.delete(path);
            }
        }
        else return;
    }


    /**
     * Runs a function
     * @param out String to add to
     * @param items List of items in the function
     * @return
     */
    private String runFunction(String out, ArrayList<Map<String,Object>> items) {
        //Runs every item in the function
        for (Map<String, Object> item : items) {

            //Variables
            if (item.containsKey("var")) {
                //Initial Variable
                out += item.get("var").toString().replace(" ;; ", " ");
                //Gets data of variables
                if (item.containsKey("data")) {
                    //Get type of variable
                    String varType = item.get("var").toString().split(" ;; ")[0].split(" ")[item.get("var").toString().split(" ;; ")[0].split(" ").length-1];
                    out += " = ";

                    //Parses output as type
                    if (item.get("data").toString().startsWith(";;")) out += item.get("data").toString().replace(";;", "");
                    else if (isBoolean(item.get("data").toString()) || isNumber(item.get("data").toString())) out += item.get("data");
                    else if (item.get("data").toString().startsWith("new ")) out += item.get("data");
                    else if (varType.equals("char")) out += "\'" + item.get("data") + "\'";
                    else out += "\"" + item.get("data") + "\"";
                }
                out += ";\n";
            }
            //For loop
            else if (item.containsKey("for")) {
                //Arguments
                out += "for (" + item.get("for") + ") {\n";
                //Items, runs as function
                if (item.containsKey("items")) {
                    ArrayList<Map<String, Object>> forItems = (ArrayList<Map<String, Object>>) item.get("items");
                    out = runFunction(out, forItems);
                }
                out += "}\n";
            }
            //While loop
            else if (item.containsKey("while")) {
                //Arguments
                out += "while (" + item.get("while") + ") {";
                //Items, Runs as function
                if (item.containsKey("items")) {
                    ArrayList<Map<String, Object>> forItems = (ArrayList<Map<String, Object>>) item.get("items");
                    out = runFunction(out, forItems);
                }
                out += "}\n";
            }
            //If Statement, Runs as function
            else if (item.containsKey("if")) {
                out += "if (" + item.get("if") + ") {";
                if (item.containsKey("items")) {
                    ArrayList<Map<String, Object>> forItems = (ArrayList<Map<String, Object>>) item.get("items");
                    out = runFunction(out, forItems);
                }
                out += "}\n";
            }
            //Run a function;
            else if (item.containsKey("runfn")) {
                out += item.get("runfn") + "(";
                if (item.containsKey("args")) {
                    String[] args = item.get("args").toString().split(",");
                    String argout = "";
                    for (String arg : args) {
                        if (arg.startsWith(";;")) argout += arg.replace(";;", "");
                        else if (isBoolean(arg) || isNumber(arg)) {
                            argout += arg;
                        } else if (arg.startsWith("new")) {
                            argout += arg;
                        } else argout += "\"" + arg + "\"";
                        argout += ", ";
                    }
                    if (argout.length() >= 2) argout = argout.substring(0, argout.length() - ", ".length());
                    out += argout;
                }
                out += ");\n";
            }

        }
    return out;
    }

    /**
     * Checks if a String is a number
     * @param item
     * @return
     */
    private boolean isNumber(String item) {
        return item.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Checks if a string is a boolean
     * @param item
     * @return
     */
    private boolean isBoolean(String item) {
            if (item.equals("true") || item.equals("false"))
            return true;
            else return false;
    }
}
