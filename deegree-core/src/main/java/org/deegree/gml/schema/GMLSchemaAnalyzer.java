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
package org.deegree.gml.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.schema.XSModelAnalyzer;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the <i>object</i> element declarations of a GML schema (both application and GML core schema
 * objects).
 * <p>
 * An element declaration is an <i>object</i> element declaration, if it is in one or more of GML's object substitution
 * groups. In the latest version of GML (3.2.1), eight (?) classes of GML objects exist:
 * <ul>
 * <li>feature</li>
 * <li>geometry</li>
 * <li>value</li>
 * <li>topology</li>
 * <li>crs</li>
 * <li>time object</li>
 * <li>coverage</li>
 * <li>style</li>
 * <li>object?</li>
 * <li>gml?</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLSchemaAnalyzer extends XSModelAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger( GMLSchemaAnalyzer.class );

    private static final String GML_PRE_32_NS = CommonNamespaces.GMLNS;

    private static final String GML_32_NS = CommonNamespaces.GML3_2_NS;

    private GMLVersion version;

    private XSElementDeclaration abstractObjectElementDecl;

    private XSElementDeclaration abstractGmlElementDecl;

    private XSElementDeclaration abstractFeatureElementDecl;

    private XSElementDeclaration abstractGeometryElementDecl;

    private XSElementDeclaration abstractValueElementDecl;

    private XSElementDeclaration abstractTopologyElementDecl;

    private XSElementDeclaration abstractCRSElementDecl;

    private XSElementDeclaration abstractTimeObjectElementDecl;

    private XSElementDeclaration abstractCoverageElementDecl;

    private XSElementDeclaration abstractStyleElementDecl;

    private XSElementDeclaration abstractCurveSegmentElementDecl;

    private XSElementDeclaration abstractSurfacePatchElementDecl;

    private XSTypeDefinition abstractFeatureElementTypeDecl;

    /**
     * Creates a new {@link GMLSchemaAnalyzer} instance for the given GML version and using the specified schemas.
     * 
     * @param version
     * @param schemaUrls
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public GMLSchemaAnalyzer( GMLVersion version, String... schemaUrls ) throws ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        super( schemaUrls );
        this.version = version;
        switch ( version ) {
        case GML_2: {
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "_Geometry", GML_PRE_32_NS );
            abstractFeatureElementTypeDecl = xmlSchema.getTypeDefinition( "AbstractFeatureType", GML_PRE_32_NS );
            break;
        }
        case GML_30:
        case GML_31: {
            abstractObjectElementDecl = xmlSchema.getElementDeclaration( "_Object", GML_PRE_32_NS );
            abstractGmlElementDecl = xmlSchema.getElementDeclaration( "_GML", GML_PRE_32_NS );
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "_Geometry", GML_PRE_32_NS );
            abstractValueElementDecl = xmlSchema.getElementDeclaration( "_Value", GML_PRE_32_NS );
            abstractTopologyElementDecl = xmlSchema.getElementDeclaration( "_Topology", GML_PRE_32_NS );
            abstractCRSElementDecl = xmlSchema.getElementDeclaration( "_CRS", GML_PRE_32_NS );
            abstractTimeObjectElementDecl = xmlSchema.getElementDeclaration( "_TimeObject", GML_PRE_32_NS );
            abstractCoverageElementDecl = xmlSchema.getElementDeclaration( "_Coverage", GML_PRE_32_NS );
            abstractStyleElementDecl = xmlSchema.getElementDeclaration( "_Style", GML_PRE_32_NS );
            abstractCurveSegmentElementDecl = xmlSchema.getElementDeclaration( "_CurveSegment", GML_PRE_32_NS );
            abstractSurfacePatchElementDecl = xmlSchema.getElementDeclaration( "_SurfacePatch", GML_PRE_32_NS );
            abstractFeatureElementTypeDecl = xmlSchema.getTypeDefinition( "AbstractFeatureType", GML_PRE_32_NS );
            break;
        }
        case GML_32: {
            abstractObjectElementDecl = xmlSchema.getElementDeclaration( "AbstractObject", GML_32_NS );
            abstractGmlElementDecl = xmlSchema.getElementDeclaration( "AbstractGML", GML_32_NS );
            abstractFeatureElementDecl = xmlSchema.getElementDeclaration( "AbstractFeature", GML_32_NS );
            abstractGeometryElementDecl = xmlSchema.getElementDeclaration( "AbstractGeometry", GML_32_NS );
            abstractValueElementDecl = xmlSchema.getElementDeclaration( "AbstractValue", GML_32_NS );
            abstractTopologyElementDecl = xmlSchema.getElementDeclaration( "AbstractTopology", GML_32_NS );
            abstractCRSElementDecl = xmlSchema.getElementDeclaration( "AbstractCRS", GML_32_NS );
            abstractTimeObjectElementDecl = xmlSchema.getElementDeclaration( "AbstractTimeObject", GML_32_NS );
            abstractCoverageElementDecl = xmlSchema.getElementDeclaration( "AbstractCoverage", GML_32_NS );
            abstractStyleElementDecl = xmlSchema.getElementDeclaration( "AbstractStyle", GML_32_NS );
            abstractCurveSegmentElementDecl = xmlSchema.getElementDeclaration( "AbstractCurveSegment", GML_32_NS );
            abstractSurfacePatchElementDecl = xmlSchema.getElementDeclaration( "AbstractSurfacePatch", GML_32_NS );
            abstractFeatureElementTypeDecl = xmlSchema.getTypeDefinition( "AbstractFeatureType", GML_32_NS );
            break;
        }
        }
    }

    /**
     * Returns the element declaration of the abstract object element, i.e.
     * <code>{http://www.opengis.net/gml}_Object</code> (GML 3.0 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractObject</code> (GML 3.2).
     * 
     * @return declaration object of the abstract object element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractObjectElementDeclaration() {
        return abstractObjectElementDecl;
    }

    /**
     * Returns the element declaration of the abstract GML element, i.e. <code>{http://www.opengis.net/gml}_GML</code>
     * (GML 3.0 to 3.1) or <code>{http://www.opengis.net/gml/3.2}AbstractGML</code> (GML 3.2).
     * 
     * @return declaration object of the abstract GML element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractGMLElementDeclaration() {
        return abstractGmlElementDecl;
    }

    /**
     * Returns the element declaration of the abstract feature element, i.e.
     * <code>{http://www.opengis.net/gml}_Feature</code> (GML 2 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractFeature</code> (GML 3.2).
     * 
     * @return declaration object of the abstract feature element
     */
    public XSElementDeclaration getAbstractFeatureElementDeclaration() {
        return abstractFeatureElementDecl;
    }

    /**
     * Returns the element declaration of the abstract geometry element, i.e.
     * <code>{http://www.opengis.net/gml}_Geometry</code> (GML 2 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractGeometry</code> (GML 3.2).
     * 
     * @return declaration object of the abstract geometry element
     */
    public XSElementDeclaration getAbstractGeometryElementDeclaration() {
        return abstractGeometryElementDecl;
    }

    /**
     * Returns the element declaration of the abstract curve segment element, i.e.
     * <code>{http://www.opengis.net/gml}_CurveSegment</code> (GML 3 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractCurveSegment</code> (GML 3.2).
     * 
     * @return declaration object of the abstract curve segment element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractCurveSegmentElementDeclaration() {
        return abstractCurveSegmentElementDecl;
    }

    /**
     * Returns the element declaration of the abstract surface patch element, i.e.
     * <code>{http://www.opengis.net/gml}_SurfacePatch</code> (GML 3 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractSurfacePatch</code> (GML 3.2).
     * 
     * @return element declaration object of the abstract geometry element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractSurfacePatchElementDeclaration() {
        return abstractSurfacePatchElementDecl;
    }

    public List<XSElementDeclaration> getObjectElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractObjectElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getGmlElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGmlElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getFeatureElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractFeatureElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSTypeDefinition> getFeatureTypeDefinitions( String namespace, boolean onlyConcrete ) {
        return getSubtypes( abstractFeatureElementTypeDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getFeatureCollectionElementDeclarations( String namespace, boolean onlyConcrete ) {
        List<XSElementDeclaration> fcDecls = null;

        switch ( version ) {
        case GML_2:
        case GML_30:
        case GML_31: {
            // TODO do this the right way
            fcDecls = new ArrayList<XSElementDeclaration>();
            if ( xmlSchema.getElementDeclaration( "_FeatureCollection", GML_PRE_32_NS ) != null ) {
                fcDecls.addAll( getSubstitutions(
                                                  xmlSchema.getElementDeclaration( "_FeatureCollection", GML_PRE_32_NS ),
                                                  namespace, true, onlyConcrete ) );
            }
            if ( xmlSchema.getElementDeclaration( "FeatureCollection", GML_PRE_32_NS ) != null ) {
                fcDecls.addAll( getSubstitutions(
                                                  xmlSchema.getElementDeclaration( "FeatureCollection", GML_PRE_32_NS ),
                                                  namespace, true, onlyConcrete ) );
            }

            break;
        }
        case GML_32:
            List<XSElementDeclaration> featureDecls = getFeatureElementDeclarations( namespace, onlyConcrete );
            fcDecls = new ArrayList<XSElementDeclaration>();
            for ( XSElementDeclaration featureDecl : featureDecls ) {
                if ( isGML32FeatureCollection( featureDecl ) ) {
                    fcDecls.add( featureDecl );
                }
            }
            break;
        }
        return fcDecls;
    }

    /**
     * Returns whether the given feature element declaration is a feature collection.
     * <p>
     * GML 3.2 does not have an abstract feature collection element anymore (to be precise: it's deprecated). Every
     * <code>gml:AbstractFeature</code> element that has a property whose content model extends
     * <code>gml:AbstractFeatureMemberType</code> is a feature collection. See OGC 07-061, section 6.5.
     * </p>
     * 
     * @param featureDecl
     *            feature element declaration, must not be <code>null</code>
     * @return true, if the given element declaration is a feature collection, false otherwise
     */
    private boolean isGML32FeatureCollection( XSElementDeclaration featureDecl ) {
        XSComplexTypeDecl type = (XSComplexTypeDecl) featureDecl.getTypeDefinition();
        List<XSElementDeclaration> propDecls = getPropertyDecls( type );
        for ( XSElementDeclaration propDecl : propDecls ) {
            XSTypeDefinition propType = propDecl.getTypeDefinition();
            if ( propType.derivedFrom( GML_32_NS, "AbstractFeatureMemberType",
                                       (short) ( XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_EXTENSION
                                                 | XSConstants.DERIVATION_UNION | XSConstants.DERIVATION_LIST ) ) ) {
                return true;
            }
            // handle deprecated FeatureCollection types as well (their properties are not based on
            // AbstractFeatureMemberType, but on FeaturePropertyType)
            if ( propType.derivedFrom( GML_32_NS, "FeaturePropertyType",
                                       (short) ( XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_EXTENSION
                                                 | XSConstants.DERIVATION_UNION | XSConstants.DERIVATION_LIST ) ) ) {
                return true;
            }
        }
        return false;
    }

    private List<XSElementDeclaration> getPropertyDecls( XSComplexTypeDecl type ) {
        List<XSElementDeclaration> propDecls = new ArrayList<XSElementDeclaration>();
        getPropertyDecls( type.getParticle(), propDecls );
        return propDecls;
    }

    private void getPropertyDecls( XSParticle particle, List<XSElementDeclaration> propertyDecls ) {
        if ( particle != null ) {
            XSTerm term = particle.getTerm();
            if ( term instanceof XSElementDeclaration ) {
                propertyDecls.add( (XSElementDeclaration) term );
            } else if ( term instanceof XSModelGroup ) {
                XSObjectList particles = ( (XSModelGroup) term ).getParticles();
                for ( int i = 0; i < particles.getLength(); i++ ) {
                    getPropertyDecls( (XSParticle) particles.item( i ), propertyDecls );
                }
            } else {
                LOG.warn( "Unhandled term type: " + term.getClass() );
            }
        }
    }

    public List<XSElementDeclaration> getGeometryElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGeometryElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getValueElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractValueElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getTopologyElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTopologyElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getCRSElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCRSElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getTimeObjectElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTimeObjectElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getCoverageElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCoverageElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getStyleElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractStyleElementDecl, namespace, true, onlyConcrete );
    }
}
