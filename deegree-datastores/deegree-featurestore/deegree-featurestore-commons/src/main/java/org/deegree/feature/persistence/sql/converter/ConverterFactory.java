//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.converter;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.feature.persistence.sql.AbstractSQLFeatureStore;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.filter.sql.DBField;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ConverterFactory {

    private static final QName GML32_TIME_UNION = new QName( GML3_2_NS, "TimePositionUnion" );

    private static final QName GML32_DOUBLE_LIST = new QName( GML3_2_NS, "doubleList" );

    public static ParticleConverter<?> buildConverter( PrimitiveMapping pm, AbstractSQLFeatureStore fs ) {
        PrimitiveType pt = pm.getType();
        String column = ( (DBField) pm.getMapping() ).getColumn();
        XSSimpleTypeDefinition xsTypeDef = pt.getXSType();
        if ( xsTypeDef != null && !( xsTypeDef.getAnonymous() ) ) {
            QName typeName = new QName( xsTypeDef.getNamespace(), xsTypeDef.getName() );
            if ( GML32_TIME_UNION.equals( typeName ) ) {
                return new TimePositionUnionConverter( pt, column );
            } else if ( GML32_DOUBLE_LIST.equals( typeName ) && column.equals( "origlocat" ) ) {
                GeometryStorageParams geometryStorageParams = new GeometryStorageParams( null, null, DIM_3 );
                GeometryMapping mapping = new GeometryMapping( null, pm.isVoidable(), pm.getMapping(), GEOMETRY,
                                                               geometryStorageParams, null );
                ParticleConverter<?> geomConverter = fs.getGeometryConverter( mapping );
                return new DoubleListConverter( pt, (ParticleConverter<TypedObjectNode>) geomConverter );
            }
        }
        return new DefaultPrimitiveConverter( pt, column );
    }
}