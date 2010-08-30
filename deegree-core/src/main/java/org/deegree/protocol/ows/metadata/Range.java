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

/**
 * The <code>Range</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Range {

    private String minimumValue;

    private String maximumValue;

    private String spacing;

    private String rangeClosure;

    /**
     * @return minimumValue, may be <code>null</code>
     */
    public String getMinimumValue() {
        return minimumValue;
    }

    /**
     * @param value
     */
    public void setMinimumValue( String value ) {
        this.minimumValue = value;
    }

    /**
     * @return maximumValue, may be <code>null</code>
     */
    public String getMaximumValue() {
        return maximumValue;
    }

    /**
     * @param value
     */
    public void setMaximumValue( String value ) {
        this.maximumValue = value;
    }

    /**
     * @return spacing, may be <code>null</code>.
     */
    public String getSpacing() {
        return spacing;
    }

    /**
     * @param value
     */
    public void setSpacing( String value ) {
        this.spacing = value;
    }

    /**
     * @param rangeClosure
     */
    public void setRangeClosure( String rangeClosure ) {
        this.rangeClosure = rangeClosure;
    }

    /**
     * @return rangeClosure, may be <code>null</code>.
     */
    public String getRangeClosure() {
        return rangeClosure;
    }

}
