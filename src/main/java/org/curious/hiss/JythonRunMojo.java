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

import org.python.util.PythonInterpreter;

/**
 * Goal for running the project
 * @goal run
 */
public class JythonRunMojo extends JythonAbstractMojo{ 
    public void execute() throws MojoExecutionException, MojoFailureException{ 
        Properties properties = new Properties();

        if(pythonPath == null){
            properties.setProperty("python.path", baseDirectory + "/src/main/jython");
        }else{
            properties.setProperty("python.path", baseDirectory + "/src/main/jython" + File.pathSeparator + pythonPath);
        }

        String mainFile;
        if(pythonMain == null){
            mainFile = baseDirectory + "/src/main/jython/" + groupId.replace(".", "/") + "/" + artifactId + "/main.py";
            System.out.println("Python main was not specified.  Infering pythonMain as " + groupId + "." + artifactId + ".main");
        }else{
            mainFile = baseDirectory + "/src/main/jython/" + pythonMain.replace(".", "/") + ".py";
        }

        if(!new File(mainFile).exists()){
            throw new MojoFailureException("No module named " + pythonMain);
        }
            
        if(pythonArgs == null){
            PythonInterpreter.initialize(System.getProperties(), properties, new String[]{});
        }else{
            PythonInterpreter.initialize(System.getProperties(), properties, pythonArgs);
        }
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.setIn(System.in);
        interpreter.setOut(System.out);
        interpreter.setErr(System.err);
        interpreter.execfile(mainFile);
    }
}
