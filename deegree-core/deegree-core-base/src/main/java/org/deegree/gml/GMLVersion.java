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
package org.deegree.gml;

import static org.deegree.commons.utils.StringUtils.isSet;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;

/**
 * Enum type for the GML versions that are differerentiated in deegree's GML subsystem.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public enum GMLVersion {

    /** GML 2 versions (any in the range from 2.0.0 to 2.1.2) */
    GML_2( GMLNS, "2.1", "2.1.2" ),
    /** GML 3.0 versions (either 3.0.0 or 3.0.1) */
    GML_30( GMLNS, "3.0", "3.0.1" ),
    /** GML 3.1 versions (either 3.1.0 or 3.1.1) */
    GML_31( GMLNS, "3.1", "3.1.1" ),
    /** GML 3.2 versions (either 3.2.1 or 3.2.2) */
    GML_32( GML3_2_NS, "3.2", "3.2.2" );

    private final String ns;

    private String mimeType;

    private final String mimeTypeOld;

    private GMLVersion( String ns, String mimeVersionNew, String mimeVersionOld ) {
        this.ns = ns;
        this.mimeType = "application/gml+xml; version=" + mimeVersionNew;
        this.mimeTypeOld = "text/xml; subtype=gml/" + mimeVersionOld;
    }

    /**
     * Returns the namespace for elements from this GML version.
     * 
     * @return the namespace, never <code>null</code>
     */
    public String getNamespace() {
        return ns;
    }

    /**
     * Returns the mime type for this GML version.
     * 
     * @return the mime type, never <code>null</code>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the mime type for this GML version (deprecated style).
     * 
     * @return the mime type (deprecated style), never <code>null</code>
     */
    public String getMimeTypeOldStyle() {
        return mimeTypeOld;
    }

    /**
     * This method creates a {@link GMLVersion} from the given mimetype. Expected is following format: <b>'some/type';
     * subtype=gml/x.y.z</b>. If the mime does not comply the defaultversion is returned. This method uses version
     * negotiation, e.g. if the given version equals or is larger then a version the next possible gml version will be
     * used, e.g. gml/3.2.0 will match to {@link GMLVersion#GML_32} (gml/3.2.1), gml/3.0.9 (none existing) will match to
     * {@link GMLVersion#GML_31}.
     * 
     * @param mimeType
     *            to be parsed
     * @param defaultVersion
     *            to be used if the mime type is not compliant with above rules, may be <code>null</code>
     * @return the GMLVersion of the mime type or the default version.
     */
    public static GMLVersion fromMimeType( String mimeType, GMLVersion defaultVersion ) {
        GMLVersion result = defaultVersion;
        if ( isSet( mimeType ) ) {
            String subType = null;
            String[] split = mimeType.split( ";" );
            if ( split.length > 1 ) {
                subType = split[split.length - 1];
                if ( isSet( subType ) ) {
                    subType = subType.toLowerCase().trim();
                    int index = subType.lastIndexOf( "=gml/" );
                    if ( index > -1 ) {
                        String version = subType.substring( index + "=gml/".length() );
                        if ( isSet( version ) ) {
                            Version v = null;
                            try {
                                v = Version.parseVersion( version );
                            } catch ( InvalidParameterValueException n ) {
                                // not a number, assuming default
                            }
                            if ( v != null ) {
                                if ( v.compareTo( new Version( 2, 1, 2 ) ) <= 0 ) {
                                    result = GML_2;
                                } else if ( v.compareTo( new Version( 3, 0, 1 ) ) <= 0 ) {
                                    result = GML_30;
                                } else if ( v.compareTo( new Version( 3, 1, 1 ) ) <= 0 ) {
                                    result = GML_31;
                                } else if ( v.compareTo( new Version( 3, 2, 2 ) ) <= 0 ) {
                                    result = GML_32;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return mimeTypeOld;
    }
}
