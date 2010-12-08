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
package org.deegree.cs;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.slf4j.Logger;

/**
 * Abstract base class for all transformers. Stores a target coordinate system and creates {@link Transformation}
 * objects for a given source CRS.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about created transformation chain.")
public abstract class Transformer {

    private static final Logger LOG = getLogger( Transformer.class );

    private final CoordinateSystem targetCRS;

    private final CRS tCRS;

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
        this.tCRS = new CRS( targetCRS );
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
        if ( targetCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL", "Transformer(String)",
                                                                     "targetCRS" ) );
        }
        this.targetCRS = CRSRegistry.lookup( targetCRS );
        this.tCRS = new CRS( targetCRS );
    }

    /**
     * Creates a new Transformer object, with the given target CRS.
     * 
     * @param targetCRS
     *            to transform incoming coordinates to.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     * @throws UnknownCRSException
     *             if the wrapped crs was null
     */
    protected Transformer( final CRS targetCRS ) throws IllegalArgumentException, UnknownCRSException {
        if ( targetCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "Transformer(CoordinateSystem)", "targetCRS" ) );
        }

        this.targetCRS = targetCRS.getWrappedCRS();
        this.tCRS = targetCRS;
    }

    /**
     * @param definedTransformation
     *            to use instead of the CRSFactory.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    protected Transformer( Transformation definedTransformation ) {
        if ( definedTransformation == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "Transformer(Transformation)",
                                                                     "definedTransformation" ) );
        }
        targetCRS = definedTransformation.getTargetCRS();
        this.tCRS = new CRS( targetCRS );
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
        return createCRSTransformation( sourceCRS, null );
    }

    /**
     * Creates a transformation chain, which can be used to transform incoming coordinates (in the given source CRS)
     * into this Transformer's targetCRS. The list of transformations can be used to define separate transformations
     * which <b>must</b> be used into the resulting transformation chain.
     * 
     * @param sourceCRS
     *            in which the coordinates are defined.
     * @param toBeUsedTransformations
     *            a list of transformations which must be used on the resulting transformation chain.
     * 
     * @return the Transformation chain.
     * @throws TransformationException
     * @throws TransformationException
     *             if no transformation chain could be created.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     * 
     */
    protected Transformation createCRSTransformation( CoordinateSystem sourceCRS,
                                                      List<Transformation> toBeUsedTransformations )
                            throws TransformationException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "createCRSTransformation( CoordinateSystem )",
                                                                     "sourceCRS" ) );
        }
        return checkOrCreateTransformation( sourceCRS, toBeUsedTransformations );
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
        return CRSRegistry.getTransformation( null, CRSRegistry.lookup( sourceCRS ), targetCRS );
    }

    /**
     * @return the targetCRS
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * @return the target crs as a wrapped {@link CRS}
     */
    public final CRS getWrappedTargetCRS() {
        return this.tCRS;
    }

    /**
     * Simple method to check for the CRS transformation to use. If the Transformer was initialized with a
     * {@link Transformation} this will be used (if the sourceCRS fits). If it does not fit or no transformation was
     * given, a new Transformation will be created using the {@link TransformationFactory}
     * 
     * @param sourceCRS
     * @param toBeUsedTransformations
     * @return the transformation needed to convert from given source to the constructed target crs.
     * @throws TransformationException
     */
    private synchronized Transformation checkOrCreateTransformation( CoordinateSystem sourceCRS,
                                                                     List<Transformation> toBeUsedTransformations )
                            throws TransformationException {
        if ( definedTransformation == null
             || !( definedTransformation.getSourceCRS().equals( sourceCRS ) && definedTransformation.getTargetCRS().equals(
                                                                                                                            targetCRS ) ) ) {
            definedTransformation = CRSRegistry.getTransformation( null, sourceCRS, targetCRS, toBeUsedTransformations );
            if ( LOG.isDebugEnabled() ) {
                if ( definedTransformation == null ) {
                    LOG.debug( "Identity transformation (null)." );
                } else {
                    LOG.debug( "Resulting transform: {}",
                               definedTransformation.getTransformationPath( null ).toString() );
                }
            }
        }
        return definedTransformation;
    }
}
