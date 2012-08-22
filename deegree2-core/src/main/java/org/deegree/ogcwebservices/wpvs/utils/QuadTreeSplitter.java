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

package org.deegree.ogcwebservices.wpvs.utils;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSDeegreeParams;

/**
 * <p>
 * The <code>QuadTreeSplitter</code> class can be used to create x-y axis alligned request quads from a qiven List of
 * {@link ResolutionStripe} s. These Stripes depend on the ViewFrustrum and it's projection on the x-y plane (the so
 * called footprint). To create an approximation of this footprint a Quadtree (a geometric spatial structure, which
 * recursively divides a boundingbox into four containing boundingboxes) is built. The leafs of this tree are merged
 * according to their resolution and size to create the requeststripes.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class QuadTreeSplitter {

    private static final ILogger LOG = LoggerFactory.getLogger( QuadTreeSplitter.class );

    private QuadNode rootNode;

    private TreeSet<ResolutionStripe> resolutionStripes;

    private double minimalTerrainHeight;

    private double minimalResolution;

    private CoordinateSystem crs;

    private boolean highQuality;

    private double scale;

    // private double imageWidth;

    /**
     * Creates a new Quadtree, from the given resolutionstripes. The resulting tree can be used to generate
     * requeststripes which are (x-y) axis aligned.
     * <p>
     * The Quality argument is used for the recursive termination criteria, if it is set to true the requeststripes will
     * accurately approximate the footprint (=projection of the viewfrustrum onto the ground) and the resolutions given
     * in the resolutionstripes this results in a lot of requests which can slow down the wpvs. If set to false the
     * footprint and the given resolutions will be approximated poorly but only a few requeststripes are created,
     * resulting in a faster wpvs.
     * </p>
     * 
     * @param resolutionStripes
     *            the different resolutionstripes.
     * @param imageWidth
     *            the width of the target image, necessary for calculating the width resolution of the requeststripe.
     * @param highQuality
     *            true if accurate (but many) requeststripes should be generated, false if none accurate (but only a
     *            few) requests should be generated.
     */
    public QuadTreeSplitter( ArrayList<ResolutionStripe> resolutionStripes, double imageWidth, boolean highQuality ) {
        if ( resolutionStripes == null || resolutionStripes.size() <= 0 ) {
            return;
        }
        this.resolutionStripes = new TreeSet<ResolutionStripe>( resolutionStripes );
        // this.imageWidth = imageWidth;
        this.crs = resolutionStripes.get( 0 ).getCRSName();
        this.scale = resolutionStripes.get( 0 ).getScale();
        this.minimalTerrainHeight = resolutionStripes.get( 0 ).getMinimalTerrainHeight();
        this.highQuality = highQuality;
        // For the merge, the check is newmin < existing_min therefore -> min large and max small
        // Position minPos = GeometryFactory.createPosition( 0, 0, 0 );
        // Position maxPos = GeometryFactory.createPosition( 0, 0, 0 );
        Envelope bbox = resolutionStripes.get( 0 ).getSurface().getEnvelope();
        minimalResolution = Double.MAX_VALUE;
        double maxResolution = Double.MIN_VALUE;

        // find the highest and loweset maxResolution (which are needed for termination decissions and
        // find create the axis-alligned bbox of the resolutionstripes which will be the rootnode.
        for ( int i = 0; i < resolutionStripes.size(); ++i ) {
            try {
                bbox = bbox.merge( resolutionStripes.get( i ).getSurface().getEnvelope() );
                // minimalResolution is the smallest number
                minimalResolution = Math.min( minimalResolution, resolutionStripes.get( i ).getMinResolution() );
                maxResolution = Math.max( maxResolution, resolutionStripes.get( i ).getMaxResolution() );
            } catch ( GeometryException e ) {
                e.printStackTrace();
                System.out.println( e.getLocalizedMessage() );
            }
        }
        LOG.logDebug( "Found minimalResolution: " + minimalResolution );
        LOG.logDebug( "Found maximumResolution: " + maxResolution );
        try {
            if ( Math.abs( minimalResolution ) < 0.00001 ) { // almost null
                minimalResolution = Math.pow( maxResolution, 1.0 / resolutionStripes.size() );
                LOG.logDebug( "Recalculated minimalResolution (< 0.00001) ) to: " + minimalResolution );
            }
            Position min = bbox.getMin();

            double zValue = min.getZ();
            if ( Double.isNaN( min.getZ() ) ) {
                zValue = minimalTerrainHeight;
            }

            Vector3d leftPoint = new Vector3d( min.getX(), min.getY(), zValue );
            Vector3d rightPoint = new Vector3d( min.getX() + ( bbox.getWidth() ), min.getY() + ( bbox.getHeight() ),
                                                zValue );
            Vector3d ul = new Vector3d( leftPoint.x, rightPoint.y, zValue );
            double quadResolution = StripeFactory.calcScaleOfVector( leftPoint, rightPoint, imageWidth );
            double quadResolution2 = StripeFactory.calcScaleOfVector( leftPoint, ul, imageWidth );
            if ( quadResolution < quadResolution2 ) {
                quadResolution = quadResolution2;
            }

            LOG.logDebug( "The root node of the quadtree has a max resolution of: " + maxResolution
                          + " and a min Resolution of: " + minimalResolution );

            rootNode = new QuadNode( GeometryFactory.createSurface( bbox, crs ), maxResolution, minimalResolution );

            createTree( rootNode, quadResolution );

        } catch ( GeometryException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        }
    }

    /**
     * After instantiating a Quadtree, this method can be called to build the (x-y) axis-alligned request stripes.
     * 
     * @param extraRequestPercentage
     *            a percentage to be added to the resulting stripes ( a value between [0,1] ), which might correct gapes
     *            between stripes.{@link WPVSDeegreeParams#getExtendRequestPercentage()}
     * @param quadMergeCount
     *            the number of leaves this splitter can have, before it starts merging leaves together
     *            {@link WPVSDeegreeParams#getQuadMergeCount() }
     * @return the (x-y) axis-alligned request squares best fitted the given resolutionstripes.
     */
    public ArrayList<ResolutionStripe> getRequestQuads( double extraRequestPercentage, int quadMergeCount ) {

        ArrayList<ResolutionStripe> resultList = new ArrayList<ResolutionStripe>();
        if ( rootNode != null ) {
            LinkedHashMap<Double, ArrayList<QuadNode>> lhm = new LinkedHashMap<Double, ArrayList<QuadNode>>( 100 );
            outputNodes( rootNode, lhm );
            Set<Double> keys = lhm.keySet();
            int nrOfStripes = 0;
            for ( Double resolution : keys ) {
                if ( lhm.containsKey( resolution ) ) {
                    List<QuadNode> originalNodes = lhm.get( resolution );
                    nrOfStripes += originalNodes.size();
                }
            }

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                if ( nrOfStripes < quadMergeCount ) {
                    LOG.logDebug( "Not merging leaves because there are less leaves: " + nrOfStripes
                                  + " then configured quadMergeCount:" + quadMergeCount );
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    outputNodes( rootNode, sb );
                    LOG.logDebug( "tree-leaves:\n" + sb.toString() );
                } catch ( GeometryException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            retrieveStripes( resultList, lhm, extraRequestPercentage, ( nrOfStripes > quadMergeCount ) );
        }
        return resultList;
    }

    private void retrieveStripes( List<ResolutionStripe> resultList, LinkedHashMap<Double, ArrayList<QuadNode>> lhm,
                                  double extraRequestPercentage, boolean mergeAndSort ) {
        Set<Double> keys = lhm.keySet();
        for ( Double resolution : keys ) {
            if ( lhm.containsKey( resolution ) ) {
                List<QuadNode> originalNodes = lhm.get( resolution );
                List<QuadNode> result = new ArrayList<QuadNode>( originalNodes.size() );
                if ( mergeAndSort ) {
                    // sorted to x values first.
                    LOG.logDebug( "Sorting x order" );
                    List<QuadNode> resultX = mergeAndSort( originalNodes );

                    // Check if sorting to y results in better values;
                    for ( QuadNode node : originalNodes ) {
                        node.compareY();
                    }
                    LOG.logDebug( "Sorting y order" );
                    List<QuadNode> resultY = mergeAndSort( originalNodes );

                    // Find the optimal sorting order (lesser quads) and check if the perpendicular
                    // order results in lesser requeststripes (it usually does)
                    if ( resultX.size() < resultY.size() ) {
                        for ( QuadNode node : resultX ) {
                            node.compareY();
                        }
                        LOG.logDebug( "res-Sorting x order" );
                        result = mergeAndSort( resultX );
                    } else {
                        for ( QuadNode node : resultY ) {
                            node.compareX();
                        }
                        LOG.logDebug( "res-Sorting y order" );
                        result = mergeAndSort( resultY );
                    }
                } else {
                    result.addAll( originalNodes );
                }
                for ( QuadNode quad : result ) {
                    Envelope env = quad.getBBox().getEnvelope();
                    Position envMin = env.getMin();
                    Position envMax = env.getMax();

                    double width = env.getWidth();
                    double height = env.getHeight();

                    // enlarge the envelope to extraRequestPercentage to correct gapes between stripes.
                    double extraWidth = width * extraRequestPercentage;
                    double extraHeight = height * extraRequestPercentage;

                    Position newMin = GeometryFactory.createPosition( envMin.getX() - extraWidth, envMin.getY()
                                                                                                  - extraHeight,
                                                                      envMin.getZ() );

                    Position newMax = GeometryFactory.createPosition( envMax.getX() + extraWidth, envMax.getY()
                                                                                                  + extraHeight,
                                                                      envMax.getZ() );

                    double minResolution = quad.getMinResolution();
                    double maxResolution = quad.getMaxResolution();

                    if ( minResolution > maxResolution ) {
                        double tmp = minResolution;
                        minResolution = maxResolution;
                        maxResolution = tmp;
                    }

                    try {
                        Surface resultSurface = GeometryFactory.createSurface(
                                                                               GeometryFactory.createEnvelope( newMin,
                                                                                                               newMax,
                                                                                                               this.crs ),
                                                                               this.crs );
                        ResolutionStripe rs = new ResolutionStripe( resultSurface, maxResolution, minResolution,
                                                                    minimalTerrainHeight, scale );
                        resultList.add( rs );

                    } catch ( GeometryException e ) {
                        LOG.logError( e.getLocalizedMessage(), e );
                    }
                }
            }
        }
    }

    /**
     * A little helper function which first sorts the given list of quadnodes according to their sorting order and
     * afterwards merges all stripes which are adjacent and have the same resolution.
     * 
     * @param toBeMerged
     *            the list of Quadnodes which must be sorted and merged.
     * @return a list containing the merged quadnodes of the given list.
     */
    private List<QuadNode> mergeAndSort( List<QuadNode> toBeMerged ) {
        Collections.sort( toBeMerged );
        List<QuadNode> resultList = new ArrayList<QuadNode>( toBeMerged );
        boolean needsResort = true;
        while ( needsResort ) {
            needsResort = false;
            List<QuadNode> tmpResults = new ArrayList<QuadNode>( resultList );
            Iterator<QuadNode> it = tmpResults.iterator();
            QuadNode first = null;
            QuadNode second = null;
            resultList.clear();
            // Iterate over the quadnodes.
            while ( second != null || it.hasNext() ) {
                // this is neccessary if the first could not be merged with the next, we don't want to lose the
                // iterators reference, therefore the first quad references the second which was actually the it.next()
                if ( second == null ) {
                    first = it.next();
                } else {
                    first = second;
                    second = null;
                }
                Envelope requestEnvelope = first.getBBox().getEnvelope();
                int nrOfMerges = 0;
                double curMinResolution = first.getMinResolution();
                double curMaxResolution = first.getMaxResolution();
                while ( it.hasNext() && second == null ) {
                    second = it.next();
                    // if the first and the second quad cannot be merged, the second will become the first, if they can
                    // merge, they will be merged together and the iterator moves on, this means, we have to go over all
                    // quads again after this round, for else we might not find all possible merges.
                    if ( first.canMerge( second ) ) {
                        try {
                            Envelope tmpEnvelope = requestEnvelope;
                            requestEnvelope = requestEnvelope.merge( second.getBBox().getEnvelope() );
                            // curMinResolution = first.getMinResolution() / ( nrOfMerges + 2 );
                            // curMaxResolution = first.getMaxResolution() / ( nrOfMerges + 2 );
                            int rH = (int) Math.round( requestEnvelope.getHeight() / Math.abs( curMinResolution ) );
                            int rW = (int) Math.round( requestEnvelope.getWidth() / Math.abs( curMinResolution ) );
                            if ( rH < WPVSConfiguration.MAX_REQUEST_SIZE && rW < WPVSConfiguration.MAX_REQUEST_SIZE ) {
                                LOG.logDebug( "Merging quads:\n"
                                              + WKTAdapter.export( GeometryFactory.createSurface( tmpEnvelope, crs ) ).toString()
                                              + "\n" + WKTAdapter.export( second.getBBox() ).toString() );
                                // double tmpScale = WPVSConfiguration.MAX_REQUEST_SIZE / (double)((rH > rW )?rW:rH);
                                // curMaxResolution *=tmpScale;
                                // curMinResolution *=tmpScale;

                                second = null;
                                needsResort = true;
                                nrOfMerges++;
                            } else {
                                requestEnvelope = tmpEnvelope;
                                LOG.logDebug( "Cannot merge two quads, because their merged sizes( rw: "
                                              + rW
                                              + ", rH: "
                                              + rH
                                              + " would be bigger as the maximum retrievable size:\n"
                                              + WKTAdapter.export( GeometryFactory.createSurface( requestEnvelope, crs ) ).toString()
                                              + "\n" + WKTAdapter.export( second.getBBox() ).toString() );

                            }
                        } catch ( GeometryException ge ) {
                            // An error occured, it might be best to not merge these envelopes.
                            LOG.logError( ge.getLocalizedMessage(), ge );
                        }
                    }

                }
                if ( nrOfMerges > 0 ) {
                    try {
                        Surface resultSurface = GeometryFactory.createSurface( requestEnvelope, crs );
                        resultList.add( new QuadNode( resultSurface,
                        // first.getMaxResolution(),
                                                      // first.getMinResolution(),
                                                      curMaxResolution, curMinResolution, first.isComparingX() ) );
                    } catch ( GeometryException ge ) {
                        // An error occured, it might be best not to merge these envelopes.
                        LOG.logError( ge.getLocalizedMessage(), ge );
                    }
                } else {
                    resultList.add( first );
                }
            }
        }
        return resultList;
    }

    /**
     * This Method actually builds the tree. The decission of a split is made by evaluating the minimal maxResolution of
     * the intersecting ResultionStripe.
     * 
     * @param father
     *            the Father node which will be splittet into four axis aligned sons
     * @param quadResolution
     *            the maxResolution of a width axis of the axis-aligned bbox
     * @throws GeometryException
     *             if the Envelope cannot be created
     */
    private void createTree( QuadNode father, double quadResolution )
                            throws GeometryException {
        LOG.logDebug( WKTAdapter.export( father.getBBox() ).toString() );
        LOG.logDebug( father.toString() );
        LOG.logDebug( "Fatherresolution: " + quadResolution );
        Position min = father.getBBox().getEnvelope().getMin();
        double widthHalf = 0.5 * father.getBBox().getEnvelope().getWidth();
        double heightHalf = 0.5 * father.getBBox().getEnvelope().getHeight();
        double lowerLeftX = min.getX();
        double lowerLeftY = min.getY();
        double sonsResolution = 0.5 * quadResolution;
        // recursion is not necessary if the resolution is smaller then the minimalResolution.
        if ( sonsResolution < minimalResolution ) {
            LOG.logDebug( "The currentresolution (" + sonsResolution + ") is smaller then the minimalResolution("
                          + minimalResolution + ")" );
            return;
        }

        checkSon( father, sonsResolution, QuadNode.LOWER_LEFT_SON, lowerLeftX, lowerLeftY, lowerLeftX + widthHalf,
                  lowerLeftY + heightHalf );

        // lowerright
        checkSon( father, sonsResolution, QuadNode.LOWER_RIGHT_SON, lowerLeftX + widthHalf, lowerLeftY,
                  lowerLeftX + ( 2 * widthHalf ), lowerLeftY + heightHalf );

        // upperleft
        checkSon( father, sonsResolution, QuadNode.UPPER_LEFT_SON, lowerLeftX, lowerLeftY + heightHalf, lowerLeftX
                                                                                                        + widthHalf,
                  lowerLeftY + ( 2 * heightHalf ) );

        // upperright
        checkSon( father, sonsResolution, QuadNode.UPPER_RIGHT_SON, lowerLeftX + widthHalf, lowerLeftY + heightHalf,
                  lowerLeftX + 2 * widthHalf, lowerLeftY + 2 * heightHalf );
    }

    /**
     * Decides if the father quad has to be subdivided into it's sons.
     * 
     * @param father
     *            the Father quad to divide
     * @param quadResolution
     *            the maxResolution of the fathers son (half the maxResolution of the father)
     * @param SON_ID
     *            the son to check
     * @param lowerLeftX
     *            minx of the bbox of the fathers son
     * @param lowerLeftY
     *            miny of the bbox of the fathers son
     * @param upperRightX
     *            maxx of the bbox of the fathers son
     * @param upperRightY
     *            maxY of the bbox of the fathers son
     * @throws GeometryException
     *             if no surface can be created
     */
    private void checkSon( QuadNode father, double quadResolution, final int SON_ID, double lowerLeftX,
                           double lowerLeftY, double upperRightX, double upperRightY )
                            throws GeometryException {
        Position min = GeometryFactory.createPosition( lowerLeftX, lowerLeftY, minimalTerrainHeight );
        /*
         * father.getBBox() .getEnvelope() .getMin() .getZ() );
         */
        Position max = GeometryFactory.createPosition( upperRightX, upperRightY, minimalTerrainHeight );
        /*
         * father.getBBox() .getEnvelope() .getMax() .getZ());
         */
        Surface bbox = GeometryFactory.createSurface( GeometryFactory.createEnvelope( min, max, crs ), crs );

        ResolutionStripe intersectedStripe = ( highQuality ) ? getIntersectionForQualityConfiguration( bbox )
                                                            : getIntersectionForFastConfiguration( bbox );

        if ( intersectedStripe != null ) { // found an intersecting resolutionStripe
            LOG.logDebug( "Following quad( 1 ) Found intersecting stripe(2):\n" + WKTAdapter.export( bbox ) + "\n"
                          + WKTAdapter.export( intersectedStripe.getSurface() ) );
            QuadNode son = new QuadNode( bbox, intersectedStripe.getMaxResolution(),
                                         intersectedStripe.getMinResolution() );
            double intersectResolution = ( highQuality ) ? intersectedStripe.getMinResolution()
                                                        : intersectedStripe.getMaxResolution();
            LOG.logDebug( "sonsResolution: " + intersectResolution + " currentResolution of quad: " + quadResolution );
            father.addSon( SON_ID, son );
            int rH = (int) Math.round( bbox.getEnvelope().getHeight() / Math.abs( intersectedStripe.getMinResolution() ) );
            int rW = (int) Math.round( bbox.getEnvelope().getWidth() / Math.abs( intersectedStripe.getMinResolution() ) );
            /* ( rH < WPVSConfiguration.MAX_REQUEST_SIZE && rW < WPVSConfiguration.MAX_REQUEST_SIZE ) || */
            if ( ( rH > WPVSConfiguration.MAX_REQUEST_SIZE || rW > WPVSConfiguration.MAX_REQUEST_SIZE )
                 || quadResolution >= ( intersectResolution * 0.95 ) ) {
                createTree( son, quadResolution );
            }
        } else {
            LOG.logDebug( "Following quad found no intersections :\n" + WKTAdapter.export( bbox ) );
        }
    }

    /**
     * Finds the resolutionstripe with the lowest minResolution which intersects with the given bbox. Resulting in a lot
     * of different requests.
     * 
     * @param bbox
     *            the BoundingBox of the Envelope to check.
     * @return the resolutionStripe which intersects the bbox.
     */
    private ResolutionStripe getIntersectionForQualityConfiguration( Surface bbox ) {
        LOG.logDebug( "Trying to find intersection with Quality, e.g. min( of all intersecting minResolutions )" );
        ResolutionStripe resultStripe = null;
        for ( ResolutionStripe stripe : resolutionStripes ) {
            if ( bbox.intersects( stripe.getSurface() ) ) {
                if ( resultStripe != null ) {
                    if ( ( stripe.getMinResolution() < resultStripe.getMinResolution() ) ) {
                        resultStripe = stripe;
                    }
                } else {
                    resultStripe = stripe;
                }
            }
        }
        return resultStripe;
    }

    /**
     * Finds the resolutionstripe with the highest maxResolution which intersects with the given bbox. Resulting in only
     * a few different requests.
     * 
     * @param bbox
     *            the BoundingBox of the Envelope to check.
     * @return the resolutionStripe which intersects the bbox.
     */
    private ResolutionStripe getIntersectionForFastConfiguration( Surface bbox ) {
        LOG.logDebug( "Trying to find intersection with Speed, e.g. max( of all intersecting maxResolutions )" );
        ResolutionStripe resultStripe = null;
        for ( ResolutionStripe stripe : resolutionStripes ) {
            if ( bbox.intersects( stripe.getSurface() ) ) {
                if ( resultStripe != null ) {
                    if ( ( stripe.getMaxResolution() < resultStripe.getMaxResolution() ) ) {
                        resultStripe = stripe;
                    }
                } else {
                    resultStripe = stripe;
                }
            }
        }
        return resultStripe;
    }

    /**
     * Outputs the tree
     * 
     * @param g2d
     *            if the quadtree should be drawn.
     */
    public void outputTree( Graphics2D g2d ) {
        if ( rootNode != null ) {
            if ( g2d != null ) {
                System.out.println( "number Of leaves-> " + outputNodes( rootNode, g2d ) );
            } else {
                outputNodes( rootNode, "" );
            }
        }
    }

    private int outputNodes( QuadNode father, Graphics2D g2d ) {
        if ( father.isLeaf() ) {
            drawSquare( father, g2d, Color.BLACK );
            return 1;
        }
        QuadNode[] nodes = father.getSons();
        int result = 0;
        for ( QuadNode node : nodes ) {
            if ( node != null ) {
                result += outputNodes( node, g2d );
            }

        }
        return result;
    }

    private void outputNodes( QuadNode father, String indent ) {
        if ( father.isLeaf() ) {
            System.out.println( indent + "(father)" + father.getBBox() );
        } else {
            QuadNode[] nodes = father.getSons();
            for ( QuadNode node : nodes ) {
                if ( node != null ) {
                    indent += "-";
                    outputNodes( node, indent );
                }
            }
        }
    }

    private void outputNodes( QuadNode father, StringBuilder sb )
                            throws GeometryException {
        if ( father.isLeaf() ) {
            sb.append( WKTAdapter.export( father.getBBox() ).toString() ).append( "\n" );
        } else {
            QuadNode[] nodes = father.getSons();
            for ( QuadNode node : nodes ) {
                if ( node != null ) {
                    outputNodes( node, sb );
                }
            }
        }
    }

    /**
     * Find the leaf nodes and add them according to their maxResolution in a LinkedHashMap.
     * 
     * @param father
     *            the node to check
     * @param outputMap
     *            the map to output to.
     */
    private void outputNodes( QuadNode father, LinkedHashMap<Double, ArrayList<QuadNode>> outputMap ) {
        if ( father.isLeaf() ) {
            Double key = new Double( father.getMinResolution() );
            ArrayList<QuadNode> ts = outputMap.get( key );
            if ( ts == null ) { // I know, but I don't put null values so it's ok
                ts = new ArrayList<QuadNode>();
                outputMap.put( key, ts );
            }
            if ( ts.add( father ) == false ) {
                System.out.println( "quadnode allready in set" );
            }
        } else {
            QuadNode[] nodes = father.getSons();
            for ( QuadNode node : nodes ) {
                if ( node != null ) {
                    outputNodes( node, outputMap );
                }
            }
        }
    }

    private void drawSquare( QuadNode node, Graphics2D g2d, Color c ) {
        if ( g2d != null ) {
            g2d.setColor( c );
            Envelope env = node.getBBox().getEnvelope();
            Position min = env.getMin();
            int height = (int) env.getHeight();
            int width = (int) env.getWidth();
            g2d.drawRect( (int) min.getX(), (int) min.getY(), width, height );
            Composite co = g2d.getComposite();
            g2d.setColor( new Color( c.getRed(), c.getGreen(), c.getBlue(), 64 ) );
            g2d.fillRect( (int) min.getX(), (int) min.getY(), width, height );
            g2d.setComposite( co );
        }
    }

    /**
     * 
     * The <code>QuadNode</code> class is the bean for every node of the quadtree. It contains a axis-aligned BBox and
     * the maxResolution of its associated resolutionStripe. It can have upto four sons.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    private class QuadNode implements Comparable<QuadNode> {

        private QuadNode[] sons;

        private Surface bbox;

        private double maxResolution;

        private double minResolution;

        private double comparePosition;

        private double compareLength;

        private boolean comparingX;

        /**
         * The Denoting lower left son
         */
        static final int LOWER_LEFT_SON = 0;

        /**
         * The Denoting lower right son
         */
        static final int LOWER_RIGHT_SON = 1;

        /**
         * The Denoting upper left son
         */
        static final int UPPER_LEFT_SON = 2;

        /**
         * The Denoting upper right son
         */
        static final int UPPER_RIGHT_SON = 3;

        /**
         * 
         * @param bbox
         * @param maxResolution
         * @param minResolution
         */
        QuadNode( Surface bbox, double maxResolution, double minResolution ) {
            this.bbox = bbox;
            sons = new QuadNode[4];
            this.maxResolution = maxResolution;
            this.minResolution = minResolution;
            comparePosition = bbox.getEnvelope().getMin().getX();
            compareLength = bbox.getEnvelope().getWidth();
            comparingX = true;
        }

        /**
         * 
         * @param bbox
         * @param maxResolution
         * @param minResolution
         * @param compareDirection
         */
        QuadNode( Surface bbox, double maxResolution, double minResolution, boolean compareDirection ) {
            this( bbox, maxResolution, minResolution );
            if ( compareDirection )
                compareX();
            else
                compareY();
        }

        /**
         * Add a son to this node.
         * 
         * @param sonID
         *            which son
         * @param son
         *            the reference
         */
        void addSon( final int sonID, QuadNode son ) {
            if ( sonID == LOWER_LEFT_SON || sonID == LOWER_RIGHT_SON || sonID == UPPER_LEFT_SON
                 || sonID == UPPER_RIGHT_SON ) {
                this.sons[sonID] = son;
            }
        }

        /**
         * @return bbox of node
         */
        Surface getBBox() {
            return bbox;
        }

        /**
         * set the node to compare along the x axis
         */
        void compareX() {
            comparePosition = bbox.getEnvelope().getMin().getX();
            compareLength = bbox.getEnvelope().getWidth();
            comparingX = true;
        }

        /**
         * set the node to compare along the y axis
         */
        void compareY() {
            comparePosition = bbox.getEnvelope().getMin().getY();
            compareLength = bbox.getEnvelope().getHeight();
            comparingX = false;
        }

        /**
         * If this Quadnode has no sons it is called a leaf.
         * 
         * @return true if no sons, false otherwhise.
         */
        boolean isLeaf() {
            return ( sons[0] == null && sons[1] == null && sons[2] == null && sons[3] == null );
        }

        /**
         * @return the sons or null if no sons are set.
         */
        QuadNode[] getSons() {
            return sons;
        }

        /**
         * @param sonID
         * @return the son or null if no son could be mapped to the sonid.
         */
        QuadNode getSon( final int sonID ) {
            if ( sonID != LOWER_LEFT_SON || sonID != LOWER_RIGHT_SON || sonID != UPPER_LEFT_SON
                 || sonID != UPPER_RIGHT_SON )
                return null;
            return sons[sonID];
        }

        /**
         * @return The max maxResolution of the Stripe.
         */
        double getMaxResolution() {
            return maxResolution;
        }

        /**
         * @return the minResolution value.
         */
        double getMinResolution() {
            return minResolution;
        }

        /**
         * @return true if the comparing will be along the x axis.
         */
        boolean isComparingX() {
            return comparingX;
        }

        /**
         * @return true if the comparing will be along the y axis.
         */
        double getComparePosition() {
            return comparePosition;
        }

        /**
         * @return true if the length will be compared
         */
        double getCompareLength() {
            return compareLength;
        }

        /*
         * Attention, the equal result "0" is not really a check for the equality of two Quadnodes, it just reflex, that
         * two QuadNodes have the same sorting properties -> the position - (y or x) and the length in this direction
         * are equal. It is very plausible that they have totally different positions and length in the other (not
         * checked) direction.
         * 
         * @see java.lang.Comparable#compareTo(T)
         */
        public int compareTo( QuadNode other ) {
            double otherPosition = other.getComparePosition();
            if ( Math.abs( comparePosition - otherPosition ) < 0.00001 ) {
                double otherLength = other.getCompareLength();
                if ( Math.abs( compareLength - otherLength ) < 0.00001 ) {
                    if ( comparingX ) {
                        double thisMinY = this.bbox.getEnvelope().getMin().getY();
                        double otherMinY = other.getBBox().getEnvelope().getMin().getY();
                        if ( ( Math.abs( thisMinY - otherMinY ) < 0.00001 ) )
                            return 0;
                        if ( thisMinY < otherMinY )
                            return 1;
                        return -1;
                    }
                    double thisMinX = this.bbox.getEnvelope().getMin().getX();
                    double otherMinX = other.getBBox().getEnvelope().getMin().getX();
                    if ( ( Math.abs( thisMinX - otherMinX ) < 0.00001 ) )
                        return 0;
                    if ( thisMinX < otherMinX )
                        return 1;
                    return -1;
                }
                if ( compareLength < otherLength ) {
                    return -1;
                }
                return 1;
            }
            if ( comparePosition < otherPosition )
                return -1;
            return 1;
        }

        /**
         * simple check if two quadnodes can be merged, according to their positions, length and if they are adjacent.
         * 
         * @param other
         * @return true if this QuadNode can be merged with the Other.
         */
        boolean canMerge( QuadNode other ) {
            double otherPosition = other.getComparePosition();
            if ( Math.abs( comparePosition - otherPosition ) < 0.01 ) {
                double otherLength = other.compareLength;
                if ( Math.abs( compareLength - otherLength ) < 0.01 ) {
                    // the origins and the length are mergable, now check if the Quadnodes are
                    // adjacent
                    if ( comparingX ) {
                        double thisMaxY = this.bbox.getEnvelope().getMax().getY();
                        double thisMinY = this.bbox.getEnvelope().getMin().getY();
                        double otherMinY = other.getBBox().getEnvelope().getMin().getY();
                        double otherMaxY = other.getBBox().getEnvelope().getMax().getY();
                        return ( ( Math.abs( thisMaxY - otherMinY ) < 0.00001 ) || ( Math.abs( thisMinY - otherMaxY ) < 0.00001 ) );
                    }
                    // comparing Y
                    double thisMaxX = this.bbox.getEnvelope().getMax().getX();
                    double thisMinX = this.bbox.getEnvelope().getMin().getX();
                    double otherMinX = other.getBBox().getEnvelope().getMin().getX();
                    double otherMaxX = other.getBBox().getEnvelope().getMax().getX();
                    return ( Math.abs( thisMaxX - otherMinX ) < 0.00001 )
                           || ( Math.abs( thisMinX - otherMaxX ) < 0.00001 );

                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "QuadNode sorted in Direction: " + ( ( comparingX ) ? "x" : "y" ) + " comparePosition: "
                   + comparePosition + " compareLength: " + compareLength;
        }

    }
}
