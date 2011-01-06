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

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import org.python.util.PythonInterpreter;

/**
 * Goal for running the project
 * @goal run
 */
public class JythonRunMojo extends JythonAbstractMojo{ 
    public void execute() throws MojoExecutionException, MojoFailureException{ 
        Properties properties = new Properties();
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader(); 
        Class classLoaderClass = URLClassLoader.class; 
        
        try { 
            Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[]{URL.class}); 
            method.setAccessible(true); 
            method.invoke(systemClassLoader, new Object[]{new File(baseDirectory + "/src/main/resources/").toURI().toURL()}); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 

        if(pythonPath == null){
            pythonPath = "";
        }
        properties.setProperty("python.path", baseDirectory + "/src/main/jython" + File.pathSeparator + pythonPath);

        String mainFile;
        if(pythonMain == null){
            mainFile = baseDirectory + "/src/main/jython/" + artifactId + "/main.py";
            System.out.println("Python main was not specified.  Infering pythonMain as " + artifactId + ".main");
        }else{
            mainFile = baseDirectory + "/src/main/jython/" + pythonMain.replace(".", "/") + ".py";
        }
        if(!new File(mainFile).exists()){
            throw new MojoFailureException("No module named " + pythonMain);
        }

        if(pythonArgs == null){
            pythonArgs = new String[]{};
        }
        PythonInterpreter.initialize(System.getProperties(), properties, pythonArgs);

        PythonInterpreter interpreter = new PythonInterpreter();
        //interpreter.setIn(System.in);
        interpreter.setOut(System.out);
        interpreter.setErr(System.err);
        interpreter.execfile(mainFile);
    }
}
