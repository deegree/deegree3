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
package org.deegree.datatypes;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * General data type constants definition. the type values are the same as in
 * <code>java.sql.Types<code>. Except for several geometry types,
 * <code>UNKNOWN</code>, <code>FEATURE</code>, <code>FEATURES</code> and <code>FEATURECOLLECTION</code> that are not
 * known by <code>java.sql.Types</code>.
 * <p>
 * NOTE: Generally, it would be feasible to extend <code>java.sql.Types</code>, but unfortunately, this is not possible,
 * as it's default constructor is not visible.
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see java.sql.Types
 */
public final class Types {

    private static ILogger LOG = LoggerFactory.getLogger( Types.class );

    private static URI GMLNS = CommonNamespaces.GMLNS;

    // generic sql types

    /**
     * maps to java.sql.Types.ARRAY (why not use directly?)
     */
    public final static int ARRAY = java.sql.Types.ARRAY;

    /**
     * maps to java.sql.Types.BIGINT (why not use directly?)
     */
    public final static int BIGINT = java.sql.Types.BIGINT;

    /**
     * maps to java.sql.Types.BINARY (why not use directly?)
     */
    public final static int BINARY = java.sql.Types.BINARY;

    /**
     * maps to java.sql.Types.BIT (why not use directly?)
     */
    public final static int BIT = java.sql.Types.BIT;

    /**
     * maps to java.sql.Types.BLOB (why not use directly?)
     */
    public final static int BLOB = java.sql.Types.BLOB;

    /**
     * maps to java.sql.Types.BOOLEAN (why not use directly?)
     */
    public final static int BOOLEAN = java.sql.Types.BOOLEAN;

    /**
     * maps to java.sql.Types.CHAR (why not use directly?)
     */
    public final static int CHAR = java.sql.Types.CHAR;

    /**
     * maps to java.sql.Types.CLOB (why not use directly?)
     */
    public final static int CLOB = java.sql.Types.CLOB;

    /**
     * maps to java.sql.Types.DATALINK (why not use directly?)
     */
    public final static int DATALINK = java.sql.Types.DATALINK;

    /**
     * maps to java.sql.Types.DATE (why not use directly?)
     */
    public final static int DATE = java.sql.Types.DATE;

    /**
     * maps to java.sql.Types.DECIMAL (why not use directly?)
     */
    public final static int DECIMAL = java.sql.Types.DECIMAL;

    /**
     * maps to java.sql.Types.DISTINCT (why not use directly?)
     */
    public final static int DISTINCT = java.sql.Types.DISTINCT;

    /**
     * maps to java.sql.Types.DOUBLE (why not use directly?)
     */
    public final static int DOUBLE = java.sql.Types.DOUBLE;

    /**
     * maps to java.sql.Types.FLOAT (why not use directly?)
     */
    public final static int FLOAT = java.sql.Types.FLOAT;

    /**
     * maps to java.sql.Types.INTEGER (why not use directly?)
     */
    public final static int INTEGER = java.sql.Types.INTEGER;

    /**
     * maps to java.sql.Types.JAVA_OBJECT (why not use directly?)
     */
    public final static int JAVA_OBJECT = java.sql.Types.JAVA_OBJECT;

    /**
     * maps to java.sql.Types.LONGVARBINARY (why not use directly?)
     */
    public final static int LONGVARBINARY = java.sql.Types.LONGVARBINARY;

    /**
     * maps to java.sql.Types.LONGVARCHAR (why not use directly?)
     */
    public final static int LONGVARCHAR = java.sql.Types.LONGVARCHAR;

    /**
     * maps to java.sql.Types.NULL (why not use directly?)
     */
    public final static int NULL = java.sql.Types.NULL;

    /**
     * maps to java.sql.Types.NUMERIC (why not use directly?)
     */
    public final static int NUMERIC = java.sql.Types.NUMERIC;

    /**
     * maps to java.sql.Types.OTHER (why not use directly?)
     */
    public final static int OTHER = java.sql.Types.OTHER;

    /**
     * maps to java.sql.Types.REAL (why not use directly?)
     */
    public final static int REAL = java.sql.Types.REAL;

    /**
     * maps to java.sql.Types.REF (why not use directly?)
     */
    public final static int REF = java.sql.Types.REF;

    /**
     * maps to java.sql.Types.SMALLINT (why not use directly?)
     */
    public final static int SMALLINT = java.sql.Types.SMALLINT;

    /**
     * maps to java.sql.Types.STRUCT (why not use directly?)
     */
    public final static int STRUCT = java.sql.Types.STRUCT;

    /**
     * maps to java.sql.Types.TIME (why not use directly?)
     */
    public final static int TIME = java.sql.Types.TIME;

    /**
     * maps to java.sql.Types.TIMESTAMP (why not use directly?)
     */
    public final static int TIMESTAMP = java.sql.Types.TIMESTAMP;

