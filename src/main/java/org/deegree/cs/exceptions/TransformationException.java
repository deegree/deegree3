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

package org.deegree.cs.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.cs.coordinatesystems.CoordinateSystem;

/**
 * The <code>TransformationException</code> class can be thrown if a transformation exception occurs. For example in the
 * process of creating a transformation step.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class TransformationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1475176551325426832L;

    private Map<Integer, String> transformErrors = new HashMap<Integer, String>();

    private List<Point3d> transformedPoints = null;

    /**
     * Initializes the error message map with the given size.
     * 
     * @param numberOfCoordinates
     *            in the list of coordinates which will be transformed.
     */
    public TransformationException( int numberOfCoordinates ) {
        if ( numberOfCoordinates < 0 ) {
            numberOfCoordinates = 0;
        }
        transformErrors = new HashMap<Integer, String>( numberOfCoordinates );
    }

    /**
     * @param message
     */
    public TransformationException( String message ) {
        super( message );
    }

    /**
     * @param cause
     */
    public TransformationException( Throwable cause ) {
        super( cause );
    }

    /**
     * @param message
     * @param cause
     */
    public TransformationException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * @param sourceCS
     *            from which crs
     * @param targetCS
     *            to which crs
     * @param cause
     *            for the exception.
     */
    public TransformationException( CoordinateSystem sourceCS, CoordinateSystem targetCS, String cause ) {
        super(
               new StringBuilder( "Can't transform from: " ).append( sourceCS.getCode() ).append( " into " ).append(
                                                                                                                     targetCS.getCode() ).append(
                                                                                                                                                  " because: " ).append(
                                                                                                                                                                         cause ).toString() );
    }

    /**
     * @return the transformErrors may be empty but will never be <code>null</code>.
     */
    public final Map<Integer, String> getTransformErrors() {
        return transformErrors;
    }

    /**
     * @param coordinateNumber
     *            the position of the coordinate-tuple/triple which were responsible for the transformationexception.
     * @param errorMessage
     *            the error message for given coordinate pair (in the array of coordinates).
     */
    public final void setTransformError( int coordinateNumber, String errorMessage ) {
        String value = "";
        Integer key = Integer.valueOf( coordinateNumber );
        if ( transformErrors.containsKey( key ) && transformErrors.get( key ) != null ) {
            value = transformErrors.get( key ) + ";";
        }
        value += errorMessage;
        transformErrors.put( key, value );
    }

    /**
     * @return the transformedPoints, which are the points that were (successfully or unsuccessfully) transformed or
     *         <code>null</code> if no points were set.
     */
    public final List<Point3d> getTransformedPoints() {
        return transformedPoints;
    }

    /**
     * @param transformedPoints
     *            which were (successfully or unsuccessfully) transformed until the exception(s) occurred.
     */
    public final void setTransformedPoints( List<Point3d> transformedPoints ) {
        this.transformedPoints = transformedPoints;
    }

}
