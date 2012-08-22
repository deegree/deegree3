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
package org.deegree.model.filterencoding;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfacePatch;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * deegree-specific <code>ComparisonOperation</code> that allows to check the type of a property.
 * <p>
 * This is useful if the property has an abstract type with several concrete implementations, for example
 * 'gml:_Geometry'.
 * <p>
 * NOTE: Currently supported types to test are:
 * <ul>
 * <li>gml:Point</li>
 * <li>gml:_Curve</li>
 * <li>gml:_Surface</li>
 * </ul>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PropertyIsInstanceOfOperation extends ComparisonOperation {

    private PropertyName propertyName;

    private QualifiedName typeName;

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * Creates a new instance of <code>PropertyIsInstanceOfOperation</code>.
     * 
     * @param propertyName
     * @param typeName
     */
    public PropertyIsInstanceOfOperation( PropertyName propertyName, QualifiedName typeName ) {
        super( OperationDefines.PROPERTYISINSTANCEOF );
        this.propertyName = propertyName;
        this.typeName = typeName;
    }

    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append( "<ogc:" ).append( getOperatorName() ).append( ">" );
        sb.append( propertyName.toXML() );
        sb.append( "<ogc:Literal gml='http://www.opengis.net/gml'>" );
        sb.append( "gml:" ).append( typeName.getLocalName() ).append( "</ogc:Literal>" );
        sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
        return sb;
    }

    public StringBuffer to100XML() {
        return toXML();
    }

    public StringBuffer to110XML() {
        return toXML();
    }

    /**
     * Calculates the <code>Operation</code>'s logical value based on the certain property values of the given feature.
     * 
     * @param feature
     *            that determines the values of <code>PropertyNames</code> in the expression
     * @return true, if the <code>Operation</code> evaluates to true, else false
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public boolean evaluate( Feature feature )
                            throws FilterEvaluationException {
        boolean equals = false;
        Object propertyValue = null;
        try {
            FeatureProperty property = feature.getDefaultProperty( propertyName.getValue() );
            if ( property == null ) {
                return false;
            }
            propertyValue = property.getValue();
        } catch ( PropertyPathResolvingException e ) {
            String msg = "Error evaluating PropertyIsInstanceOf operation: " + e.getMessage();
            throw new FilterEvaluationException( msg );
        }

        if ( CommonNamespaces.GMLNS.equals( this.typeName.getNamespace() ) ) {
            String localName = this.typeName.getLocalName();
            if ( "Point".equals( localName ) ) {
                equals = propertyValue instanceof Point || propertyValue instanceof MultiPoint;
            } else if ( "_Curve".equals( localName ) ) {
                equals = propertyValue instanceof Curve || propertyValue instanceof MultiCurve;
            } else if ( "_Surface".equals( localName ) ) {
                equals = propertyValue instanceof Surface || propertyValue instanceof MultiSurface
                         || propertyValue instanceof SurfacePatch;
            } else {
                String msg = "Error evaluating PropertyIsInstanceOf operation: " + this.typeName
                             + " is not a supported type to check for.";
                throw new FilterEvaluationException( msg );
            }
        } else {
            String msg = "Error evaluating PropertyIsInstanceOf operation: " + this.typeName
                         + " is not a supported type to check for.";
            throw new FilterEvaluationException( msg );
        }
        return equals;
    }

    /**
     * Given a DOM-fragment, a corresponding Operation-object is built. This method recursively calls other buildFromDOM
     * () - methods to validate the structure of the DOM-fragment.
     * 
     * @param element
     *            to build from
     * @return the Bean of the DOM
     * 
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Operation buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name equals 'PropertyIsInstanceOf'
        if ( !element.getLocalName().equals( "PropertyIsInstanceOf" ) )
            throw new FilterConstructionException( "Name of element does not equal 'PropertyIsInstanceOf'!" );

        ElementList children = XMLTools.getChildElements( element );
        if ( children.getLength() != 2 ) {
            throw new FilterConstructionException( "'PropertyIsInstanceOf' requires exactly 2 elements!" );
        }

        PropertyName propertyName = (PropertyName) PropertyName.buildFromDOM( children.item( 0 ) );
        QualifiedName typeName = null;
        try {
            typeName = XMLTools.getRequiredNodeAsQualifiedName( element, "ogc:Literal/text()", nsContext );
        } catch ( XMLParsingException e ) {
            throw new FilterConstructionException( e.getMessage() );
        }
        return new PropertyIsInstanceOfOperation( propertyName, typeName );
    }

    /**
     * @return the propertyName of this Operation
     */
    public PropertyName getPropertyName() {
        return propertyName;
    }

    /**
     * @return the typeName
     */
    public QualifiedName getTypeName() {
        return typeName;
    }

}
