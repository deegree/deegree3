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
package org.deegree.coverage;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * This class represents an abstract coverage.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractCoverage implements Coverage {

    private GeometryFactory geomFactory = new GeometryFactory();

    private CRS crs;

    private Envelope envelope;

    private SupplementProperties supplementProperties;

    /**
     * Instantiate an AbstractCoverage with no envelope and no rangeset.
     */
    public AbstractCoverage() {
        this( null, null );
    }

    /**
     * Instantiate an AbstractCoverage with given envelope.
     * 
     * @param envelope
     *            The envelope of the coverage.
     */
    public AbstractCoverage( Envelope envelope ) {
        this( envelope, null );
    }

    /**
     * Instantiate an AbstractCoverage with given envelope, rangeset and {@link SupplementProperties}.
     * 
     * @param envelope
     *            The envelope of the coverage.
     * @param supplementProperties
     *            allows the possibility to add general objects to this Coverage.
     */
    public AbstractCoverage( Envelope envelope, SupplementProperties supplementProperties ) {
        setEnvelope( envelope );
        this.supplementProperties = supplementProperties;
        if ( this.supplementProperties == null ) {
            this.supplementProperties = new SupplementProperties();
        }

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
            this.envelope = this.envelope.merge( envelope );
        }
    }

    /**
     * @return the coordinate system of the raster
     */
    public CRS getCoordinateSystem() {
        return crs;
    }

    /**
     * @param crs
     */
    public void setCoordinateSystem( CRS crs ) {
        this.crs = crs;
        if ( envelope != null ) {
            // rb: this is not correct, the values of the envelope should be converted to the given crs, shouldn't they.
            this.envelope = geomFactory.createEnvelope( envelope.getMin().getAsArray(), envelope.getMax().getAsArray(),
                                                        crs );
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return supplementProperties.getName();
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        supplementProperties.setName( name );
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return supplementProperties.getLabel();
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel( String label ) {
        supplementProperties.setLabel( label );
    }

    /**
     * @return the supplementProperties
     */
    public final SupplementProperties getSupplementProperties() {
        return supplementProperties;
    }

    /**
     * @param supplementProperties
     *            the supplementProperties to set
     */
    public final void setSupplementProperties( SupplementProperties supplementProperties ) {
        this.supplementProperties.clear();
        if ( supplementProperties != null ) {
            this.supplementProperties.putAll( supplementProperties );
        }
    }
}
