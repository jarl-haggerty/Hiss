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

import java.io.IOException;

import org.python.util.PythonInterpreter;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.ClassClassPath;
import javassist.ClassPath;

import org.apache.maven.artifact.Artifact;

/**
 * The class from which all other goals inherit.
 */
public abstract class JythonAbstractMojo extends AbstractMojo{ 

    /**
     * @parameter default-value="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List<Artifact> pluginArftifacts;

    /**
     * @parameter default-value="${basedir}"
     * @required
     * @readonly
     */
    protected String baseDirectory;

    /**
     * @parameter expression="${python.main}"
     */
    protected String pythonMain;

    /**
     * @parameter default-value="${project.groupId}"
     * @required
     * @readonly
     */
    protected String groupId;

    /**
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    protected String artifactId;

    /**
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     */
    protected String version;

    /**
     * @parameter expression="${python.path}"
     */
    protected String pythonPath;

    /**
     * @parameter default-value="${python.args}"
     */
    protected String[] pythonArgs;

    public void addPythonPath(String pythonPath){
        for(String place : pythonPath.split(File.pathSeparator)){
            File from = new File(place);
            try{
                copyDirectory(from, new File(from.getName()));
                Runtime.getRuntime().exec("jar uf " + baseDirectory + "/target/" + artifactId + "-" + version + ".jar " + from.getName()).waitFor();
                deleteDirectory(new File(from.getName()));
            }catch(IOException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void createMain(String pythonPath, String pythonMain){
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new ClassClassPath(PythonInterpreter.class));
            CtClass mainClass = pool.makeClass("Main");

            String mainSource = "";
            mainSource += "public static void main(String[] args){\n";
            mainSource += "    String pythonPath = System.getProperty(\"python.path\");\n";
            mainSource += "    if(pythonPath == null){\n";
            mainSource += "        pythonPath = \"\";\n";
            mainSource += "    }\n";

            mainSource += "    String jar = ClassLoader.getSystemResource(\"Main.class\").toString().replaceAll(\"jar:file:/|/Main.class|!\", \"\");\n";
            mainSource += "    pythonPath += java.io.File.pathSeparator + jar;\n";
            for(String place : pythonPath.split(File.pathSeparator)){
                if(!place.isEmpty()){
            mainSource += "    pythonPath += java.io.File.pathSeparator + jar + \"/" + new File(place).getName() + "\";\n";
                }
            }

            mainSource += "    java.util.Properties properties = new java.util.Properties();\n";
            mainSource += "    properties.setProperty(\"python.path\", pythonPath);\n";
            mainSource += "    org.python.util.PythonInterpreter.initialize(System.getProperties(), properties, args);\n";

            mainSource += "    org.python.util.PythonInterpreter interpreter = new org.python.util.PythonInterpreter();\n";
            mainSource += "    interpreter.setIn(System.in);\n";
            mainSource += "    interpreter.setOut(System.out);\n";
            mainSource += "    interpreter.setOut(System.err);\n";
            mainSource += "    interpreter.execfile(ClassLoader.getSystemResourceAsStream(\"" + pythonMain.replace(".", "/") + ".py\"));\n";
            mainSource += "}\n";

            CtMethod mainMethod = CtNewMethod.make(mainSource, mainClass);
            mainClass.addMethod(mainMethod);
            byte[] b = mainClass.toBytecode();
            FileOutputStream writer = new FileOutputStream("Main.class");
            writer.write(b);
            writer.close();
            FileWriter manifestWriter = new FileWriter("manifest.txt");
            manifestWriter.write("Main-Class: Main\n");
            manifestWriter.close();
            FileWriter runWriter = new FileWriter("__run__.py");
            runWriter.write("import " + pythonMain + "\n");
            runWriter.close();
            Runtime.getRuntime().exec("jar ufm " + baseDirectory + "/target/" + artifactId + "-" + version + ".jar manifest.txt Main.class __run__.py").waitFor();
            new File("Main.class").delete();
            new File("manifest.txt").delete();
            new File("__run__.py").delete();
        }catch(IOException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(CannotCompileException e) {
            e.printStackTrace();
        }
    }

    public void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                              new File(targetLocation, children[i]));
            }
        }else{
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
            path.delete();
        }
    }
}
