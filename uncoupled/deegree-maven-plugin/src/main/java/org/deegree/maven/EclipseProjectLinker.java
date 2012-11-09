//$HeadURL$
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
 * @goal create-links
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class EclipseProjectLinker extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {

        String eclipseWorkspace = System.getProperty( "eclipse.workspace" );
        String formatter = System.getProperty( "eclipse.formatter" );
        String linkJunction = System.getProperty( "eclipse.junction" );

        if ( eclipseWorkspace == null ) {
            throw new MojoExecutionException( "Need property 'eclipse.workspace'." );
        }
        File f = new File( eclipseWorkspace );
        if ( !f.isDirectory() ) {
            throw new MojoExecutionException( "Property 'eclipse.workspace' must point to a directory." );
        }

        File target = project.getBasedir();
        String cmd = "ln -sf -t " + eclipseWorkspace + " " + target;

        // if user wants to use mike ruscovichs junction.exe let him
        if ( linkJunction != null ) {
            File junctionExe = new File( linkJunction );
            if ( junctionExe.isFile() )
                cmd = linkJunction + " " + eclipseWorkspace + "\\" + target.getName() + " " + target;
            else {
                getLog().info( "using junction.exe from path ( junction.exe is available from http://technet.microsoft.com/en-gb/sysinternals/bb896768 )" );
                cmd = "junction.exe " + eclipseWorkspace + "\\" + target.getName() + " " + target;
            }
        }
        getLog().info( "*** CMD: " + cmd );

        try {
            Runtime.getRuntime().exec( cmd );
        } catch ( IOException e ) {
            throw new MojoExecutionException( "Unable to execute cmd: " + cmd );
        }

        if ( formatter != null ) {
            File prefs = new File( project.getBasedir(), ".settings/org.eclipse.jdt.core.prefs" );
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
                fmt.load( in = EclipseProjectLinker.class.getResourceAsStream( "/" + formatter
                                                                               + "_formatter.properties" ) );
                in.close();

                props.putAll( fmt );

                props.store( out = new FileOutputStream( prefs ), null );
            } catch ( IOException e ) {
                throw new MojoExecutionException( "Could not read/write eclipse preferences.", e );
            } finally {
                IOUtils.closeQuietly( in );
                IOUtils.closeQuietly( out );
            }
        }
    }

}
