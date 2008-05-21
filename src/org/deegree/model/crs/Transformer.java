//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
package org.deegree.model.crs;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.crs.exceptions.UnknownCRSException;
import org.deegree.model.crs.transformations.TransformationFactory;
import org.deegree.model.crs.transformations.coordinate.CRSTransformation;
import org.deegree.model.i18n.Messages;

/**
 * Abstract base class for all transformers. Stores a target coordinate system and creates {@link CRSTransformation}
 * objects for a given source CRS.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public abstract class Transformer {

    private final CoordinateSystem targetCRS;

    /**
     * Creates a new Transformer object, with the given target CRS.
     * 
     * @param targetCRS
     *            to transform incoming coordinates to.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     */
    protected Transformer( final CoordinateSystem targetCRS ) throws IllegalArgumentException {
        if ( targetCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "Transformer(CoordinateSystem)", "targetCRS" ) );
        }
        this.targetCRS = targetCRS;
    }

    /**
     * Creates a new Transformer object, with the given id as the target CRS.
     * 
     * @param targetCRS
     *            an identifier to which all incoming coordinates shall be transformed.
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    protected Transformer( String targetCRS ) throws UnknownCRSException, IllegalArgumentException {
        this.targetCRS = CRSFactory.create( targetCRS );
    }

    /**
     * Creates a transformation chain, which can be used to transform incoming coordinates (in the given source CRS)
     * into this Transformer's targetCRS.
     * 
     * @param sourceCRS
     *            in which the coordinates are defined.
     * @return the Transformation chain.
     * @throws TransformationException
     *             if no transformation chain could be created.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     * 
     */
    protected CRSTransformation createCRSTransformation( CoordinateSystem sourceCRS )
                            throws TransformationException, IllegalArgumentException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "createCRSTransformation( CoordinateSystem )",
                                                                     "sourceCRS" ) );
        }
        TransformationFactory factory = TransformationFactory.getInstance();
        return factory.createFromCoordinateSystems( sourceCRS, targetCRS );
    }

    /**
     * Creates a transformation chain, which can be used to transform incoming coordinates (in the given source CRS)
     * into this Transformer's targetCRS.
     * 
     * @param sourceCRS
     *            in which the coordinates are defined.
     * @return the Transformation chain.
     * @throws TransformationException
     *             if no transformation chain could be created.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * 
     */
    protected CRSTransformation createCRSTransformation( String sourceCRS )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "createCRSTransformation( CoordinateSystem )",
                                                                     "sourceCRS" ) );
        }
        TransformationFactory factory = TransformationFactory.getInstance();
        return factory.createFromCoordinateSystems( CRSFactory.create( sourceCRS ), targetCRS );
    }

    /**
     * @return the targetCRS
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }
}