    /**
     * maps to java.sql.Types.TINYINT (why not use directly?)
     */
    public final static int TINYINT = java.sql.Types.TINYINT;

    /**
     * maps to java.sql.Types.VARBINARY (why not use directly?)
     */
    public final static int VARBINARY = java.sql.Types.VARBINARY;

    /**
     * maps to java.sql.Types.VARCHAR (why not use directly?)
     */
    public final static int VARCHAR = java.sql.Types.VARCHAR;

    // geometry + gml types

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int GEOMETRY = java.sql.Types.VARCHAR + 10000;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int MULTIGEOMETRY = java.sql.Types.VARCHAR + 10001;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int FEATURE = java.sql.Types.VARCHAR + 10002;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int FEATURECOLLECTION = java.sql.Types.VARCHAR + 10004;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int POINT = java.sql.Types.VARCHAR + 11000;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int CURVE = java.sql.Types.VARCHAR + 11001;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int SURFACE = java.sql.Types.VARCHAR + 11002;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int MULTIPOINT = java.sql.Types.VARCHAR + 11003;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int MULTICURVE = java.sql.Types.VARCHAR + 11004;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int MULTISURFACE = java.sql.Types.VARCHAR + 11005;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int ENVELOPE = java.sql.Types.VARCHAR + 11006;

    /**
     * redefinition of java.sql.Types.VARCHAR
     */
    public static final int ANYTYPE = java.sql.Types.VARCHAR + 11007;

    /**
     * {http://www.opengis.net/gml}:GeGeometryPropertyType
     */
    public static final QualifiedName GEOMETRY_PROPERTY_NAME = new QualifiedName( "GeometryPropertyType", GMLNS );

    /**
     * {http://www.opengis.net/gml}:MultiGeometryPropertyType
     */
    public static final QualifiedName MULTI_GEOMETRY_PROPERTY_NAME = new QualifiedName( "MultiGeometryPropertyType",
                                                                                        GMLNS );

    /**
     * {http://www.opengis.net/gml}:FeaturePropertyType
     */
    public static final QualifiedName FEATURE_PROPERTY_NAME = new QualifiedName( "FeaturePropertyType", GMLNS );

    //
    /**
     * TODO check if this is really needed {http://www.opengis.net/gml}:FeatureArrayPropertyType
     */
    public static final QualifiedName FEATURE_ARRAY_PROPERTY_NAME = new QualifiedName( "FeatureArrayPropertyType",
                                                                                       GMLNS );

    // key instances: Integer, value instances: String
    private static Map<Integer, String> typeNameMap = new HashMap<Integer, String>();

    // key instances: String, value instances: Integer
    private static Map<String, Integer> typeCodeMap = new HashMap<String, Integer>();

    static {
        try {
            Field[] fields = java.sql.Types.class.getFields();
            for ( int i = 0; i < fields.length; i++ ) {
                String typeName = fields[i].getName();
                Integer typeCode = (Integer) fields[i].get( null );
                typeNameMap.put( typeCode, typeName );
                typeCodeMap.put( typeName, typeCode );
            }
        } catch ( Exception e ) {
            BootLogger.logError( "Error populating sql type code maps: " + e.getMessage(), e );
        }
    }

    /**
     * Returns the generic sql type code for the given type name.
     * 
     * @param typeName
     * @return the generic sql type code for the given type name.
     * @throws UnknownTypeException
     *             if the type name is not an sql type name
     * @see java.sql.Types
     */
    public static int getTypeCodeForSQLType( String typeName )
                            throws UnknownTypeException {
        Integer typeCode = typeCodeMap.get( typeName );
        if ( typeCode == null ) {
            throw new UnknownTypeException( "Type name '" + typeName + "' does not denote an sql type." );
        }
        return typeCode.intValue();
    }

    /**
     * Returns the generic sql type name for the given type code.
     * 
     * @param typeCode
     * @return the generic sql type name for the given type code.
     * @throws UnknownTypeException
     *             if the type code is not an sql type code
     * @see java.sql.Types
     */
    public static String getTypeNameForSQLTypeCode( int typeCode )
                            throws UnknownTypeException {
        String typeName = typeNameMap.get( new Integer( typeCode ) );
        if ( typeName == null ) {
            throw new UnknownTypeException( "Type code '" + typeCode + "' does not denote an sql type." );
        }
        return typeName;
    }

