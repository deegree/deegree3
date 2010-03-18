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

package org.deegree.protocol.wcs;

import org.deegree.commons.tom.ows.Version;
import org.deegree.coverage.raster.interpolation.InterpolationType;

/**
 * Important constants from the WCS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WCSConstants {

    /** Namespace for elements from the WCS 1.0.0 specification */
    public static final String WCS_100_NS = "http://www.opengis.net/wcs";

    /** Location of the schema */
    public static final String WCS_100_SCHEMA = "http://schemas.opengis.net/wcs/1.0.0/wcsCapabilities.xsd";

    /** Namespace for elements from the WCS 1.1.0 specification */
    public static final String WCS_110_NS = "http://www.opengis.net/wcs/1.1";

    /** Common namespace prefix for elements from WCS 1.0.0 specifications */
    public static final String WCS_100_PRE = "wcs_1_0_0";

    /** Common namespace prefix for elements from WCS specifications */
    public static final String WCS_110_PRE = "wcs_1_1_0";

    /** WCS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** WCS protocol version 1.1.0 */
    public static final Version VERSION_110 = Version.parseVersion( "1.1.0" );

    /** WCS 1.0.0 exception xml mime type */
    public static final String EXCEPTION_FORMAT_100 = "application/vnd.ogc.se_xml";

    /**
     * Enum type for discriminating between the different types of WebCoverageService (WCS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WCSRequestType {

        /** Describe a coverage. */
        DescribeCoverage,
        /** Retrieve the capabilities of the service. */
        GetCapabilities,
        /** Retrieve a coverage for a certain region. */
        GetCoverage
    }

    /**
     * 
     * The <code>ExeptionCode_1_0_0</code> defines the exception codes for the WCS 1.0.0 specification.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    public enum ExeptionCode_1_0_0 {
        /** the current update sequence equals the requested one */
        CurrentUpdateSequence,
        /** the current update sequence is lower than the requested one */
        InvalidUpdateSequence
    }

    /**
     * 
     * The <code>InterpolationMethod</code> class maps Interpolations to the wcs 1.0.0 protocol name.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    public enum InterpolationMethod {
        /** Interpolating nearest neighbor. */
        Nearest_Neighbor( "nearest neighbor" ),
        /** Interpolating bilinear. */
        Bilinear( "bilinear" ),
        /** Interpolating bicubic. */
        Bicubic( "bicubic" ),
        /** Interpolating lost area. */
        LostArea( "lost area" ),
        /** Interpolating barycentric. */
        Barycentric( "barycentric" ),
        /** Not Interpolating. */
        None( "none" );

        private final String name_1_0_0;

        private InterpolationMethod( String name_1_0_0 ) {
            this.name_1_0_0 = name_1_0_0;
        }

        /**
         * 
         * @param version
         *            of the suspected protocol.
         * @return the name as it is define in the spec for given protocol version.
         */
        public String getProtocolName( Version version ) {
            if ( VERSION_100.equals( version ) ) {
                return name_1_0_0;
            }
            return name();
        }

        /**
         * @param ip
         * @return an heuristically determined Interpolation type
         */
        public static InterpolationMethod map( String ip ) {
            InterpolationMethod result = None;
            if ( ip != null ) {
                String t = ip.toLowerCase();
                if ( t.contains( "neigh" ) ) {
                    result = Nearest_Neighbor;
                } else if ( t.contains( "bili" ) ) {
                    result = Bilinear;
                } else if ( t.contains( "bicu" ) ) {
                    result = Bicubic;
                } else if ( t.contains( "lost" ) ) {
                    result = LostArea;
                } else if ( t.contains( "bary" ) ) {
                    result = Barycentric;
                }
            }
            return result;
        }

        /**
         * @param type
         * @return maps the Raster-API {@link InterpolationType} to the WCS {@link InterpolationMethod}.
         */
        public static InterpolationMethod map( InterpolationType type ) {
            InterpolationMethod result = None;
            if ( type != null ) {
                switch ( type ) {
                case NEAREST_NEIGHBOR:
                    result = Nearest_Neighbor;
                    break;
                case BILINEAR:
                    result = Bilinear;
                    break;
                case NONE:
                    result = None;
                    break;
                }
            }
            return result;
        }

        /**
         * @return this WCS interpolation type as a raster api type, or <code>null</code> if it the interpolation
         *         method is not supported by the raster api.
         */
        public InterpolationType asRasterAPIType() {
            return InterpolationType.fromString( name() );
        }
    }
}
