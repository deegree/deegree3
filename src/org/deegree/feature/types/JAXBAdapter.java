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

package org.deegree.feature.types;

import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.deegree.feature.types.jaxb.AbstractPropertyDecl;
import org.deegree.feature.types.jaxb.ApplicationSchemaDecl;
import org.deegree.feature.types.jaxb.FeaturePropertyDecl;
import org.deegree.feature.types.jaxb.FeatureTypeDecl;
import org.deegree.feature.types.jaxb.GeometryPropertyDecl;
import org.deegree.feature.types.jaxb.SimplePropertyDecl;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PrimitiveType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;

/**
 * Adapter for converting the contents of a deegree application schema declaration document to an
 * {@link ApplicationSchema}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class JAXBAdapter {

    private ApplicationSchemaDecl jaxbSchema;

    public JAXBAdapter( URL url ) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.types.jaxb" );
        Unmarshaller u = jc.createUnmarshaller();
        this.jaxbSchema = (ApplicationSchemaDecl) u.unmarshal( url );
    }

    public JAXBAdapter( ApplicationSchemaDecl jaxbSchema ) {
        this.jaxbSchema = jaxbSchema;
    }

    public ApplicationSchema getApplicationSchema() {
        FeatureType[] fts = new FeatureType[jaxbSchema.getFeatureType().size()];
        int i = 0;
        for ( FeatureTypeDecl jaxbFt : jaxbSchema.getFeatureType() ) {
            fts[i++] = convert( jaxbFt );
        }
        return new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>() );
    }

    private FeatureType convert( FeatureTypeDecl jaxbFt ) {
        QName ftName = jaxbFt.getName();
        List<PropertyType> props = new ArrayList<PropertyType>();
        for ( JAXBElement<? extends AbstractPropertyDecl> jaxbPropertyEl : jaxbFt.getAbstractProperty() ) {
            props.add( convert( jaxbPropertyEl.getValue() ) );
        }
        if ( "".equals( ftName.getPrefix() ) ) {
            ftName = new QName( jaxbSchema.getTargetNamespace(), ftName.getLocalPart() );
        }
        return new GenericFeatureType( ftName, props, false );
    }

    private PropertyType convert( AbstractPropertyDecl jaxbPropertyDecl ) {
        PropertyType pt = null;
        if ( jaxbPropertyDecl instanceof SimplePropertyDecl ) {
            pt = convertSimplePropertyDecl( (SimplePropertyDecl) jaxbPropertyDecl );
        } else if ( jaxbPropertyDecl instanceof GeometryPropertyDecl ) {
            pt = convertGeometryPropertyDecl( (GeometryPropertyDecl) jaxbPropertyDecl );
        } else if ( jaxbPropertyDecl instanceof FeaturePropertyDecl ) {
            pt = convertFeaturePropertyDecl( (FeaturePropertyDecl) jaxbPropertyDecl );
        } else {
            throw new RuntimeException();
        }
        return pt;
    }

    private SimplePropertyType convertSimplePropertyDecl( SimplePropertyDecl jaxbPropertyDecl ) {
        QName propName = getPropertyName( jaxbPropertyDecl );
        int minOccurs = getMinOccurs( jaxbPropertyDecl );
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        PrimitiveType type = null;
        switch ( jaxbPropertyDecl.getType() ) {
        case STRING: {
            type = PrimitiveType.STRING;
            break;
        }
        case INTEGER: {
            type = PrimitiveType.INTEGER;
            break;
        }
        case BOOLEAN: {
            type = PrimitiveType.BOOLEAN;
            break;
        }
        case DATE: {
            type = PrimitiveType.DATE;
            break;
        }
        case DECIMAL: {
            type = PrimitiveType.DECIMAL;
            break;
        }
        }
        return new SimplePropertyType( propName, minOccurs, maxOccurs, type, false, null );
    }

    private GeometryPropertyType convertGeometryPropertyDecl( GeometryPropertyDecl jaxbPropertyDecl ) {
        QName propName = getPropertyName( jaxbPropertyDecl );
        int minOccurs = getMinOccurs( jaxbPropertyDecl );
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        // TODO
        CoordinateDimension dim = CoordinateDimension.DIM_2_OR_3;
        return new GeometryPropertyType( propName, minOccurs, maxOccurs, null, dim, false, null, BOTH );
    }

    private FeaturePropertyType convertFeaturePropertyDecl( FeaturePropertyDecl jaxbPropertyDecl ) {
        QName propName = getPropertyName( jaxbPropertyDecl );
        int minOccurs = getMinOccurs( jaxbPropertyDecl );
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        QName valueFtName = jaxbPropertyDecl.getType();
        if ( "".equals( valueFtName.getPrefix() ) ) {
            valueFtName = new QName( jaxbSchema.getTargetNamespace(), valueFtName.getLocalPart() );
        }
        return new FeaturePropertyType( propName, minOccurs, maxOccurs, valueFtName, false, null, BOTH );
    }

    private QName getPropertyName( AbstractPropertyDecl jaxbPropertyDecl ) {
        return normalizeQName( jaxbPropertyDecl.getName() );
    }

    private QName normalizeQName( QName name ) {
        if ( "".equals( name.getPrefix() ) ) {
            name = new QName( jaxbSchema.getTargetNamespace(), name.getLocalPart() );
        }
        return name;
    }

    private int getMinOccurs( AbstractPropertyDecl propertyDecl ) {
        int minOccurs = 1;
        if ( propertyDecl.getMinOccurs() != null ) {
            minOccurs = propertyDecl.getMinOccurs().intValue();
        }
        return minOccurs;
    }

    private int getMaxOccurs( AbstractPropertyDecl propertyDecl ) {
        int maxOccurs = 1;
        if ( propertyDecl.getMaxOccurs() != null ) {
            if ( "unbounded".equals( propertyDecl.getMaxOccurs() ) ) {
                maxOccurs = -1;
            } else {
                maxOccurs = Integer.parseInt( propertyDecl.getMaxOccurs() );
            }
        }
        return maxOccurs;
    }

    /**
     * @param args
     * @throws JAXBException
     * @throws MalformedURLException
     */
    public static void main( String[] args )
                            throws JAXBException, MalformedURLException {

        URL url = new URL( "file:/home/schneider/workspace/d3_core/resources/schema/feature/example.xml" );
        JAXBAdapter adapter = new JAXBAdapter( url );
        ApplicationSchema schema = adapter.getApplicationSchema();
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            System.out.println( "\nft: " + ft );
        }
    }
}
