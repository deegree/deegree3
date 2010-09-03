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
package org.deegree.protocol.wps.client.input.type;

import java.net.URL;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.ows.metadata.Range;
import org.deegree.protocol.wps.client.param.ValueWithRef;

/**
 * {@link InputType} that defines a literal input.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LiteralInputType extends InputType {

    private ValueWithRef dataType;

    private ValueWithRef defaultUom;

    private ValueWithRef[] supportedUoms;

    private String[] allowedValues;

    /**
     * Triple for (minValue, maxValue, spacing). Both minValue and maxValue are included in the interval. When the
     * interval is continuous spacing is set to: 1 (for integers), 0 (for floats)
     */
    private Range[] range;

    private boolean anyValue;

    private ValueWithRef reference;

    /**
     * Creates a new {@link LiteralInputType} instance.
     * 
     * @param id
     * @param inputTitle
     * @param inputAbstract
     * @param minOccurs
     * @param maxOccurs
     * @param dataType
     * @param defaultUom
     * @param supportedUoms
     * @param allowedValues
     * @param range
     * @param anyValue
     * @param reference
     */
    public LiteralInputType( CodeType id, LanguageString inputTitle, LanguageString inputAbstract, String minOccurs,
                             String maxOccurs, ValueWithRef dataType, ValueWithRef defaultUom,
                             ValueWithRef[] supportedUoms, String[] allowedValues, Range[] range, boolean anyValue,
                             ValueWithRef reference ) {
        super( id, inputTitle, inputAbstract, minOccurs, maxOccurs );
        this.dataType = dataType;
        this.defaultUom = defaultUom;
        this.supportedUoms = supportedUoms;
        this.allowedValues = allowedValues;
        this.range = range;
        this.anyValue = anyValue;
        this.reference = reference;
    }

    @Override
    public Type getType() {
        return Type.LITERAL;
    }

    /**
     * 
     * @return a string array with the concrete values the input can take
     */
    public String[] getAllowedValues() {
        return allowedValues;
    }

    /**
     * 
     * @return an array of {@link Range} instances, each describing the interval in which the input values can be.
     */
    public Range[] getRanges() {
        return range;
    }

    /**
     * Returns a {@link ValueWithRef} instance (that encapsulates a String and an {@link URL}), as data type for the
     * literal input.
     * 
     * @return the data type of the literal input
     */
    public ValueWithRef getDataType() {
        return dataType;
    }

    /**
     * Returns a {@link ValueWithRef} instance (that encapsulates a String and an {@link URL}), as default
     * Unit-of-measure for the literal input.
     * 
     * @return default Unit-of-measure for the literal input
     */
    public ValueWithRef getDefaultUom() {
        return defaultUom;
    }

    /**
     * Returns an array of {@link ValueWithRef} instances (that encapsulates a String and an {@link URL}), as default
     * Unit-of-measure for the literal input.
     * 
     * @return an array of supported Unit-of-measure instance for the literal input
     */
    public ValueWithRef[] getSupportedUoms() {
        return supportedUoms;
    }

    /**
     * Returns whether any value is accepted as input or not.
     * 
     * @return true, if any value is accepted as input. False otherwise.
     */
    public boolean isAnyValue() {
        return anyValue;
    }
}
