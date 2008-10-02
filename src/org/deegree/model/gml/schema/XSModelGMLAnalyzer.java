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

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.xml.schema.XSModelAnalyzer;

/**
 * Provides convenient access to "relevant" element declarations of a GML schema.
 * <p>
 * An element declaration is relevant if it defines a class of GML objects. Currently, eight types of GML objects exist:
 * <ul>
 * <li>feature</li>
 * <li>geometry</li>
 * <li>value</li>
 * <li>topology</li>
 * <li>crs</li>
 * <li>time object</li>
 * <li>coverage</li>
 * <li>style</li>
 * </ul>
 * </p>
 * <p>
 * Please refer to chapter 10 of the book "Geography Mark-Up Language - Foundations for the Geo-Web" by Ron Lake et al.
 * for details.
 * </p>
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

    private XSElementDeclaration abstractValueElementDecl;

    private XSElementDeclaration abstractTopologyElementDecl;

    private XSElementDeclaration abstractCRSElementDecl;

    private XSElementDeclaration abstractTimeObjectElementDecl;

    private XSElementDeclaration abstractCoverageElementDecl;

    private XSElementDeclaration abstractStyleElementDecl;   
    
    public XSModelGMLAnalyzer( String url, GMLVersion mode ) throws ClassCastException, ClassNotFoundException,
                            InstantiationException, IllegalAccessException {
        super( url );
        this.mode = mode;
        switch ( mode ) {
        case VERSION_2: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "_Geometry", GML_PRE_32_NS );
            break;
        }
        case VERSION_30:
        case VERSION_31: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "_Geometry", GML_PRE_32_NS );
            abstractValueElementDecl = xmlSchema.getElementDeclaration( "_Value", GML_PRE_32_NS );
            abstractTopologyElementDecl = xmlSchema.getElementDeclaration( "_Topology", GML_PRE_32_NS );
            abstractCRSElementDecl = xmlSchema.getElementDeclaration( "_CRS", GML_PRE_32_NS );
            abstractTimeObjectElementDecl = xmlSchema.getElementDeclaration( "_TimeObject", GML_PRE_32_NS );
            abstractCoverageElementDecl = xmlSchema.getElementDeclaration( "_Coverage", GML_PRE_32_NS );
            abstractStyleElementDecl = xmlSchema.getElementDeclaration( "_Style", GML_PRE_32_NS );
            break;
        }
        case VERSION_32: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "AbstractFeature", GML_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "AbstractGeometry", GML_32_NS );
            abstractValueElementDecl = xmlSchema.getElementDeclaration( "AbstractValue", GML_32_NS );
            abstractTopologyElementDecl = xmlSchema.getElementDeclaration( "AbstractTopology", GML_32_NS );
            abstractCRSElementDecl = xmlSchema.getElementDeclaration( "AbstractCRS", GML_32_NS );
            abstractTimeObjectElementDecl = xmlSchema.getElementDeclaration( "AbstractTimeObject", GML_32_NS );
            abstractCoverageElementDecl = xmlSchema.getElementDeclaration( "AbstractCoverage", GML_32_NS );
            abstractStyleElementDecl = xmlSchema.getElementDeclaration( "AbstractStyle", GML_32_NS );
            break;
        }
        }
    }

    public List<XSElementDeclaration> getFeatureElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractFeatureElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getFeatureCollectionElementDeclarations( String namespace, boolean onlyConcrete ) {
        List<XSElementDeclaration> fcDecls = null;

        switch ( mode ) {
        case VERSION_2:
        case VERSION_30:
        case VERSION_31: {
            // TODO do this the right way
            fcDecls = new ArrayList<XSElementDeclaration>();
            if (xmlSchema.getElementDeclaration( "_FeatureCollection", GML_PRE_32_NS ) != null) {
                fcDecls.addAll( getSubstitutions( xmlSchema.getElementDeclaration( "_FeatureCollection", GML_PRE_32_NS ), namespace,
                                                  onlyConcrete ));                
            }
            if (xmlSchema.getElementDeclaration( "FeatureCollection", GML_PRE_32_NS ) != null) {
                fcDecls.addAll( getSubstitutions( xmlSchema.getElementDeclaration( "FeatureCollection", GML_PRE_32_NS ), namespace,
                                                  onlyConcrete ));                
            }

            break;
        }
        case VERSION_32:
            // GML 3.2 does not have an abstract feature collection element anymore
            // Every gml:AbstractFeature having a property whose content model extends gml:AbstractFeatureMemberType is
            // a feature collection. See OGC 07-061, section 6.5
            fcDecls = new ArrayList<XSElementDeclaration> ();
            
            // TODO            
        }
        return fcDecls;
    }

    public List<XSElementDeclaration> getGeometryElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGeometryElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getValueElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractValueElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getTopologyElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTopologyElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getCRSElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCRSElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getTimeObjectElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTimeObjectElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getCoverageElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCoverageElementDecl, namespace, onlyConcrete );
    }

    public List<XSElementDeclaration> getStyleElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractStyleElementDecl, namespace, onlyConcrete );
    }
}
