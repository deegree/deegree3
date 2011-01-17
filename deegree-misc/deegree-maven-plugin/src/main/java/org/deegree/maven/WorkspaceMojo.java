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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.h2.util.IOUtils.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;

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

    private void zip( File f, ZipOutputStream out, URI workspace )
                            throws IOException {
        if ( f.getName().equalsIgnoreCase( ".svn" ) ) {
            return;
        }
        String name = workspace.relativize( f.getAbsoluteFile().toURI() ).toString();
        if ( f.isDirectory() ) {
            ZipEntry e = new ZipEntry( name );
            out.putNextEntry( e );
            File[] fs = f.listFiles();
            if ( fs != null ) {
                for ( File f2 : fs ) {
                    zip( f2, out, workspace );
                }
            }
        } else {
            ZipEntry e = new ZipEntry( name );
            out.putNextEntry( e );
            InputStream is = null;
            try {
                is = new FileInputStream( f );
                copy( is, out );
            } finally {
                closeQuietly( is );
            }
        }
    }

    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        File dir = new File( project.getBasedir(), "src/main/webapp/WEB-INF/workspace" );
        if ( !dir.isDirectory() ) {
            dir = new File( project.getBasedir(), "src/main/webapp/WEB-INF/conf" );
        }
        ZipOutputStream out = null;
        try {
            File target = new File( project.getBasedir(), "target" );
            if ( !target.exists() && !target.mkdirs() ) {
                throw new MojoFailureException( "Could not create target directory!" );
            }
            File workspaceFile = new File( project.getBasedir(), "target/" + project.getArtifactId() + "-"
                                                                 + project.getVersion() + "-workspace.zip" );
            OutputStream os = new FileOutputStream( workspaceFile );
            out = new ZipOutputStream( os );

            zip( dir, out, dir.getAbsoluteFile().toURI() );
            log.info( "Attaching " + workspaceFile );
            Artifact artifact = new AttachedArtifact( project.getArtifact(), "zip", "workspace",
                                                      new DefaultArtifactHandler( "zip" ) );
            artifact.setFile( workspaceFile );
            artifact.setResolved( true );
            project.addAttachedArtifact( artifact );
        } catch ( IOException e ) {
            log.debug( e );
            throw new MojoFailureException( "Could not create workspace zip artifact: " + e.getLocalizedMessage() );
        } finally {
            closeQuietly( out );
        }
    }

}
