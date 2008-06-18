//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.crs.transformations.coordinate;

import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.crs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DirectTransform</code> class wraps the access to a polynomial transformation, by calling it's
 * applyPolynomial method.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class DirectTransform extends CRSTransformation {

    private static Logger LOG = LoggerFactory.getLogger( DirectTransform.class );

    private final PolynomialTransformation transformation;

    /**
     * @param transformation to apply
     * @param sourceCRS in which the points will be defined.
     */
    public DirectTransform( PolynomialTransformation transformation, CoordinateSystem sourceCRS ) {
        super( sourceCRS, transformation.getTargetCRS());
        this.transformation = transformation;
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder( "A " );
            sb.append( getName() );
            sb.append( " with incoming points: " );
            sb.append( srcPts );
            LOG.debug( sb.toString() );
        }
        if( isInverse ){
            LOG.warn( "A Direct Transformation cannot be inverse yet" );
        }
        return transformation.applyPolynomial( srcPts );
    }

    @Override
    public boolean isIdentity() {
        // a transformation cannot be an identity it doesn't make a lot of sense.
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " - Direct-Transformation: " + transformation.getName();
    }

    @Override
    public String getName() {
        return transformation.getName();
    }

}
