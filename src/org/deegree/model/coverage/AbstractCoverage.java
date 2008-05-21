//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Envelope;

/**
 * 
 * This class represents an abstract coverage.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class AbstractCoverage {

    private GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private CoordinateSystem crs;
    
    private Envelope envelope;

    /**
     * Instantiate an AbstractCoverage with no envelope.
     */
    public AbstractCoverage() {
        this( null );
    }

    /**
     * Instantiate an AbstractCoverage with given envelope.
     * 
     * @param envelope
     *            The envelope of the coverage.
     */
    public AbstractCoverage( Envelope envelope ) {
        setEnvelope( envelope );
    }

    /**
     * @return GeometryFactory for creation of envelopes, etc.
     */
    protected GeometryFactory getGeometryFactory() {
        return geomFactory;
    }

    /**
     * @return The envelope of the coverage.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * @param envelope
     *            New envelope for the coverage.
     */
    protected void setEnvelope( Envelope envelope ) {
        if ( envelope != null && envelope.getCoordinateSystem() != null ) {
            setCoordinateSystem( envelope.getCoordinateSystem() );
        }
        this.envelope = envelope;
    }

    /**
     * Extend the envelope of the coverage.
     * 
     * The new envelope of the coverage will contain the old and the given envelope.
     * 
     * @param envelope
     *            Envelope to add.
     */
    protected void extendEnvelope( Envelope envelope ) {
        if ( this.envelope == null ) {
            this.envelope = envelope;
        } else {
            this.envelope = this.envelope.merger( envelope );
        }
    }

    /**
     * @return the coordinate system of the raster
     */
    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }

    /**
     * @param crs
     */
    public void setCoordinateSystem( CoordinateSystem crs ) {
        this.crs = crs;
    }
}
