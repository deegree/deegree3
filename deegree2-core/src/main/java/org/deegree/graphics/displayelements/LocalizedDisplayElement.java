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
package org.deegree.graphics.displayelements;

import java.awt.Component;
import java.io.Serializable;

import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.MultiPoint;

/**
 * Basic interface for DisplayElements that are not Geometries but shall be rendered to one or more
 * locations.
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
abstract class LocalizedDisplayElement extends GeometryDisplayElement implements Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 2022439787509226293L;

    /**
     * the object to render
     */
    protected Component renderableObject = null;

    /**
     * Creates a new LocalizedDisplayElement_Impl object.
     *
     * @param feature
     * @param geometry
     * @param renderbaleObject
     */
    LocalizedDisplayElement( Feature feature, MultiPoint geometry, Component renderbaleObject ) {
        super( feature, geometry );
        setRenderableObject( renderbaleObject );
    }

    /**
     * sets the object that shall be rendered
     * @param o to render
     *
     */
    public void setRenderableObject( Component o ) {
        this.renderableObject = o;
    }

}
