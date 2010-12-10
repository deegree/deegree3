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

import static org.apache.commons.io.FileUtils.copyFile;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * @goal copy
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CopyMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private Copy[] files;

    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if ( files == null ) {
            log.debug( "No files configured." );
            return;
        }

        File basedir = project.getBasedir();
        for ( Copy copy : files ) {
            log.info( "Copy " + copy.from + " to " + copy.to );
            File from = new File( basedir, copy.from );
            File to = new File( basedir, copy.to );
            if ( !to.getParentFile().mkdirs() ) {
                log.warn( "Could not create parent directories for " + to + "." );
                continue;
            }
            try {
                copyFile( from, to );
            } catch ( IOException e ) {
                log.warn( "Could not copy " + copy.from + " to " + copy.to + ": " + e.getLocalizedMessage() );
                log.debug( e );
            }
        }
    }

    /**
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class Copy {
        /**
         * @parameter
         */
        String from;

        /**
         * @parameter
         */
        String to;

        @Override
        public String toString() {
            return from + " -> " + to;
        }
    }

}
