//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.postgis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.postgis.jaxbconfig.AbstractPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.ApplicationSchemaDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureTypeDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.SimplePropertyType.PrimitiveType;

/**
 * Adapter between JAXB {@link ApplicationSchemaDecl} and {@link ApplicationSchema} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAXBApplicationSchemaAdapter {

    /**
     * Converts a JAXB {@link ApplicationSchemaDecl} object (that represents a mapped PostGIS application schema) into a
     * {@link PostGISApplicationSchema} object.
     * 
     * @param jaxbAppSchema
     *            mapped PostGIS application schema, must not be <code>null</code>
     * @return internal application schema object, never <code>null</code>
     */
    public static PostGISApplicationSchema toInternal( ApplicationSchemaDecl jaxbAppSchema ) {
        FeatureType[] fts = new FeatureType[jaxbAppSchema.getFeatureType().size()];
        Map<QName, FeatureTypeMapping> ftNameToMapping = new HashMap<QName, FeatureTypeMapping>();
        int i = 0;
        for ( FeatureTypeDecl jaxbFt : jaxbAppSchema.getFeatureType() ) {
            Pair<FeatureType, FeatureTypeMapping> ftAndMapping = toFeatureType( jaxbFt );
            fts[i++] = ftAndMapping.first;
            ftNameToMapping.put( ftAndMapping.first.getName(), ftAndMapping.second );
        }
        ApplicationSchema appSchema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>(), null );
        return new PostGISApplicationSchema( appSchema, jaxbAppSchema.getGlobalMappingHints(), ftNameToMapping );
    }

    private static Pair<FeatureType, FeatureTypeMapping> toFeatureType( FeatureTypeDecl jaxbFt ) {
        QName ftName = jaxbFt.getName();
        Map<QName, PropertyMappingType> propNameToMapping = new HashMap<QName, PropertyMappingType>();
        List<PropertyType> props = new ArrayList<PropertyType>();
        for ( JAXBElement<? extends AbstractPropertyDecl> jaxbPropertyEl : jaxbFt.getAbstractProperty() ) {
            Pair<? extends PropertyType, ? extends PropertyMappingType> propAndMapping = toPropertyType(
                                                                                                         jaxbPropertyEl.getValue(),
                                                                                                         ftName );
            props.add( propAndMapping.first );
            propNameToMapping.put( propAndMapping.first.getName(), propAndMapping.second );
        }
        boolean isAbstract = false;
        if ( jaxbFt.isAbstract() != null ) {
            isAbstract = jaxbFt.isAbstract();
        }
        FeatureTypeMapping ftMapping = new FeatureTypeMapping( jaxbFt.getFeatureTypeMappingHints(), propNameToMapping );
        return new Pair<FeatureType, FeatureTypeMapping>( new GenericFeatureType( ftName, props, isAbstract ),
                                                          ftMapping );
    }

    private static Pair<? extends PropertyType, ? extends PropertyMappingType> toPropertyType(
                                                                                               AbstractPropertyDecl jaxbPropertyDecl,
                                                                                               QName ftName ) {
        Pair<? extends PropertyType, ? extends PropertyMappingType> ptAndMapping = null;
        QName propName = jaxbPropertyDecl.getName();
        if ( propName.getPrefix() == null || "".equals( propName.getPrefix() ) ) {
            propName = new QName( ftName.getNamespaceURI(), propName.getLocalPart() );
        }
        if ( jaxbPropertyDecl instanceof SimplePropertyDecl ) {
            ptAndMapping = toSimplePropertyType( propName, (SimplePropertyDecl) jaxbPropertyDecl );
        } else if ( jaxbPropertyDecl instanceof GeometryPropertyDecl ) {
            ptAndMapping = toGeometryPropertyType( propName, (GeometryPropertyDecl) jaxbPropertyDecl );
        } else if ( jaxbPropertyDecl instanceof FeaturePropertyDecl ) {
            ptAndMapping = toFeaturePropertyType( propName, (FeaturePropertyDecl) jaxbPropertyDecl );
        } else {
            throw new RuntimeException( "Unhandled property type: " + jaxbPropertyDecl.getClass() );
        }
        return ptAndMapping;
    }

    private static Pair<SimplePropertyType, SimplePropertyMappingType> toSimplePropertyType(
                                                                                             QName propName,
                                                                                             SimplePropertyDecl jaxbPropertyDecl ) {
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
        return new Pair<SimplePropertyType, SimplePropertyMappingType>( new SimplePropertyType( propName, minOccurs,
                                                                                                maxOccurs, type ),
                                                                        jaxbPropertyDecl.getSimplePropertyMapping() );
    }

    private static Pair<GeometryPropertyType, GeometryPropertyMappingType> toGeometryPropertyType(
                                                                                                   QName propName,
                                                                                                   GeometryPropertyDecl jaxbPropertyDecl ) {
        int minOccurs = getMinOccurs( jaxbPropertyDecl );
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        // TODO
        CoordinateDimension dim = CoordinateDimension.DIM_2_OR_3;
        return new Pair<GeometryPropertyType, GeometryPropertyMappingType>(
                                                                            new GeometryPropertyType( propName,
                                                                                                      minOccurs,
                                                                                                      maxOccurs, null,
                                                                                                      dim ),
                                                                            jaxbPropertyDecl.getGeometryPropertyMapping() );
    }

    private static Pair<FeaturePropertyType, FeaturePropertyMappingType> toFeaturePropertyType(
                                                                                                QName propName,
                                                                                                FeaturePropertyDecl jaxbPropertyDecl ) {
        int minOccurs = getMinOccurs( jaxbPropertyDecl );
        int maxOccurs = getMaxOccurs( jaxbPropertyDecl );
        QName valueFtName = jaxbPropertyDecl.getType();
        return new Pair<FeaturePropertyType, FeaturePropertyMappingType>( new FeaturePropertyType( propName, minOccurs,
                                                                                                   maxOccurs,
                                                                                                   valueFtName ),
                                                                          jaxbPropertyDecl.getFeaturePropertyMapping() );
    }

    private static int getMinOccurs( AbstractPropertyDecl propertyDecl ) {
        int minOccurs = 1;
        if ( propertyDecl.getMinOccurs() != null ) {
            minOccurs = propertyDecl.getMinOccurs().intValue();
        }
        return minOccurs;
    }

    private static int getMaxOccurs( AbstractPropertyDecl propertyDecl ) {
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
     * Converts a {@link PostGISApplicationSchema} object into a JAXB {@link ApplicationSchemaDecl}.
     * 
     * @param postgisSchema
     *            PostGIS application schema, must not be <code>null</code>
     * @return JAXB representation, never <code>null</code>
     */
    public static ApplicationSchemaDecl toJAXB( PostGISApplicationSchema postgisSchema ) {

        ApplicationSchemaDecl jaxbSchema = new ApplicationSchemaDecl();
        jaxbSchema.setGlobalMappingHints( postgisSchema.getGlobalHints() );
        jaxbSchema.getFeatureType().addAll( toJAXBFeatureTypeDecls( postgisSchema ) );
        return jaxbSchema;
    }

    private static List<FeatureTypeDecl> toJAXBFeatureTypeDecls( PostGISApplicationSchema postgisSchema ) {
        List<FeatureTypeDecl> ftDecls = new ArrayList<FeatureTypeDecl>();
        for ( FeatureType ft : postgisSchema.getSchema().getFeatureTypes() ) {
            ftDecls.add( toJAXBFeatureTypeDecls( ft, postgisSchema.getFtMapping( ft.getName() ) ) );
        }
        return ftDecls;
    }

    private static FeatureTypeDecl toJAXBFeatureTypeDecls( FeatureType ft, FeatureTypeMapping ftMapping ) {
        FeatureTypeDecl ftDecl = new FeatureTypeDecl();
        ftDecl.setAbstract( ft.isAbstract() ? Boolean.TRUE : null );
        ftDecl.setName( ft.getName() );
        ftDecl.setFeatureTypeMappingHints( ftMapping.getFeatureTypeHints() );
        ftDecl.getAbstractProperty().addAll( toJAXBPropertyDecls( ft, ftMapping ) );
        return ftDecl;
    }

    private static Collection<? extends JAXBElement<? extends AbstractPropertyDecl>> toJAXBPropertyDecls(
                                                                                                          FeatureType ft,
                                                                                                          FeatureTypeMapping ftMapping ) {
        Collection<JAXBElement<AbstractPropertyDecl>> propDecls = new ArrayList<JAXBElement<AbstractPropertyDecl>>();
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            propDecls.add( toJAXBPropertyDecl( pt, ftMapping.getPropertyHints( pt.getName() ) ) );
        }
        return propDecls;
    }

    private static JAXBElement<AbstractPropertyDecl> toJAXBPropertyDecl( PropertyType pt,
                                                                         PropertyMappingType propertyHints ) {
        AbstractPropertyDecl propDecl = null;
        QName elName = null;

        if ( pt instanceof SimplePropertyType<?> ) {
            propDecl = new SimplePropertyDecl();
            elName = QName.valueOf( "{http://www.deegree.org/feature/featuretype}SimpleProperty" );
            ((SimplePropertyDecl) propDecl).setSimplePropertyMapping( (SimplePropertyMappingType) propertyHints );
            org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.STRING;
            PrimitiveType primitiveType = ((SimplePropertyType) pt).getPrimitiveType();
            switch (primitiveType) {
            case BOOLEAN: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.BOOLEAN;
                break;
            }
            case DATE: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.DATE;
                break;
            }
            case DATE_TIME: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.DATE;
                break;
            }
            case DECIMAL: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.FLOAT;
                break;
            }
            case DOUBLE: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.FLOAT;
                break;
            }            
            case INTEGER: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.INTEGER;
                break;
            }
            case STRING: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.STRING;
                break;
            }
            case TIME: {
                jaxbPrimitiveType = org.deegree.feature.persistence.postgis.jaxbconfig.PrimitiveType.DATE;
                break;
            }            
            }
            ((SimplePropertyDecl) propDecl).setType( jaxbPrimitiveType );
        } else if ( pt instanceof GeometryPropertyType ) {
            propDecl = new GeometryPropertyDecl();
            elName = QName.valueOf( "{http://www.deegree.org/feature/featuretype}GeometryProperty" );
            ((GeometryPropertyDecl) propDecl).setGeometryPropertyMapping( (GeometryPropertyMappingType) propertyHints );
        } else if ( pt instanceof FeaturePropertyType ) {
            elName = QName.valueOf( "{http://www.deegree.org/feature/featuretype}FeatureProperty" );
            propDecl = new FeaturePropertyDecl();
            if ( ( (FeaturePropertyType) pt ).getFTName() != null ) {
                ( (FeaturePropertyDecl) propDecl ).setType( ( (FeaturePropertyType) pt ).getFTName() );
            }
            ((FeaturePropertyDecl) propDecl).setFeaturePropertyMapping( (FeaturePropertyMappingType) propertyHints );            
        } else {
            // TODO
            elName = QName.valueOf( "{http://www.deegree.org/feature/featuretype}SimpleProperty" );
            propDecl = new SimplePropertyDecl();
        }

        propDecl.setName( pt.getName() );
        if ( pt.getMinOccurs() != 1 ) {
            propDecl.setMinOccurs( BigInteger.valueOf( pt.getMinOccurs() ) );
        }
        if ( pt.getMaxOccurs() != 1 ) {
            if ( pt.getMaxOccurs() != -1 ) {
                propDecl.setMaxOccurs( "" + pt.getMaxOccurs() );
            } else {
                propDecl.setMaxOccurs( "unbounded" );
            }
        }

        return new JAXBElement<AbstractPropertyDecl>( elName, (Class<AbstractPropertyDecl>) propDecl.getClass(),
                                                      propDecl );
    }
}
