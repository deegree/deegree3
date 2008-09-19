//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.gml.schema;

import java.util.List;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.xml.schema.XSModelAnalyzer;

/**
 * Provides convenient access to "relevant" element declarations of a GML schema.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XSModelGMLAnalyzer extends XSModelAnalyzer {

    private static final String GML_PRE_32_NS = "http://www.opengis.net/gml";

    private static final String GML_32_NS = "http://www.opengis.net/gml/3.2";

    private GMLVersion mode;

    private XSElementDeclaration abstractFeatureElementDecl;

    private XSElementDeclaration abstractGeometryElementDecl;

    public XSModelGMLAnalyzer( String url, GMLVersion mode ) throws ClassCastException, ClassNotFoundException,
                            InstantiationException, IllegalAccessException {
        super( url );
        mode = mode;
        switch ( mode ) {
        case VERSION_2:
        case VERSION_30:
        case VERSION_31: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "_Geometry", GML_PRE_32_NS );
            break;
        }
        case VERSION_32: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "AbstractFeature", GML_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "AbstractGeometry", GML_32_NS );
            break;
        }
        }
    }

    public List<XSElementDeclaration> getFeatureElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractFeatureElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getGeometryElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGeometryElementDecl, namespace, onlyConcrete );
    }    
}
