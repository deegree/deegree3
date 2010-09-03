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
package org.deegree.protocol.ows.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * The <code>Domain</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Domain {

    private String name;

    private PossibleValues possibleValues;

    private String defaultValue;

    private String meaningName;

    private String meaningURL;

    private String dataTypeName;

    private String dataTypeURL;

    private ValuesUnit valuesUnit;

    private List<Pair<URL, URL>> metadata;

    /**
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return name, may be <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * @param possibleValues
     */
    public void setPossibleValues( PossibleValues possibleValues ) {
        this.possibleValues = possibleValues;
    }

    /**
     * @return possibleValues, may be <code>null</code>
     */
    public PossibleValues getPossibleValues() {
        return possibleValues;
    }

    /**
     * @param defaultValue
     */
    public void setDefaultValue( String defaultValue ) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return defaultValue, may be <code>null</code>
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param meaningName
     */
    public void setMeaningName( String meaningName ) {
        this.meaningName = meaningName;
    }

    /**
     * @param meaningURL
     */
    public void setMeaningURL( String meaningURL ) {
        this.meaningURL = meaningURL;
    }

    /**
     * @return meaningName, may be <code>null</code>
     */
    public String getMeaningName() {
        return meaningName;
    }

    /**
     * @return meaningURL, may be <code>null</code>
     */
    public String getMeaningURL() {
        return meaningURL;
    }

    /**
     * @param dataTypeName
     */
    public void setDataTypeName( String dataTypeName ) {
        this.dataTypeName = dataTypeName;
    }

    /**
     * @param dataTypeURL
     */
    public void setDataTypeURL( String dataTypeURL ) {
        this.dataTypeURL = dataTypeURL;
    }

    /**
     * @return dataTypeName, may be <code>null</code>
     */
    public String getDataTypeName() {
        return dataTypeName;
    }

    /**
     * @return dataTypeURL, may be <code>null</code>
     */
    public String getDataTypeURL() {
        return dataTypeURL;
    }

    /**
     * @param valuesUnit
     */
    public void setValuesUnit( ValuesUnit valuesUnit ) {
        this.valuesUnit = valuesUnit;
    }

    /**
     * @return valuesUnit, may be <code>null</code>
     */
    public ValuesUnit getValuesUnit() {
        return valuesUnit;
    }

    /**
     * @return metadata, never <code>null</code>
     */
    public List<Pair<URL, URL>> getMetadata() {
        if ( metadata == null ) {
            metadata = new ArrayList<Pair<URL, URL>>();
        }
        return metadata;
    }

}
