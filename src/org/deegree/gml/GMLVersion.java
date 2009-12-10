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

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

/**
 * Enum type for the GML versions that have to be differerentiated in deegree's GML subsystem.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public enum GMLVersion {
    /** GML 2 versions (any in the range from 2.0.0 to 2.1.2) */
    GML_2( GMLNS ),
    /** GML 3.0 versions (either 3.0.0 or 3.0.1) */
    GML_30( GMLNS ),
    /** GML 3.1 versions (either 3.1.0 or 3.1.1) */
    GML_31( GMLNS ),
    /** GML 3.2 versions (3.2.1) */
    GML_32( GML3_2_NS );

    private String ns;

    private GMLVersion( String ns ) {
        this.ns = ns;
    }

    /**
     * Returns the namespace for elements from this GML version.
     * 
     * @return the namespace, never <code>null</code>
     */
    public String getNamespace() {
        return ns;
    }
}
