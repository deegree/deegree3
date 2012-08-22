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
package org.deegree.ogcwebservices.wpvs.j3d;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceBoundary;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class DefaultSurface extends Shape3D {

    /**
     * The geometry to create the Shape3D from.
     */
    protected Geometry geometry;

    private String parentID;

    private String objectID;

    /**
     *
     * @param objectID
     * @param parentID
     * @param geometry
     */
    public DefaultSurface( String objectID, String parentID, Geometry geometry ) {
        super();
        if ( !( geometry instanceof MultiSurface || geometry instanceof Surface ) ) {
            throw new UnsupportedOperationException( "Currently only surface and multisurfaces are supported" );
        }
        this.geometry = geometry;
        this.parentID = parentID;
        this.objectID = objectID;

        /*
         * System.out.println( "new Appearance" ); Color3f specular = new Color3f( 0.7f, 0.7f, 0.7f ); Color3f white =
         * new Color3f( 0.4f, 1, 0.4f );
         *
         * Material targetMaterial = new Material(); targetMaterial.setAmbientColor( white );
         * targetMaterial.setDiffuseColor( white ); targetMaterial.setSpecularColor( specular );
         * targetMaterial.setShininess( 75.0f ); targetMaterial.setLightingEnable( true ); targetMaterial.setCapability(
         * Material.ALLOW_COMPONENT_WRITE ); // ColoringAttributes ca = new ColoringAttributes(); ca.setShadeModel(
         * ColoringAttributes.SHADE_GOURAUD ); // Appearance defaultAppearance = new Appearance();
         * defaultAppearance.setMaterial( targetMaterial ); defaultAppearance.setColoringAttributes( ca ); //
         * PolygonAttributes targetPolyAttr = new PolygonAttributes(); targetPolyAttr.setCapability(
         * PolygonAttributes.ALLOW_MODE_WRITE ); targetPolyAttr.setCapability( PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE );
         * targetPolyAttr.setPolygonMode( PolygonAttributes.POLYGON_FILL ); targetPolyAttr.setCullFace(
         * PolygonAttributes.CULL_NONE ); // pa.setPolygonMode( PolygonAttributes.POLYGON_LINE );
         * defaultAppearance.setPolygonAttributes( targetPolyAttr ); setAppearance( defaultAppearance );
         */
    }

    /**
     * @return the ID of the Object this Surface is a part of (e.g. the building id if this is a wall)
     */
    public String getParentID() {
        return parentID;
    }

    /**
     * @return the objectID value.
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * @return a String composited of the parentID and "_" and the objectID
     */
    public String getDefaultSurfaceID() {
        return parentID + '_' + objectID;
    }

    /**
     *
     * @return the surface geometry encapsulated
     */
    public Geometry getSurfaceGeometry() {
        return geometry;
    }

    /**
     * this method must be called before adding the surface to a Group
     */
    public void compile() {
        setAppearanceOverrideEnable( true );
        addGeometries( geometry );
        setCapability( Shape3D.ALLOW_GEOMETRY_READ );
        setCapability( Shape3D.ALLOW_GEOMETRY_WRITE );
    }

    /**
     * Adds the given surface.
     *
     * @param surface
     *            to be added( only surface and multisurface are currently supported).
     */
    public void addGeometries( org.deegree.model.spatialschema.Geometry surface ) {
        /**
         * First create the coordinates and the contours
         */
        if ( surface != null ) {
            if ( surface instanceof MultiSurface ) {
                for ( Surface multiSurface : ( (MultiSurface) surface ).getAllSurfaces() ) {
                    GeometryInfo geomInfo = getGeometryInfo( multiSurface );
                    addGeometry( geomInfo.getGeometryArray() );
                }
            } else if ( surface instanceof Surface ) {
                // ((extractSurface( (Surface) surface, coordinates, contourCount );
                GeometryInfo geomInfo = getGeometryInfo( (Surface) surface );
                addGeometry( geomInfo.getGeometryArray() );
            } else {
                throw new IllegalArgumentException( "Don't know how to create a textured surface from given geometry" );
            }
        }
    }

    /**
     * Creates a geometry info of the given surface.
     *
     * @param surface
     * @return the geometry info
     */
    public GeometryInfo getGeometryInfo( Surface surface ) {

        GeometryInfo geometryInfo = new GeometryInfo( GeometryInfo.POLYGON_ARRAY );
        List<Point3d> coordinates = new ArrayList<Point3d>();
        List<Integer> contourCount = new ArrayList<Integer>( 200 );

        extractSurface( surface, coordinates, contourCount );

        /**
         * Now create the geometry info as a polygon array
         */
        geometryInfo.setCoordinates( coordinates.toArray( new Point3d[0] ) );
        int[] sc = new int[contourCount.size()];
        int stripCountNumber = 0;
        for ( int i = 0; i < contourCount.size(); ++i ) {
            sc[i] = contourCount.get( i ).intValue();
            stripCountNumber += sc[i];
        }
        geometryInfo.setStripCounts( sc );
        // we don't actually need the contours, just an array which has the right length.
        geometryInfo.setContourCounts( new int[] { sc.length } );

        // recalculate the normals
        geometryInfo.recomputeIndices();
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals( geometryInfo );

        // convert the polygon into indexed triangles for faster access.
        geometryInfo.convertToIndexedTriangles();
        return geometryInfo;
    }

    /**
     * Extract the actual coordinates of a surface and puts them with in the given list.
     *
     * @param surface
     *            to export
     * @param coordinates
     *            to fill
     * @param contourCount
     *            will the number of coordinates in a contour (rings).
     */
    protected void extractSurface( Surface surface, List<Point3d> coordinates, List<Integer> contourCount ) {
        if ( surface != null ) {
            SurfaceBoundary sb = surface.getSurfaceBoundary();
            if ( sb != null ) {
                Ring outerRing = sb.getExteriorRing();
                if ( outerRing != null ) {
                    Position[] pos = outerRing.getPositions();
                    if ( pos != null ) {
                        // the surface start and end point are the same, ignoring the last node.
                        for ( int i = 0; i < pos.length - 1; ++i ) {
                            coordinates.add( pos[i].getAsPoint3d() );
                        }
                        contourCount.add( new Integer( pos.length - 1 ) );
                    }

                    Ring[] innerRings = sb.getInteriorRings();
                    if ( innerRings != null ) {
                        for ( Ring innerRing : innerRings ) {
                            pos = innerRing.getPositions();
                            if ( pos != null ) {
                                // the surface start and end point are the same, ignoring the last node.
                                for ( int i = 0; i < pos.length - 1; ++i ) {
                                    coordinates.add( pos[i].getAsPoint3d() );
                                }
                                contourCount.add( new Integer( pos.length - 1 ) );
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * @return a String representation of all the geometries inside this surface
     */
    public String getGeometryAsString() {
        StringBuffer sb = new StringBuffer( numGeometries() );
        for ( int i = 0; i < numGeometries(); ++i ) {
            javax.media.j3d.Geometry ga = getGeometry( i );
            sb.append( ga.toString() );
        }
        return sb.toString();
    }
}
