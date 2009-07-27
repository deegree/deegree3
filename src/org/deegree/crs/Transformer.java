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
package org.deegree.crs;

import java.security.InvalidParameterException;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.crs.i18n.Messages;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.TransformationFactory;
import org.deegree.crs.transformations.coordinate.CRSTransformation;

/**
 * Abstract base class for all transformers. Stores a target coordinate system and creates {@link CRSTransformation}
 * objects for a given source CRS.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public abstract class Transformer {

    private final CoordinateSystem targetCRS;

    private Transformation definedTransformation = null;

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
        this.targetCRS = CRSRegistry.lookup( targetCRS );
    }

    /**
     * @param definedTransformation
     *            to use instead of the CRSFactory.
     */
    protected Transformer( Transformation definedTransformation ) {
        if ( definedTransformation == null ) {
            throw new InvalidParameterException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                      "GeoTransformer(CRSTransformation)", "targetCRS" ) );
        }
        targetCRS = definedTransformation.getTargetCRS();
        this.definedTransformation = definedTransformation;
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
    protected Transformation createCRSTransformation( CoordinateSystem sourceCRS )
                            throws TransformationException, IllegalArgumentException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "createCRSTransformation( CoordinateSystem )",
                                                                     "sourceCRS" ) );
        }
        return checkOrCreateTransformation( sourceCRS );
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
    protected Transformation createCRSTransformation( String sourceCRS )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "createCRSTransformation( CoordinateSystem )",
                                                                     "sourceCRS" ) );
        }
        TransformationFactory factory = TransformationFactory.getInstance();
        return factory.createFromCoordinateSystems( CRSRegistry.lookup( sourceCRS ),
                                                    targetCRS );
    }

    /**
     * @return the targetCRS
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * Simple method to check for the CRS transformation to use. If the Transformer was initialized with a
     * {@link Transformation} this will be used (if the sourceCRS fits). If it does not fit or no transformation was
     * given, a new Transformation will be created using the {@link TransformationFactory}
     *
     * @param sourceCRS
     * @return the transformation needed to convert from given source to the constructed target crs.
     * @throws TransformationException
     */
    private synchronized Transformation checkOrCreateTransformation( CoordinateSystem sourceCRS )
                            throws TransformationException {
        if ( definedTransformation == null
             || ! ( definedTransformation.getSourceCRS().equals( sourceCRS ) && definedTransformation.getTargetCRS().equals(
                                                                                                                            targetCRS ) ) ) {
            definedTransformation = TransformationFactory.getInstance().createFromCoordinateSystems( sourceCRS,
                                                                                                     targetCRS );
        }
        return definedTransformation;
    }
}
