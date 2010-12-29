/*
Copyright 2010 Jarl Haggerty

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
       
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.curious.hiss;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.python.util.PythonInterpreter;

import org.apache.maven.artifact.Artifact;

/**
 * Goal for executing tests written in Jython.
 * @goal test
 */
public class JythonTestMojo extends JythonAbstractMojo{ 
    public void execute() throws MojoExecutionException, MojoFailureException{
        Properties properties = new Properties();
        if(pythonPath == null){
            pythonPath = "";
        }

        properties.setProperty("python.path", baseDirectory + "/src/main/jython" + File.pathSeparator + pythonPath);
            
        if(pythonArgs == null){
            pythonArgs = new String[]{};
        }

        PythonInterpreter.initialize(System.getProperties(), properties, pythonArgs);

        PythonInterpreter interpreter = new PythonInterpreter();
        //interpreter.setIn(System.in);
        interpreter.setOut(System.out);
        interpreter.setErr(System.err);

        runTests(new File(baseDirectory + "/src/test/jython/" + groupId.replace(".", "/") + "/" + artifactId), interpreter);
    }

    public void runTests(File directory, PythonInterpreter interpreter){
        File[] files = directory.listFiles();
        for(int i = 0;i < files.length;i++){
            File file = files[i];
            if(file.isDirectory()){
                runTests(file, interpreter);
            }else if(file.getName().substring(file.getName().length()-3).equals(".py")){
                try{
                    System.out.println("\nRunning tests in " + file.getAbsolutePath().replace(baseDirectory + "/src/test/jython/", "").replace(".py", "").replace(File.separator, ".") + "\n");
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String source = "", line;
                    while((line = reader.readLine()) != null){
                        source += line + "\n";
                    }
                    reader.close();
                    interpreter.exec(source);
                    Matcher matcher = Pattern.compile("def\\s+(test.+\\(\\)):[\r|\n|\r\n]").matcher(source);
                    while(matcher.find()){
                        String input = "";
                        input += "try:\n";
                        input += "    " + matcher.group(1) + "\n";
                        input += "except AssertionError:\n";
                        input += "    print \"    " + matcher.group(1).replace("()", "") + " failed\\n\"\n";
                        interpreter.exec(input);
                    }
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }               
}
