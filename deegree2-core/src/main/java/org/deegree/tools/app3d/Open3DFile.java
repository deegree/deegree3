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

package org.deegree.tools.app3d;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wpvs.j3d.DefaultSurface;
import org.deegree.ogcwebservices.wpvs.j3d.Object3DFactory;
import org.deegree.ogcwebservices.wpvs.j3d.TexturedSurface;
import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * <code>Open3DFile</code> reads a vrml, gml and shape files and creates a j3d scene from them.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Open3DFile {
    private static ILogger LOG = LoggerFactory.getLogger( Open3DFile.class );

    private String fileName;

    private BranchGroup openedFile;

    private View3DFile parent;

    /**
     * @param fileName
     * @param parent
     *            the frame to which message can be sent
     */
    public Open3DFile( String fileName, View3DFile parent ) {
        this.fileName = fileName;
        this.parent = parent;
        openedFile = null;
    }

    /**
     * @param progressBar
     *
     *
     */
    public void openFile( final JProgressBar progressBar ) {

        try {
            if ( fileName.toUpperCase().endsWith( ".SHP" ) ) {
                openedFile = readShape( fileName, progressBar );
            } else if ( fileName.toUpperCase().endsWith( ".XML" ) || fileName.toUpperCase().endsWith( ".GML" ) ) {
                openedFile = readGML( fileName, progressBar );
            } else if ( fileName.toUpperCase().endsWith( ".WRL" ) || fileName.toUpperCase().endsWith( ".VRML" ) ) {
                openedFile = readVRML( fileName, progressBar );
            }
        } catch ( IOException e ) {
            parent.showExceptionDialog( e.getMessage() + "\nPlease, see the error log for detailed information." );
        }
        if ( progressBar != null ) {
            progressBar.setValue( 100 );
        }

    }

    /**
     * @return the instantiated branchgroup or <code>null</code> if no branchgroup was instantiated
     */
    public BranchGroup getOpenedFile() {
        return openedFile;
    }

    private BranchGroup readShape( String fileName, JProgressBar progressBar )
                            throws IOException {
        try {
            ShapeFile file = new ShapeFileReader( fileName ).read();
            if ( progressBar != null ) {
                progressBar.setValue( 10 );
            }
            return createUniformShape3D( file.getFeatureCollection(), progressBar );
        } catch ( Exception e ) {
            LOG.logError( "Could not open shape file: " + fileName + " because: " + e.getMessage(), e );
            throw new IOException( "Could not open shape file: " + fileName );
        }
    }

    private BranchGroup readGML( String fileName, JProgressBar progressBar )
                            throws IOException {
        try {
            XMLFragment doc = new XMLFragment( new File( fileName ) );
            boolean isCityGML = ( doc.getRootElement().getOwnerDocument().lookupPrefix(
                                                                                        CommonNamespaces.CITYGMLNS.toASCIIString() ) != null )
                                || CommonNamespaces.CITYGMLNS.toASCIIString().equals(
                                                                                      doc.getRootElement().getOwnerDocument().lookupNamespaceURI(
                                                                                                                                                  "" ) );

            if ( !isCityGML ) {
                isCityGML = isCityGMLDefined( doc.getRootElement() );
            }
            boolean useDeegree = false;
            if ( progressBar != null ) {
                progressBar.setValue( 5 );
            }
            if ( isCityGML ) {
                useDeegree = ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                                                                                        parent,
                                                                                        "The given file contains cityGML, please choose the import method.\n 1) Load the file with all it's textures and colors.\n 2) Load the file with a uniform color.\nDue to a bug in Java3d the first option may lead to unwanted results.\n\nLoad the file with all colors and textures?",
                                                                                        "Choose import method",
                                                                                        JOptionPane.YES_NO_OPTION ) );
                XSLTDocument transformer = null;
                if ( useDeegree ) {
                    transformer = new XSLTDocument( View3DFile.class.getResource( "citygmlTOgml3.xsl" ) );
                } else {
                    transformer = new XSLTDocument( View3DFile.class.getResource( "toShape.xsl" ) );
                }
                doc = transformer.transform( doc );
            }
            if ( progressBar != null ) {
                progressBar.setValue( 10 );
            }
            GMLFeatureCollectionDocument gmlDoc = new GMLFeatureCollectionDocument();
            gmlDoc.setRootElement( doc.getRootElement() );

            FeatureCollection fc = gmlDoc.parse();
            if ( LOG.isDebug() ) {
                File f = File.createTempFile( "in_file", ".gml" );
                LOG.logDebug( "temp file created at: ", f );
                f.deleteOnExit();
                FileWriter fw = new FileWriter( f );
                fw.write( gmlDoc.getAsPrettyString() );
                fw.close();
                GMLFeatureAdapter fa = new GMLFeatureAdapter( true );
                GMLFeatureCollectionDocument faDoc = fa.export( fc );
                f = File.createTempFile( "out_file", ".gml" );
                f.deleteOnExit();
                fw = new FileWriter( f );
                LOG.logDebug( "temp file created at: ", f );
                fw.write( faDoc.getAsPrettyString() );
                fw.close();
            }
            if ( useDeegree ) {
                return createShape3DWithMaterial( fc, progressBar );
            }
            return createUniformShape3D( fc, progressBar );
        } catch ( Exception e ) {
            LOG.logError( "Could not open gml file: " + fileName + " because: " + e.getMessage(), e );
            throw new IOException( "Could not open gml file: " + fileName );
        }
    }

    private BranchGroup readVRML( String fileName, JProgressBar progressBar )
                            throws IOException {
        VrmlLoader loader = new VrmlLoader();
        try {
            if ( progressBar != null ) {
                progressBar.setValue( 10 );
            }
            Scene scene = loader.load( fileName );
            if ( scene != null ) {
                if ( progressBar != null ) {
                    progressBar.setValue( 60 );
                    Thread.sleep( 50 );
                }
                BranchGroup bg = scene.getSceneGroup();
                bg.setCapability( BranchGroup.ALLOW_DETACH );
                if ( progressBar != null ) {
                    progressBar.setValue( 90 );
                    Thread.sleep( 50 );
                }
                return bg;
            }
            throw new Exception( "Could not create scene from file: " + fileName );
        } catch ( Exception e ) {
            LOG.logError( "Error while loading vrml from file: " + fileName, e );
            throw new IOException( "Could not create vrml scene from file: " + fileName + " because: " + e.getMessage() );
        }
    }

    /**
     * @param contextNode
     * @return true if the namespace "http://www.citygml.org/citygml/1/0/0" was found in one of the nodes of the
     *         dom-tree.
     */
    private boolean isCityGMLDefined( Node contextNode ) {

        boolean isCityGML = ( contextNode.lookupPrefix( CommonNamespaces.CITYGMLNS.toASCIIString() ) != null )
                            || CommonNamespaces.CITYGMLNS.toASCIIString().equals( contextNode.lookupNamespaceURI( null ) );
        if ( !isCityGML ) {
            NodeList nl = contextNode.getChildNodes();
            for ( int i = 0; i < nl.getLength(); ++i ) {
                isCityGML = isCityGMLDefined( nl.item( i ) );
                if ( isCityGML ) {
                    return true;
                }
            }
        }
        return isCityGML;
    }

    private Shape3D mapGeometryToShape3D( Geometry geom, Point3d translation ) {
        if ( geom instanceof Point ) {
            return createShape3D( (Point) geom, translation );
        } else if ( geom instanceof Curve ) {
            return createShape3D( (Curve) geom, translation );
        } else if ( geom instanceof Surface ) {
            return createShape3D( (Surface) geom, translation );
        } else if ( geom instanceof MultiSurface ) {
            return createShape3D( (MultiSurface) geom, translation );
        } else {
            if ( geom == null ) {
                LOG.logError( "Could not map the geometry which was not instantiated" );
            } else {
                LOG.logError( "Could not map the geometry: " + geom.getClass().getName() );
            }
            return null;
        }
    }

    private Shape3D createShape3D( Point p, Point3d translation ) {
        GeometryArray geomArray = new PointArray( 1, GeometryArray.COORDINATES );
        double z = p.getZ();
        if ( Double.isInfinite( z ) || Double.isNaN( z ) ) {
            z = 0;
        }
        geomArray.setCoordinate( 0, new Point3d( p.getX() + translation.x, p.getY() + translation.y, z + translation.z ) );
        Shape3D result = new Shape3D( geomArray );
        result.setAppearanceOverrideEnable( true );
        return result;
    }

    private Shape3D createShape3D( Curve c, Point3d translation ) {
        int totalPoints = 0;
        List<Integer> failSegments = new ArrayList<Integer>();
        for ( int i = 0; i < c.getNumberOfCurveSegments(); ++i ) {
            try {
                totalPoints += c.getCurveSegmentAt( i ).getNumberOfPoints();
            } catch ( GeometryException e ) {
                LOG.logError( "Could not get CurveSegment at position: " + i );
                failSegments.add( new Integer( i ) );
            }
        }

        LineArray geomArray = new LineArray( totalPoints, GeometryArray.COORDINATES );
        for ( int i = 0, pointCounter = 0; i < c.getNumberOfCurveSegments(); ++i ) {
            if ( !failSegments.contains( new Integer( i ) ) ) {
                CurveSegment segment = null;
                try {
                    segment = c.getCurveSegmentAt( i );
                } catch ( GeometryException e ) {
                    // cannot happen.
                }
                for ( int k = 0; k < segment.getNumberOfPoints(); ++k ) {
                    Position p = segment.getPositionAt( k );
                    double z = p.getZ();
                    if ( Double.isInfinite( z ) || Double.isNaN( z ) ) {
                        z = 0;
                    }
                    geomArray.setCoordinate( pointCounter++, new Point3d( p.getX() + translation.x, p.getY()
                                                                                                    + translation.y,
                                                                          z + translation.z ) );
                }
            }
        }
        Shape3D result = new Shape3D( geomArray );
        result.setAppearanceOverrideEnable( true );
        return result;
    }

    /**
     *
     * @param surface
     *            to be created
     * @param translation
     *            to origin of the scene
     * @return a Shape3D created from the surface.
     */
    private Shape3D createShape3D( Surface surface, Point3d translation ) {
        GeometryInfo geometryInfo = new GeometryInfo( GeometryInfo.POLYGON_ARRAY );
        Position[] pos = surface.getSurfaceBoundary().getExteriorRing().getPositions();
        Ring[] innerRings = surface.getSurfaceBoundary().getInteriorRings();
        int numberOfRings = 1;
        int numberOfCoordinates = 3 * ( pos.length                   );
        if ( innerRings != null ) {
            for ( int i = 0; i < innerRings.length; i++ ) {
                numberOfRings++;
                numberOfCoordinates += ( 3 * innerRings[i].getPositions().length );
            }
        }

        float[] coords = new float[numberOfCoordinates];
        int contourCounts[] = { numberOfRings };
        int[] stripCounts = new int[numberOfRings];
        numberOfRings = 0;
        stripCounts[numberOfRings++] = pos.length;

        int z = 0;
        for ( int i = 0; i < pos.length; i++ ) {
            double zValue = pos[i].getZ();
            if ( Double.isInfinite( zValue ) || Double.isNaN( zValue ) ) {
                zValue = 0;
            }
            // LOG.logDebug( "Found a point in a surface: " + pos[i] );
            coords[z++] = (float) ( pos[i].getX() + translation.x );
            coords[z++] = (float) ( pos[i].getY() + translation.y );
            coords[z++] = (float) ( zValue + translation.z );
        }

        if ( innerRings != null ) {
            for ( int j = 0; j < innerRings.length; j++ ) {
                pos = innerRings[j].getPositions();
                stripCounts[numberOfRings++] = pos.length;
                for ( int i = 0; i < pos.length; i++ ) {
                    double zValue = pos[i].getZ();
                    if ( Double.isInfinite( zValue ) || Double.isNaN( zValue ) ) {
                        zValue = 0;
                    }
                    coords[z++] = (float) ( pos[i].getX() + translation.x );
                    coords[z++] = (float) ( pos[i].getY() + translation.y );
                    coords[z++] = (float) ( zValue + translation.z );
                }
            }
        }
        geometryInfo.setCoordinates( coords );
        geometryInfo.setStripCounts( stripCounts );
        geometryInfo.setContourCounts( contourCounts );
        geometryInfo.recomputeIndices();

        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals( geometryInfo );
        Shape3D result = new Shape3D( geometryInfo.getGeometryArray() );
        result.setCapability( Shape3D.ALLOW_GEOMETRY_READ );
        result.setCapability( Shape3D.ALLOW_GEOMETRY_WRITE );
        result.setAppearanceOverrideEnable( true );
        return result;
    }

    /**
     * @param multiSurface
     * @param translation
     * @return a Shape3D created from the multisurfaces.
     */
    private Shape3D createShape3D( MultiSurface multiSurface, Point3d translation ) {
        LOG.logDebug( "Found a Multi surface" );
        Shape3D result = new Shape3D();
        result.setCapability( Shape3D.ALLOW_GEOMETRY_READ );
        result.setCapability( Shape3D.ALLOW_GEOMETRY_WRITE );
        Surface[] allSurfaces = multiSurface.getAllSurfaces();
        for ( int surfaceCount = 0; surfaceCount < allSurfaces.length; ++surfaceCount ) {
            Surface surface = multiSurface.getSurfaceAt( surfaceCount );
            Shape3D s3D = createShape3D( surface, translation );
            result.addGeometry( s3D.getGeometry() );
        }
        result.setAppearanceOverrideEnable( true );
        return result;
    }

    /**
     * This method recursively constructs all the surfaces contained in the given feature. If the Feature contains a
     * PropertyType of {@link Types#FEATURE} this Feature will also be traversed, if it contains a
     * {@link Types#GEOMETRY} a {@link DefaultSurface} will be created.
     *
     * @param o3DFactory
     *            the Factory to create the defaultservice
     * @param feature
     *            the feature to traverse.
     */
    private void createSurfaces( Object3DFactory o3DFactory, Feature feature, Map<String, TexturedSurface> textureMap,
                                 BranchGroup result ) {

        FeatureType ft = feature.getFeatureType();
        PropertyType[] propertyTypes = ft.getProperties();
        for ( PropertyType pt : propertyTypes ) {
            if ( pt.getType() == Types.FEATURE ) {
                FeatureProperty[] fp = feature.getProperties( pt.getName() );
                if ( fp != null ) {
                    for ( int i = 0; i < fp.length; i++ ) {
                        createSurfaces( o3DFactory, (Feature) fp[i].getValue(), textureMap, result );
                    }
                }
            } else if ( pt.getType() == Types.GEOMETRY ) {
                DefaultSurface ds = o3DFactory.createSurface( feature, textureMap );
                if ( ds != null ) {
                    ds.compile();
                    result.addChild( ds );
                    // resolutionStripe.addFeature( id + "_" + ds.getDefaultSurfaceID(), ds );
                }
            }
        }
    }

    private BranchGroup createUniformShape3D( FeatureCollection fc, JProgressBar progressBar ) {
        BranchGroup bg = new BranchGroup();
        bg.setCapability( BranchGroup.ALLOW_DETACH );
        Appearance app = new Appearance();
        RenderingAttributes ra = new RenderingAttributes();
        ra.setDepthBufferEnable( true );
        app.setRenderingAttributes( ra );
        Material material = new Material();
        material.setAmbientColor( new Color3f( Color.WHITE ) );
        material.setDiffuseColor( new Color3f( Color.RED ) );
        material.setSpecularColor( new Color3f( Color.BLUE ) );
        ColoringAttributes ca = new ColoringAttributes();
        ca.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
        ca.setCapability( ColoringAttributes.NICEST );
        app.setColoringAttributes( ca );
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace( PolygonAttributes.CULL_NONE );
        pa.setBackFaceNormalFlip( true );
        pa.setPolygonMode( PolygonAttributes.POLYGON_FILL );
        app.setPolygonAttributes( pa );
        app.setMaterial( material );
        Envelope bbox = null;
        for ( int i = 0; i < fc.size(); ++i ) {
            Feature f = fc.getFeature( i );
            Geometry geom = f.getDefaultGeometryPropertyValue();
            if ( LOG.isDebug() ) {
                try {
                    LOG.logDebug( "Found geometry: " + WKTAdapter.export( geom ) );
                } catch ( GeometryException e ) {
                    LOG.logError( e.getMessage(), e );
                }

            }
            if ( bbox == null ) {
                bbox = geom.getEnvelope();
            } else {
                try {
                    bbox = bbox.merge( geom.getEnvelope() );
                    LOG.logDebug( "merging the bboxes resulted in: " + bbox );
                } catch ( GeometryException e ) {
                    LOG.logError( "Couldn't merge the bboxes of the found featureCollection", e );
                }
            }
        }
        if ( progressBar != null ) {
            progressBar.setValue( 20 );
        }
        Point3d centroid = new Point3d( 0, 0, 0 );
        if ( bbox != null ) {
            Point p = bbox.getCentroid();
            double zValue = p.getZ();
            if ( Double.isInfinite( zValue ) || Double.isNaN( zValue ) ) {
                zValue = 0;
            }
            centroid.set( -p.getX(), -p.getY(), -zValue );
        }
        final double progress = ( (double) ( ( progressBar.getMaximum() - 10 ) - progressBar.getValue() ) ) / fc.size();
        double currentProg = progressBar.getValue();
        for ( int i = 0; i < fc.size(); ++i ) {
            Feature f = fc.getFeature( i );
            if ( progressBar != null ) {
                currentProg += progress;
                if ( ( (int) Math.floor( currentProg ) - progressBar.getValue() ) > 5 ) {
                    progressBar.setValue( (int) Math.floor( currentProg ) );
                }
            }
            Geometry geom = f.getDefaultGeometryPropertyValue();
            Shape3D shape = mapGeometryToShape3D( geom, centroid );
            if ( shape != null ) {
                shape.setAppearance( app );
                bg.addChild( shape );
            } else {
                // System.out.println( "ERRORORORORORORO" );
            }
        }
        if ( !bg.getAllChildren().hasMoreElements() ) {
            throw new IllegalArgumentException( "Could not read any 3D-Info from given featurecollection." );
        }
        return bg;
    }

    /**
     * Use the Object3DFactory of the wpvs to create colored surfaces and textures.
     *
     * @param fc
     * @return the branch group
     */
    private BranchGroup createShape3DWithMaterial( FeatureCollection fc, JProgressBar progressBar ) {
        BranchGroup bg = new BranchGroup();
        bg.setCapability( BranchGroup.ALLOW_DETACH );

        Object3DFactory o3DFactory = new Object3DFactory();
        Map<String, TexturedSurface> textureMap = new HashMap<String, TexturedSurface>( fc.size() * 10 );
        final double progress = ( (double) ( ( progressBar.getMaximum() - 10 ) - progressBar.getValue() ) ) / fc.size();
        double currentProg = progressBar.getValue();
        for ( int i = 0; i < fc.size(); ++i ) {
            if ( progressBar != null ) {
                currentProg += progress;
                if ( ( (int) Math.floor( currentProg ) - progressBar.getValue() ) > 5 ) {
                    progressBar.setValue( (int) Math.floor( currentProg ) );
                }
            }
            Feature feature = fc.getFeature( i );
            createSurfaces( o3DFactory, feature, textureMap, bg );
        }
        if ( textureMap.size() > 0 ) {
            Set<String> keys = textureMap.keySet();
            for ( String key : keys ) {
                if ( key != null ) {
                    TexturedSurface surf = textureMap.get( key );
                    if ( surf != null ) {
                        surf.compile();
                        bg.addChild( surf );
                    }
                }

            }
        }
        if ( !bg.getAllChildren().hasMoreElements() ) {
            throw new IllegalArgumentException( "Could not read any 3D-Info from given featurecollection." );
        }
        return bg;
    }
}
