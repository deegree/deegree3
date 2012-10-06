//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.time.primitive;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.GMLStdProps;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TemporalPosition;

/**
 * Zero-dimensional geometric primitive that represents an identifiable position in time.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TimeInstant implements TimeGeometricPrimitive, Comparable<TimeInstant> {

    private final String id;

    private final GMLStdProps gmlProps;

    private final TemporalPosition value;

    public TimeInstant( String id, GMLStdProps stdProps, TemporalPosition value ) {
        this.id = id;
        this.gmlProps = stdProps;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public GMLStdProps getGMLProperties() {
        return gmlProps;
    }

    /**
     * Returns the {@link TemporalPosition}.
     * 
     * @return temporal position, never <code>null</code>
     */
    public TemporalPosition getPosition() {
        return value;
    }

    @Override
    public List<?> getRelatedTimes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFrame() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareTo( TimeInstant that ) {
        return value.compareTo( that.value );
    }

    @Override
    public GMLObjectType getType() {
        throw new UnsupportedOperationException ("Implement me");
    }

    @Override
    public List<Property> getProperties() {
        throw new UnsupportedOperationException ("Implement me");
    }

    @Override
    public List<Property> getProperties( QName propName ) {
        throw new UnsupportedOperationException ("Implement me");
    }
}
