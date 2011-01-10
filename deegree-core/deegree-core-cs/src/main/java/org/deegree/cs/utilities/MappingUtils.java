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

package org.deegree.cs.utilities;

import static org.deegree.cs.transformations.coordinate.ConcatenatedTransform.concatenate;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.cs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.cs.transformations.coordinate.MatrixTransform;
import org.slf4j.Logger;

/**
 * The <code>MappingUtils</code> maps some information onto another.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information transformation substitution process.")
public class MappingUtils {
    private static final Logger LOG = getLogger( MappingUtils.class );

    private static String EPSG_SINGLE = "EPSG:";

    private static String EPSG_DOUBLE = "EPSG::";

    private static String X_OGC = "urn:x-ogc:def:";

    private static String OGC = "urn:ogc:def:";

    /**
     * Match the given code to all known epsg representations, currently:
     * <ul>
     * <li>urn:x-ogc:def:${operationName}:EPSG::${epsgCode}</li>
     * <li>urn:x-ogc:def:${operationName}:EPSG:${epsgCode}</li>
     * <li>urn:ogc:def:${operationName}:EPSG::${epsgCode}</li>
     * <li>urn:ogc:def:${operationName}:EPSG:${epsgCode}</li>
     * <li>EPSG::${epsgCode}</li>
     * <li>EPSG:${epsgCode}</li>
     * <li>Any string containing EPSG:${epsgCode} or EPSG::${epsgCode}
     * </ul>
     * 
     * @param compare
     *            the String to compare
     * @param operationName
     *            the name of the 'operation', normally an epsg urn is something like this:
     *            urn:ogc:def:${operationName}:EPSG::1234
     * 
     * @param epsgCode
     *            to check
     * @return true if the given code matches the given String.
     */
    public static boolean matchEPSGString( String compare, String operationName, String epsgCode ) {
        return compare != null
               && ( ( EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( X_OGC + operationName + ":" + EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( X_OGC + operationName + ":" + EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( OGC + operationName + ":" + EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( OGC + operationName + ":" + EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( compare.toUpperCase().contains( EPSG_SINGLE + epsgCode ) ) || ( compare.toUpperCase().contains( EPSG_DOUBLE
                                                                                                                         + epsgCode ) ) );

    }

    /**
     * Iterates over all given Transformations and tries to map one of the given Transformations onto the some part of
     * the given Transformation chain.
     * 
     * @param userRequested
     *            a list of Transformations which should be used in the given transformation chain.
     * @param originalChain
     *            the transformation chain which is to be replaced with some (or all) of the given transformations.
     * @return a Transformation which contains some or all of the given transformations.
     * @throws TransformationException
     */
    public static Transformation updateFromDefinedTransformations( List<Transformation> userRequested,
                                                                   Transformation originalChain )
                            throws TransformationException {
        if ( originalChain == null || userRequested == null || userRequested.isEmpty() ) {
            return originalChain;
        }
        Iterator<Transformation> it = userRequested.iterator();
        while ( it.hasNext() ) {
            Transformation tbu = it.next();
            if ( tbu != null ) {
                originalChain = traverseAndReplace( originalChain, tbu );
            }
        }
        return originalChain;
    }

    /**
     * Traverse the given original transformation chain and replace some (or all) part(s) of the transformation chain
     * with the given transformation.
     * 
     * @param createdResult
     * @param tbu
     * @return the transformation which uses the given tbu (if it matched).
     * @throws TransformationException
     */
    private static Transformation traverseAndReplace( Transformation originalChain, Transformation tbu )
                            throws TransformationException {
        if ( originalChain == null ) {
            return null;
        }
        if ( originalChain.equalOnCRS( tbu ) ) {
            return tbu;
        }

        if ( originalChain.contains( tbu.getSourceCRS() ) && originalChain.contains( tbu.getTargetCRS() ) ) {
            // some part of the transformation matches the given source and target of to be used transformation, lets
            // find and replace it.
            if ( "Concatenated-Transform".equals( originalChain.getImplementationName() ) ) {
                return reorganizeConcatenate( (ConcatenatedTransform) originalChain, tbu );
            }
            // rb: what kind of transformation could this probably be???
            LOG.warn( "Could not handle transformation replacement of type:" + originalChain.getImplementationName()
                      + " ignoring requested transformation: " + tbu.getCodeAndName() );

        } else {
            LOG.debug( "Found no matching (requested) transformation: " + tbu.getCodeAndName()
                       + " in resulting transform, transformation chain will not be altered." );
        }
        // none of the above match.
        return originalChain;
    }

    /**
     * Reorganize the the given concatenated transformation so that some (or all) part(s) of the given transformation
     * uses the given transformation.
     * 
     * @param ct
     *            the original chain
     * @param tbu
     *            the transformation to be used
     * @return the transformation which uses the given tbu (if it matched).
     * @throws TransformationException
     */
    private static Transformation reorganizeConcatenate( ConcatenatedTransform ct, Transformation tbu )
                            throws TransformationException {
        Deque<Transformation> chain = new LinkedList<Transformation>();
        obtainChain( ct, tbu.getSourceCRS(), tbu.getTargetCRS(), chain );
        if ( chain.isEmpty() ) {
            LOG.debug( "Found no matching (requested) transformation: " + tbu.getCodeAndName()
                       + " in concatenated transform, transformation chain will not be altered." );
            return ct;
        }
        Transformation first = chain.peekFirst();
        if ( first == null || !first.getSourceCRS().equals( tbu.getSourceCRS() ) ) {
            LOG.debug( "Found no matching (requested) transformation: " + tbu.getCodeAndName()
                       + " in concatenated transform, transformation chain will not be altered." );
            return ct;
        }
        Transformation last = chain.peekLast();
        if ( last == null || !last.getTargetCRS().equals( tbu.getTargetCRS() ) ) {
            LOG.debug( "Found no matching (requested) transformation: " + tbu.getCodeAndName()
                       + " in concatenated transform, transformation chain will not be altered." );
            return ct;
        }
        Queue<Transformation> resultChain = new LinkedList<Transformation>();
        if ( !ct.getSourceCRS().equals( first.getSourceCRS() ) ) {
            chain.clear();
            obtainChain( ct, ct.getSourceCRS(), tbu.getSourceCRS(), chain );
            if ( !chain.isEmpty() ) {
                if ( chain.getLast() != null && chain.getLast().equals( first ) ) {
                    // get out the last transformation (which is the first of the old chain).
                    chain.removeLast();
                }
                if ( !chain.isEmpty() ) {
                    Transformation prev = chain.getFirst();
                    // align the axis of source crs of the given transformation to the targetcrs of the previous one.
                    if ( prev != null ) {
                        Transformation allign = MatrixTransform.createAllignMatrixTransform( prev.getSourceCRS(),
                                                                                             tbu.getSourceCRS() );
                        if ( !TransformationFactory.isIdentity( allign ) ) {
                            resultChain.add( allign );
                        }
                    }
                    resultChain.addAll( chain );
                }
            }
        }
        // add the requested transformation.
        resultChain.add( tbu );
        if ( !ct.getTargetCRS().equals( last.getTargetCRS() ) ) {
            chain.clear();
            obtainChain( ct, tbu.getTargetCRS(), ct.getTargetCRS(), chain );
            if ( !chain.isEmpty() ) {
                if ( chain.getFirst() != null && chain.getFirst().equals( last ) ) {
                    // get out the first transformation (which is the last of the old chain).
                    chain.removeFirst();
                }
                if ( !chain.isEmpty() ) {
                    Transformation next = chain.getFirst();
                    if ( next != null ) {
                        Transformation allign = MatrixTransform.createAllignMatrixTransform( tbu.getTargetCRS(),
                                                                                             next.getSourceCRS() );
                        // align the axis of target crs of the given transformation to the source crs of the next
                        // one.
                        if ( !TransformationFactory.isIdentity( allign ) ) {
                            resultChain.add( allign );
                        }
                    }
                    resultChain.addAll( chain );
                }
            }
        }
        // just one transformation.
        if ( resultChain.size() == 1 ) {
            return resultChain.poll();
        }
        Iterator<Transformation> it = resultChain.iterator();
        Transformation result = null;
        while ( it.hasNext() ) {
            result = concatenate( result, it.next() );
        }
        return result;
    }

    /**
     * Traverse the concatenated transformation and recursively find all transformations which go from source to target
     * crs.
     * 
     * @param ct
     *            to traverse.
     * @param sourceCRS
     *            starting crs
     * @param targetCRS
     *            ending crs
     * @param chain
     *            (stack) containing the transformations found recursively.
     */
    private static void obtainChain( Transformation ct, CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                                     Deque<Transformation> chain ) {
        // if the source and target are the same
        if ( sourceCRS.equals( targetCRS ) ) {
            return;
        }
        if ( "Concatenated-Transform".equals( ct.getImplementationName() ) ) {
            obtainChain( ( (ConcatenatedTransform) ct ).getFirstTransform(), sourceCRS, targetCRS, chain );
            CoordinateSystem nSource = sourceCRS;
            if ( !chain.isEmpty() ) {
                nSource = chain.peekLast().getTargetCRS();
            }
            obtainChain( ( (ConcatenatedTransform) ct ).getSecondTransform(), nSource, targetCRS, chain );
        } else {
            if ( ct.contains( sourceCRS ) && ct.contains( targetCRS ) ) {
                chain.addLast( ct );
            } else {
                if ( sourceCRS.equals( ct.getSourceCRS() ) ) {
                    chain.addLast( ct );
                }
                if ( targetCRS.equals( ct.getTargetCRS() ) ) {
                    chain.addLast( ct );
                }
            }
        }
    }

}
