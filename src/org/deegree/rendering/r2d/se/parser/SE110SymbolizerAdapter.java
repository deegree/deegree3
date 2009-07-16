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

package org.deegree.rendering.r2d.se.parser;

import static java.awt.Color.decode;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.rendering.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.deegree.commons.filter.Expression;
import org.deegree.commons.filter.Filter;
import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.xml.Filter110XMLAdapter;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.feature.Feature;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.se.unevaluated.Continuation.Updater;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Font;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.Halo;
import org.deegree.rendering.r2d.styling.components.LinePlacement;
import org.deegree.rendering.r2d.styling.components.Mark;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.Font.Style;
import org.deegree.rendering.r2d.styling.components.Mark.SimpleMark;
import org.deegree.rendering.r2d.styling.components.Stroke.LineCap;
import org.deegree.rendering.r2d.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>SE110SymbolizerAdapter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SE110SymbolizerAdapter extends XMLAdapter {

    static final Logger LOG = getLogger( SE110SymbolizerAdapter.class );

    private static final NamespaceContext nscontext = CommonNamespaces.getNamespaceContext();

    /**
     * @param root
     * @return the new symbolizer. In case of a text symbolizer, the second part of the pair can be used to evaluate the
     *         text to render.
     */
    public Pair<Symbolizer<?>, Continuation<StringBuffer>> parseSymbolizer( OMElement root ) {
        if ( root.getLocalName().equals( "PointSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parsePointSymbolizer( root ), null );
        }
        if ( root.getLocalName().equals( "LineSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parseLineSymbolizer( root ), null );
        }
        if ( root.getLocalName().equals( "PolygonSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parsePolygonSymbolizer( root ), null );
        }
        if ( root.getLocalName().equals( "TextSymbolizer" ) ) {
            return (Pair) parseTextSymbolizer( root );
        }

        return null;
    }

    /**
     * @param root
     * @return the symbolizer
     */
    public Symbolizer<PointStyling> parsePointSymbolizer( OMElement root ) {
        String name = getNodeAsString( root, new XPath( "se:Name", nscontext ), null );

        QName geom = getNodeAsQName( root, new XPath( "se:Geometry", nscontext ), null );
        PointStyling baseOrEvaluated = new PointStyling();
        final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( root );

        if ( pair == null ) {
            return new Symbolizer<PointStyling>( baseOrEvaluated, geom, name );
        }

        baseOrEvaluated.graphic = pair.first;

        if ( pair.second != null ) {
            return new Symbolizer<PointStyling>( baseOrEvaluated, new Continuation<PointStyling>() {
                @Override
                public void updateStep( PointStyling base, Feature f ) {
                    pair.second.evaluate( base.graphic, f );
                }
            }, geom, name );
        }

        return new Symbolizer<PointStyling>( baseOrEvaluated, geom, name );
    }

    /**
     * @param root
     * @return the symbolizer
     */
    public Symbolizer<LineStyling> parseLineSymbolizer( OMElement root ) {
        String name = getNodeAsString( root, new XPath( "se:Name", nscontext ), null );

        QName geom = getNodeAsQName( root, new XPath( "se:Geometry", nscontext ), null );
        LineStyling baseOrEvaluated = new LineStyling();

        final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( root );

        Continuation<LineStyling> contn = null;

        if ( pair != null ) {
            baseOrEvaluated.stroke = pair.first;

            if ( pair.second != null ) {
                contn = new Continuation<LineStyling>() {
                    @Override
                    public void updateStep( LineStyling base, Feature f ) {
                        pair.second.evaluate( base.stroke, f );
                    }
                };
            }
        }

        contn = updateOrContinue( root, "se:PerpendicularOffset", baseOrEvaluated, new Updater<LineStyling>() {
            @Override
            public void update( LineStyling obj, String val ) {
                obj.perpendicularOffset = Double.parseDouble( val );
            }
        }, new Filter110XMLAdapter(), contn );

        if ( contn == null ) {
            return new Symbolizer<LineStyling>( baseOrEvaluated, geom, name );
        }

        return new Symbolizer<LineStyling>( baseOrEvaluated, contn, geom, name );
    }

    /**
     * @param root
     * @return the symbolizer
     */
    public Symbolizer<PolygonStyling> parsePolygonSymbolizer( OMElement root ) {
        String name = getNodeAsString( root, new XPath( "se:Name", nscontext ), null );

        QName geom = getNodeAsQName( root, new XPath( "se:Geometry", nscontext ), null );
        PolygonStyling baseOrEvaluated = new PolygonStyling();

        final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( root );

        Continuation<PolygonStyling> contn = null;

        if ( pair != null ) {
            baseOrEvaluated.stroke = pair.first;

            if ( pair.second != null ) {
                contn = new Continuation<PolygonStyling>() {
                    @Override
                    public void updateStep( PolygonStyling base, Feature f ) {
                        pair.second.evaluate( base.stroke, f );
                    }
                };
            }
        }

        final Pair<Fill, Continuation<Fill>> fillPair = parseFill( root );

        if ( fillPair != null ) {
            baseOrEvaluated.fill = fillPair.first;

            if ( fillPair.second != null ) {
                contn = new Continuation<PolygonStyling>() {
                    @Override
                    public void updateStep( PolygonStyling base, Feature f ) {
                        fillPair.second.evaluate( base.fill, f );
                    }
                };
            }
        }

        contn = updateOrContinue( root, "se:PerpendicularOffset", baseOrEvaluated, new Updater<PolygonStyling>() {
            @Override
            public void update( PolygonStyling obj, String val ) {
                obj.perpendicularOffset = Double.parseDouble( val );
            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:Displacement/se:DisplacementX", baseOrEvaluated,
                                  new Updater<PolygonStyling>() {
                                      @Override
                                      public void update( PolygonStyling obj, String val ) {
                                          obj.displacementX = Double.parseDouble( val );
                                      }
                                  }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:Displacement/se:DisplacementY", baseOrEvaluated,
                                  new Updater<PolygonStyling>() {
                                      @Override
                                      public void update( PolygonStyling obj, String val ) {
                                          obj.displacementY = Double.parseDouble( val );
                                      }
                                  }, new Filter110XMLAdapter(), contn );

        if ( contn == null ) {
            return new Symbolizer<PolygonStyling>( baseOrEvaluated, geom, name );
        }

        return new Symbolizer<PolygonStyling>( baseOrEvaluated, contn, geom, name );
    }

    /**
     * @param root
     * @return the symbolizer
     */
    public Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>> parseTextSymbolizer( OMElement root ) {
        String name = getNodeAsString( root, new XPath( "se:Name", nscontext ), null );

        QName geom = getNodeAsQName( root, new XPath( "se:Geometry", nscontext ), null );
        TextStyling baseOrEvaluated = new TextStyling();
        Continuation<TextStyling> contn = null;

        Filter110XMLAdapter parser = new Filter110XMLAdapter();
        contn = updateOrContinue( root, "se:LabelPlacement/se:PointPlacement/se:AnchorPoint/se:AnchorPointX",
                                  baseOrEvaluated, new Updater<TextStyling>() {
                                      @Override
                                      public void update( TextStyling obj, String val ) {
                                          obj.anchorPointX = Double.parseDouble( val );
                                      }
                                  }, parser, contn );

        contn = updateOrContinue( root, "se:LabelPlacement/se:PointPlacement/se:AnchorPoint/se:AnchorPointY",
                                  baseOrEvaluated, new Updater<TextStyling>() {
                                      @Override
                                      public void update( TextStyling obj, String val ) {
                                          obj.anchorPointY = Double.parseDouble( val );
                                      }
                                  }, parser, contn );

        contn = updateOrContinue( root, "se:LabelPlacement/se:PointPlacement/se:Displacement/se:DisplacementX",
                                  baseOrEvaluated, new Updater<TextStyling>() {
                                      @Override
                                      public void update( TextStyling obj, String val ) {
                                          obj.displacementX = Double.parseDouble( val );
                                      }
                                  }, parser, contn );

        contn = updateOrContinue( root, "se:LabelPlacement/se:PointPlacement/se:Displacement/se:DisplacementY",
                                  baseOrEvaluated, new Updater<TextStyling>() {
                                      @Override
                                      public void update( TextStyling obj, String val ) {
                                          obj.displacementY = Double.parseDouble( val );
                                      }
                                  }, parser, contn );

        contn = updateOrContinue( root, "se:LabelPlacement/se:PointPlacement/se:Rotation", baseOrEvaluated,
                                  new Updater<TextStyling>() {
                                      @Override
                                      public void update( TextStyling obj, String val ) {
                                          obj.rotation = Double.parseDouble( val );
                                      }
                                  }, parser, contn );

        final Pair<LinePlacement, Continuation<LinePlacement>> pair = parseLinePlacement( root );
        if ( pair != null ) {
            baseOrEvaluated.linePlacement = pair.first;

            if ( pair.second != null ) {
                contn = new Continuation<TextStyling>() {
                    @Override
                    public void updateStep( TextStyling base, Feature f ) {
                        pair.second.evaluate( base.linePlacement, f );
                    }
                };
            }
        }

        final Pair<Halo, Continuation<Halo>> haloPair = parseHalo( root );
        if ( haloPair != null ) {
            baseOrEvaluated.halo = haloPair.first;

            if ( haloPair.second != null ) {
                contn = new Continuation<TextStyling>() {
                    @Override
                    public void updateStep( TextStyling base, Feature f ) {
                        haloPair.second.evaluate( base.halo, f );
                    }
                };
            }
        }

        final Pair<Font, Continuation<Font>> fontPair = parseFont( root );
        if ( fontPair != null ) {
            baseOrEvaluated.font = fontPair.first;

            if ( fontPair.second != null ) {
                contn = new Continuation<TextStyling>() {
                    @Override
                    public void updateStep( TextStyling base, Feature f ) {
                        fontPair.second.evaluate( base.font, f );
                    }
                };
            }
        }

        final Pair<Fill, Continuation<Fill>> fillPair = parseFill( root );
        if ( fillPair != null ) {
            baseOrEvaluated.fill = fillPair.first;

            if ( fillPair.second != null ) {
                contn = new Continuation<TextStyling>() {
                    @Override
                    public void updateStep( TextStyling base, Feature f ) {
                        fillPair.second.evaluate( base.fill, f );
                    }
                };
            }
        }

        Continuation<StringBuffer> label = updateOrContinue( root, "se:Label", new StringBuffer(),
                                                             new Updater<StringBuffer>() {
                                                                 @Override
                                                                 public void update( StringBuffer obj, String val ) {
                                                                     obj.append( val );
                                                                 }
                                                             }, parser, null );

        if ( contn == null ) {
            Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, geom, name );
            return new Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>>( sym, label );
        }

        Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, contn, geom, name );
        return new Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>>( sym, label );
    }

    private Pair<Font, Continuation<Font>> parseFont( OMElement root ) {
        root = getElement( root, new XPath( "se:Font", nsContext ) );
        if ( root == null ) {
            return null;
        }

        Font baseOrEvaluated = new Font();
        Continuation<Font> contn = null;
        Filter110XMLAdapter parser = new Filter110XMLAdapter();

        contn = updateOrContinue( root, "se:SvgParameter[@name='font-family']", baseOrEvaluated, new Updater<Font>() {
            @Override
            public void update( Font obj, String val ) {
                obj.fontFamily.add( val );
            }
        }, parser, contn );
        contn = updateOrContinue( root, "se:SvgParameter[@name='font-style']", baseOrEvaluated, new Updater<Font>() {
            @Override
            public void update( Font obj, String val ) {
                obj.fontStyle = Style.valueOf( val.toUpperCase() );
            }
        }, parser, contn );
        contn = updateOrContinue( root, "se:SvgParameter[@name='font-weight']", baseOrEvaluated, new Updater<Font>() {
            @Override
            public void update( Font obj, String val ) {
                obj.bold = val.equalsIgnoreCase( "bold" );
            }
        }, parser, contn );
        contn = updateOrContinue( root, "se:SvgParameter[@name='font-size']", baseOrEvaluated, new Updater<Font>() {
            @Override
            public void update( Font obj, String val ) {
                obj.fontSize = Integer.parseInt( val );
            }
        }, parser, contn );

        return new Pair<Font, Continuation<Font>>( baseOrEvaluated, contn );

    }

    private Pair<Halo, Continuation<Halo>> parseHalo( OMElement root ) {
        root = getElement( root, new XPath( "se:Halo", nsContext ) );
        if ( root == null ) {
            return null;
        }

        Halo baseOrEvaluated = new Halo();
        Continuation<Halo> contn = null;

        contn = updateOrContinue( root, "se:Radius", baseOrEvaluated, new Updater<Halo>() {
            @Override
            public void update( Halo obj, String val ) {
                obj.radius = Double.parseDouble( val );

            }
        }, new Filter110XMLAdapter(), contn );

        final Pair<Fill, Continuation<Fill>> fillPair = parseFill( root );

        if ( fillPair != null ) {
            baseOrEvaluated.fill = fillPair.first;

            if ( fillPair.second != null ) {
                contn = new Continuation<Halo>() {
                    @Override
                    public void updateStep( Halo base, Feature f ) {
                        fillPair.second.evaluate( base.fill, f );
                    }
                };
            }
        }

        return new Pair<Halo, Continuation<Halo>>( baseOrEvaluated, contn );
    }

    private Pair<LinePlacement, Continuation<LinePlacement>> parseLinePlacement( OMElement root ) {
        root = getElement( root, new XPath( "se:LabelPlacement/se:LinePlacement", nsContext ) );
        if ( root == null ) {
            return null;
        }

        LinePlacement baseOrEvaluated = new LinePlacement();
        Continuation<LinePlacement> contn = null;

        contn = updateOrContinue( root, "se:PerpendicularOffset", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.perpendicularOffset = Double.parseDouble( val );

            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:InitialGap", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.initialGap = Double.parseDouble( val );

            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:Gap", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.gap = Double.parseDouble( val );

            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:GeneralizeLine", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.generalizeLine = Boolean.parseBoolean( val );

            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:IsAligned", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.isAligned = Boolean.parseBoolean( val );

            }
        }, new Filter110XMLAdapter(), contn );

        contn = updateOrContinue( root, "se:IsRepeated", baseOrEvaluated, new Updater<LinePlacement>() {
            @Override
            public void update( LinePlacement obj, String val ) {
                obj.repeat = Boolean.parseBoolean( val );

            }
        }, new Filter110XMLAdapter(), contn );

        return new Pair<LinePlacement, Continuation<LinePlacement>>( baseOrEvaluated, contn );
    }

    /**
     * @param root
     * @return a base graphic and a continuation, or an evaluated graphic
     */
    private Pair<Graphic, Continuation<Graphic>> parseGraphic( OMElement root ) {
        Filter110XMLAdapter parser = new Filter110XMLAdapter();

        OMElement graphic = getElement( root, new XPath( "se:Graphic", nscontext ) );

        if ( graphic == null ) {
            return null;
        }

        Graphic base = new Graphic();
        Continuation<Graphic> contn = null;

        Iterator<?> iter = graphic.getChildElements();
        while ( iter.hasNext() ) {
            OMElement elem = (OMElement) iter.next();

            if ( elem.getLocalName().equals( "Mark" ) ) {
                final Pair<Mark, Continuation<Mark>> pair = parseMark( elem );
                if ( pair != null ) {
                    base.mark = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Graphic>() {
                            @Override
                            public void updateStep( Graphic base, Feature f ) {
                                pair.second.evaluate( base.mark, f );
                            }
                        };
                        break;
                    }
                }
            }
            if ( elem.getLocalName().equals( "ExternalGraphic" ) ) {
                try {
                    base.image = parseExternalGraphic( elem );
                    if ( base.image != null ) {
                        break;
                    }
                } catch ( IOException e ) {
                    LOG.debug( "Stack trace", e );
                    LOG.warn( get( "R2D.EXTERNAL_GRAPHIC_NOT_LOADED" ), elem.getLineNumber(), elem.toString() );
                }
            }
        }

        contn = updateOrContinue( graphic, "se:Opacity", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.opacity = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:Size", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.size = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:Rotation", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.rotation = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:AnchorPoint/se:AnchorPointX", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.anchorPointX = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:AnchorPoint/se:AnchorPointY", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.anchorPointY = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:Displacement/se:DisplacementX", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.displacementX = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( graphic, "se:Displacement/se:DisplacementY", base, new Updater<Graphic>() {
            public void update( Graphic obj, String val ) {
                obj.displacementY = Double.parseDouble( val );
            }
        }, parser, contn );

        return new Pair<Graphic, Continuation<Graphic>>( base, contn );
    }

    /**
     * @param g
     * @return a pre-rendered image
     * @throws IOException
     */
    public BufferedImage parseExternalGraphic( OMElement g )
                            throws IOException {
        if ( g == null ) {
            return null;
        }

        // TODO inline content
        // TODO color replacement

        // TODO in case of svg, load/render it with batik
        // String format = getNodeAsString( g, new XPath( "se:Format", nscontext ), null );

        String str = getNodeAsString( g, new XPath( "se:OnlineResource/@xlink:href", nscontext ), null );
        if ( str != null ) {
            URL url = resolve( str );
            LOG.debug( "Loading external graphic from URL {}", url );
            return ImageIO.read( url );

        }

        return null;
    }

    /**
     * @param mark
     * @return a base mark and a continuation, or an evaluated mark
     */
    public Pair<Mark, Continuation<Mark>> parseMark( OMElement mark ) {
        if ( mark == null ) {
            return null;
        }

        Mark base = new Mark();
        Continuation<Mark> contn = null;

        String name = getNodeAsString( mark, new XPath( "se:WellKnownName", nscontext ), null );
        if ( name != null ) {
            base.wellKnown = SimpleMark.valueOf( name.toUpperCase() );
        }

        final Pair<Fill, Continuation<Fill>> fill = parseFill( mark );
        if ( fill != null ) {
            base.fill = fill.first;
            if ( fill.second != null ) {
                contn = new Continuation<Mark>() {
                    @Override
                    public void updateStep( Mark base, Feature f ) {
                        fill.second.evaluate( base.fill, f );
                    }
                };
            }
        }

        final Pair<Stroke, Continuation<Stroke>> stroke = parseStroke( mark );
        if ( stroke != null ) {
            base.stroke = stroke.first;
            if ( stroke.second != null ) {
                contn = new Continuation<Mark>() {
                    @Override
                    public void updateStep( Mark base, Feature f ) {
                        stroke.second.evaluate( base.stroke, f );
                    }
                };
            }
        }

        return new Pair<Mark, Continuation<Mark>>( base, contn );
    }

    /**
     * @param root
     * @return a base fill + continuation, or evaluated fill
     */
    public Pair<Fill, Continuation<Fill>> parseFill( OMElement root ) {
        OMElement fill = getElement( root, new XPath( "se:Fill", nscontext ) );
        if ( fill == null ) {
            return null;
        }

        Filter110XMLAdapter parser = new Filter110XMLAdapter();

        Fill base = new Fill();
        Continuation<Fill> contn = null;

        OMElement graphicFill = getElement( fill, new XPath( "se:GraphicFill", nscontext ) );
        if ( graphicFill != null ) {
            final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( graphicFill );
            if ( pair != null ) {
                base.graphic = pair.first;
                if ( pair.second != null ) {
                    contn = new Continuation<Fill>() {
                        @Override
                        public void updateStep( Fill base, Feature f ) {
                            pair.second.evaluate( base.graphic, f );
                        }
                    };
                }
            }
        }

        contn = updateOrContinue( fill, "se:SvgParameter[@name='fill']", base, new Updater<Fill>() {
            @Override
            public void update( Fill obj, String val ) {
                // keep alpha value
                int alpha = obj.color.getAlpha();
                obj.color = decode( val );
                obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
            }
        }, parser, contn );

        contn = updateOrContinue( fill, "se:SvgParameter[@name='fill-opacity']", base, new Updater<Fill>() {
            @Override
            public void update( Fill obj, String val ) {
                // keep original color
                float alpha = Float.parseFloat( val );
                float[] cols = obj.color.getRGBColorComponents( null );
                obj.color = new Color( cols[0], cols[1], cols[2], alpha );
            }
        }, parser, contn );

        return new Pair<Fill, Continuation<Fill>>( base, contn );
    }

    /**
     * @param root
     * @return a base stroke + contn, or evaluated stroke
     */
    public Pair<Stroke, Continuation<Stroke>> parseStroke( OMElement root ) {
        OMElement stroke = getElement( root, new XPath( "se:Stroke", nscontext ) );
        if ( stroke == null ) {
            return null;
        }

        Filter110XMLAdapter parser = new Filter110XMLAdapter();

        Stroke base = new Stroke();
        Continuation<Stroke> contn = null;

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                // keep alpha value
                int alpha = obj.color.getAlpha();
                obj.color = decode( val );
                obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-opacity']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                // keep original color
                float alpha = Float.parseFloat( val );
                float[] cols = obj.color.getRGBColorComponents( null );
                obj.color = new Color( cols[0], cols[1], cols[2], alpha );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-width']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                obj.width = Double.parseDouble( val );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-linejoin']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                obj.linejoin = LineJoin.valueOf( val.toUpperCase() );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-linecap']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                obj.linecap = LineCap.valueOf( val.toUpperCase() );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-dasharray']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                obj.dasharray = splitAsDoubles( val, " " );
            }
        }, parser, contn );

        contn = updateOrContinue( stroke, "se:SvgParameter[@name='stroke-dashoffset']", base, new Updater<Stroke>() {
            @Override
            public void update( Stroke obj, String val ) {
                obj.dashoffset = Double.parseDouble( val );
            }
        }, parser, contn );

        OMElement graphicFill = getElement( stroke, new XPath( "se:GraphicFill", nscontext ) );
        if ( graphicFill != null ) {
            final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( graphicFill );
            if ( pair != null ) {
                base.fill = pair.first;
                if ( pair.second != null ) {
                    contn = new Continuation<Stroke>() {
                        @Override
                        public void updateStep( Stroke base, Feature f ) {
                            pair.second.evaluate( base.fill, f );
                        }
                    };
                }
            }
        }

        OMElement graphicStroke = getElement( stroke, new XPath( "se:GraphicStroke", nscontext ) );
        if ( graphicStroke != null ) {
            final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( graphicStroke );

            if ( pair != null ) {
                base.stroke = pair.first;
                if ( pair.second != null ) {
                    contn = new Continuation<Stroke>() {
                        @Override
                        public void updateStep( Stroke base, Feature f ) {
                            pair.second.evaluate( base.stroke, f );
                        }
                    };
                }

                contn = updateOrContinue( stroke, "se:GraphicStroke/se:InitialGap", base, new Updater<Stroke>() {
                    @Override
                    public void update( Stroke obj, String val ) {
                        obj.strokeInitialGap = Double.parseDouble( val );
                    }
                }, parser, contn );

                contn = updateOrContinue( stroke, "se:GraphicStroke/se:Gap", base, new Updater<Stroke>() {
                    @Override
                    public void update( Stroke obj, String val ) {
                        obj.strokeGap = Double.parseDouble( val );
                    }
                }, parser, contn );
            }

        }

        return new Pair<Stroke, Continuation<Stroke>>( base, contn );
    }

    /**
     * @param root
     * @return a new style
     */
    public org.deegree.rendering.r2d.se.unevaluated.Style parseFeatureTypeStyle( OMElement root ) {
        LinkedList<Continuation<LinkedList<Symbolizer<?>>>> result = new LinkedList<Continuation<LinkedList<Symbolizer<?>>>>();
        HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();
        // TODO name, description, ftname, semantictypeidentifier
        for ( OMElement elem : getElements( root, new XPath( "se:Rule | se:OnlineResource", nscontext ) ) ) {
            OMElement fil = getElement( elem, new XPath( "ogc:Filter", nscontext ) );
            Filter filter = null;
            if ( fil != null ) {
                Filter110XMLAdapter parser = new Filter110XMLAdapter();
                parser.setRootElement( fil );
                filter = parser.parse();
            }

            LinkedList<Symbolizer<?>> syms = new LinkedList<Symbolizer<?>>();

            if ( elem.getLocalName().equals( "OnlineResource" ) ) {
                // TODO
            } else {
                // TODO name, description, legendgraphic, scales
                String symNames = "se:PointSymbolizer | se:TextSymbolizer | se:PolygonSymbolizer | se:LineSymbolizer";
                for ( OMElement sym : getElements( elem, new XPath( symNames, nscontext ) ) ) {
                    Pair<Symbolizer<?>, Continuation<StringBuffer>> parsedSym = parseSymbolizer( sym );
                    if ( parsedSym.second != null ) {
                        labels.put( (Symbolizer) parsedSym.first, parsedSym.second );
                    }
                    syms.add( parsedSym.first );
                }
            }
            result.add( new FilterContinuation( filter, syms ) );
        }

        return new org.deegree.rendering.r2d.se.unevaluated.Style( result, labels );
    }

    class FilterContinuation extends Continuation<LinkedList<Symbolizer<?>>> {
        private Filter filter;

        private LinkedList<Symbolizer<?>> syms;

        FilterContinuation( Filter filter, LinkedList<Symbolizer<?>> syms ) {
            this.filter = filter;
            this.syms = syms;
        }

        @Override
        public void updateStep( LinkedList<Symbolizer<?>> base, Feature f ) {
            try {
                if ( filter == null || filter.evaluate( f ) ) {
                    base.addAll( syms );
                }
            } catch ( FilterEvaluationException e ) {
                LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), filter.toString() );
            }
        }

    }

    /**
     * @param <T>
     * @param root
     * @param name
     * @param obj
     * @param updater
     * @param parser
     * @param contn
     * @return a continuation or null, if none was created and input
     */
    private <T> Continuation<T> updateOrContinue( OMElement root, String name, T obj, final Updater<T> updater,
                                                  Filter110XMLAdapter parser, Continuation<T> contn ) {
        for ( OMElement elem : getElements( root, new XPath( name, nscontext ) ) ) {
            if ( elem != null ) {
                Iterator<?> iter = elem.getChildren();
                final LinkedList<Pair<String, Pair<Expression, String>>> text = new LinkedList<Pair<String, Pair<Expression, String>>>(); // no
                // real 'alternative', have we?
                boolean textOnly = true;
                while ( iter.hasNext() ) {
                    Object cur = iter.next();
                    if ( cur instanceof OMElement ) {
                        OMElement om = (OMElement) cur;
                        Expression expr = parser.parseExpression( (OMElement) cur );
                        Pair<Expression, String> second = new Pair<Expression, String>( expr, get( "R2D.LINE",
                                                                                                   om.getLineNumber(),
                                                                                                   om ) );
                        text.add( new Pair<String, Pair<Expression, String>>( null, second ) );
                        textOnly = false;
                    }
                    if ( cur instanceof OMText ) {
                        OMText t = (OMText) cur;
                        if ( textOnly && !text.isEmpty() ) { // concat text in case of multiple text nodes from
                            // beginning
                            String txt = text.removeLast().first;
                            text.add( new Pair<String, Pair<Expression, String>>( txt + t.getText(), null ) );
                        } else {
                            text.add( new Pair<String, Pair<Expression, String>>( t.getText(), null ) );
                        }
                    }
                }

                if ( textOnly ) {
                    updater.update( obj, text.getFirst().first );
                } else {
                    contn = new Continuation<T>( contn ) {
                        @Override
                        public void updateStep( T base, Feature f ) {
                            String tmp = "";
                            for ( Pair<String, Pair<Expression, String>> p : text ) {
                                if ( p.first != null ) {
                                    tmp += p.first;
                                }
                                if ( p.second != null ) {
                                    try {
                                        Object evald = p.second.first.evaluate( f );
                                        if ( evald == null ) {
                                            LOG.warn( get( "R2D.EXPRESSION_TO_NULL" ), p.second.second );
                                        } else {
                                            tmp += evald;
                                        }
                                    } catch ( FilterEvaluationException e ) {
                                        LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), p.second.second );
                                    }
                                }
                            }

                            updater.update( base, tmp );
                        }
                    };
                }
            }

        }
        return contn;
    }

}
