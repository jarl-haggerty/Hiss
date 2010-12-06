/**
* Copyright 2006 - 2007 Servprise International, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
