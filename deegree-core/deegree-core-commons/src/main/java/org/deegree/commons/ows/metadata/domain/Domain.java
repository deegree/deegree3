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
package org.deegree.commons.ows.metadata.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.StringOrRef;

/**
 * Defines the domain of validity for a quantity with an optional name.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>Valid domain (or allowed set of values) of one quantity,
 * with its name or identifier.</cite>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class Domain {

	private String name;

	private PossibleValues possibleValues;

	private String defaultValue;

	private StringOrRef meaning;

	private StringOrRef dataType;

	private StringOrRef valuesUnitUom;

	private StringOrRef valuesUnitRefSys;

	private List<OMElement> metadata;

	/**
	 * Creates a new {@link Domain} instance with an {@link AllowedValues} value model.
	 * @param name domain name, may be <code>null</code>
	 * @param allowedValues allowed values, must not be <code>null</code>
	 */
	public Domain(String name, List<String> allowedValues) {
		this.name = name;
		List<Values> values = new ArrayList<Values>();
		for (String value : allowedValues) {
			values.add(new Value(value));
		}
		possibleValues = new AllowedValues(values);
		this.metadata = new ArrayList<OMElement>();
	}

	/**
	 * Creates a new {@link Domain} instance with a {@link NoValues} value model and a
	 * default value (as used for service profile constraints, e.g. in WFS 2.0).
	 * @param name domain name, may be <code>null</code>
	 * @param defaultValue default value, must not be <code>null</code>
	 */
	public Domain(String name, String defaultValue) {
		this.name = name;
		possibleValues = new NoValues();
		this.defaultValue = defaultValue;
		this.metadata = new ArrayList<OMElement>();
	}

	/**
	 * Creates a new {@link Domain} instance.
	 * @param name domain name, may be <code>null</code>
	 * @param possibleValues possible values, may be <code>null</code>
	 * @param defaultValue default value, may be <code>null</code>
	 * @param meaning meaning, may be <code>null</code>
	 * @param dataType data type, may be <code>null</code>
	 * @param valuesUnitUom unit-of-measure identifier for the value, may be
	 * <code>null</code>
	 * @param valuesUnitRefSys unit-of-measure reference system identifier, may be
	 * <code>null</code>
	 * @param metadata additional metadata, may be <code>null</code>
	 */
	public Domain(String name, PossibleValues possibleValues, String defaultValue, StringOrRef meaning,
			StringOrRef dataType, StringOrRef valuesUnitUom, StringOrRef valuesUnitRefSys, List<OMElement> metadata) {
		this.name = name;
		this.possibleValues = possibleValues;
		this.defaultValue = defaultValue;
		this.meaning = meaning;
		this.dataType = dataType;
		this.valuesUnitUom = valuesUnitUom;
		this.valuesUnitRefSys = valuesUnitRefSys;
		if (metadata != null) {
			this.metadata = metadata;
		}
		else {
			this.metadata = new ArrayList<OMElement>();
		}
	}

	/**
	 * Returns the name or identifier of the quantity.
	 * <p>
	 * From OWS Common 2.0: <cite>Name or identifier of this quantity.</cite>
	 * </p>
	 * @return quantity name, may be <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the possible values.
	 * <p>
	 * From OWS Common 2.0: <cite>Specifies the possible values of this quantity.</cite>
	 * </p>
	 * @return possible values, may be <code>null</code>
	 */
	public PossibleValues getPossibleValues() {
		return possibleValues;
	}

	/**
	 * @param possibleValues
	 */
	public void setPossibleValues(PossibleValues possibleValues) {
		this.possibleValues = possibleValues;
	}

	/**
	 * Returns the default value.
	 * <p>
	 * From OWS Common 2.0: <cite>The default value for a quantity for which multiple
	 * values are allowed.</cite>
	 * </p>
	 * @return default value, may be <code>null</code>
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the meaning.
	 * <p>
	 * From OWS Common 2.0: <cite>Definition of the meaning or semantics of this set of
	 * values. This Meaning can provide more specific, complete, precise, machine
	 * accessible, and machine understandable semantics about this quantity, relative to
	 * other available semantic information. For example, other semantic information is
	 * often provided in "documentation" elements in XML Schemas or "description" elements
	 * in GML objects.</cite>
	 * </p>
	 * @return meaning, may be <code>null</code>
	 */
	public StringOrRef getMeaning() {
		return meaning;
	}

	/**
	 * @param meaning
	 */
	public void setMeaning(StringOrRef meaning) {
		this.meaning = meaning;
	}

	/**
	 * Returns the data type.
	 * <p>
	 * From OWS Common 2.0: <cite>Definition of the data type of this set of values. In
	 * this case, the xlink:href attribute can reference a URN for a well-known data type.
	 * For example, such a URN could be a data type identification URN defined in the
	 * "ogc" URN namespace.</cite>
	 * </p>
	 * @return data type, may be <code>null</code>
	 */
	public StringOrRef getDataType() {
		return dataType;
	}

	/**
	 * @param dataTypeName
	 */
	public void setDataTypeName(StringOrRef dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns the unit-of-measure identifier.
	 * <p>
	 * From OWS Common 2.0: <cite>Identifier of unit of measure of this set of values.
	 * Should be included then this set of values has units (and not a more complete
	 * reference system).</cite>
	 * </p>
	 * @return unit-of-measure identifier for the value, may be <code>null</code>
	 */
	public StringOrRef getValuesUnitUom() {
		return valuesUnitUom;
	}

	/**
	 * @param valuesUnitUom
	 */
	public void setValuesUnitUom(StringOrRef valuesUnitUom) {
		this.valuesUnitUom = valuesUnitUom;
	}

	/**
	 * Returns the unit reference system identifier.
	 * <p>
	 * From OWS Common 2.0: <cite>Identifier of reference system used by this set of
	 * values. Should be included then this set of values has a reference system (not just
	 * units).</cite>
	 * </p>
	 * @return unit-of-measure reference system identifier, may be <code>null</code>
	 */
	public StringOrRef getValuesUnitRefSys() {
		return valuesUnitRefSys;
	}

	/**
	 * @param valuesUnitRefSys
	 */
	public void setValuesUnitRefSys(StringOrRef valuesUnitRefSys) {
		this.valuesUnitRefSys = valuesUnitRefSys;
	}

	/**
	 * Returns additional metadata.
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of other metadata about this
	 * quantity. A list of required and optional other metadata elements for this quantity
	 * should be specified in the Implementation Specification for this service.</cite>
	 * </p>
	 * <p>
	 * TODO does this need to be typed?
	 * </p>
	 * @return additional metadata, may be <code>null</code>
	 */
	public List<OMElement> getMetadata() {
		return metadata;
	}

}
