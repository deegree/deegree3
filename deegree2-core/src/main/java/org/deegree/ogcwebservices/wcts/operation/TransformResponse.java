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

package org.deegree.ogcwebservices.wcts.operation;

import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.ogcwebservices.wcts.data.TransformableData;
import org.deegree.owscommon_1_1_0.Manifest;

/**
 * <code>TransformResponse</code> wraps the requested inputdata (e.g. the metadata) and the transformed feature
 * collections.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TransformResponse {

    private final boolean store;

    private final Manifest inputData;

    private final TransformableData<?> transformableData;

    private final int dataPresentation;

    private final CoordinateSystem sourceCRS;

    private final CoordinateSystem targetCRS;

    /**
     * @param sourceCRS
     *            defining the source crs of the transformable data.
     * @param targetCRS
     *            defining the target crs of the transformable data.
     * @param dataPresentation
     *            a flag signaling the way the data was presented to the wcts. Valid values are {@link Transform#INLINE}
     *            and {@link Transform#MULTIPART}. If another value is given, {@link Transform#MULTIPART} is assumed.
     * @param store
     *            true if the transformableData should be stored, false otherwise.
     *
     * @param inputData
     *            may be <code>null</code>. Meaning the request did not provide any inputdata or it was a kvp
     *            request.
     * @param transformableData
     *            the transformed data.
     * @throws IllegalArgumentException
     *             if the transformableData object is <code>null</code>.
     */
    public TransformResponse( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, int dataPresentation,
                              boolean store, Manifest inputData, TransformableData<?> transformableData )
                            throws IllegalArgumentException {
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        if ( dataPresentation != Transform.INLINE && dataPresentation != Transform.MULTIPART ) {
            dataPresentation = Transform.MULTIPART;
        }
        this.dataPresentation = dataPresentation;
        this.store = store;
        this.inputData = inputData;
        if ( transformableData == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "WCTS_MISSING_ARGUMENT", transformableData ) );
        }
        this.transformableData = transformableData;
    }

    /**
     * @return true if the data should be stored.
     */
    public final boolean shouldStore() {
        return store;
    }

    /**
     * @return the inputData, may be <code>null</code>
     */
    public final Manifest getInputData() {
        return inputData;
    }

    /**
     * @return the transformableData, will never be <code>null</code>
     */
    public final TransformableData<?> getTransformableData() {
        return transformableData;
    }

    /**
     * @return a flag signaling the way the data was presented to the wcts. Valid values are {@link Transform#INLINE}
     *         and {@link Transform#MULTIPART}.
     */
    public final int getDataPresentation() {
        return dataPresentation;
    }

    /**
     * @return the sourceCRS
     */
    public final CoordinateSystem getSourceCRS() {
        return sourceCRS;
    }

    /**
     * @return the targetCRS
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }

}
