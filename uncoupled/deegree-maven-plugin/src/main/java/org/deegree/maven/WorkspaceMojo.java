//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.maven;

import static java.util.Collections.reverse;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.maven.utils.ClasspathHelper.getDependencyArtifacts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.deegree.maven.utils.ZipUtils;

/**
 * @goal attach-workspace
 * @phase package
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WorkspaceMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * 
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * 
     * @component
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * 
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;


    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        File dir = determineWorkspaceDirectory();
        ZipOutputStream out = null;
        try {
            Set<?> artifacts = getDependencyArtifacts( project, artifactResolver, artifactFactory, metadataSource,
                                                       localRepository, "deegree-workspace", true );
            List<Artifact> workspaces = new ArrayList<Artifact>();
            for ( Object o : artifacts ) {
                workspaces.add( (Artifact) o );
            }
            reverse( workspaces );

            Set<?> jarDeps = getDependencyArtifacts( project, artifactResolver, artifactFactory, metadataSource,
                                                     localRepository, "jar", false );

            File target = new File( project.getBasedir(), "target" );
            if ( !target.exists() && !target.mkdirs() ) {
                throw new MojoFailureException( "Could not create target directory!" );
            }
            File workspaceFile = new File( project.getBasedir(), "target/" + project.getArtifactId() + "-"
                                                                 + project.getVersion() + ".deegree-workspace" );
            OutputStream os = new FileOutputStream( workspaceFile );
            out = new ZipOutputStream( os );

            HashSet<String> visitedFiles = new HashSet<String>();
            ZipUtils.zip( dir, out, dir.getAbsoluteFile().toURI(), visitedFiles );

            ZipUtils.zipModules( jarDeps, visitedFiles, out );
            ZipUtils.zipJarDependencies( jarDeps, log, visitedFiles, out );

            ZipUtils.integrateWorkspaces( workspaces, log, target, out, visitedFiles );

            attachArtifact( log, workspaceFile );
        } catch ( Throwable e ) {
            log.debug( e );
            throw new MojoFailureException( "Could not create workspace zip artifact: " + e.getLocalizedMessage() );
        } finally {
            closeQuietly( out );
        }
    }

    private void attachArtifact( Log log, File workspaceFile ) {
        log.info( "Attaching " + workspaceFile );
        Artifact artifact = project.getArtifact();
        if ( artifact.getType() == null || !artifact.getType().equals( "deegree-workspace" ) ) {
            DefaultArtifactHandler defHandler = new DefaultArtifactHandler( "deegree-workspace" );
            artifact = new AttachedArtifact( project.getArtifact(), "deegree-workspace", defHandler );
        }

        artifact.setFile( workspaceFile );
        artifact.setResolved( true );
        if ( project.getArtifact().getType() == null || !project.getArtifact().getType().equals( "deegree-workspace" ) ) {
            project.addAttachedArtifact( artifact );
        }
    }

    private File determineWorkspaceDirectory() {
        File dir = new File( project.getBasedir(), "src/main/webapp/WEB-INF/workspace" );
        if ( !dir.isDirectory() ) {
            dir = new File( project.getBasedir(), "src/main/webapp/WEB-INF/conf" );
        }
        if ( !dir.isDirectory() ) {
            dir = new File( project.getBasedir(), "src/main/workspace" );
        }
        return dir;
    }


}
