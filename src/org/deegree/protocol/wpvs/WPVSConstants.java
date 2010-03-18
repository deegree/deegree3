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

package org.deegree.protocol.wpvs;

import org.deegree.commons.tom.ows.Version;

/**
 * Important constants from the WPVS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPVSConstants {

    /** Namespace for elements from the WPVS specification */
    public static final String WPVS_NS = "http://www.opengis.net/wpvs";

    /** Common namespace prefix for elements from the WMS specification */
    public static final String WMS_PREFIX = "wpvs";

    /** WPVS protocol version 0.4.0 */
    public static final Version VERSION_040 = Version.parseVersion( "0.4.0" );

    /** WPVS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /**
     * Enum type for discriminating between the different types of WebPerspectiveView (WPVS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WPVSRequestType {
        /** Retrieve the capabilities of the service. */
        GetCapabilities,
        /** Retrieve the descriptions of one or more datasets or styles. */
        GetDescription,
        /** Retrieve a graphic containing a map legend for an identified dataset and style. */
        GetLegendGraphic,
        /** Retrieve a perspective view of a certain point-of-interest. */
        GetView,
    }
}
