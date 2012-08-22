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
import java.net.URL;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ParameterValueIm extends GeneralParameterValueIm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object value = null;

    /**
     * @param descriptor
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor ) {
        super( descriptor );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, Object value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, String value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, URL value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, int value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, double value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @param descriptor
     * @param value
     */
    public ParameterValueIm( GeneralOperationParameterIm descriptor, boolean value ) {
        super( descriptor );
        setValue( value );
    }

    /**
     * @return the value as a boolean.
     * @throws InvalidParameterTypeException
     */
    public boolean booleanValue()
                            throws InvalidParameterTypeException {
        return ( (Boolean) value ).booleanValue();
    }

    /**
     * @return the value as a double.
     * @throws InvalidParameterTypeException
     */
    public double doubleValue()
                            throws InvalidParameterTypeException {
        return ( (Double) value ).doubleValue();
    }

    /**
     * @return the value list as an array of doubles..
     * @throws InvalidParameterTypeException
     */
    public double[] doubleValueList()
                            throws InvalidParameterTypeException {
        return (double[]) value;
    }

    /**
     * @return the value.
     */
    public String getUnit() {
        return (String) value;
    }

    /**
     * @return the value as an object.
     *
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return the value as an int
     * @throws InvalidParameterTypeException
     */
    public int intValue()
                            throws InvalidParameterTypeException {
        return ( (Integer) value ).intValue();
    }

    /**
     * @return the value as an array of integers
     * @throws InvalidParameterTypeException
     */
    public int[] intValueList()
                            throws InvalidParameterTypeException {
        return (int[]) value;
    }

    /**
     * @param unit to set
     * @throws InvalidParameterTypeException
     */
    public void setUnit( String unit )
                            throws InvalidParameterTypeException {
        value = unit;
    }

    /**
     * @param value to set
     * @throws InvalidParameterValueException
     */
    public void setValue( boolean value )
                            throws InvalidParameterValueException {
        this.value = Boolean.valueOf( value );
    }

    /**
     * @param value to set
     * @throws InvalidParameterValueException
     */
    public void setValue( double value )
                            throws InvalidParameterValueException {
        this.value = new Double( value );
    }

    /**
     * @param value to set
     * @throws InvalidParameterValueException
     */
    public void setValue( int value )
                            throws InvalidParameterValueException {
        this.value = new Integer( value );
    }

    /**
     * @param value to set
     * @throws InvalidParameterValueException
     *
     */
    public void setValue( Object value )
                            throws InvalidParameterValueException {
        this.value = value;
    }

    /**
     * @return the value as a String
     * @throws InvalidParameterTypeException
     */
    public String stringValue()
                            throws InvalidParameterTypeException {
        return (String) value;
    }

    /**
     * @param value
     */
    public void setValue( String value ) {
        this.value = value;
    }

    /**
     * @return the location of the value file.
     * @throws InvalidParameterTypeException
     */
    public URL valueFile()
                            throws InvalidParameterTypeException {
        return (URL) value;
    }

    /**
     * @param value
     */
    public void setValue( URL value ) {
        this.value = value;
    }

}
