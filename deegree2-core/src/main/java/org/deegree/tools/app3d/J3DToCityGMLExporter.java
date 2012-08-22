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

import static java.lang.System.exit;
import static org.deegree.ogcbase.CommonNamespaces.CITYGMLNS;
import static org.deegree.ogcbase.CommonNamespaces.CITYGML_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.GMLNS;
import static org.deegree.ogcbase.CommonNamespaces.GML_PREFIX;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Leaf;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;

/**
 * The <code>J3DToCityGMLExporter</code> exports a J3D scene to citygml level 1.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
//TODO
// $node$.getName methods has been comment because of old version of Java3D libraries
public class J3DToCityGMLExporter implements J3DExporter {
    static {
        try {
            new VrmlLoader();
        } catch ( NoClassDefFoundError c ) {
            libHelp( c );
        }
    }

    private static ILogger LOG = LoggerFactory.getLogger( J3DToCityGMLExporter.class );

    private final static String PRE_C = CITYGML_PREFIX + ":";

    private final static String PRE_G = GML_PREFIX + ":";

    private static int textureCount = 0;

    // Instance variables.
    private String name = null;

    private String crsName = "EPSG:31466";

    private String cityGMLFunction = "1001";

    private String textureOutputDir;

    private boolean asWFSTransaction = false;

    // Will hold the texture file names if two different geometries use a single texture.
    // private Map<String, String> savedTextures = new HashMap<String, String>( 200 );

    private static int numberOfSurfaces = 0;

    private Transform3D transformMatrix = null;

    private Transform3D rotationMatrix = null;

    private boolean inverseYZ = true;

    private HashMap<BufferedImage, String> cachedTextures = new HashMap<BufferedImage, String>();

    /**
     * A default constructor allows the instantiation of this exported to supply the getParameterList method.
     * <p>
     * <b>NOTE</b> this constructor is only convenient, it should not be used carelessly. Instead use one of the other
     * constructors for correct instantiation of this class.
     * </p>
     */
    public J3DToCityGMLExporter() {
        // nothing
    }

    /**
     * @param params
     *            a hashmap to extract values from. For a list of supported parameters
     * @throws IOException
     *             if the key 'texdir' points to a file which is not a directory or is not writable
     */
    public J3DToCityGMLExporter( Map<String, String> params ) throws IOException {
        String texDir = params.get( "texdir" );
        if ( texDir == null || "".equals( texDir.trim() ) ) {
            LOG.logInfo( "Setting tex dir to " + System.getProperty( "user.home" ) + "/j3dTextures/" );
            texDir = System.getProperty( "user.home" ) + "/j3dTextures/";
            File t = new File( texDir );
            t.mkdir();
        }
        File tmp = new File( texDir );

        if ( !tmp.isDirectory() ) {
            throw new IOException( "Given file: " + tmp.getAbsoluteFile()
                                   + " exists, but is not a directory, please specify a valid directory." );
        }
        if ( !tmp.canWrite() ) {
            throw new IOException( "Can not write to specified directory: " + tmp.getAbsoluteFile()
                                   + ", please specify directory which can be written to." );
        }
        textureOutputDir = texDir;
        if ( !textureOutputDir.endsWith( File.separator ) ) {
            // just make sure the given path ends with the separator.
            textureOutputDir += File.separator;
        }

        double xTrans = 0;
        String t = params.get( "xtranslation" );
        if ( t != null && !"".equals( t ) ) {
            try {
                xTrans = Double.parseDouble( t );
            } catch ( NumberFormatException nfe ) {
                // nottin
            }
        }
        double yTrans = 0;
        t = params.get( "ytranslation" );
        if ( t != null && !"".equals( t ) ) {
            try {
                yTrans = Double.parseDouble( t );
            } catch ( NumberFormatException nfe ) {
                // nottin
            }
        }
        double zTrans = 0;
        t = params.get( "ztranslation" );
        if ( t != null && !"".equals( t ) ) {
            try {
                zTrans = Double.parseDouble( t );
            } catch ( NumberFormatException nfe ) {
                // nottin
            }
        }
        AxisAngle4d rotAngle = null;
        t = params.get( "rotationangle" );
        if ( t != null && !"".equals( t ) ) {
            try {
                String[] values = t.split( "," );
                LOG.logDebug( "Using rotation angle: " + t );
                if ( values.length != 4 ) {
                    LOG.logError( "The rotation angle must have 4 values and must be separated by a ',' (without spaces) with following form x,y,z,rotation (in radians) . Ignoring your values." );
                } else {
                    rotAngle = new AxisAngle4d( Double.parseDouble( values[0] ), Double.parseDouble( values[1] ),
                                                Double.parseDouble( values[2] ), Double.parseDouble( values[3] ) );
                }
            } catch ( NumberFormatException nfe ) {
                // nottin
            }
        }
        transformMatrix = new Transform3D();
        transformMatrix.setTranslation( new Vector3d( xTrans, yTrans, zTrans ) );
        if ( rotAngle != null ) {
            rotationMatrix = new Transform3D();
            rotationMatrix.setRotation( rotAngle );
        }

        this.asWFSTransaction = false;
        t = params.get( "wfstransaction" );
        if ( t != null && !"".equals( t ) ) {
            this.asWFSTransaction = t.equalsIgnoreCase( "y" ) || t.equalsIgnoreCase( "yes" );
        }
        this.name = "building";
        t = params.get( "name" );
        if ( t != null && !"".equals( t.trim() ) ) {
            this.name = t;
        }

        crsName = "EPSG:4326";
        t = params.get( "srs" );
        if ( t != null && !"".equals( t.trim() ) ) {
            this.crsName = t;
        }

        this.cityGMLFunction = "1001";
        t = params.get( "buildingFunction" );
        if ( t != null && !"".equals( t.trim() ) ) {
            this.cityGMLFunction = t;
        }
        this.inverseYZ = false;
        t = params.get( "inverseyz" );
        if ( t != null && !"".equals( t.trim() ) ) {
            this.inverseYZ = t.equalsIgnoreCase( "y" ) || t.equalsIgnoreCase( "yes" );
        }
    }

    /**
     * @param citygmlName
     *            of the exported branchgroup.
     * @param crsName
     *            of the crs to set the srsName of gmlNodes to, if <code>null</code> 'epsg:31466' will be used.
     * @param cityGMLFunction
     *            to insert om the gml
     * @param textureOutputDirectory
     *            the directory to output the textures to, if any.
     * @param translationX
     *            a value to add up to the found x - coordinates. If NaN, 0 will be used.
     * @param translationY
     *            a value to add up to the found y - coordinates. If NaN, 0 will be used.
     * @param translationZ
     *            a value to add up to the found z - coordinates. If NaN, 0 will be used.
     * @param rotAngle
     *            the rotation angle to which the all geometries should be rotated.
     * @param asWFSTransaction
     *            true if the scene should be exported as a wfs:Transaction document (the root node is wfs:Transaction)
     *            or a cityGML document (the root node is citygml:Building).
     * @param inverseYZ
     * @throws IOException
     *             if the something went wrong with wile creating, referring or addressing the given filePath.
     */
    public J3DToCityGMLExporter( String citygmlName, String crsName, String cityGMLFunction,
                                 String textureOutputDirectory, double translationX, double translationY,
                                 double translationZ, AxisAngle4d rotAngle, boolean asWFSTransaction, boolean inverseYZ )
                            throws IOException {
        if ( citygmlName != null && !"".equals( citygmlName.trim() ) ) {
            this.name = citygmlName;
        }

        if ( crsName != null && !"".equals( crsName.trim() ) ) {
            this.crsName = crsName;
        }

        if ( cityGMLFunction != null && !"".equals( cityGMLFunction.trim() ) ) {
            this.cityGMLFunction = cityGMLFunction;
        }

        if ( textureOutputDirectory != null && !"".equals( textureOutputDirectory.trim() ) ) {
            File tmp = new File( textureOutputDirectory );
            if ( !tmp.exists() ) {
                System.out.print( "The directory: " + textureOutputDirectory + " does not exist, create?: " );
                BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
                String answer = in.readLine();
                if ( answer != null && ( "yes".equalsIgnoreCase( answer ) || "y".equalsIgnoreCase( answer ) ) ) {
                    if ( !tmp.mkdir() ) {
                        throw new IOException( "Could not create given directory: " + tmp.getAbsoluteFile()
                                               + " please create it first!" );
                    }
                } else {
                    throw new IOException( "Given directory: " + tmp.getAbsoluteFile()
                                           + " does not exist, please create it first!" );
                }
            }
            if ( !tmp.isDirectory() ) {
                throw new IOException( "Given file: " + tmp.getAbsoluteFile()
                                       + " exists, but is not a directory, please specify a valid directory." );
            }
            if ( !tmp.canWrite() ) {
                throw new IOException( "Can not write to specified directory: " + tmp.getAbsoluteFile()
                                       + ", please specify directory which can be written to." );
            }
            textureOutputDir = textureOutputDirectory;
            if ( !textureOutputDirectory.endsWith( File.separator ) ) {
                // just make sure the given path ends with the separator.
                textureOutputDir += File.separator;
            }

        } else {
            throw new IOException( "No directory was given for the output textures." );
        }
        transformMatrix = new Transform3D();
        if ( Double.isNaN( translationX ) ) {
            translationX = 0;
        }
        if ( Double.isNaN( translationY ) ) {
            translationY = 0;
        }
        if ( Double.isNaN( translationZ ) ) {
            translationZ = 0;
        }
        transformMatrix.setTranslation( new Vector3d( translationX, translationY, translationZ ) );
        LOG.logDebug( "Hier:\n" + transformMatrix + "\nrotanlge: " + rotAngle );
        if ( rotAngle != null ) {
            // transformMatrix.setRotation( rotAngle );
            rotationMatrix = new Transform3D();
            rotationMatrix.setRotation( rotAngle );
        }
        LOG.logDebug( "Hier2:\n" + transformMatrix );

        this.inverseYZ = inverseYZ;
        this.asWFSTransaction = asWFSTransaction;
    }

    public void export( StringBuilder result, Group j3dScene ) {
        if ( j3dScene != null ) {
            if ( j3dScene.getCapability( Group.ALLOW_CHILDREN_READ ) ) {
                Document doc = XMLTools.create();
                Element root = null;
                Element building = doc.createElementNS( CommonNamespaces.CITYGMLNS.toASCIIString(), PRE_C + "Building" );
                if ( asWFSTransaction ) {
                    root = doc.createElementNS( CommonNamespaces.WFSNS.toASCIIString(), CommonNamespaces.WFS_PREFIX
                                                                                        + ":Transaction" );
                    root.setAttribute( "version", "1.1.0" );
                    root.setAttribute( "service", "WFS" );
                    Element insert = XMLTools.appendElement( root, CommonNamespaces.WFSNS, CommonNamespaces.WFS_PREFIX
                                                                                           + ":Insert" );
                    // Element featureCollection = XMLTools.appendElement( insert,
                    // CommonNamespaces.WFSNS,
                    // CommonNamespaces.WFS_PREFIX + ":FeatureCollection" );

                    // building = (Element)featureCollection.appendChild( building );
                    building = (Element) insert.appendChild( building );

                } else {
                    root = doc.createElementNS( CITYGMLNS.toASCIIString(), PRE_C + "CityModel" );
                    Element member = XMLTools.appendElement( root, CITYGMLNS, PRE_C + "cityObjectMember" );
                    member.appendChild( building );
                }

                // set the name element
                if ( name == null ) {
//                    name = j3dScene.getName();
                    name = "D" + UUID.randomUUID().toString();
                }
                XMLTools.appendElement( building, CommonNamespaces.GMLNS, PRE_G + "name", name );
                boolean calcBounds = false;
                Point3d lower = new Point3d( Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE );
                Point3d upper = new Point3d( Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE );
                if ( j3dScene.getCapability( Node.ALLOW_BOUNDS_READ ) ) {
                    Bounds b = j3dScene.getBounds();
                    if ( b != null ) {
                        BoundingBox bBox = new BoundingBox( b );
                        bBox.getLower( lower );
                        bBox.getUpper( upper );
                        // lower.x += transformMatrix.ttranslationX;
                        // lower.y += translationY;
                        transformMatrix.transform( lower );
                        // upper.x += translationX;
                        // upper.y += translationY;
                        transformMatrix.transform( upper );
                        Element boundBy = XMLTools.appendElement( building, GMLNS, PRE_G + "boundedBy" );
                        Element env = XMLTools.appendElement( boundBy, GMLNS, PRE_G + "Envelope" );
                        env.setAttribute( "srsName", crsName );
                        Element lowerPos = XMLTools.appendElement( env, GMLNS, PRE_G + "pos", lower.x + " " + lower.y
                                                                                              + " " + lower.z );
                        lowerPos.setAttribute( "srsDimension", "" + 3 );
                        Element upperPos = XMLTools.appendElement( env, GMLNS, PRE_G + "pos", upper.x + " " + upper.y
                                                                                              + " " + upper.z );
                        upperPos.setAttribute( "srsDimension", "" + 3 );
                    }
                } else {
                    LOG.logInfo( "The gml bounded by may not be read, it will be calculated from Hand." );
                    calcBounds = true;
                }
                // citygml creation date
                GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
                String timeStamp = cal.get( Calendar.YEAR ) + "-" + ( cal.get( Calendar.MONTH ) + 1 ) + "-"
                                   + cal.get( Calendar.DAY_OF_MONTH ) + "T" + cal.get( Calendar.HOUR_OF_DAY ) + ":"
                                   + cal.get( Calendar.MINUTE );
                XMLTools.appendElement( building, CITYGMLNS, PRE_C + "creationDate", timeStamp );
                XMLTools.appendElement( building, CITYGMLNS, PRE_C + "function", this.cityGMLFunction );
                Element multiSurfaceLod2 = XMLTools.appendElement( building, CITYGMLNS, PRE_C + "lod2MultiSurface" );
                Element multiSurfaceParent = XMLTools.appendElement( multiSurfaceLod2, GMLNS, PRE_G + "MultiSurface" );
                multiSurfaceParent.setAttribute( "srsName", crsName );
                outputGroup( j3dScene, multiSurfaceParent, transformMatrix, calcBounds, lower, upper );
                root = (Element) doc.importNode( root, true );
                XMLFragment frag = new XMLFragment( root );
                result.append( frag.getAsPrettyString() );
                LOG.logDebug( "\n\nExported " + numberOfSurfaces + " surfaces." );
            } else {
                LOG.logInfo( "The given branchgroup may not read it's children, nothing to export." );
            }
        } else {
            LOG.logInfo( "The given branchgroup may not be null, nothing to export." );
        }

    }

    /**
     * Iterates over all children of the given branchgroup and appends their citygml representation to the rootNode.
     *
     * @param j3dScene
     *            to export
     * @param rootNode
     *            to append to.
     * @param calcBounds
     *            if true the bounds should be calculated and placed in lower and upper.
     * @param upper
     *            bound
     * @param lower
     *            bound
     */
    @SuppressWarnings("unchecked")
    private void outputGroup( Group j3dScene, Element rootNode, Transform3D transformation, boolean calcBounds,
                              Point3d lower, Point3d upper ) {
        Enumeration<Node> en = j3dScene.getAllChildren();
        LOG.logDebug( "Outputting a group." );
        while ( en.hasMoreElements() ) {
            Node n = en.nextElement();
            if ( n != null ) {
                if ( n instanceof Group ) {
                    LOG.logDebug( "A group: " + n );
                    Transform3D tmpTrans = null;
                    if ( n instanceof TransformGroup ) {
                        TransformGroup tmp = (TransformGroup) n;
                        tmpTrans = new Transform3D();
                        tmp.getTransform( tmpTrans );
                        if ( tmpTrans.getBestType() != Transform3D.IDENTITY ) {
                            if ( inverseYZ ) {
                                Vector3d trans = new Vector3d();
                                tmpTrans.get( trans );
                                double tmpY = trans.y;
                                trans.y = -trans.z;
                                trans.z = tmpY;
                                tmpTrans.set( trans );
                            }
                            LOG.logDebug( "A transform group with transform:\n" + tmpTrans );
                            if ( transformation != null ) {
                                transformation.mul( tmpTrans );
                            }
                            LOG.logDebug( "Resulting transform:\n" + transformation );
                        }
                    }
                    outputGroup( (Group) n, rootNode, transformation, calcBounds, lower, upper );
                    if ( tmpTrans != null && tmpTrans.getBestType() != Transform3D.IDENTITY ) {
                        transformation.mulInverse( tmpTrans );
                        LOG.logDebug( "After undoing the inverse of transformGroup transform:\n" + transformation );
                    }
                } else if ( n instanceof Leaf ) {
                    outputLeaf( (Leaf) n, rootNode, transformation, calcBounds, lower, upper );
                }
            }
        }

    }

    /**
     * Checks if the given leaf is a shape3D or background leaf, and if so, their cityGML representation will be
     * appended to the parent node.
     *
     * @param l
     *            a j3d leaf.
     * @param parent
     *            to append to.
     * @param transformation
     * @param calcBounds
     *            if true the bounds should be calculated and placed in lower and upper.
     * @param upper
     *            bound
     * @param lower
     *            bound
     */
    private void outputLeaf( Leaf l, Element parent, Transform3D transformation, boolean calcBounds, Point3d lower,
                             Point3d upper ) {
        if ( l != null ) {
            if ( l instanceof Shape3D ) {
                outputShape3D( (Shape3D) l, parent, transformation, calcBounds, lower, upper );
            } else if ( l instanceof Background ) {
                outputBackground( (Background) l, parent );
            } else {
                LOG.logInfo( "Don't know howto output object of instance: " + l.getClass().getName() );
            }
        }
    }

    /**
     * Appends the citygml representation to the given parent node.
     *
     * @param shape
     *            (of the j3dScene) to append
     * @param parent
     *            to append to.
     * @param transformation
     * @param calcBounds
     *            if true the bounds should be calculated and placed in lower and upper.
     * @param upper
     *            bound
     * @param lower
     *            bound
     */
    @SuppressWarnings("unchecked")
    private void outputShape3D( Shape3D shape, Element parent, Transform3D transformation, boolean calcBounds,
                                Point3d lower, Point3d upper ) {
        Enumeration<Geometry> geoms = shape.getAllGeometries();
//        String shapeName = shape.getName();
//        LOG.logDebug( "Outputting a Shape3d with name: " + shapeName );
        Appearance app = shape.getAppearance();
        float ambientIntensity = 0.5f;
        Color3f ambient = new Color3f( 0.8f, 0.8f, 0.8f );
        Color3f diffuse = new Color3f( 0.8f, 0.8f, 0.8f );
        Color3f specular = new Color3f( 0.8f, 0.8f, 0.8f );

        if ( app != null ) {
            Material mat = app.getMaterial();
            if ( mat != null ) {
                mat.getAmbientColor( ambient );
                mat.getDiffuseColor( diffuse );
                mat.getSpecularColor( specular );
            }
        } else {
            LOG.logInfo( "Shape3D has no material!, setting to gray!" );
        }
        while ( geoms.hasMoreElements() ) {
            Geometry geom = geoms.nextElement();
            if ( geom != null ) {
                if ( geom instanceof GeometryArray ) {
                    GeometryArray ga = (GeometryArray) geom;
                    if ( !( ga instanceof TriangleArray ) ) {
                        LOG.logDebug( "Not a triangle Array geometry -> convert to triangles, original type is: " + ga );
                        try {
                            GeometryInfo inf = new GeometryInfo( ga );
                            inf.convertToIndexedTriangles();
                            ga = inf.getGeometryArray();
                            LOG.logDebug( "The converted type is: " + ga );
                        } catch ( IllegalArgumentException e ) {
                            LOG.logInfo( "Could not create a triangle array of the: " + ga
                                          );
                        }
                    }

                    int vertexCount = ga.getVertexCount();
                    if ( vertexCount == 0 ) {
                        LOG.logError( "No coordinates found in the geometryArray, this may not be." );
                    } else {
                        LOG.logDebug( "Number of vertices in shape3d: " + vertexCount );

                        Element appearance = parent.getOwnerDocument().createElementNS( CITYGMLNS.toASCIIString(),
                                                                                        PRE_C + "appearance" );
                        Element simpleTexture = parent.getOwnerDocument().createElementNS( CITYGMLNS.toASCIIString(),
                                                                                           PRE_C + "SimpleTexture" );

                        int vertexFormat = ga.getVertexFormat();

                        /**
                         * Textures
                         */
                        TexCoord2f[] texCoords = null;
                        if ( ( GeometryArray.TEXTURE_COORDINATE_2 & vertexFormat ) == GeometryArray.TEXTURE_COORDINATE_2 ) {
                            LOG.logDebug( "The Geometry has a texture attached to it." );

                            texCoords = new TexCoord2f[vertexCount];
                            for ( int i = 0; i < texCoords.length; ++i ) {
                                texCoords[i] = new TexCoord2f( 0, 0 );
                            }
                            ga.getTextureCoordinates( 0, ga.getInitialTexCoordIndex( 0 ), texCoords );
                            StringBuilder texCoordsAsString = new StringBuilder( texCoords.length * 2 );
                            for ( TexCoord2f coord : texCoords ) {
                                texCoordsAsString.append( coord.x ).append( " " ).append( coord.y ).append( " " );
                            }

                            // TODO try to get all textures of all texture units
                            if ( app != null ) {
                                Texture tex = app.getTexture();
                                if ( tex != null ) {
//                                    String texName = tex.getName();
                                    String texName = null;
                                    LOG.logDebug( "Texture name: " + texName );
                                    ImageComponent ic = tex.getImage( 0 );
                                    if ( ic != null ) {
                                        if ( ic instanceof ImageComponent2D ) {
//                                            LOG.logDebug( "ImageComponent name: " + ( (ImageComponent2D) ic ).getName() );
                                            BufferedImage bi = ( (ImageComponent2D) ic ).getImage();
                                            if ( bi != null ) {
                                                if ( !cachedTextures.containsKey( bi ) ) {
                                                    if ( texName == null || "".equals( texName.trim() ) ) {
                                                        texName = name + "_texture_" + textureCount++;
                                                    }
                                                    texName += ( ".jpg" );
                                                    LOG.logDebug( "No cached texture found: " + texName );
                                                    try {
                                                        File f = new File( textureOutputDir, texName );
                                                        ImageIO.write( bi, "jpg", f );
                                                        LOG.logDebug( "Wrote texture to: " + f.getAbsolutePath() );
                                                    } catch ( IOException e ) {
                                                        LOG.logError( "Failed to write texture: " + texName, e );
                                                    }
                                                    cachedTextures.put( bi, texName );
                                                } else {
                                                    LOG.logDebug( "Using old texture reference: " + texName );
                                                    texName = cachedTextures.get( bi );
                                                }

                                                // add the texture to the appearance node

                                                XMLTools.appendElement( simpleTexture, CITYGMLNS, PRE_C + "textureMap",
                                                                        textureOutputDir + texName );
                                                XMLTools.appendElement( simpleTexture, CITYGMLNS,
                                                                        PRE_C + "textureType", "specific" );
                                                XMLTools.appendElement( simpleTexture, CITYGMLNS, PRE_C + "repeat", "0" );
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Element material = XMLTools.appendElement( appearance, CITYGMLNS, PRE_C + "Material" );
                            XMLTools.appendElement( material, CITYGMLNS, PRE_C + "ambientIntensity", ""
                                                                                                     + ambientIntensity );
                            // although the citgml spec does not contain it, we set the ambient color :(
                            XMLTools.appendElement( material, CITYGMLNS, PRE_C + "ambientColor", ambient.x + " "
                                                                                                 + ambient.y + " "
                                                                                                 + ambient.z );
                            XMLTools.appendElement( material, CITYGMLNS, PRE_C + "specularColor", specular.x + " "
                                                                                                  + specular.y + " "
                                                                                                  + specular.z );
                            XMLTools.appendElement( material, CITYGMLNS, PRE_C + "diffuseColor", diffuse.x + " "
                                                                                                 + diffuse.y + " "
                                                                                                 + diffuse.z );
                        }

                        Point3d[] coords = new Point3d[vertexCount];
                        for ( int i = 0; i < coords.length; ++i ) {
                            coords[i] = new Point3d( 0, 0, 0 );
                        }
                        ga.getCoordinates( ga.getInitialVertexIndex(), coords );
                        LOG.logDebug( "Number of coords in geometry: " + coords.length );

                        if ( coords.length > 0 && coords[0] != null ) {

                            if ( ga instanceof TriangleArray ) {
                                LOG.logInfo( "Using triangles for the coords." );
                                int i = 0;
                                for ( ; i < coords.length; i += 3 ) {
                                    Element surfaceMember = XMLTools.appendElement( parent, GMLNS, PRE_G
                                                                                                   + "surfaceMember" );

                                    Element texturedSurface = XMLTools.appendElement( surfaceMember, CITYGMLNS,
                                                                                      PRE_C + "TexturedSurface" );
                                    texturedSurface.setAttribute( "orientation", "+" );

                                    Element baseSurface = XMLTools.appendElement( texturedSurface, GMLNS,
                                                                                  PRE_G + "baseSurface" );
                                    Element polygon = XMLTools.appendElement( baseSurface, GMLNS, PRE_G + "Polygon" );
                                    Element exterior = XMLTools.appendElement( polygon, GMLNS, PRE_G + "exterior" );
                                    Element lRing = XMLTools.appendElement( exterior, GMLNS, PRE_G + "LinearRing" );
                                    if ( ( i + 3 ) <= coords.length ) {
                                        for ( int j = 0; j < 3; j++ ) {
                                            Point3d coord = coords[i + j];
                                            // System.out.println( "Untranslated point: " + coord );
                                            // transformMatrix.transform( coord );
                                            // first do a transform on the old coords,
                                            if ( inverseYZ ) {
                                                double coordY = coord.y;
                                                coord.y = -coord.z;
                                                coord.z = coordY;
                                            }
                                            if ( rotationMatrix != null ) {
                                                rotationMatrix.transform( coord );
                                            }
                                            // than translate
                                            transformation.transform( coord );

                                            // use point3d.equals method to see if already present.
                                            // if ( resultingPoints.contains( coord ) ) {
                                            // resultingPoints.add( coord );
                                            // check the bounds if set
                                            if ( calcBounds ) {
                                                if ( coord.x < lower.x ) {
                                                    lower.x = coord.x;
                                                }
                                                if ( coord.y < lower.y ) {
                                                    lower.y = coord.y;
                                                }
                                                if ( coord.z < lower.z ) {
                                                    lower.z = coord.z;
                                                }
                                                if ( coord.x > upper.x ) {
                                                    upper.x = coord.x;
                                                }
                                                if ( coord.y > upper.y ) {
                                                    upper.y = coord.y;
                                                }
                                                if ( coord.z > upper.z ) {
                                                    upper.z = coord.z;
                                                }
                                            }
                                            XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", coord.x + " "
                                                                                                 + coord.y + " "
                                                                                                 + coord.z );

                                        }
                                        Point3d tmp = coords[i];
                                        XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", tmp.x + " " + tmp.y + " "
                                                                                             + tmp.z );
                                    } else {// no three points are left;
                                        LOG.logInfo( "One of the geometries have an inconsistent number of points ("
                                                     + ( coords.length - i ) + ")to create triangles-> correcting!" );
                                        Point3d current = coords[i];
                                        // transformMatrix.transform( current );
                                        transformation.transform( current );
                                        Point3d previous = coords[i - 1];
                                        // transformMatrix.transform( previous );
                                        transformation.transform( previous );
                                        Point3d tmp = null;
                                        switch ( coords.length - i ) {
                                        case 1: // take last two nodes and create a triangle.
                                            tmp = coords[i - 2];
                                            break;
                                        default: // take last node and create a triangle.
                                            tmp = coords[i + 1];
                                            break;
                                        }
                                        // transformMatrix.transform( tmp );
                                        transformation.transform( tmp );
                                        XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", current.x + " "
                                                                                             + current.y + " "
                                                                                             + current.z );
                                        XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", previous.x + " "
                                                                                             + previous.y + " "
                                                                                             + previous.z );
                                        XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", tmp.x + " " + tmp.y + " "
                                                                                             + tmp.z );
                                        XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", current.x + " "
                                                                                             + current.y + " "
                                                                                             + current.z );

                                    }

                                    // add the appearance
                                    Element tmp = (Element) appearance.cloneNode( true );
                                    if ( texCoords != null && ( i + 2 ) < texCoords.length ) {
                                        Element sTex = (Element) simpleTexture.cloneNode( true );
                                        XMLTools.appendElement( sTex, CITYGMLNS, PRE_C + "textureCoordinates",
                                                                texCoords[i].x + " " + texCoords[i].y + " "
                                                                                        + texCoords[i + 1].x + " "
                                                                                        + texCoords[i + 1].y + " "
                                                                                        + texCoords[i + 2].x + " "
                                                                                        + texCoords[i + 2].y + " "
                                                                                        + texCoords[i].x + " "
                                                                                        + texCoords[i].y );
                                        tmp.appendChild( sTex );
                                    }
                                    texturedSurface.appendChild( tmp );
                                    numberOfSurfaces++;
                                }
                            } else { // try to set as polygon.
                                LOG.logInfo( "Setting points as polygon." );
                                Point3d firstCoord = coords[0];
                                // transformMatrix.transform( firstCoord );
                                transformation.transform( firstCoord );
                                Point3d lastCoord = null;
                                Element texturedSurface = XMLTools.appendElement( parent, CITYGMLNS,
                                                                                  PRE_C + "TexturedSurface" );
                                texturedSurface.setAttribute( "orientation", "+" );

                                Element baseSurface = XMLTools.appendElement( texturedSurface, GMLNS, PRE_G
                                                                                                      + "baseSurface" );
                                Element polygon = XMLTools.appendElement( baseSurface, GMLNS, PRE_G + "Polygon" );
                                Element exterior = XMLTools.appendElement( polygon, GMLNS, PRE_G + "exterior" );
                                Element lRing = XMLTools.appendElement( exterior, GMLNS, PRE_G + "LinearRing" );

                                for ( int i = 0; i < coords.length; ++i ) {
                                    Point3d coord = coords[i];
                                    // transformMatrix.transform( coord );
                                    transformation.transform( coord );
                                    if ( calcBounds ) {
                                        if ( coord.x < lower.x ) {
                                            lower.x = coord.x;
                                        }
                                        if ( coord.y < lower.y ) {
                                            lower.y = coord.y;
                                        }
                                        if ( coord.z < lower.z ) {
                                            lower.z = coord.z;
                                        }
                                        if ( coord.x > upper.x ) {
                                            upper.x = coord.x;
                                        }
                                        if ( coord.y > upper.y ) {
                                            upper.y = coord.y;
                                        }
                                        if ( coord.z > upper.z ) {
                                            upper.z = coord.z;
                                        }
                                    }
                                    XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", coord.x + " " + coord.y + " "
                                                                                         + coord.z );
                                    lastCoord = coord;// coords[i];
                                }
                                if ( !firstCoord.equals( lastCoord ) ) {
                                    XMLTools.appendElement( lRing, GMLNS, PRE_G + "pos", firstCoord.x + " "
                                                                                         + firstCoord.y + " "
                                                                                         + firstCoord.z );
                                }

                                // add the appearance
                                Element tmp = (Element) appearance.cloneNode( true );
                                texturedSurface.appendChild( tmp );
                            }

                        }

                        /**
                         * NORMALS
                         */
                        if ( ( GeometryArray.NORMALS & vertexFormat ) == GeometryArray.NORMALS ) {
                            LOG.logDebug( "The Geometry has normals, but they are not exported (yet)." );
                            // Vector3f[] normals = new Vector3f[vertexCount];
                            // for ( int i = 0; i < normals.length; ++i ) {
                            // normals[i] = new Vector3f( 0, 0, 0 );
                            // }
                            // ga.getNormals( 0, normals );
                            // for ( Vector3f normal : normals ) {
                            // parent.append( normal ).append( " " );
                            // }
                        }
                        /**
                         * Colors 3f
                         */
                        if ( ( GeometryArray.COLOR_3 & vertexFormat ) == GeometryArray.COLOR_3 ) {
                            LOG.logDebug( "The Geometry has Colors per Vertex with 3 chanels, but they are not exported (yet)." );
                            // Color3f[] colors = new Color3f[vertexCount];
                            // for ( int i = 0; i < colors.length; ++i ) {
                            // colors[i] = new Color3f( 0, 0, 0 );
                            // }
                            // ga.getColors( 0, colors );
                            // parent.append( "\n - Colors3f (" ).append( numberOfGeom ).append( "):
                            // " );
                            // for ( Color3f color : colors ) {
                            // parent.append( color ).append( " " );
                            // }
                        }

                        /**
                         * Colors 4f
                         */
                        if ( ( GeometryArray.COLOR_4 & vertexFormat ) == GeometryArray.COLOR_4 ) {
                            LOG.logDebug( "The Geometry has Colors per Vertex with 4 chanels, but they are not exported (yet)." );
                            // Color4f[] colors = new Color4f[vertexCount];
                            // for ( int i = 0; i < colors.length; ++i ) {
                            // colors[i] = new Color4f( 0, 0, 0, 0 );
                            // }
                            // ga.getColors( 0, colors );
                            // parent.append( "\n - Colors4f (" ).append( numberOfGeom ).append( "):
                            // " );
                            // for ( Color4f color : colors ) {
                            // parent.append( color ).append( " " );
                            // }
                        }

                        // Element appearance = XMLTools.appendElement( texturedSurface,
                        // CITYGMLNS,
                        // PRE_C + "appearance" );
                    }
                } else {
                    LOG.logInfo( "Only Shape3d Objects with geometry rasters are supported for outputing" );
                }
            }
        }

    }

    /**
     * Don't know how to export the background yet.
     *
     * @param b
     *            leaf of the j3dScene
     * @param parent
     *            to append to.
     */
    private void outputBackground( @SuppressWarnings("unused")
    Background b, @SuppressWarnings("unused")
    Element parent ) {
        LOG.logError( "Cannot output a background yet." );
    }

    public String getName() {
        return "CityGML exporter";
    }

    public String getShortDescription() {
        return "Convert a given j3d:BranchGroup to CityGML LOD1";
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            outputHelp();
        }
        Map<String, String> params = new HashMap<String, String>( 5 );
        for ( int i = 0; i < args.length; i++ ) {
            String arg = args[i];
            if ( arg != null && !"".equals( arg.trim() ) ) {
                arg = arg.trim();
                if ( arg.startsWith( "-" ) ) {
                    arg = arg.substring( 1 );
                }
                if ( arg.equalsIgnoreCase( "?" ) || arg.equalsIgnoreCase( "h" ) ) {
                    outputHelp();
                } else {
                    if ( i + 1 < args.length ) {
                        String val = args[++i];
                        if ( val != null && !"".equals( val.trim() ) ) {
                            params.put( arg.toLowerCase(), val.trim() );
                        } else {
                            LOG.logError( "Invalid value for parameter: " + arg );
                        }
                    } else {
                        LOG.logError( "No value for parameter: " + arg );
                    }
                }
            }
        }

        String fileName = params.get( "infile" );
        if ( fileName == null || "".equals( fileName.trim() ) ) {
            System.out.println( "\n\n" );
            LOG.logError( "The -inFile parameter must be defined." );
            System.out.println( "\n\n" );
            outputHelp();
        }
        File tmpFile = new File( fileName );
        if ( !tmpFile.exists() ) {
            System.out.println( "\n\n" );
            LOG.logError( "The file '" + fileName + "' given by the -inFile parameter does not exist." );
            System.out.println( "\n\n" );
            System.exit( 1 );
        }
        LOG.logInfo( "\n\nTrying to load scene, if you see no message beneath this message, please check your vrml file: "
                     + fileName
                     + "if the refrerred textures are java qouted.\nIf using a Windows os, the quoted directory separator (\\\\) should be used instead of a single one (\\)" );
        VrmlLoader loader = new VrmlLoader();
        try {
            J3DToCityGMLExporter converter = new J3DToCityGMLExporter( params );
            Scene scene = loader.load( fileName );
            if ( scene != null ) {
                LOG.logInfo( "Successfully loaded the scene, trying to retrieve the branchgroup." );
                BranchGroup bg = scene.getSceneGroup();
                if ( bg != null && bg.numChildren() > 0 ) {
                    LOG.logInfo( "Retrieval of the j3d.Branchgroup was successful." );
                    StringBuilder output = new StringBuilder( 20000 );
                    converter.export( output, bg );
                    String outFile = params.get( "outfile" );
                    if ( outFile == null || "".equals( outFile.trim() ) ) {
                        System.out.println( output.toString() );
                    } else {
                        BufferedWriter fWriter = new BufferedWriter( new FileWriter( new File( outFile ) ) );
                        fWriter.write( output.toString() );
                        fWriter.flush();
                        fWriter.close();
                    }
                } else {
                    System.out.println( "The branchgroup is null or has no childres, which means that the scene was not loaded correctly (did you correctly quoted the texture paths in your vrml-file: "
                                        + fileName + ")" );
                }
            } else {
                System.out.println( "The scene was not loaded (did you correctly quoted the texture paths in your vrml-file: "
                                    + fileName + ")" );
            }
        } catch ( NoClassDefFoundError c ) {
            libHelp( c );
        } catch ( FileNotFoundException e ) {
            LOG.logError( "Could not load vrm-file because: " + e.getMessage(), e );
        } catch ( IncorrectFormatException e ) {
            LOG.logError( "Could not load vrm-file because: " + e.getMessage(), e );
        } catch ( ParsingErrorException e ) {
            LOG.logError( "Could not load vrm-file because: " + e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( "Could not create converter because: " + e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( "Error while loading the file: " + fileName + " because: " + e.getMessage(), e );
        } catch ( Throwable t ) {
            LOG.logError( "Error while loading the file: " + fileName + " because: " + t.getMessage(), t );
        }
    }

    private static void outputHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append( "The J3DToCityGMLExported program can be used to export a j3d branchgroup created from a vrml-file to citygml. \n" );
        sb.append( "Following parameters are supported:\n" );
        sb.append( "-inFile the /path/to/vrml-file\n" );
        sb.append( "-texDir directory to output the found textures to.\n" );
        sb.append( "[-outFile] the /path/to/the/output/file or standard output if not supplied.\n" );
        sb.append( "[-wfsTransaction y/n] output the citygml as part of a wfs-insert request (if omitted the citygml-building element will be root element).\n" );
        sb.append( "[-buildingFunction] the function this citygml building will have (if omitted 1001 will be used).\n" );
        sb.append( "[-srs] the coordinateSystem used for the citygml (if omitted 'EPSG:31466' will be used).\n" );
        sb.append( "[-xTranslation] the amount to add up to the x part of the found coordinates(if omitted '0' will be used).\n" );
        sb.append( "[-yTranslation] the amount to add up to the y part of the found coordinates(if omitted '0' will be used).\n" );
        sb.append( "[-zTranslation] the amount to add up to the z part of the found coordinates(if omitted '0' will be used).\n" );
        sb.append( "[-rotationAngle] the rotation angle given by comma seperated values as x,y,z,rotation(in radians)(if omitted no rotation will be applied).\n" );
        sb.append( "[-inverseYZ] (y/n)signal that the y and z coordinates should be inversed.\n" );
        sb.append( "-?|-h output this text\n" );
        sb.append( "example usage (converting the cool.vrml to a wfs:Transaction in 'epsg:12345' wit a xtranslation of 10000 and yTranslation of 50,\n" );
        sb.append( "outputing to cool_tower.xml in the directory my_output_dir (relative to the current dir)\n" );
        sb.append( "java -cp deegree.jar:lib/j3d/j3d-vrml97.jar:lib/log4j/log4j-1.2.9.jar:/lib/xml/jaxen-1.1-beta-8.jar org.deegree.tools.app3d.J3DToCityGMLExporter -inFile 'cool.vrml' -texDir 'my_output_dir' -outFile 'cool_tower.xml' -wfsTransaction 'y' -buildingFunction 'home office buildings' -xTranslation 10000 -yTranslation 50\n" );
        System.out.println( sb.toString() );
        System.exit( 1 );
    }

    private static void libHelp( NoClassDefFoundError c ) {
        StringBuilder sb = new StringBuilder( 800 );
        sb.append( "Could not load vrm-file because a specific class (" );
        sb.append( c.getMessage() );
        sb.append( ") was not found, the vrmlImporter uses following libraries: " );
        sb.append( "\n - $deegree-base$/lib/java3d/vecmath.jar" );
        sb.append( "\n - $deegree-base$/lib/java3d/j3dutils.jar" );
        sb.append( "\n - $deegree-base$/lib/java3d/j3dcore.jar" );
        sb.append( "\n - $deegree-base$/lib/j3d/j3d-vrml97.jar" );
        sb.append( "\n - $deegree-base$/lib/commons/commons-logging.jar" );
        sb.append( "\n - $deegree-base$/lib/log4j/log4j-1.2.9.jar" );
        sb.append( "\n - $deegree-base$/lib/xml/jaxen-1.1-beta-8.jar" );
        // LOG.logError( sb.toString() );
        System.out.println( sb.toString() );
        exit( 1 );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.tools.app3d.J3DExporter#getParameterList()
     */
    public Map<String, String> getParameterMap() {
        Map<String, String> params = new HashMap<String, String>();
        params.put( "name", "The name of the building" );
        params.put( "texdir", "The directory to output the j3d objects' textures to." );
        params.put( "wfstransaction", "(yes/no) Output the citygml as part of a wfs-insert request." );
        params.put( "buildingfunction", "The function this citygml building will have." );
        params.put( "srs", "the coordinateSystem used." );
        params.put( "xtranslation", "The x translation." );
        params.put( "ytranslation", "The y translation." );
        params.put( "ztranslation", "The z translation." );
        params.put( "rotationangle", "Comma seperated values as x,y,z,rotation(in radians)." );
        params.put( "inverseyz", "(yes/no) Signal that the y and z coordinates should be swapped." );
        return params;
    }

}
