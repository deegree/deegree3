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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.media.j3d.Material;
import javax.vecmath.Color3f;
import javax.vecmath.TexCoord2f;
import javax.xml.namespace.QName;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wpvs.configuration.RenderingConfiguration;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 *         $Revision$, $Date$
 * 
 */
public class Object3DFactory {

    private static InputStream materialURL;

    private static Properties material = new Properties();

    private static final ILogger LOG = LoggerFactory.getLogger( Object3DFactory.class );

    /**
     * all texture images will be stored on a Map to avoid double loading and creating a BufferedImage from an image
     * source (textureMap property)
     */
    private static Map<String, BufferedImage> textImgMap = new HashMap<String, BufferedImage>( 200 );

    private static final QualifiedName objectID = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                "fk_feature", "app" ) );

    private static final QualifiedName textMapQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                 "texturemap", "app" ) );

    private static final QualifiedName city_textMapQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                           "textureMap", CommonNamespaces.CITYGMLNS );

    private static final QualifiedName textCoordsQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                    "texturecoordinates", "app" ) );

    private static final QualifiedName city_textCoordsQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                              "textureCoordinates",
                                                                              CommonNamespaces.CITYGMLNS );

    private static final QualifiedName shininessQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                   "shininess", "app" ) );

    private static final QualifiedName city_shininessQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                             "shininess", CommonNamespaces.CITYGMLNS );

    private static final QualifiedName transparencyQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                      "transparency", "app" ) );

    private static final QualifiedName city_transparencyQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                                "transparency",
                                                                                CommonNamespaces.CITYGMLNS );

    private static final QualifiedName ambientintensityQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                          "ambientintensity", "app" ) );

    private static final QualifiedName city_ambientintensityQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                                    "ambientIntensity",
                                                                                    CommonNamespaces.CITYGMLNS );

    private static final QualifiedName specularcolorQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                       "specularcolor", "app" ) );

    private static final QualifiedName city_specularcolorQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                                 "specularColor",
                                                                                 CommonNamespaces.CITYGMLNS );

    private static final QualifiedName diffusecolorQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                      "diffusecolor", "app" ) );

    private static final QualifiedName city_diffusecolorQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                                "diffuseColor",
                                                                                CommonNamespaces.CITYGMLNS );

    private static final QualifiedName emissivecolorQn = new QualifiedName( new QName( "http://www.deegree.org/app",
                                                                                       "emissivecolor", "app" ) );

    private static final QualifiedName city_emissivecolorQn = new QualifiedName( CommonNamespaces.CITYGML_PREFIX,
                                                                                 "emissiveColor",
                                                                                 CommonNamespaces.CITYGMLNS );

    private static final RenderingConfiguration rc = RenderingConfiguration.getInstance();
    static {
        try {
            materialURL = Object3DFactory.class.getResourceAsStream( "material.properties" );
            material.load( materialURL );
        } catch ( IOException e ) {
            BootLogger.logError( e.getMessage(), e );
        }
    }

    /**
     * creates a Surface from the passed feature. It is assumed the feature is simple, contains one surfac/polygon
     * geometry and optional has material and/or texture informations. The GML schema for a valid feature is defined as:
     * 
     * <pre>
     *    &lt;xsd:schema targetNamespace=&quot;http://www.deegree.org/app&quot; xmlns:app=&quot;http://www.deegree.org/app&quot; xmlns:ogc=&quot;http://www.opengis.net/ogc&quot; xmlns:deegreewfs=&quot;http://www.deegree.org/wfs&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot; xmlns:gml=&quot;http://www.opengis.net/gml&quot; elementFormDefault=&quot;qualified&quot; attributeFormDefault=&quot;unqualified&quot;&gt;
     *        &lt;xsd:import namespace=&quot;http://www.opengis.net/gml&quot; schemaLocation=&quot;http://schemas.opengis.net/gml/3.1.1/base/feature.xsd&quot;/&gt;
     *         &lt;xsd:import namespace=&quot;http://www.opengis.net/gml&quot; schemaLocation=&quot;http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd&quot;/&gt;
     *         &lt;xsd:element name=&quot;WPVS&quot; type=&quot;app:WPVSType&quot; substitutionGroup=&quot;gml:_Feature&quot;/&gt;
     *         &lt;xsd:complexType name=&quot;WPVSType&quot;&gt;
     *             &lt;xsd:complexContent&gt;
     *                 &lt;xsd:extension base=&quot;gml:AbstractFeatureType&quot;&gt;
     *                     &lt;xsd:sequence&gt;
     *                         &lt;xsd:element name=&quot;fk_feature&quot; type=&quot;xsd:double&quot;/&gt;
     *                         &lt;xsd:element name=&quot;geometry&quot; type=&quot;gml:GeometryPropertyType&quot;/&gt;
     *                         &lt;xsd:element name=&quot;shininess&quot; type=&quot;xsd:double&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;transparency&quot; type=&quot;xsd:double&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;ambientintensity&quot; type=&quot;xsd:double&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;specularcolor&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;diffusecolor&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;emissivecolor&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;texturemap&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;texturecoordinates&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;texturetype&quot; type=&quot;xsd:string&quot; minOccurs=&quot;0&quot;/&gt;
     *                         &lt;xsd:element name=&quot;repeat&quot; type=&quot;xsd:integer&quot; minOccurs=&quot;0&quot;/&gt;
     *                     &lt;/xsd:sequence&gt;
     *                 &lt;/xsd:extension&gt;
     *             &lt;/xsd:complexContent&gt;
     *         &lt;/xsd:complexType&gt;
     *     &lt;/xsd:schema&gt;
     * </pre>
     * 
     * @param feature
     * @param texturedShapes
     *            which were loaded already
     * @return a DefaultSurface which is a derivative of a Shape3D. NOTE, the surface is not yet 'compiled'.
     */
    public DefaultSurface createSurface( Feature feature, Map<String, TexturedSurface> texturedShapes ) {
        double oId = -1d;
        if ( feature.getDefaultProperty( objectID ) != null ) {
            if ( feature.getDefaultProperty( objectID ).getValue( new Double( -1 ) ) instanceof BigDecimal ) {
                oId = ( (BigDecimal) feature.getDefaultProperty( objectID ).getValue( new Double( -1 ) ) ).doubleValue();
            } else if ( feature.getDefaultProperty( objectID ).getValue( new Double( -1 ) ) instanceof Double ) {
                oId = ( (Double) feature.getDefaultProperty( objectID ).getValue( new Double( -1d ) ) ).doubleValue();
            }
        } else {
            LOG.logDebug( "use genereted gml:id" );
            oId = Math.random();
        }

        // read texture informations (if available) from feature
        BufferedImage textImage = null;
        TexturedSurface cachedSurface = null;
        String textureFile = null;
        FeatureProperty[] fp = feature.getProperties( textMapQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_textMapQn );
        }
        if ( fp != null && fp.length > 0 ) {
            textureFile = (String) fp[0].getValue();
            if ( textureFile != null && !"".equals( textureFile.trim() ) ) {
                if ( texturedShapes.containsKey( textureFile ) ) {
                    cachedSurface = texturedShapes.get( textureFile );
                } else {
                    textImage = textImgMap.get( textureFile );
                    if ( textImage == null ) {
                        String lt = textureFile.toLowerCase();
                        try {
                            if ( lt.startsWith( "file:" ) || lt.startsWith( "http:" ) ) {
                                textImage = ImageUtils.loadImage( new URL( textureFile ) );
                            } else {
                                textImage = ImageUtils.loadImage( textureFile );
                            }
                            // textImage = ImageIO.read( new URL( textureFile ) );

                        } catch ( MalformedURLException e ) {
                            e.printStackTrace();
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                        if ( textImage != null ) {
                            textImgMap.put( textureFile, textImage );
                        } else {
                            LOG.logWarning( "Failed to load texture image: " + textureFile );
                        }
                    }
                }
            }
        }
        // float[][] textureCoords = new float[1][];
        List<TexCoord2f> textureCoords = null;
        if ( textImage != null || cachedSurface != null ) {
            fp = feature.getProperties( textCoordsQn );
            if ( fp == null || fp.length == 0 ) {
                fp = feature.getProperties( city_textCoordsQn );
            }
            if ( fp != null && fp.length > 0 ) {
                String tmp = (String) fp[0].getValue();
                LOG.logDebug( "Texture Coordinates: " + tmp );
                if ( tmp != null ) {
                    float[] tc = StringTools.toArrayFloat( tmp, ", " );
                    if ( tc != null && tc.length > 0 ) {
                        textureCoords = new ArrayList<TexCoord2f>( tc.length );
                        for ( int i = 0; i < tc.length; i += 2 ) {
                            textureCoords.add( new TexCoord2f( tc[i], tc[i + 1] ) );
                        }
                    }
                }
            }
        }

        // read color informations from feature. If not available use default values
        // from material.properties
        Double shininess = new Double( material.getProperty( "shininess" ) );
        fp = feature.getProperties( shininessQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_shininessQn );
        }
        if ( fp != null && fp.length > 0 ) {
            if ( fp[0].getValue() instanceof String ) {
                shininess = Double.parseDouble( (String) fp[0].getValue( Double.toString( shininess ) ) );
            } else {
                shininess = (Double) fp[0].getValue( shininess );
            }
        }

        float transparency = new Float( material.getProperty( "transparency" ) );
        fp = feature.getProperties( transparencyQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_transparencyQn );
        }
        if ( fp != null && fp.length > 0 ) {
            if ( fp[0].getValue() instanceof String ) {
                transparency = Float.parseFloat( (String) fp[0].getValue( Float.toString( transparency ) ) );
            } else {
                if ( fp[0].getValue() instanceof Double ) {
                    transparency = ( (Double) fp[0].getValue( transparency ) ).floatValue();
                } else {
                    transparency = (Float) fp[0].getValue( transparency );
                }
            }
        }

        Double ambientintensity = new Double( material.getProperty( "ambientintensity" ) );
        fp = feature.getProperties( ambientintensityQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_ambientintensityQn );
        }
        if ( fp != null && fp.length > 0 ) {
            if ( fp[0].getValue() instanceof String ) {
                ambientintensity = Double.parseDouble( (String) fp[0].getValue( Double.toString( ambientintensity ) ) );
            } else {
                ambientintensity = (Double) fp[0].getValue( ambientintensity );
            }
        }
        Color3f ambientcolor = new Color3f( ambientintensity.floatValue(), ambientintensity.floatValue(),
                                            ambientintensity.floatValue() );

        String tmp = material.getProperty( "specularcolor" );
        fp = feature.getProperties( specularcolorQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_specularcolorQn );
        }
        if ( fp != null && fp.length > 0 ) {
            tmp = (String) fp[0].getValue( tmp );
            tmp = createStringToolsSecureString( tmp, material.getProperty( "specularcolor" ) );
        }
        float[] tmpFl = StringTools.toArrayFloat( tmp.trim(), " " );
        Color3f specularcolor = new Color3f( tmpFl[0], tmpFl[1], tmpFl[2] );

        tmp = material.getProperty( "diffusecolor" );
        fp = feature.getProperties( diffusecolorQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_diffusecolorQn );
        }
        if ( fp != null && fp.length > 0 ) {
            tmp = (String) fp[0].getValue( tmp );
            tmp = createStringToolsSecureString( tmp, material.getProperty( "diffusecolor" ) );
        }
        tmpFl = StringTools.toArrayFloat( tmp.trim(), " " );
        Color3f diffusecolor = new Color3f( tmpFl[0], tmpFl[1], tmpFl[2] );

        tmp = material.getProperty( "emissivecolor" );
        fp = feature.getProperties( emissivecolorQn );
        if ( fp == null || fp.length == 0 ) {
            fp = feature.getProperties( city_emissivecolorQn );
        }
        if ( fp != null && fp.length > 0 ) {
            tmp = (String) fp[0].getValue( tmp );
            tmp = createStringToolsSecureString( tmp, material.getProperty( "emissivecolor" ) );
        }
        tmpFl = StringTools.toArrayFloat( tmp.trim(), " " );
        Color3f emissivecolor = new Color3f( tmpFl[0], tmpFl[1], tmpFl[2] );

        Material mat = new Material( ambientcolor, emissivecolor, diffusecolor, specularcolor, shininess.floatValue() );
        // for diffuse-color to work the ambient lighting must be switched on
        mat.setColorTarget( Material.AMBIENT_AND_DIFFUSE );

        /**
         * Please check for the right property here. The defaultPropertyValue just delivers the first geometry defined
         * in the wfs configuration.
         */
        DefaultSurface resultSurface = null;
        org.deegree.model.spatialschema.Geometry geometry = feature.getDefaultGeometryPropertyValue();
        // if ( geometry instanceof MultiSurface ) {
        // MultiSurface multiSurfaces = (MultiSurface) geometry;
        // LOG.logDebug( "Found a Multi surface" );
        // Surface[] allSurfaces = multiSurfaces.getAllSurfaces();
        // // so no texture create a new Default surface
        // } else if ( geometry instanceof Surface ) {
        resultSurface = createSurface( texturedShapes, cachedSurface, geometry, textImage, textureFile, mat,
                                       transparency, feature.getId(), Double.toString( oId ), textureCoords );
        if ( resultSurface instanceof TexturedSurface ) {
            // necessary for the caching mechanism of textures, because they will be added to the map later.
            resultSurface = null;
        }
        //
        // }
        // surface.compile();
        return resultSurface;
    }

    private String createStringToolsSecureString( String someColor, String defaultValue ) {
        if ( "".equals( someColor.trim() ) ) {
            someColor = defaultValue;
        } else {
            String[] s = someColor.split( "\\s" );
            if ( s.length == 1 || s.length > 3 ) {
                someColor = defaultValue;
            } else {
                StringBuilder sb = new StringBuilder( 50 );
                for ( int i = 0; i < s.length; i++ ) {
                    sb.append( s[i] ).append( " " );
                }
                someColor = sb.toString().trim();
            }
        }
        return someColor;
    }

    private DefaultSurface createSurface( Map<String, TexturedSurface> texturedShapes, TexturedSurface cachedSurface,
                                          Geometry surfaceToAdd, BufferedImage textImage, String textureFile,
                                          Material material, float transparency, String id, String parentId,
                                          List<TexCoord2f> textureCoords ) {
        DefaultSurface result = null;
        if ( cachedSurface != null ) {
            result = cachedSurface;
            LOG.logDebug( "Textured-Surface cached" );
            cachedSurface.addGeometry( surfaceToAdd, textureCoords );
        } else if ( textImage != null ) {
            LOG.logDebug( "Textured-Surface not cached" );
            result = new TexturedSurface( id, parentId, surfaceToAdd, material, transparency, textImage, textureCoords );
            texturedShapes.put( textureFile, (TexturedSurface) result );
        } else {
            LOG.logDebug( "3D-Surface without texture: ", surfaceToAdd );
            result = new ColoredSurface( id, parentId, surfaceToAdd, material, transparency );
        }
        return result;
    }
}
