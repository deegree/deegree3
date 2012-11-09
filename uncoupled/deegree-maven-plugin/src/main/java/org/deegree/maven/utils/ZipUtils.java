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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.maven.utils;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.deegree.commons.utils.io.Zip.unzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

/**
 * Utilities to zip workspace parts.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class ZipUtils {

    public static void zipModules( Set<?> jarDeps, HashSet<String> visitedFiles, ZipOutputStream out ) {
        if ( !jarDeps.isEmpty() && !visitedFiles.contains( "modules" ) && !visitedFiles.contains( "modules/" ) ) {
            ZipEntry e = new ZipEntry( "modules/" );
            try {
                out.putNextEntry( e );
                out.closeEntry();
            } catch ( Throwable ex ) {
                // probably duplicate entry
            }
            visitedFiles.add( "modules/" );
        }
    }

    public static void zipJarDependencies( Set<?> jarDeps, Log log, HashSet<String> visitedFiles, ZipOutputStream out ) {
        for ( Object o : jarDeps ) {
            Artifact a = (Artifact) o;
            if ( a.getScope() == null || a.getScope().equalsIgnoreCase( "runtime" )
                 || a.getScope().equalsIgnoreCase( "compile" ) ) {
                log.info( "Adding " + a + " to workspace modules directory." );
                ZipEntry entry = new ZipEntry( "modules/" + a.getFile().getName() );
                visitedFiles.add( "modules/" + a.getFile().getName() );
                try {
                    out.putNextEntry( entry );
                    FileInputStream in = new FileInputStream( a.getFile() );
                    IOUtils.copy( in, out );
                    out.closeEntry();
                    in.close();
                } catch ( Throwable e ) {
                    // probably duplicate entry
                    continue;
                }
            }
        }
    }

    public static void integrateWorkspaces( List<Artifact> workspaces, Log log, File target, ZipOutputStream out,
                                            HashSet<String> visitedFiles )
                            throws Throwable {
        for ( Artifact a : workspaces ) {
            log.info( "Processing files in dependency " + a.getArtifactId() );
            File tmp = new File( target, a.getArtifactId() );
            FileInputStream in = new FileInputStream( a.getFile() );
            try {
                unzip( in, tmp );
                zip( tmp, out, tmp.getAbsoluteFile().toURI(), visitedFiles );
            } finally {
                closeQuietly( in );
            }
        }
    }

    public static void zip( File f, ZipOutputStream out, URI parent, Set<String> visitedFiles )
                            throws Throwable {
        if ( f.getName().equalsIgnoreCase( ".svn" ) || f.getName().equalsIgnoreCase( "CVS" ) ) {
            return;
        }

        if ( parent == null ) {
            parent = f.toURI();
        }

        String name = parent.relativize( f.getAbsoluteFile().toURI() ).toString();

        if ( f.isDirectory() ) {
            zipDirectory( name, visitedFiles, out, parent, f );
        } else {
            if ( !visitedFiles.contains( name ) ) {
                visitedFiles.add( name );
                ZipEntry e = new ZipEntry( name );
                try {
                    out.putNextEntry( e );
                } catch ( Throwable ex ) {
                    // probably duplicate entry
                }
                InputStream is = null;
                try {
                    is = new FileInputStream( f );
                    copy( is, out );
                } finally {
                    closeQuietly( is );
                }
            }
        }
    }

    private static void zipDirectory( String name, Set<String> visitedFiles, ZipOutputStream out, URI parent, File f )
                            throws Throwable {
        if ( !name.isEmpty() && !visitedFiles.contains( name ) ) {
            visitedFiles.add( name );
            ZipEntry e = new ZipEntry( name );
            try {
                out.putNextEntry( e );
            } catch ( Throwable ex ) {
                // probably duplicate entry
            }
        }
        File[] fs = f.listFiles();
        if ( fs != null ) {
            for ( File f2 : fs ) {
                zip( f2, out, parent, visitedFiles );
            }
        }
    }

}
