/*
Copyright 2010 Jarl Haggerty
This file is part of Hiss.

Hiss is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Hiss is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Hiss.  If not, see <http://www.gnu.org/licenses/>.
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
import java.io.IOException;

import org.python.util.PythonInterpreter;

import org.apache.maven.artifact.Artifact;

/**
 * Goal for creating a jar with just the Jython source files and possibly a main class and __run__.py built into it.
 * @execute phase = "test"
 * @goal test
 */
public class JythonTestMojo extends JythonAbstractMojo{ 
    public void execute() throws MojoExecutionException, MojoFailureException{
        Properties properties = new Properties();

        if(pythonPath == null){
            properties.setProperty("python.path", baseDirectory + "/src/main/jython");
        }else{
            properties.setProperty("python.path", baseDirectory + "/src/main/jython" + File.pathSeparator + pythonPath);
        }
            
        if(pythonArgs == null){
            PythonInterpreter.initialize(System.getProperties(), properties, new String[]{});
        }else{
            PythonInterpreter.initialize(System.getProperties(), properties, pythonArgs);
        }
        PythonInterpreter interpreter = new PythonInterpreter();
        //interpreter.setIn(System.in);
        interpreter.setOut(System.out);
        interpreter.setErr(System.err);

        runTests(new File(baseDirectory + "/src/test/jython/" + groupId.replace(".", "/") + "/" + artifactId + "/test"), interpreter);
    }

    public void runTests(File directory, PythonInterpreter interpreter){
        for(File file : directory.listFiles()){
            if(file.isDirectory()){
                runTests(file);
            }else if(file.getName().substring(file.getName().length()-3).equals(".py")){
                System.out.println("Running tests in " + file.getAbsolutePath().replace(baseDirectory + "/src/test/jython/", "").replace(".py", "").replace(File.separator, ""));
                interpreter.execfile(file.getAbsolutePath());
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String source = "", line;
                while((line = reader.readLine()) != null){
                    source += line + "\n";
                }
                reader.close();
                Matcher matcher = Pattern.compile("def\\s+(test.+\\(\\)):[\r|\n|\r\n]").matcher(source);
                while(matcher.find()){
                    String input = "";
                    input += "try:\n";
                    input += "    " + matcher.group() + "\n";
                    input += "except AssertionError:\n";
                    input += "    print " + matcher.group() + " failed\n";
                    interpreter.exec(input);
                }
            }
        }
    }               
}