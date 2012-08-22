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

package org.deegree.ogcwebservices.wcts.capabilities.mdprofiles;

import org.deegree.crs.transformations.Transformation;
import org.deegree.model.crs.CoordinateSystem;

/**
 * The <code>TransformationMetadata</code> class implements the MetadataProfile for the description of a
 * Transformation.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TransformationMetadata implements MetadataProfile<Transformation> {

    private final CoordinateSystem sourceCRS;

    private final CoordinateSystem targetCRS;

    private final Transformation transformation;

    private final String description;

    private final String transformID;

    /**
     * @param transformation
     *            described by this metadata
     * @param transformID
     * @param sourceCRS
     *            the source crs
     * @param targetCRS
     *            the target crs
     * @param description
     *            the metadata
     */
    public TransformationMetadata( Transformation transformation, String transformID, CoordinateSystem sourceCRS,
                                   CoordinateSystem targetCRS, String description ) {
        this.transformation = transformation;
        this.transformID = transformID;
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.description = description;
    }

    /**
     * The result may be <code>null</code> in which case the 'default' transformation chain will be used.
     *
     * @return the {@link Transformation} described by this metadata, if <code>null</code> the default transformation
     *         chain should be used.
     */
    public Transformation getParsedMetadataType() {
        return transformation;
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

    /**
     * @return the description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @return the id of the transform as it was configured.
     */
    public final String getTransformID() {
        return transformID;
    }

}
