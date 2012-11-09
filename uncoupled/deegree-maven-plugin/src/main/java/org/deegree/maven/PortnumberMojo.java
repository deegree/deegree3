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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.channels.FileLock;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal generate-portnumber
 * @phase initialize
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PortnumberMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        PrintStream out = null;
        BufferedReader reader = null;
        FileLock lock = null;
        FileInputStream fis = null;
        try {
            File portfile = new File( System.getProperty( "java.io.tmpdir" ) + "/portnumbers" );
            int port = 1025;
            if ( portfile.exists() ) {
                fis = new FileInputStream( portfile );
                reader = new BufferedReader( new InputStreamReader( fis ) );

                boolean read = false;
                while ( !read ) {
                    try {
                        port = Integer.parseInt( reader.readLine() ) + 1;
                        read = true;
                    } catch ( NumberFormatException e ) {
                        // someone is currently writing
                        try {
                            Thread.sleep( 100 );
                        } catch ( InterruptedException e1 ) {
                            return;
                        } finally {
                            closeQuietly( out );
                            closeQuietly( reader );
                            closeQuietly( fis );
                        }
                    }
                }
                reader.close();
            }
            if ( port > 18000 ) {
                port = 1025;
            }

            getLog().info( "Using portnumber " + port + " for this run." );

            project.getProperties().put( "portnumber", "" + port );

            FileOutputStream fos = new FileOutputStream( portfile );
            out = new PrintStream( fos );
            while ( ( lock = fos.getChannel().tryLock() ) == null ) {
                try {
                    Thread.sleep( 100 );
                } catch ( InterruptedException e ) {
                    return;
                }
            }
            out.println( "" + port );
            lock.release();
            out.close();
        } catch ( Throwable e ) {
            getLog().error( e );
        } finally {
            closeQuietly( out );
            closeQuietly( reader );
            closeQuietly( fis );
        }
    }
}
