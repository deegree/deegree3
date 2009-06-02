//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.feature.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.SimplePropertyType.PrimitiveType;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class JAXBAdapter {

    public org.deegree.feature.types.ApplicationSchema convert( ApplicationSchemaDecl jaxbSchema ) {
        FeatureType[] fts = new FeatureType[jaxbSchema.getFeatureType().size()];
        int i = 0;
        for ( FeatureTypeDecl jaxbFt : jaxbSchema.getFeatureType() ) {
            fts[i++] = convert( jaxbFt );
        }
        return new ApplicationSchema (fts, null, null);
    }

    private FeatureType convert( FeatureTypeDecl jaxbFt ) {
        QName ftName = jaxbFt.getName();
        List<PropertyType> props = new ArrayList<PropertyType>();
        for ( JAXBElement<? extends AbstractPropertyDecl> jaxbPropertyEl : jaxbFt.getAbstractProperty() ) {
            props.add( convert( jaxbPropertyEl.getValue() ) );
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
        QName propName = getPropertyName (jaxbPropertyDecl);        
        int minOccurs = jaxbPropertyDecl.getMinOccurs().intValue();
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        PrimitiveType type = null;
        switch (jaxbPropertyDecl.getType()) {
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
            type = PrimitiveType.INTEGER;
            break;
        }
        case FLOAT: {
            type = PrimitiveType.FLOAT;
            break;
        }                
        }        
        return new SimplePropertyType (propName, minOccurs, maxOccurs, type);
    }
   
    private GeometryPropertyType convertGeometryPropertyDecl( GeometryPropertyDecl jaxbPropertyDecl ) {
        QName propName = getPropertyName (jaxbPropertyDecl);        
        int minOccurs = jaxbPropertyDecl.getMinOccurs().intValue();
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );        
        return new GeometryPropertyType (propName, minOccurs, maxOccurs, null );
    }

    private FeaturePropertyType convertFeaturePropertyDecl( FeaturePropertyDecl jaxbPropertyDecl ) {
        QName propName = getPropertyName (jaxbPropertyDecl);
        int minOccurs = jaxbPropertyDecl.getMinOccurs().intValue();
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        QName valueFtName = jaxbPropertyDecl.getType();
        return new FeaturePropertyType(propName, minOccurs, maxOccurs, valueFtName);
    }

    private QName getPropertyName( AbstractPropertyDecl jaxbPropertyDecl ) {
        return jaxbPropertyDecl.getName();
    }

    private int getMaxOccurs (AbstractPropertyDecl propertyDecl) {
        int maxOccurs = 1;
        if (propertyDecl.getMaxOccurs() != null) {
            if ("unbounded".equals( maxOccurs )) {
                maxOccurs = -1;
            } else {
                maxOccurs = Integer.parseInt( propertyDecl.getMaxOccurs());
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

        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.types.jaxb" );
        Unmarshaller u = jc.createUnmarshaller();
        ApplicationSchemaDecl jaxbSchema = (ApplicationSchemaDecl) u.unmarshal( new URL(
                                                                                         "file:/home/schneider/workspace/d3_commons/resources/schema/feature/example.xml" ) );
        for ( FeatureTypeDecl ft : jaxbSchema.getFeatureType() ) {
            System.out.println( "ft: " + ft.getName() );
        }
    }
}
