//$HeadURL$
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
package org.deegree.framework.version;

import java.util.Properties;

import org.deegree.framework.util.BootLogger;

/**
 * The version number is created by 3 parts, the first represents the version number, the second a
 * essential update of a release, the third the build number. The version number could be numeric or
 * alphanumeric, e.g. 'Foo2' or '2.0'.<BR>
 * e.g.:<BR>
 * 2.0alpha.142 - version no. 2, release 0 alpha, build 142<BR>
 * 2.0beta.178 - version no. 2, release 0 beta, build 178 <BR>
 * 2.0.198 - version no. 2, release 0, build 198 <BR>
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 *
 * @author last edited by: $Author$
 *
 * @version 3.0 . $Revision$, $Date$
 */
public final class Version {

    private static String BUILD_DATE;

    private static String VERSION_NUMBER;

    private static String BUILD_NUMBER;

    private static String BUILD_BY;

    private static String SVN_REVISION, SVN_PATH;

    /*
     * Class loader to get version properties from resource file
     */
    static {
        try {
            // fetch version property
            Properties versionProps = new Properties();
            versionProps.load( Version.class.getResourceAsStream( "version.properties" ) );
            VERSION_NUMBER = versionProps.getProperty( "version.number" );

            // fetch build properties
            Properties buildProps = new Properties();
            buildProps.load( Version.class.getResourceAsStream( "buildId.properties" ) );
            BUILD_DATE = buildProps.getProperty( "build.date" );
            BUILD_NUMBER = buildProps.getProperty( "build.number" );
            BUILD_BY = buildProps.getProperty( "build.by" );
            SVN_REVISION = buildProps.getProperty( "svn.revision" ).trim();
            SVN_PATH = buildProps.getProperty( "svn.path" ).trim();
        } catch ( Exception ex ) {
            BootLogger.logError( "Error fetching version / build properties: " + ex.getMessage(), ex );
        }
    }

    private Version() {
        // Don't let anyone instantiate this class.
    }

    /**
     * Returns the version of the application. The version number is created by 3 parts, the first
     * represents the version number, the second a essential update of a release, the third the
     * build number. The version number could be numeric or alphanumeric, e.g. 'Foo2' or '2.0'.
     * <P>
     * e.g.:<BR>
     * 1.0.42 - version no. 1, release 0, build 42<BR>
     * 1.1.78 - version no. 1, release 1, build 78<BR>
     * 2.0.98 - version no. 2, release 0, build 98<BR>
     *
     * @return the version string
     */
    public static String getVersion() {
        final String s = getVersionNumber() + " (" + getBuildDate() + " build-" + getBuildNumber() + "-" + getBuildBy()
                         + ")";
        return s;
    }

    /**
     * Returns the version number.
     *
     * @return the version number
     */
    public static String getVersionNumber() {
        return Version.VERSION_NUMBER;
    }

    /**
     * Returns the current build number.
     *
     * @return the current build number
     */
    public static String getBuildNumber() {
        return Version.BUILD_NUMBER;
    }

    /**
     * Returns the date string when the current build was created.
     *
     * @return the date as String
     */
    public static String getBuildDate() {
        return Version.BUILD_DATE;
    }

    /**
     * Returns the name of the builder.
     *
     * @return the name of the builder
     */
    public static String getBuildBy() {
        return Version.BUILD_BY;
    }

    /**
     * @return the svn revision number and path
     */
    public static String getSvnInfo() {
        return "revision " + SVN_REVISION + " of " + SVN_PATH;
    }

    /**
     * @return the svn revision number
     */
    public static String getSvnRevision() {
        return SVN_REVISION;
    }

    /**
     * @return the svn path
     */
    public static String getSvnPath() {
        return SVN_PATH;
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        System.out.println( "deegree version: " + getVersion() + "\n" + getSvnInfo() );
    }
}
