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

import org.apache.maven.artifact.Artifact;

/**
 * Goal for creating a jar with the pythonPath and jython built into it.
 * @execute phase = "package"
 * @goal uberjar
 */
public class JythonUberJarMojo extends JythonAbstractMojo{ 
    public void execute() throws MojoExecutionException, MojoFailureException{
        if(pythonPath == null){
            pythonPath = "";
        }
            
        addPythonPath(baseDirectory + "/src/main/jython/" + groupId.substring(0, groupId.indexOf(".")) + File.pathSeparator + pythonPath);

        if(pythonMain != null){
            createMain(pythonPath, pythonMain);
        }

        Iterator<Artifact> artifactIterator = pluginArftifacts.iterator();
        while(artifactIterator.hasNext()){
            Artifact artifact = artifactIterator.next();
            if (artifact.getGroupId().equals("org.python") && artifact.getArtifactId().equals("jython")){
                try{
                    Runtime.getRuntime().exec("jar xf " + artifact.getFile().getAbsolutePath() + " org com").waitFor();
                    Runtime.getRuntime().exec("jar uf " + baseDirectory + "/target/" + artifactId + "-" + version + ".jar org").waitFor();
                    Runtime.getRuntime().exec("jar uf " + baseDirectory + "/target/" + artifactId + "-" + version + ".jar com").waitFor();
                }catch(IOException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                deleteDirectory(new File("org"));
                deleteDirectory(new File("com"));
            }
        }

        
    }
}
