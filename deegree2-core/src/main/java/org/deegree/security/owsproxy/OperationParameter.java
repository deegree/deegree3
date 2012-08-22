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
package org.deegree.security.owsproxy;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Bean like class for encapsulating parameters of a owsProxy policy pre/post condition
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OperationParameter {

    private boolean userCoupled = false;

    private String name = null;

    private List<String> values;

    private List<Element> complexValues;

    private boolean any = false;

    /**
     * @param name
     * @param values
     * @param complexValues
     * @param userCoupled
     */
    public OperationParameter( String name, List<String> values, List<Element> complexValues, boolean userCoupled ) {
        this.name = name;
        this.values = values;
        this.complexValues = complexValues;
        this.userCoupled = userCoupled;
    }

    /**
     * @param name
     * @param any
     */
    public OperationParameter( String name, boolean any ) {
        this.any = any;
        this.name = name;
    }

    /**
     * @return Returns the name of a parameter
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     *
     * @return all values
     */
    public List<String> getValues() {
        return values;
    }

    /**
     *
     * @return all values
     */
    public List<Element> getComplexValues() {
        return complexValues;
    }

    /**
     * returns the first value of the list as integer. This is useful for operation parameter that
     * only allow one single string expression (e.g. BBOX)
     *
     * @return first value of a list as String
     */
    public String getFirstAsString() {
        return values.get( 0 );
    }

    /**
     * returns the first value of the list as integer. This is useful for operation parameter that
     * only allow one single integer expression (e.g. maxHeight)
     *
     * @return first value of a list as integer
     */
    public int getFirstAsInt() {
        return Integer.parseInt( values.get( 0 ) );
    }

    /**
     * returns the first value of the list as double. This is useful for operation parameter that
     * only allow one single double expression (e.g. resolution)
     *
     * @return first value of a list as double
     */
    public double getFirstAsDouble() {
        return Double.parseDouble( values.get( 0 ) );
    }

    /**
     *
     * @param values
     */
    public void setValues( List<String> values ) {
        this.values.clear();
        this.values = values;
    }

    /**
     *
     * @param complexValues
     */
    public void setComplexValues( List<Element> complexValues ) {
        this.complexValues.clear();
        this.complexValues = complexValues;
    }

    /**
     *
     * @param value
     */
    public void addValue( String value ) {
        values.add( value );
    }

    /**
     *
     * @param complexValue
     */
    public void addComplexValue( Element complexValue ) {
        complexValues.add( complexValue );
    }

    /**
     *
     * @param value
     */
    public void removeValue( String value ) {
        values.remove( value );
    }

    /**
     * @return Returns the userCoupled.
     */
    public boolean isUserCoupled() {
        return userCoupled;
    }

    /**
     * @param userCoupled
     *            The userCoupled to set.
     */
    public void setUserCoupled( boolean userCoupled ) {
        this.userCoupled = userCoupled;
    }

    /**
     * @return Returns the all.
     */
    public boolean isAny() {
        return any;
    }

    /**
     * @param any
     */
    public void setAny( boolean any ) {
        this.any = any;
    }

}
