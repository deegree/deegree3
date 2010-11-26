//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.cs.transformations.ntv2;

import static org.deegree.cs.transformations.TransformationFactory.createWGSAlligned;
import static org.deegree.cs.transformations.coordinate.ConcatenatedTransform.concatenate;
import static org.deegree.cs.transformations.coordinate.MatrixTransform.createMatrixTransform;
import static org.deegree.cs.utilities.Matrix.swapAndRotateGeoAxis;
import static org.deegree.cs.utilities.ProjectionUtils.DTR;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.utilities.Matrix;
import org.deegree.cs.utilities.ProjectionUtils;
import org.slf4j.Logger;

import au.com.objectix.jgridshift.GridShift;
import au.com.objectix.jgridshift.GridShiftFile;
import au.com.objectix.jgridshift.SubGrid;

/**
 * An NTv2 Transformation uses a GridShift file to transform ordinates defined in a source CRS based on a given
 * ellipsoid to ordinates in a target CRS based on another ellipsoid. The Coordinate systems are normally
 * {@link GeographicCRS}s.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get stack traces if an error occurred while loading / transforming (on) a gridshift file.")
public class NTv2Transformation extends Transformation {

    private static final Logger LOG = getLogger( NTv2Transformation.class );

    private GridShiftFile gsf;

    private boolean isIdentity;

    private URL gridURL;

    private boolean swapFromSource;

    private boolean swapToTarget;

    private NTv2Transformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id ) {
        super( sourceCRS, targetCRS, id );
        swapFromSource = checkAxisOrientation( sourceCRS.getAxis() );
        swapToTarget = checkAxisOrientation( targetCRS.getAxis() );
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @param id
     * @param gridURL
     */
    public NTv2Transformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id, URL gridURL ) {
        this( sourceCRS, targetCRS, id );
        if ( gridURL == null ) {
            throw new NullPointerException( "The NTv2 transformation needs a grid file to work on." );
        }
        try {
            this.gridURL = gridURL;
            // rb: a hack, because loading from the gridURL may result in undefined results.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = new BufferedInputStream( gridURL.openStream() );
            byte[] b = new byte[32];
            int r = in.read( b );
            while ( r != -1 ) {
                out.write( b, 0, r );
                r = in.read( b );
            }
            in.close();
            out.flush();
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream( out.toByteArray() );

            gsf = new GridShiftFile();
            gsf.loadGridShiftFile( bin, false );
            bin.close();
        } catch ( FileNotFoundException e ) {
            LOG.debug( "Could not find the gridshift file stack trace.", e );
            LOG.error( "Could not find the gridshift file because: " + e.getLocalizedMessage() );
            throw new IllegalArgumentException( "Could not load the gridshift file: " + gridURL, e );
        } catch ( IOException e ) {
            LOG.debug( "Could not read the gridshift file stack trace.", e );
            LOG.error( "Could not read the gridshift file because: " + e.getLocalizedMessage() );
            throw new IllegalArgumentException( "Could not load the gridshift file: " + gridURL, e );
        }

        String fromEllips = gsf.getFromEllipsoid();
        String toEllips = gsf.getToEllipsoid();

        Ellipsoid sourceEl = sourceCRS.getGeodeticDatum().getEllipsoid();
        Ellipsoid targetEl = targetCRS.getGeodeticDatum().getEllipsoid();

        // rb: patched the gridshift file for access to the axis
        if ( Math.abs( sourceEl.getSemiMajorAxis() - gsf.getFromSemiMajor() ) > 0.001
             || Math.abs( sourceEl.getSemiMinorAxis() - gsf.getFromSemiMinor() ) > 0.001 ) {
            LOG.warn( "The given source CRS' ellipsoid (" + sourceEl.getCode().getOriginal()
                      + ") does not match the 'from' ellipsoid (" + fromEllips + ")defined in the gridfile: " + gridURL );
        }

        if ( Math.abs( targetEl.getSemiMajorAxis() - gsf.getToSemiMajor() ) > 0.001
             || Math.abs( targetEl.getSemiMinorAxis() - gsf.getToSemiMinor() ) > 0.001 ) {
            LOG.warn( "The given target CRS' ellipsoid (" + targetEl.getCode().getOriginal()
                      + ") does not match the 'to' ellipsoid (" + toEllips + ") defined in the gridfile: " + gridURL );
        }

        isIdentity = ( Math.abs( gsf.getFromSemiMajor() - gsf.getToSemiMajor() ) < 0.001 )
                     && ( Math.abs( gsf.getFromSemiMinor() - gsf.getToSemiMinor() ) < 0.001 );
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @param id
     * @param gsf
     *            the loaded gridshift file
     */
    public NTv2Transformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id,
                               GridShiftFile gsf ) {
        this( sourceCRS, targetCRS, id );
        this.gsf = gsf;
        String fromEllips = gsf.getFromEllipsoid();
        String toEllips = gsf.getToEllipsoid();

        Ellipsoid sourceEl = sourceCRS.getGeodeticDatum().getEllipsoid();
        Ellipsoid targetEl = targetCRS.getGeodeticDatum().getEllipsoid();

        // rb: patched the gridshift file for access to the axis
        if ( Math.abs( sourceEl.getSemiMajorAxis() - gsf.getFromSemiMajor() ) > 0.001
             || Math.abs( sourceEl.getSemiMinorAxis() - gsf.getFromSemiMinor() ) > 0.001 ) {
            LOG.warn( "The given source CRS' ellipsoid (" + sourceEl.getCode().getOriginal()
                      + ") does not match the 'from' ellipsoid (" + fromEllips + ")defined in the gridfile: " + gridURL );
        }

        if ( Math.abs( targetEl.getSemiMajorAxis() - gsf.getToSemiMajor() ) > 0.001
             || Math.abs( targetEl.getSemiMinorAxis() - gsf.getToSemiMinor() ) > 0.001 ) {
            LOG.warn( "The given target CRS' ellipsoid (" + targetEl.getCode().getOriginal()
                      + ") does not match the 'to' ellipsoid (" + toEllips + ") defined in the gridfile: " + gridURL );
        }

        isIdentity = ( Math.abs( gsf.getFromSemiMajor() - gsf.getToSemiMajor() ) < 0.001 )
                     && ( Math.abs( gsf.getFromSemiMinor() - gsf.getToSemiMinor() ) < 0.001 );
    }

    /**
     * @param axis
     * @return
     */
    private boolean checkAxisOrientation( Axis[] axis ) {
        boolean result = false;
        if ( axis == null || axis.length != 2 ) {
            result = false;
        } else {
            Axis first = axis[0];
            Axis second = axis[1];
            LOG.debug( "First crs Axis: " + first );
            LOG.debug( "Second crs Axis: " + second );
            if ( first != null && second != null ) {
                if ( Axis.AO_WEST == Math.abs( second.getOrientation() ) ) {
                    result = true;
                    if ( Axis.AO_NORTH != Math.abs( first.getOrientation() ) ) {
                        LOG.warn( "The given projection uses a second axis which is not mappable (  " + second
                                  + ") please check your configuration, assuming y, x axis-order." );
                    }
                }
            }
        }
        LOG.debug( "Incoming ordinates will" + ( ( result ) ? " " : " not " ) + "be swapped." );
        return result;
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        GridShift shifter = new GridShift();

        for ( Point3d p : srcPts ) {
            // rb: only degrees are supported :-)
            shifter.setLonPositiveEastDegrees( p.x * ProjectionUtils.RTD );
            shifter.setLatDegrees( p.y * ProjectionUtils.RTD );
            boolean shift = false;
            try {
                if ( isInverseTransform() ) {
                    shift = gsf.gridShiftReverse( shifter );
                } else {
                    shift = gsf.gridShiftForward( shifter );
                }
            } catch ( IOException e ) {
                LOG.debug( "Exception occurred: " + e.getLocalizedMessage(), e );
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
            if ( !shift ) {
                StringBuilder sb = new StringBuilder( "Could not do " );
                sb.append( ( isInverseTransform() ? "an inverse" : "a forward" ) ).append( " transform because: " );
                sb.append( "gridfile is loaded: " ).append( gsf.isLoaded() );
                SubGrid[] subGridTree = gsf.getSubGridTree();
                if ( subGridTree == null ) {
                    sb.append( "no sub grid tree could be retrieved." );
                } else {
                    sb.append( "Getting SubGrid for coordinates: " );
                    sb.append( shifter.getLonPositiveEastDegrees() ).append( "," ).append( shifter.getLatDegrees() );
                    SubGrid sg = subGridTree[0];
                    SubGrid forCoord = sg.getSubGridForCoord( shifter.getLonPositiveWestSeconds(),
                                                              shifter.getLatSeconds() );
                    if ( forCoord == null ) {
                        sb.append( ". Retrieval of SubGrid for coordinates: " );
                        sb.append( shifter.getLonPositiveEastDegrees() ).append( "," );
                        sb.append( shifter.getLatDegrees() );
                        sb.append( " Failed." );
                    }
                }
                LOG.info( sb.toString() );
            } else {
                StringBuilder sb = new StringBuilder( "Successfully applied " );
                sb.append( ( isInverseTransform() ? "an inverse" : "a forward" ) ).append(
                                                                                           " transform for incoming points: " );
                sb.append( shifter.getLonPositiveEastDegrees() ).append( "," ).append( shifter.getLatDegrees() );
                sb.append( ", result->" );
                sb.append( shifter.getShiftedLonPositiveEastDegrees() ).append( "," ).append(
                                                                                              shifter.getShiftedLatDegrees() );
                LOG.debug( sb.toString() );
            }
            // if ( swapToTarget ) {
            // p.x = shifter.getShiftedLatDegrees() * DTR;
            // p.y = shifter.getShiftedLonPositiveEastDegrees() * DTR;
            // } else {
            p.x = shifter.getShiftedLonPositiveEastDegrees() * DTR;
            p.y = shifter.getShiftedLatDegrees() * DTR;
            // }
        }
        return srcPts;
    }

    @Override
    public String getImplementationName() {
        return "NTv2";
    }

    @Override
    public boolean isIdentity() {
        return isIdentity;
    }

    /**
     * @return the url to the gridfile.
     */
    public URL getGridfile() {
        return this.gridURL;
    }

    /**
     * @return the loaded gridshift file
     */
    public GridShiftFile getGridfileRef() {
        return this.gsf;
    }

    @Override
    public void inverse() {
        super.inverse();
        boolean s = swapFromSource;
        this.swapFromSource = swapToTarget;
        this.swapToTarget = s;
    }

    /**
     * Create a concatenated (swap) transform for the {@link NTv2Transformation} if the ({@link GeographicCRS}) source
     * and target are not aligned with the expected lon/lat.
     * 
     * @param transform
     *            to create a swap matrix for.
     * @return the concatenated Swap tranformation for the given transform Matrix
     */
    public final static Transformation createAxisAllignedNTv2Transformation( NTv2Transformation transform ) {
        Transformation result = transform;
        final GeographicCRS sourceCRS = (GeographicCRS) result.getSourceCRS();
        final GeographicCRS targetCRS = (GeographicCRS) result.getTargetCRS();
        if ( sourceCRS != null && targetCRS != null ) {
            final GeographicCRS alignedSource = createWGSAlligned( sourceCRS );
            final GeographicCRS alignedTarget = createWGSAlligned( targetCRS );
            try {
                final Matrix first = swapAndRotateGeoAxis( sourceCRS, alignedSource );
                final Matrix second = swapAndRotateGeoAxis( alignedTarget, targetCRS );
                result = concatenate( createMatrixTransform( sourceCRS, alignedSource, first ), result,
                                      createMatrixTransform( alignedTarget, targetCRS, second ) );
            } catch ( TransformationException e ) {
                LOG.warn( "Could not create an alignment matrix for the supplied NTv2 transformation, are the coordinate systems correctly defined?" );
            }
        }
        return result;
    }
}
