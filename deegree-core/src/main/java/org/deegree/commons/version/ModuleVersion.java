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
package org.deegree.commons.version;

import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public final class ModuleVersion {

    private static final Logger LOG = LoggerFactory.getLogger( ModuleVersion.class );

    private String buildVersion;

    private String buildDate;

    private String buildBy;

    private String svnRevision;

    private String svnPath;

    /**
     * @param buildInfoPropertiesURL
     */
    public ModuleVersion( URL buildInfoPropertiesURL ) {
        try {
            // fetch build properties
            Properties buildProps = new Properties();
            buildProps.load( buildInfoPropertiesURL.openStream() );
            buildVersion = buildProps.getProperty( "build.version" );
            buildDate = buildProps.getProperty( "build.date" );
            buildBy = buildProps.getProperty( "build.by" );
            // svnRevision = buildProps.getProperty( "svn.revision" ).trim();
            // svnPath = buildProps.getProperty( "svn.path" ).trim();
        } catch ( Exception ex ) {
            LOG.error( "Error fetching version / build properties: " + ex.getMessage(), ex );
        }
    }

    /**
     * Returns the version number.
     * 
     * @return the version number
     */
    public String getVersionNumber() {
        return buildVersion;
    }

    /**
     * Returns the date string when the current build was created.
     * 
     * @return the date as String
     */
    public String getBuildDate() {
        return buildDate;
    }

    /**
     * Returns the name of the builder.
     * 
     * @return the name of the builder
     */
    public String getBuildBy() {
        return buildBy;
    }

    /**
     * @return the svn revision number and path
     */
    public String getSvnInfo() {
        return "revision " + svnRevision + " of " + svnPath;
    }

    /**
     * @return the svn revision number
     */
    public String getSvnRevision() {
        return svnRevision;
    }

    /**
     * @return the svn path
     */
    public String getSvnPath() {
        return svnPath;
    }

    @Override
    public String toString() {
        return buildVersion + " (build@" + buildDate + " by " + buildBy + ")";
    }
}
