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

package org.deegree.services.wpvs.io;

import org.deegree.geometry.Envelope;

/**
 * The <code>ModelBackendInfo</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class ModelBackendInfo {

    private int ordinateCount;

    private int textureOrdinateCount;

    private Envelope datasetEnvelope;

    /**
     * Initialize with 0;
     */
    public ModelBackendInfo() {
        this( 0, 0, null );
    }

    /**
     * @param ordinateCount
     * @param textureOrdinateCount
     * @param envelope
     */
    public ModelBackendInfo( int ordinateCount, int textureOrdinateCount, Envelope envelope ) {
        this.ordinateCount = ordinateCount;
        this.textureOrdinateCount = textureOrdinateCount;
        this.datasetEnvelope = envelope;
    }

    /**
     * Add the ordinates from the given object to the total ordinates
     * 
     * @param object
     */
    public final void addOrdinates( DataObjectInfo<?> object ) {
        ordinateCount += object.getInsertedOrdinates();
        textureOrdinateCount += object.getInsertedTextureOrdinates();
    }

    /**
     * Add the given number of ordinates to the total ordinates
     * 
     * @param numberOfOrdinates
     */
    public final void addOrdinates( int numberOfOrdinates ) {
        ordinateCount += numberOfOrdinates;
    }

    /**
     * Add the given number of ordinates to the total texture ordinates
     * 
     * @param numberOfTextureOrdinates
     */
    public final void addTextureOrdinates( int numberOfTextureOrdinates ) {
        textureOrdinateCount += numberOfTextureOrdinates;
    }

    /**
     * @return the ordinateCount, e.g. the number of floats in the model backend for a given modeltype..
     */
    public final int getOrdinateCount() {
        return ordinateCount;
    }

    /**
     * @return the textureOrdinateCount
     */
    public final int getTextureOrdinateCount() {
        return textureOrdinateCount;
    }

    @Override
    public String toString() {
        return "vertexOrdinates: " + ordinateCount + " | textureOrdinates: " + textureOrdinateCount;
    }

    /**
     * Add the values from the given info to this info.
     * 
     * @param info
     *            to get add the values from.
     */
    public void add( ModelBackendInfo info ) {
        if ( info != null ) {
            this.ordinateCount += info.ordinateCount;
            this.textureOrdinateCount += info.textureOrdinateCount;
            if ( info.getDatasetEnvelope() != null ) {
                if ( this.datasetEnvelope == null ) {
                    this.datasetEnvelope = info.getDatasetEnvelope();
                } else {
                    this.datasetEnvelope = this.datasetEnvelope.merge( info.getDatasetEnvelope() );
                }
            }
        }
    }

    /**
     * @param datasetEnvelope
     *            the datasetEnvelope to set
     */
    public void setDatasetEnvelope( Envelope datasetEnvelope ) {
        this.datasetEnvelope = datasetEnvelope;
    }

    /**
     * @return the datasetEnvelope
     */
    public Envelope getDatasetEnvelope() {
        return datasetEnvelope;
    }

}
