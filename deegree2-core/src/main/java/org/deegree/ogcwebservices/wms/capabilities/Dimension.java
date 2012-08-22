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
package org.deegree.ogcwebservices.wms.capabilities;

import static org.deegree.framework.xml.XMLTools.getNodeAsBoolean;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.w3c.dom.Element;

/**
 * The Dimension element declares the _existence_ of a dimension. The optional element <Dimension> is used in
 * Capabilities XML to declare that one or more dimensional parameters are relevant to the information holdings of that
 * server. The Dimension element does not provide valid values for a Dimension; that is the role of the Extent element
 * described below. A Dimension element includes a required name, a required measurement units specifier, and an
 * optional unitSymbol.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */
public class Dimension {

    private static final NamespaceContext nsContext = getNamespaceContext();

    private String name = null;

    private String unitSymbol = null;

    private String units = null;

    private String defaultValue;

    private boolean multipleValues;

    private boolean nearestValue;

    private boolean current;

    private String values;

    /**
     * constructor initializing the class with the <Dimension>
     *
     * @param name
     * @param units
     * @param unitSymbol
     */
    public Dimension( String name, String units, String unitSymbol ) {
        setName( name );
        setUnits( units );
        setUnitSymbol( unitSymbol );
    }

    /**
     * @param elem
     * @throws XMLParsingException
     */
    public Dimension( Element elem ) throws XMLParsingException {
        name = elem.getAttribute( "name" );
        units = elem.getAttribute( "units" );
        unitSymbol = elem.hasAttribute( "unitSymbol" ) ? elem.getAttribute( "unitSymbol" ) : null;
        defaultValue = elem.hasAttribute( "default" ) ? elem.getAttribute( "default" ) : null;
        multipleValues = getNodeAsBoolean( elem, "@multipleValues", nsContext, false );
        nearestValue = getNodeAsBoolean( elem, "@nearestValue", nsContext, false );
        current = getNodeAsBoolean( elem, "@current", nsContext, false );
        values = elem.getTextContent();
    }

    /**
     * @return the name of the dimension
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the dimension
     *
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the units the dimension is measured
     */
    public String getUnits() {
        return units;
    }

    /**
     * sets the units the dimension is measured
     *
     * @param units
     */
    public void setUnits( String units ) {
        this.units = units;
    }

    /**
     * @return the unit symbols
     */
    public String getUnitSymbol() {
        return unitSymbol;
    }

    /**
     * sets the unit symbols
     *
     * @param unitSymbol
     */
    public void setUnitSymbol( String unitSymbol ) {
        this.unitSymbol = unitSymbol;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "name = " + name + "\n";
        ret += ( "units = " + units + "\n" );
        ret += ( "unitSymbol = " + unitSymbol + "\n" );
        return ret;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue( String defaultValue ) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the multipleValues
     */
    public boolean isMultipleValues() {
        return multipleValues;
    }

    /**
     * @param multipleValues
     *            the multipleValues to set
     */
    public void setMultipleValues( boolean multipleValues ) {
        this.multipleValues = multipleValues;
    }

    /**
     * @return the nearestValue
     */
    public boolean isNearestValue() {
        return nearestValue;
    }

    /**
     * @param nearestValue
     *            the nearestValue to set
     */
    public void setNearestValue( boolean nearestValue ) {
        this.nearestValue = nearestValue;
    }

    /**
     * @return the current
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @param current
     *            the current to set
     */
    public void setCurrent( boolean current ) {
        this.current = current;
    }

    /**
     * @return the values
     */
    public String getValues() {
        return values;
    }

    /**
     * @param values
     *            the values to set
     */
    public void setValues( String values ) {
        this.values = values;
    }

}
