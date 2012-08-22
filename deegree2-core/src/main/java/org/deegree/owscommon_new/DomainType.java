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
package org.deegree.owscommon_new;

import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.Values;
import org.deegree.model.crs.CoordinateSystem;

/**
 * <code>DomainType</code> is describes the domain of a parameter according to the OWS common
 * specification 1.0.0. It also implements quite a few extensions.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DomainType extends Parameter {

    private QualifiedName name = null;

    private List<TypedLiteral> values = null;

    private List<Interval> ranges = null;

    private TypedLiteral defaultValue = null;

    private boolean anyValueAllowed = false;

    private String meaning = null;

    private boolean noValuesAllowed = false;

    private CoordinateSystem referenceSystem = null;

    private QualifiedName unitOfMeasure = null;

    private Values valueList = null;

    private Object metadata = null;

    /**
     * Standard constructor that initializes all encapsulated data.
     *
     * @param optional
     * @param repeatable
     * @param description
     * @param direction
     * @param name
     * @param values
     * @param ranges
     * @param defaultValue
     * @param anyValueAllowed
     * @param meaning
     * @param noValuesAllowed
     * @param referenceSystem
     * @param unitOfMeasure
     * @param valueList
     * @param metadata
     */
    public DomainType( boolean optional, boolean repeatable, String description, int direction, QualifiedName name,
                       List<TypedLiteral> values, List<Interval> ranges, TypedLiteral defaultValue,
                       boolean anyValueAllowed, String meaning, boolean noValuesAllowed,
                       CoordinateSystem referenceSystem, QualifiedName unitOfMeasure, Values valueList, Object metadata ) {
        super( optional, repeatable, description, direction );

        this.name = name;
        this.values = values;
        this.ranges = ranges;
        this.defaultValue = defaultValue;
        this.anyValueAllowed = anyValueAllowed;
        this.meaning = meaning;
        this.noValuesAllowed = noValuesAllowed;
        this.referenceSystem = referenceSystem;
        this.unitOfMeasure = unitOfMeasure;
        this.valueList = valueList;
        this.metadata = metadata;
    }

    /**
     * @return Returns whether any value is allowed.
     */
    public boolean isAnyValueAllowed() {
        return anyValueAllowed;
    }

    /**
     * @return Returns the defaultValue.
     */
    public TypedLiteral getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return Returns the meaning.
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * @return Returns the metadata.
     */
    public Object getMetadata() {
        return metadata;
    }

    /**
     * @return Returns the name.
     */
    public QualifiedName getName() {
        return name;
    }

    /**
     * @return Returns the noValuesAllowed.
     */
    public boolean areNoValuesAllowed() {
        return noValuesAllowed;
    }

    /**
     * @return Returns the ranges.
     */
    public List<Interval> getRanges() {
        return ranges;
    }

    /**
     * @return Returns the referenceSystem.
     */
    public CoordinateSystem getReferenceSystem() {
        return referenceSystem;
    }

    /**
     * @return Returns the unitOfMeasure.
     */
    public QualifiedName getUnitOfMeasure() {
        return unitOfMeasure;
    }

    /**
     * @return Returns the valueList.
     */
    public Values getValueList() {
        return valueList;
    }

    /**
     * @return Returns the values.
     */
    public List<TypedLiteral> getValues() {
        return values;
    }

}
