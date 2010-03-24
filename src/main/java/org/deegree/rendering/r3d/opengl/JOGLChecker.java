//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.rendering.r3d.opengl;

import javax.media.opengl.GLCanvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that contains a method for checking that JOGL is installed correctly.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class JOGLChecker {

    private static final Logger LOG = LoggerFactory.getLogger( JOGLChecker.class );

    /**
     * Checks that the JOGL native libraries are available on the system.
     * <p>
     * If the libraries are not available, are corresponding message (with help information) is logged and an
     * {@link UnsatisfiedLinkError} is thrown.
     *
     * @throws UnsatisfiedLinkError
     *             if the native libraries are not available
     */
    public static void check()
                            throws UnsatisfiedLinkError {

        try {
            // check that JOGL's native libraries are available
            new GLCanvas();
        } catch ( UnsatisfiedLinkError e ) {
            StringBuilder sb = new StringBuilder( "Cannot initialize JOGL (Java OpenGL bindings) -- " );
            sb.append( "the native JOGL libraries cannot be loaded." );

            String os = System.getProperty( "os.name" );
            if ( os != null ) {
                os = os.toLowerCase().trim();
                if ( os.contains( "win" ) ) {
                    sb.append( " Hint: set your system's PATH environment variable to include the directory that contains gluegen-rt.dll, jogl_awt.dll jogl_cg.dll and jogl.dll." );
                } else {
                    sb.append( " Hint: set your systems's LD_LIBRARY_PATH environment variable to include the directory that contains libgluegen-rt.so, libjogl_awt.so, libjogl_cg.so, libjogl.so. This can be achieved by exporting the LD_LIBRARY_PATH (export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:/path/to/jogl/libs/)." );
                }
            }
            sb.append( " See http://wiki.deegree.org/deegreeWiki/deegree3/jogl for more information." );

            String msg = sb.toString();
            LOG.error( msg );
            throw new UnsatisfiedLinkError( msg );
        }
    }
}
