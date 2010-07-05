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
package org.deegree.commons.tom.primitive;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.Pair;

/**
 * {@link TypedObjectNode} that represents a primitive value, e.g. an XML text node or an XML attribute value with type
 * information.
 * 
 * @see PrimitiveType
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PrimitiveValue implements TypedObjectNode, Comparable<PrimitiveValue> {

    private final Object value;

    private final String textValue;

    private final XSSimpleTypeDefinition xsdType;

    private final PrimitiveType type;

    /**
     * @param value
     * @param type
     * @throws IllegalArgumentException
     */
    public PrimitiveValue( String value, PrimitiveType type ) throws IllegalArgumentException {
        this.value = XMLValueMangler.xmlToInternal( value, type );
        this.textValue = value;
        this.xsdType = null;
        this.type = type;
    }

    /**
     * @param value
     * @param xsdType
     * @throws IllegalArgumentException
     */
    public PrimitiveValue( String value, XSSimpleTypeDefinition xsdType ) throws IllegalArgumentException {
        this.textValue = value;
        this.xsdType = xsdType;
        this.type = XMLValueMangler.getPrimitiveType( xsdType );
        this.value = XMLValueMangler.xmlToInternal( value, type );
    }

    /**
     * @param value
     * @throws IllegalArgumentException
     */
    public PrimitiveValue( Object value ) throws IllegalArgumentException {
        this.textValue = XMLValueMangler.internalToXML( value );
        this.xsdType = null;
        this.type = PrimitiveType.determinePrimitiveType( value );
        this.value = value;
    }

    /**
     * Returns the canonical object representation of the value.
     * 
     * @return the canonical object representation of the value, never <code>null</code>
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the text representation of the value.
     * 
     * @return the text representation of the value, never <code>null</code>
     */
    public String getAsText() {
        return textValue;
    }

    /**
     * Returns the type of the value.
     * 
     * @return the type of the value, never <code>null</code>
     */
    public PrimitiveType getType() {
        return type;
    }

    /**
     * Returns the XML schema type for the value.
     * 
     * @return the XML schema type for the value, can be <code>null</code>
     */
    public XSSimpleTypeDefinition getXSType() {
        return xsdType;
    }

    @Override
    public int compareTo( PrimitiveValue o ) {
        Pair<Object, Object> comparables = makeComparable( value, o.value );
        return ( (Comparable) comparables.first ).compareTo( ( (Comparable) comparables.second ) );
    }

    @Override
    public boolean equals( Object o ) {

        // TODO make this failproof
        Object thatValue = o;
        if ( o instanceof PrimitiveValue ) {
            thatValue = ( (PrimitiveValue) o ).value;
        }

        Pair<Object, Object> comparablePair = makeComparable( value, thatValue );

        // NOTE: don't use #equals() for BigDecimal, because new BigDecimal("155.00") is not equal to
        // new BigDecimal("155")
        if ( comparablePair.first instanceof BigDecimal ) {
            return ( ( (BigDecimal) comparablePair.first ).compareTo( (BigDecimal) comparablePair.second ) == 0 );
        }
        return comparablePair.first.equals( comparablePair.second );
    }

    @Override
    public int hashCode() {
        // TODO: see ticket #113
        return value.hashCode();
    }

    @Override
    public String toString() {
        return textValue;
    }

    /**
     * @param value1
     * @param value2
     * @return should be a ComparablePair now that we have it...
     * @throws IllegalArgumentException
     */
    public static Pair<Object, Object> makeComparable( Object value1, Object value2 )
                            throws IllegalArgumentException {
        Pair<Object, Object> result = new Pair<Object, Object>( value1, value2 );
        if ( !( value1 instanceof String ) ) {
            if ( value1 instanceof Number ) {
                result = new Pair<Object, Object>( value1, new BigDecimal( value2.toString() ) );
            } else if ( value1 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( value1, new Date( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value1 instanceof DateTime ) {
                try {
                    result = new Pair<Object, Object>( value1, new DateTime( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value1 instanceof Time ) {
                try {
                    result = new Pair<Object, Object>( value1, new Time( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value1 instanceof CodeType ) {
                result = new Pair<Object, Object>( value1, new CodeType( value2.toString(),
                                                                         ( (CodeType) value1 ).getCodeSpace() ) );
            } else if ( value1 instanceof Measure ) {
                result = new Pair<Object, Object>( value1, new Measure( value2.toString(),
                                                                        ( (Measure) value1 ).getUomUri() ) );
            }
        } else if ( !( value2 instanceof String ) ) {
            if ( value2 instanceof Number ) {
                result = new Pair<Object, Object>( new BigDecimal( value1.toString() ), value2 );
            } else if ( value2 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( new Date( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value2 instanceof DateTime ) {
                try {
                    result = new Pair<Object, Object>( new DateTime( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value2 instanceof Time ) {
                try {
                    result = new Pair<Object, Object>( new Time( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new IllegalArgumentException( e.getMessage() );
                }
            } else if ( value1 instanceof CodeType ) {
                result = new Pair<Object, Object>( new CodeType( value1.toString(),
                                                                 ( (CodeType) value2 ).getCodeSpace() ), value2 );
            } else if ( value1 instanceof Measure ) {
                result = new Pair<Object, Object>( new Measure( value1.toString(), ( (Measure) value2 ).getUomUri() ),
                                                   value2 );
            }
        }

        // TODO create comparable numbers in a more efficient manner
        if ( result.first instanceof Number && !( result.first instanceof BigDecimal ) ) {
            result.first = new BigDecimal( result.first.toString() );
        }
        if ( result.second instanceof Number && !( result.second instanceof BigDecimal ) ) {
            result.second = new BigDecimal( result.second.toString() );
        }

        return result;
    }
}
