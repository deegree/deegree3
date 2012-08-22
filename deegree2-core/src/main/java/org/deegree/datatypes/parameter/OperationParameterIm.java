// $HeadURL$
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
package org.deegree.datatypes.parameter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OperationParameterIm extends GeneralOperationParameterIm implements Serializable {

    private static final long serialVersionUID = -837184501017553294L;

    private Comparable<?> maximumValue;

    private Comparable<?> minimumValue;

    private Set<?> validValues;

    private Class<?> valueClass;

    private Object defaultValue;

    /**
     * Convenience constructor.
     *
     * @param name
     * @param validValues
     */
    public OperationParameterIm( String name, String[] validValues ) {
        this( name, null, 1, 0, null, null, buildSet( validValues ), String.class, null );
    }

    /**
     * Convenience constructor.
     *
     * @param name
     * @param validValues
     * @param defaultValue to default to.
     */
    public OperationParameterIm( String name, String[] validValues, Object defaultValue ) {
        this( name, null, 1, 0, null, null, buildSet( validValues ), defaultValue.getClass(), defaultValue );
    }

    /**
     * @param name
     * @param remarks
     * @param maximumOccurs
     * @param minimumOccurs
     * @param maximumValue
     * @param minimumValue
     * @param validValues
     * @param valueClass
     * @param defaultValue
     */
    public OperationParameterIm( String name, String remarks, int maximumOccurs, int minimumOccurs,
                                 Comparable<?> maximumValue, Comparable<?> minimumValue, Set<?> validValues, Class<?> valueClass,
                                 Object defaultValue ) {
        super( name, remarks, maximumOccurs, minimumOccurs );
        this.maximumValue = maximumValue;
        this.minimumValue = minimumValue;
        this.validValues = validValues;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    /**
     * @return Returns the defaultValue.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue( Object defaultValue ) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return Returns the maximumValue.
     *
     */
    public Comparable<?> getMaximumValue() {
        return maximumValue;
    }

    /**
     * @param maximumValue
     *            The maximumValue to set.
     */
    public void setMaximumValue( Comparable<?> maximumValue ) {
        this.maximumValue = maximumValue;
    }

    /**
     * @return Returns the minimumValue.
     */
    public Comparable<?> getMinimumValue() {
        return minimumValue;
    }

    /**
     * @param minimumValue
     *            The minimumValue to set.
     */
    public void setMinimumValue( Comparable<?> minimumValue ) {
        this.minimumValue = minimumValue;
    }

    /**
     * @return Returns the validValues.
     */
    public Set<?> getValidValues() {
        return validValues;
    }

    /**
     * @param validValues
     *            The validValues to set.
     */
    public void setValidValues( Set<?> validValues ) {
        this.validValues = validValues;
    }

    /**
     * @return Returns the valueClass.
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    /**
     * @param valueClass
     *            The valueClass to set.
     */
    public void setValueClass( Class<?> valueClass ) {
        this.valueClass = valueClass;
    }

    /**
     *
     * @param values
     * @return a set of the given String.
     */
    private static Set<String> buildSet( String[] values ) {
        Set<String> valueSet = new LinkedHashSet<String>();
        if ( values != null ) {
            for ( int i = 0; i < values.length; i++ ) {
                valueSet.add( values[i] );
            }
        }
        return valueSet;
    }
}