    /**
     * mapping between GML-typenames and java-classnames for GML-geometry types
     * 
     * @param gmlTypeName
     *            the name of the GML type name
     * @return the internal type
     * @throws UnknownTypeException
     *             if the given name cannot be mapped to a known type.
     */
    public static int getJavaTypeForGMLType( String gmlTypeName )
                            throws UnknownTypeException {
        if ( "GeometryPropertyType".equals( gmlTypeName ) )
            return Types.GEOMETRY;

        if ( "PointPropertyType".equals( gmlTypeName ) )
            // return Types.POINT;
            return Types.GEOMETRY;

        if ( "MultiPointPropertyType".equals( gmlTypeName ) )
            // return Types.MULTIPOINT;
            return Types.GEOMETRY;

        if ( "PolygonPropertyType".equals( gmlTypeName ) )
            // return Types.SURFACE;
            return Types.GEOMETRY;

        if ( "MultiPolygonPropertyType".equals( gmlTypeName ) )
            // return Types.MULTISURFACE;
            return Types.GEOMETRY;

        if ( "LineStringPropertyType".equals( gmlTypeName ) )
            // return Types.CURVE;
            return Types.GEOMETRY;

        if ( "MultiLineStringPropertyType".equals( gmlTypeName ) )
            // return Types.MULTICURVE;
            return Types.GEOMETRY;

        if ( "CurvePropertyType".equals( gmlTypeName ) )
            // return Types.POINT;
            return Types.GEOMETRY;

        if ( "MultiCurvePropertyType".equals( gmlTypeName ) )
            // return Types.POINT;
            return Types.GEOMETRY;

        if ( "SurfacePropertyType".equals( gmlTypeName ) )
            // return Types.POINT;
            return Types.GEOMETRY;

        if ( "MultiSurfacePropertyType".equals( gmlTypeName ) )
            // return Types.POINT;
            return Types.GEOMETRY;

        throw new UnknownTypeException( "Unsupported Type: '" + gmlTypeName + "'" );
    }

    /**
     * mapping between xml-typenames and java-classnames for XMLSCHEMA-simple types
     * 
     * @param schemaTypeName
     *            of the XML schema type
     * @return the internal type
     * @throws UnknownTypeException
     *             if the given name cannot be mapped to a known type.
     * @todo TODO map them all over registry
     */
    public static int getJavaTypeForXSDType( String schemaTypeName )
                            throws UnknownTypeException {

        if ( "integer".equals( schemaTypeName ) || "int".equals( schemaTypeName ) || "long".equals( schemaTypeName )|| "short".equals( schemaTypeName ) )
            return Types.INTEGER;

        if ( "string".equals( schemaTypeName ) )
            return Types.VARCHAR;

        if ( "date".equals( schemaTypeName ) )
            return Types.DATE;

        if ( "boolean".equals( schemaTypeName ) )
            return Types.BOOLEAN;

        if ( "float".equals( schemaTypeName ) )
            return Types.FLOAT;

        if ( "double".equals( schemaTypeName ) )
            return Types.DOUBLE;

        if ( "decimal".equals( schemaTypeName ) )
            return Types.DECIMAL;

        if ( "dateTime".equals( schemaTypeName ) )
            return Types.TIMESTAMP;

        if ( "time".equals( schemaTypeName ) )
            return Types.TIME;

        if ( "date".equals( schemaTypeName ) )
            return Types.DATE;

        if ( "anyURI".equals( schemaTypeName ) )
            return Types.VARCHAR;

        if ( "anyType".equals( schemaTypeName ) )
            return Types.ANYTYPE;

        throw new UnknownTypeException( "Unsupported Type:" + schemaTypeName );
    }

    /**
     * Scale is set to 0.
     * 
     * @param type
     *            SQL datatype code
     * @param precision
     *            precision (just used for type NUMERIC)
     * @return typename
     */
    public static String getXSDTypeForSQLType( int type, int precision ) {
        return getXSDTypeForSQLType( type, precision, 0 );
    }

    /**
     * 
     * @param type
     *            SQL datatype code
     * @param precision
     *            precision (just used for type NUMERIC)
     * @param scale
     *            scale (just used for type NUMERIC)
     * @return typename
     */
    public static String getXSDTypeForSQLType( int type, int precision, int scale ) {
        String s = null;

        switch ( type ) {
        case Types.VARCHAR:
        case Types.CHAR:
            s = "string";
            break;
        case Types.NUMERIC: {
            if ( precision == 0 || scale > 0 ) {
                s = "double";
                break;
            }
            s = "integer";
            break;
        }
        case Types.DECIMAL:
            s = "decimal";
            break;
        case Types.DOUBLE:
        case Types.REAL:
            s = "double";
            break;
        case Types.FLOAT:
            s = "float";
            break;
        case Types.INTEGER:
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.BIGINT:
            s = "integer";
            break;
        case Types.TIMESTAMP:
            s = "dateTime";
            break;
        case Types.TIME:
            s = "time";
            break;
        case Types.DATE:
            s = "date";
            break;
        case Types.CLOB:
            s = "string";
            break;
        case Types.BIT:
        case Types.BOOLEAN:
            s = "boolean";
            break;
        case Types.GEOMETRY:
        case Types.OTHER:
        case Types.STRUCT:
            s = "gml:GeometryPropertyType";
            break;
        case Types.FEATURE:
            s = "gml:FeaturePropertyType";
            break;
        default:
            LOG.logWarning( "could not determine XSDType for SQLType; using 'XXX': " + type );
            s = "code: " + type;
        }
        return s;
    }

}
