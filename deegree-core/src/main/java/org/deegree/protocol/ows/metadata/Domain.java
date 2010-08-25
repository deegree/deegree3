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

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * The <code></code> class TODO add class documentation here.
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

    private List<Pair<String, String>> metadata;

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPossibleValues( PossibleValues possibleValues ) {
        this.possibleValues = possibleValues;
    }

    public PossibleValues getPossibleValues() {
        return possibleValues;
    }

    public void setDefaultValue( String defaultValue ) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setMeaningName( String meaningName ) {
        this.meaningName = meaningName;
    }

    public void setMeaningURL( String meaningURL ) {
        this.meaningURL = meaningURL;
    }

    public String getMeaningName() {
        return meaningName;
    }

    public String getMeaningURL() {
        return meaningURL;
    }

    public void setDataTypeName( String dataTypeName ) {
        this.dataTypeName = dataTypeName;
    }

    public void setDataTypeURL( String dataTypeURL ) {
        this.dataTypeURL = dataTypeURL;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public String getDataTypeURL() {
        return dataTypeURL;
    }

    public void setValuesUnit( ValuesUnit valuesUnit ) {
        this.valuesUnit = valuesUnit;
    }

    public ValuesUnit getValuesUnit() {
        return valuesUnit;
    }

    public List<Pair<String, String>> getMetadata() {
        if ( metadata == null ) {
            metadata = new ArrayList<Pair<String, String>>();
        }
        return metadata;
    }

}
