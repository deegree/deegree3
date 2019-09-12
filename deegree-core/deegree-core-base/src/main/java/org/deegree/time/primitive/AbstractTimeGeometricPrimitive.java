/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

import org.deegree.commons.tom.gml.property.Property;

abstract class AbstractTimeGeometricPrimitive extends AbstractTimePrimitive implements TimeGeometricPrimitive {

    private final String frame;

    /**
     * Creates a new {@link AbstractTimeGeometricPrimitive} instance.
     *
     * @param id
     *            gml id, can be <code>null</code>
     * @param props
     *            can be empty, but must not be <code>null</code>
     * @param relatedTimes
     *            can be empty, but must not be <code>null</code>
     * @param frame
     *            time frame, can be <code>null</code>
     */
    protected AbstractTimeGeometricPrimitive( final String id, final List<Property> props,
                                              final List<RelatedTime> relatedTimes, final String frame ) {
        super( id, props, relatedTimes );
        this.frame = frame;
    }

    @Override
    public String getFrame() {
        return frame;
    }
}
