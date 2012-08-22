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

package org.deegree.owscommon.com110;

import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.owscommon.OWSMetadata;

/**
 * FIXME should be renamed.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class OWSDomainType110 {

    /*
     * FIXME When this class OWSDomainType110 gets integrated properly, the class OWSMetadata should
     * be provided with an identifier (private String name), to enable identification of the kind of
     * OWSMetadata. With this handle, the implemented String 'measurementType' becomes obsolete,
     * because its content could be moved into the String 'name' within the OWSMetadata object.
     */

    public static final String REFERENCE_SYSTEM = "REFERENCE_SYSTEM";

    public static final String UOM = "UOM";

    // the choice of one of these four elements is mandatory:
    private OWSAllowedValues allowedValues;

    private boolean anyValue;

    private boolean noValues;

    private OWSMetadata valuesListReference;

    // these elements are optional:
    private TypedLiteral defaultValue;

    private OWSMetadata meaning;

    private OWSMetadata owsDataType;

    private String measurementType; // choice between REFERENCE_SYSTEM and UOM

    private OWSMetadata measurement;

    private OWSMetadata[] metadata;

    // mandatory attribute:
    public String name;

    /**
     * Creates a new <code>DomainType110</code> object. This is the basic constructor, which is
     * called from any other public constructor.
     *
     * @param allowedValues
     * @param anyValue
     * @param noValues
     * @param valuesListReference
     * @param defaultValue
     * @param meaning
     * @param dataType
     * @param measurementType
     * @param measurement
     * @param metadata
     * @param name
     */
    private OWSDomainType110( OWSAllowedValues allowedValues, boolean anyValue, boolean noValues,
                              OWSMetadata valuesListReference, String defaultValue, OWSMetadata meaning,
                              OWSMetadata dataType, String measurementType, OWSMetadata measurement,
                              OWSMetadata[] metadata, String name ) {

        if ( anyValue == true && noValues == true ) {
            throw new IllegalArgumentException( "anyValue and noValues cannot both be true." );
        }

        if ( measurement != null ) {

            if ( measurementType == null ) {
                throw new NullPointerException( "measurementType cannot be null." );
            }
            if ( REFERENCE_SYSTEM.equals( measurementType ) || UOM.equals( measurementType ) ) {
                this.measurementType = measurementType;
            } else {
                throw new IllegalArgumentException( "measurementType must be either " + getClass().getName()
                                                    + ".REFERENCE_SYSTEM or " + getClass().getName() + ".UOM" );
            }
            this.measurement = measurement;
        }

        this.allowedValues = allowedValues;
        this.anyValue = anyValue;
        this.noValues = noValues;
        this.valuesListReference = valuesListReference;
        setDefaultValue( defaultValue );
        this.meaning = meaning;
        this.owsDataType = dataType;
        if ( metadata == null ) {
            this.metadata = new OWSMetadata[0];
        } else {
            this.metadata = metadata;
        }
        this.name = name;

    }

    /**
     * Creates a new <code>DomainType110</code> object.
     *
     * Use this constructor to create the <code>DomainType110</code> object with an
     * <code>OWSAllowedValues</code> object.
     *
     * @param allowedValues
     * @param defaultValue
     * @param meaning
     * @param dataType
     * @param measurementType
     *            Must be either "REFERENCE_SYSTEM" or "UOM".
     * @param measurement
     * @param metadata
     * @param name
     *            mandatory attribute
     */
    public OWSDomainType110( OWSAllowedValues allowedValues, String defaultValue, OWSMetadata meaning,
                             OWSMetadata dataType, String measurementType, OWSMetadata measurement,
                             OWSMetadata[] metadata, String name ) {

        this( allowedValues, false, false, null, defaultValue, meaning, dataType, measurementType, measurement,
              metadata, name );
    }

    /**
     * Creates a new <code>DomainType110</code> object.
     *
     * Use this constructor to create the <code>DomainType110</code> object when
     * <code>anyValue</code> OR <code>noValues</code> is true.
     *
     * @param anyValue
     *            Cannot be true if noValues is true.
     * @param noValues
     *            Cannot be true if anyValue is true.
     * @param defaultValue
     * @param meaning
     * @param dataType
     * @param measurementType
     *            Must be either "REFERENCE_SYSTEM" or "UOM".
     * @param measurement
     * @param metadata
     * @param name
     *            mandatory attribute
     */
    public OWSDomainType110( boolean anyValue, boolean noValues, String defaultValue, OWSMetadata meaning,
                             OWSMetadata dataType, String measurementType, OWSMetadata measurement,
                             OWSMetadata[] metadata, String name ) {

        this( null, anyValue, noValues, null, defaultValue, meaning, dataType, measurementType, measurement, metadata,
              name );
    }

    /**
     * Creates a new <code>DomainType110</code> object.
     *
     * Use this constructor to create the <code>DomainType110</code> object with an
     * <code>OWSMetadata</code> object of valuesListReference.
     *
     * @param valuesListReference
     * @param defaultValue
     * @param meaning
     * @param dataType
     * @param measurementType
     *            Must be either "REFERENCE_SYSTEM" or "UOM".
     * @param measurement
     * @param metadata
     * @param name
     *            mandatory attribute
     */
    public OWSDomainType110( OWSMetadata valuesListReference, String defaultValue, OWSMetadata meaning,
                             OWSMetadata dataType, String measurementType, OWSMetadata measurement,
                             OWSMetadata[] metadata, String name ) {

        this( null, false, false, valuesListReference, defaultValue, meaning, dataType, measurementType, measurement,
              metadata, name );
    }

    /**
     * @return Returns the allowedValues.
     */
    public OWSAllowedValues getAllowedValues() {
        return allowedValues;
    }

    /**
     * @return Returns the anyValue.
     */
    public boolean isAnyValue() {
        return anyValue;
    }

    /**
     * @return Returns the noValues.
     */
    public boolean hasNoValues() {
        return noValues;
    }

    /**
     * @return Returns the valuesListReference.
     */
    public OWSMetadata getValuesListReference() {
        return valuesListReference;
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
    public OWSMetadata getMeaning() {
        return meaning;
    }

    /**
     * @return Returns the owsDataType.
     */
    public OWSMetadata getOwsDataType() {
        return owsDataType;
    }

    /**
     * Returns the measurementType, or null if the measurement object is null.
     *
     * @return Returns the measurementType.
     */
    public String getMeasurementType() {
        return measurementType;
    }

    /**
     * @return Returns the measurement.
     */
    public OWSMetadata getMeasurement() {
        return measurement;
    }

    /**
     * Returns an array of OWSMetadata objects. If metadata is null, an array of size 0 is returned.
     *
     * @return Returns the metadata.
     */
    public OWSMetadata[] getMetadata() {
        return metadata;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue( TypedLiteral defaultValue ) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the defaultValue to a new TypedLiteral with the given value.
     *
     * @param defaultURI
     */
    private void setDefaultValue( String value ) {
        this.defaultValue = new TypedLiteral( value, null );
    }

}
