//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-misc/deegree-maven-plugin/src/main/java/org/deegree/maven/EclipseProjectLinker.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal eclipse
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 31419 $, $Date: 2011-08-02 17:42:17 +0200 (Di, 02. Aug 2011) $
 */
public class EclipseMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {

        File target = project.getBasedir();

        mergePrefs( "org.eclipse.jdt.core.prefs" );
        mergePrefs( "org.eclipse.jdt.ui.prefs" );
        mergePrefs( "org.eclipse.wst.validation.prefs" );
        mergePrefs( "org.eclipse.wst.xml.core.prefs" );

        File src = new File( target, "src/eclipse" );
        if ( src.exists() || src.isDirectory() ) {
            File[] prefs = src.listFiles( new FileFilter() {
                @Override
                public boolean accept( File file ) {
                    return file.getName().endsWith( ".prefs" );
                }
            } );
            for ( File file : prefs ) {
                merge (file);
            }
        }
    }

    private void merge( File src ) throws MojoExecutionException {
        File target = new File( project.getBasedir(), ".settings/" + src.getName() );
        getLog().info( "Merging: .settings/" + src.getName() );
        target.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
            Properties props = new Properties();
            if ( target.exists() ) {
                props.load( in = new FileInputStream( target ) );
                in.close();
            }

            Properties fmt = new Properties();
            fmt.load( in = new FileInputStream( src ) );
            in.close();

            props.putAll( fmt );
            props.store( out = new FileOutputStream( target ), null );
        } catch ( IOException e ) {
            throw new MojoExecutionException( "Error merging project prefs to '" + target + "':" + e.getMessage(), e );
        } finally {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }        
    }

    private void mergePrefs( String file )
                            throws MojoExecutionException {

        File prefs = new File( project.getBasedir(), ".settings/" + file );
        getLog().info( "Merging: .settings/" + file );
        prefs.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
            Properties props = new Properties();
            if ( prefs.exists() ) {
                props.load( in = new FileInputStream( prefs ) );
                in.close();
            }

            Properties fmt = new Properties();
            fmt.load( in = EclipseMojo.class.getResourceAsStream( "/eclipse/" + file ) );
            in.close();

            props.putAll( fmt );

            props.store( out = new FileOutputStream( prefs ), null );
        } catch ( IOException e ) {
            throw new MojoExecutionException( "Error merging global prefs to '" + prefs + "':" + e.getMessage(), e );
        } finally {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }
    }
}
