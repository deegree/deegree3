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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color4f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;

import com.sun.j3d.utils.image.TextureLoader;

/**
 * The <code>RenderingConfiguration</code> class is a simple wrapper to retrieve the configuration of the rendering
 * options.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public final class RenderingConfiguration {
    private static ILogger LOG = LoggerFactory.getLogger( RenderingConfiguration.class );

    private static RenderingConfiguration renderConfig;

    private final TextureAttributes textureAttributes;

    private final ColoringAttributes coloringAttributes;

    private final PolygonAttributes terrainPolygonAttributes;

    private final PolygonAttributes surfacePolygonAttributes;

    private final boolean objectShadingEnabled;

    /**
     * The renderingOptions defined in a file called rendering_options.properties
     */
    private final Properties renderingOptions;

    private final String propertieFile = "rendering_options.properties";

    private boolean terrainShadingEnabled;

    private RenderingConfiguration() {
        // Singleton pattern
        renderingOptions = new Properties();
        try {
            InputStream renderingProps = RenderingConfiguration.class.getResourceAsStream( "/" + propertieFile );
            if ( renderingProps == null ) {
                renderingProps = RenderingConfiguration.class.getResourceAsStream( propertieFile );
            }
            renderingOptions.load( renderingProps );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }

        this.textureAttributes = createTextureAttributes();
        this.coloringAttributes = createColoringAttributes();
        this.terrainPolygonAttributes = createTerrainPolygonAttributes();
        this.surfacePolygonAttributes = createSurfacePolygonAttributes();
        this.objectShadingEnabled = optionTrueFalse( "object_shading_enabled" );
        this.terrainShadingEnabled = optionTrueFalse( "terrain_shading_enabled" );

    }

    /**
     * @return true if the given option fails or is not set to false,no, 0
     */
    private boolean optionTrueFalse( String option ) {
        boolean result = true;
        // finding a configured material shading property
        String configuredProperty = (String) renderingOptions.get( option );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "false".equalsIgnoreCase( configuredProperty ) || "0".equalsIgnoreCase( configuredProperty )
                 || "no".equalsIgnoreCase( configuredProperty ) ) {
                result = false;
            } else if ( !"TRUE".equalsIgnoreCase( configuredProperty ) || "1".equalsIgnoreCase( configuredProperty )
                        || "yes".equalsIgnoreCase( configuredProperty ) ) {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", option, configuredProperty,
                                                     "true" ) );
            }
        }
        return result;
    }

    /**
     * @return the configured PolygonAttributes for surfaces
     */
    private PolygonAttributes createSurfacePolygonAttributes() {
        PolygonAttributes targetPolyAttr = new PolygonAttributes();
        targetPolyAttr.setCapability( PolygonAttributes.ALLOW_MODE_READ );
        targetPolyAttr.setCapability( PolygonAttributes.ALLOW_CULL_FACE_READ );
        targetPolyAttr.setCapability( PolygonAttributes.ALLOW_NORMAL_FLIP_READ );
        targetPolyAttr.setPolygonMode( PolygonAttributes.POLYGON_FILL );

        // finding a configured backface flip property
        String configuredProperty = (String) renderingOptions.get( "surface_backflip" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "TRUE".equalsIgnoreCase( configuredProperty ) || "1".equalsIgnoreCase( configuredProperty )
                 || "yes".equalsIgnoreCase( configuredProperty ) ) {
                targetPolyAttr.setBackFaceNormalFlip( true );
            } else if ( "false".equalsIgnoreCase( configuredProperty ) || "0".equalsIgnoreCase( configuredProperty )
                        || "no".equalsIgnoreCase( configuredProperty ) ) {
                targetPolyAttr.setBackFaceNormalFlip( false );
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "surface_backflip",
                                                     configuredProperty, "false" ) );
                targetPolyAttr.setBackFaceNormalFlip( false );
            }

        }

        int configuredValue = PolygonAttributes.CULL_NONE;
        // finding a configured surface culling
        configuredProperty = (String) renderingOptions.get( "surface_culling" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "BACK".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_BACK;
            } else if ( "FRONT".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_FRONT;
            } else if ( "NONE".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_NONE;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "surface_culling",
                                                     configuredProperty, "NONE" ) );
            }
        }
        targetPolyAttr.setCullFace( configuredValue );
        return targetPolyAttr;

    }

    /**
     * @return the configured Polygon Attributes for the dgm (terrain)
     */
    private PolygonAttributes createTerrainPolygonAttributes() {
        // what kind of drawing
        PolygonAttributes targetPolyAttr = new PolygonAttributes();
        targetPolyAttr.setPolygonMode( PolygonAttributes.POLYGON_FILL );

        // finding a configured backface flip property
        String configuredProperty = (String) renderingOptions.get( "terrain_backflip" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "true".equalsIgnoreCase( configuredProperty ) || "1".equalsIgnoreCase( configuredProperty )
                 || "yes".equalsIgnoreCase( configuredProperty ) ) {
                targetPolyAttr.setBackFaceNormalFlip( true );
            } else if ( "false".equalsIgnoreCase( configuredProperty ) || "0".equalsIgnoreCase( configuredProperty )
                        || "no".equalsIgnoreCase( configuredProperty ) ) {
                targetPolyAttr.setBackFaceNormalFlip( false );
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "terrain_backflip",
                                                     configuredProperty, "false" ) );

            }
        }

        int configuredValue = PolygonAttributes.CULL_NONE;
        // finding a configured surface culling
        configuredProperty = (String) renderingOptions.get( "terrain_culling" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "BACK".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_BACK;
            } else if ( "FRONT".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_FRONT;
            } else if ( "NONE".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = PolygonAttributes.CULL_NONE;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "terrain_culling",
                                                     configuredProperty, "NONE" ) );
            }
        }
        targetPolyAttr.setCullFace( configuredValue );
        return targetPolyAttr;
    }

    /**
     * @return the conifured coloring attributes
     */
    private ColoringAttributes createColoringAttributes() {
        // and some Coloring attribs
        // the coloring attributes
        ColoringAttributes ca = new ColoringAttributes();
        int configuredValue = ColoringAttributes.SHADE_GOURAUD;
        // finding a configured shading model
        String configuredProperty = (String) renderingOptions.get( "shading_model" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "SHADE_FLAT".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = ColoringAttributes.SHADE_FLAT;
            } else if ( "SHADE_GOURAUD".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = ColoringAttributes.SHADE_GOURAUD;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "shading_model",
                                                     configuredProperty, "SHADE_GOURAUD" ) );

            }

        }
        ca.setShadeModel( configuredValue );

        configuredValue = ColoringAttributes.NICEST;
        // finding a configured shading model quality
        configuredProperty = (String) renderingOptions.get( "shading_quality" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "FASTEST".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = ColoringAttributes.FASTEST;
            } else if ( "NICEST".equalsIgnoreCase( configuredProperty ) ) {
                configuredValue = ColoringAttributes.NICEST;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "shading_quality",
                                                     configuredProperty, "NICEST" ) );
            }
        }
        ca.setCapability( configuredValue );
        return ca;
    }

    /**
     * @return the configured textureAttributes
     */
    private TextureAttributes createTextureAttributes() {
        int blendFunc = TextureAttributes.MODULATE;
        // finding a configured blending function
        String configuredProperty = (String) renderingOptions.get( "blend_function" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "BLEND".equalsIgnoreCase( configuredProperty ) ) {
                blendFunc = TextureAttributes.BLEND;
            } else if ( "DECAL".equalsIgnoreCase( configuredProperty ) ) {
                blendFunc = TextureAttributes.DECAL;
            } else if ( "COMBINE".equalsIgnoreCase( configuredProperty ) ) {
                blendFunc = TextureAttributes.COMBINE;
            } else if ( "MODULATE".equalsIgnoreCase( configuredProperty ) ) {
                blendFunc = TextureAttributes.MODULATE;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "blend_function",
                                                     configuredProperty, "MODULATE" ) );
            }
        }
        Color4f blendColor = new Color4f( 0, 0, 0, 0 );
        if ( blendFunc == TextureAttributes.BLEND ) {
            configuredProperty = (String) renderingOptions.get( "blend_color" );
            if ( configuredProperty != null ) {
                String[] split = configuredProperty.trim().split( "," );
                if ( split != null && split.length == 4 ) {
                    try {
                        float r = Float.parseFloat( split[0] );
                        float g = Float.parseFloat( split[1] );
                        float b = Float.parseFloat( split[2] );
                        float a = Float.parseFloat( split[3] );
                        blendColor = new Color4f( r, g, b, a );
                    } catch ( NumberFormatException nfe ) {
                        LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "blend_color",
                                                             configuredProperty, "0,0,0,0" ) );
                    }
                } else {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "blend_color",
                                                         configuredProperty, "0,0,0,0" ) );
                }
            }
        }

        int persCorrection = TextureAttributes.NICEST;
        configuredProperty = (String) renderingOptions.get( "terrain_texture_perspective_correction" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "FASTEST".equalsIgnoreCase( configuredProperty ) ) {
                persCorrection = TextureAttributes.FASTEST;
            } else if ( "NICEST".equalsIgnoreCase( configuredProperty ) ) {
                persCorrection = TextureAttributes.NICEST;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                     "terrain_texture_perspective_correction", configuredProperty,
                                                     "NICEST" ) );
            }
        }

        TextureAttributes textureAttribs = new TextureAttributes( blendFunc, new Transform3D(), blendColor,
                                                                  persCorrection );

        if ( TextureAttributes.COMBINE == blendFunc ) {
            /*
             * Get the configured Combine parametersof the texture attributes. first rgb then alpha
             */
            // combineFunction type
            int combineFunctionRGB = TextureAttributes.COMBINE_MODULATE;
            configuredProperty = (String) renderingOptions.get( "combine_function_rgb" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                if ( "COMBINE_REPLACE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_REPLACE;
                } else if ( "COMBINE_ADD".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_ADD;
                } else if ( "COMBINE_ADD_SIGNED".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_ADD_SIGNED;
                } else if ( "COMBINE_SUBTRACT".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_SUBTRACT;
                } else if ( "COMBINE_INTERPOLATE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_INTERPOLATE;
                } else if ( "COMBINE_DOT3".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_DOT3;
                } else if ( "COMBINE_MODULATE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionRGB = TextureAttributes.COMBINE_MODULATE;
                } else {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "combine_function_rbg",
                                                         configuredProperty, "COMBINE_MODULATE" ) );
                }
            }
            textureAttribs.setCombineRgbMode( combineFunctionRGB );

            int combineFunctionAlpha = TextureAttributes.COMBINE_MODULATE;
            configuredProperty = (String) renderingOptions.get( "combine_function_alpha" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                if ( "COMBINE_REPLACE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_REPLACE;
                } else if ( "COMBINE_ADD".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_ADD;
                } else if ( "COMBINE_ADD_SIGNED".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_ADD_SIGNED;
                } else if ( "COMBINE_SUBTRACT".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_SUBTRACT;
                } else if ( "COMBINE_INTERPOLATE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_INTERPOLATE;
                } else if ( "COMBINE_DOT3".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_DOT3;
                } else if ( "COMBINE_MODULATE".equalsIgnoreCase( configuredProperty ) ) {
                    combineFunctionAlpha = TextureAttributes.COMBINE_MODULATE;
                } else {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "combine_function_alpha",
                                                         configuredProperty, "COMBINE_MODULATE" ) );
                }
            }
            textureAttribs.setCombineAlphaMode( combineFunctionAlpha );

            // Combine source Color configuration
            int combineColorSourceRGB = TextureAttributes.COMBINE_TEXTURE_COLOR;
            for ( int i = 0; ++i < 3; ) {
                if ( i == 1 ) {
                    combineColorSourceRGB = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
                } else if ( i == 2 ) {
                    combineColorSourceRGB = TextureAttributes.COMBINE_CONSTANT_COLOR;
                }
                String propertyString = "combine_color_source_rgb_" + i;
                configuredProperty = (String) renderingOptions.get( propertyString );
                if ( configuredProperty != null ) {
                    configuredProperty = configuredProperty.trim();
                    if ( "COMBINE_TEXTURE_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceRGB = TextureAttributes.COMBINE_TEXTURE_COLOR;
                    } else if ( "COMBINE_CONSTANT_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceRGB = TextureAttributes.COMBINE_CONSTANT_COLOR;
                    } else if ( "COMBINE_PREVIOUS_TEXTURE_UNIT_STATE".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceRGB = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
                    } else if ( "COMBINE_OBJECT_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceRGB = TextureAttributes.COMBINE_OBJECT_COLOR;
                    } else {
                        LOG.logWarning( Messages.getMessage(
                                                             "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                             propertyString,
                                                             configuredProperty,
                                                             ( ( i == 0 ) ? "COMBINE_MODULATE"
                                                                         : ( ( i == 1 ) ? "COMBINE_PREVIOUS_TEXTURE_UNIT_STATE"
                                                                                       : "COMBINE_CONSTANT_COLOR" ) ) ) );
                    }

                }
                textureAttribs.setCombineRgbSource( i, combineColorSourceRGB );
            }

            int combineColorSourceAlpha = TextureAttributes.COMBINE_TEXTURE_COLOR;
            for ( int i = 0; ++i < 3; ) {
                if ( i == 1 ) {
                    combineColorSourceAlpha = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
                } else if ( i == 2 ) {
                    combineColorSourceAlpha = TextureAttributes.COMBINE_CONSTANT_COLOR;
                }

                String propertyString = "combine_color_source_alpha_" + i;
                configuredProperty = (String) renderingOptions.get( propertyString );
                if ( configuredProperty != null ) {
                    configuredProperty = configuredProperty.trim();
                    if ( "COMBINE_TEXTURE_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceAlpha = TextureAttributes.COMBINE_TEXTURE_COLOR;
                    } else if ( "COMBINE_CONSTANT_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceAlpha = TextureAttributes.COMBINE_CONSTANT_COLOR;
                    } else if ( "COMBINE_PREVIOUS_TEXTURE_UNIT_STATE".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceAlpha = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
                    } else if ( "COMBINE_OBJECT_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                        combineColorSourceAlpha = TextureAttributes.COMBINE_OBJECT_COLOR;
                    } else {
                        LOG.logWarning( Messages.getMessage(
                                                             "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                             propertyString,
                                                             configuredProperty,
                                                             ( ( i == 0 ) ? "COMBINE_MODULATE"
                                                                         : ( ( i == 1 ) ? "COMBINE_PREVIOUS_TEXTURE_UNIT_STATE"
                                                                                       : "COMBINE_CONSTANT_COLOR" ) ) ) );
                    }
                }
                textureAttribs.setCombineAlphaSource( i, combineColorSourceAlpha );
            }

            // Combine color function to use
            int combineColorFunctionRGB = TextureAttributes.COMBINE_SRC_COLOR;
            configuredProperty = (String) renderingOptions.get( "combine_color_function_rgb" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                if ( "COMBINE_ONE_MINUS_SRC_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                    combineColorFunctionRGB = TextureAttributes.COMBINE_ONE_MINUS_SRC_COLOR;
                } else if ( "COMBINE_SRC_COLOR".equalsIgnoreCase( configuredProperty ) ) {
                    combineColorFunctionRGB = TextureAttributes.COMBINE_SRC_COLOR;
                } else {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                         "combine_color_function_rgb", configuredProperty,
                                                         "COMBINE_SRC_COLOR" ) );
                }

            }
            textureAttribs.setCombineRgbFunction( 0, combineColorFunctionRGB );
            textureAttribs.setCombineRgbFunction( 1, combineColorFunctionRGB );
            textureAttribs.setCombineRgbFunction( 2, combineColorFunctionRGB );

            int combineColorFunctionAlpha = TextureAttributes.COMBINE_SRC_ALPHA;
            configuredProperty = (String) renderingOptions.get( "combine_color_function_alpha" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                if ( "COMBINE_ONE_MINUS_SRC_ALPHA".equalsIgnoreCase( configuredProperty ) ) {
                    combineColorFunctionAlpha = TextureAttributes.COMBINE_ONE_MINUS_SRC_ALPHA;
                } else if ( "COMBINE_SRC_ALPHA".equalsIgnoreCase( configuredProperty ) ) {
                    combineColorFunctionAlpha = TextureAttributes.COMBINE_SRC_ALPHA;
                } else {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                         "combine_color_function_alpha", configuredProperty,
                                                         "COMBINE_SRC_ALPHA" ) );
                }
            }
            textureAttribs.setCombineAlphaFunction( 0, combineColorFunctionAlpha );
            textureAttribs.setCombineAlphaFunction( 1, combineColorFunctionAlpha );
            textureAttribs.setCombineAlphaFunction( 2, combineColorFunctionAlpha );

            // And the scale of the output color
            int combineScaleFactorRGB = 1;
            configuredProperty = (String) renderingOptions.get( "combine_scale_factor_rgb" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                try {
                    combineScaleFactorRGB = Integer.parseInt( configuredProperty );
                } catch ( NumberFormatException nfe ) {
                    combineScaleFactorRGB = 1;
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "combine_scale_factor_rgb",
                                                         configuredProperty, "1" ) );
                }
                if ( combineScaleFactorRGB != 1 && combineScaleFactorRGB != 2 && combineScaleFactorRGB != 4 ) {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "combine_scale_factor_rgb",
                                                         configuredProperty, "1" ) );
                    combineScaleFactorRGB = 1;

                }
            }
            textureAttribs.setCombineRgbScale( combineScaleFactorRGB );

            int combineScaleFactorAlpha = 1;
            configuredProperty = (String) renderingOptions.get( "combine_scale_factor_alpha" );
            if ( configuredProperty != null ) {
                configuredProperty = configuredProperty.trim();
                try {
                    combineScaleFactorAlpha = Integer.parseInt( configuredProperty );
                } catch ( NumberFormatException nfe ) {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                         "combine_scale_factor_alpha", configuredProperty, "1" ) );

                    combineScaleFactorAlpha = 1;
                }
                if ( combineScaleFactorAlpha != 1 && combineScaleFactorAlpha != 2 && combineScaleFactorAlpha != 4 ) {
                    LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY",
                                                         "combine_scale_factor_alpha", configuredProperty, "1" ) );
                    combineScaleFactorAlpha = 1;
                }
            }
            textureAttribs.setCombineAlphaScale( combineScaleFactorAlpha );
        }
        return textureAttribs;
    }

    /**
     * @return gets a RenderingConfiguration as a singleton pattern.
     */
    public static synchronized RenderingConfiguration getInstance() {
        if ( renderConfig == null ) {
            renderConfig = new RenderingConfiguration();
        }
        return renderConfig;
    }

    /**
     * @return the coloringAttributes, Configured rendering properties valid for all rendered primitives.
     */
    public final ColoringAttributes getColoringAttributes() {
        return coloringAttributes;
    }

    /**
     * @return the TextureAttributes, Configured rendering properties valid for all rendered textured primitives.
     */
    public final TextureAttributes getTextureAttributes() {
        return textureAttributes;
    }

    /**
     * @return the surfacePolygonAttributes, Configured rendering properties for the rendered surfaces.
     */
    public final PolygonAttributes getSurfacePolygonAttributes() {
        return surfacePolygonAttributes;
    }

    /**
     * @return the terrainPolygonAttributes, Configured rendering properties for the rendered terrain.
     */
    public final PolygonAttributes getTerrainPolygonAttributes() {
        return terrainPolygonAttributes;
    }

    /**
     * @param textureImage
     *            the image to load as a texture
     * @return a Texture with loaded according the configured mipmap properties.
     */
    public final Texture getTexture( BufferedImage textureImage ) {
        TextureLoader tl = null;
        boolean isMipMapped = true;
        String configuredProperty = (String) renderingOptions.get( "terrain_texture_mipmapping" );
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "TRUE".equalsIgnoreCase( configuredProperty ) || "1".equalsIgnoreCase( configuredProperty )
                 || "yes".equalsIgnoreCase( configuredProperty ) ) {
                tl = new TextureLoader( textureImage, TextureLoader.GENERATE_MIPMAP );
            } else if ( "false".equalsIgnoreCase( configuredProperty ) || "0".equalsIgnoreCase( configuredProperty )
                        || "no".equalsIgnoreCase( configuredProperty ) ) {
                tl = new TextureLoader( textureImage );
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "terrain_texture_mipmapping",
                                                     configuredProperty, "false" ) );
                tl = new TextureLoader( textureImage );
            }
        } else {
            tl = new TextureLoader( textureImage, TextureLoader.GENERATE_MIPMAP );
        }

        configuredProperty = (String) renderingOptions.get( "terrain_anisotropic_filter" );
        int anisotropic = Texture.ANISOTROPIC_SINGLE_VALUE;
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim();
            if ( "NONE".equalsIgnoreCase( configuredProperty ) ) {
                anisotropic = Texture.ANISOTROPIC_NONE;

            } else if ( !"SINGLE_VALUE".equalsIgnoreCase( configuredProperty ) ) {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "terrain_anisotropic_filter",
                                                     configuredProperty, "SINGLE_VALUE" ) );
            }
        }

        configuredProperty = (String) renderingOptions.get( "terrain_texture_filter" );
        int textureFilter = Texture.NICEST;
        if ( configuredProperty != null ) {
            configuredProperty = configuredProperty.trim().toUpperCase();
            if ( "FASTEST".equals( configuredProperty ) ) {
                // uses the fastest available method for processing geometry.
                textureFilter = Texture.FASTEST;
            } else if ( "NICEST".equals( configuredProperty ) ) {
                // uses the nicest available method for processing geometry
                // nottin
            } else if ( "BASE_LEVEL_POINT".equals( configuredProperty ) ) {
                // selects the nearest texel in the base level texture image
                textureFilter = Texture.BASE_LEVEL_POINT;
            } else if ( "BASE_LEVEL_LINEAR".equals( configuredProperty ) ) {
                // performs a bilinear interpolation on the four nearest texels in the base level texture image
                textureFilter = Texture.BASE_LEVEL_LINEAR;
            } else if ( "LINEAR_SHARPEN".equals( configuredProperty ) ) {
                // sharpens the resulting image by extrapolating from the base level plus one image to the base level
                // image of this texture object
                textureFilter = Texture.LINEAR_SHARPEN;
            } else if ( "LINEAR_SHARPEN_RGB".equals( configuredProperty ) ) {
                // performs linear sharpen filter for the rgb components only. The alpha component is computed using
                // BASE_LEVEL_LINEAR filter
                textureFilter = Texture.LINEAR_SHARPEN_RGB;
            } else if ( "LINEAR_SHARPEN_ALPHA".equals( configuredProperty ) ) {
                // performs linear sharpen filter for the alpha component only. The rgb components are computed using
                // BASE_LEVEL_LINEAR filter.
                textureFilter = Texture.LINEAR_SHARPEN_ALPHA;
            } else if ( "FILTER4".equals( configuredProperty ) ) {
                // applies an application-supplied weight function on the nearest 4x4 texels in the base level texture
                // image
                textureFilter = Texture.FILTER4;
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_UNKNOWN_RENDERING_PROPERTY", "terrain_texture_filter",
                                                     configuredProperty, "FASTEST" ) );
            }
        }

        Texture texture = tl.getTexture();
        texture.setEnable( true );
        texture.setAnisotropicFilterMode( anisotropic );
        texture.setMagFilter( textureFilter );
        texture.setMinFilter( textureFilter );

        // texture.setAnisotropicFilterMode( Texture.ANISOTROPIC_SINGLE_VALUE );
        texture.setCapability( Texture.ALLOW_ENABLE_READ | ( ( isMipMapped ) ? Texture.ALLOW_MIPMAP_MODE_READ : 0 )
                               | Texture.CLAMP_TO_EDGE );
        return texture;
    }

    /**
     * @return the useMaterialShading
     */
    public boolean isObjectShadingEnabled() {
        return objectShadingEnabled;
    }

    /**
     * @return the terrainShadingEnabled
     */
    public final boolean isTerrainShadingEnabled() {
        return terrainShadingEnabled;
    }
}
