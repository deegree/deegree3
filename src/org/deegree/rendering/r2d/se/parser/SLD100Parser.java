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
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.stax.StAXParsingHelper.asQName;
import static org.deegree.commons.xml.stax.StAXParsingHelper.resolve;
import static org.deegree.rendering.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.xml.Filter110XMLDecoder;
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
 * <code>SLD100Parser</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SLD100Parser {

    static final Logger LOG = getLogger( SLD100Parser.class );

    static final ElseFilter ELSEFILTER = new ElseFilter();

    // done and tested, same as SE
    private static Pair<Fill, Continuation<Fill>> parseFill( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Fill" );

        Fill base = new Fill();
        Continuation<Fill> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Fill" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "GraphicFill" ) ) {
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
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

            if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String cssName = in.getAttributeValue( null, "name" );
                if ( cssName.equals( "fill" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep alpha value
                            int alpha = obj.color.getAlpha();
                            obj.color = decode( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn );
                }

                if ( cssName.equals( "fill-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep original color
                            float alpha = Float.parseFloat( val );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn );
                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "Fill" );

        return new Pair<Fill, Continuation<Fill>>( base, contn );
    }

    // done and tested, same as SE
    private static Pair<Stroke, Continuation<Stroke>> parseStroke( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Stroke" );

        Stroke base = new Stroke();
        Continuation<Stroke> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Stroke" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String name = in.getAttributeValue( null, "name" );

                if ( name.equals( "stroke" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // keep alpha value
                            int alpha = obj.color.getAlpha();
                            obj.color = decode( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // keep original color
                            float alpha = Float.parseFloat( val );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-width" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.width = Double.parseDouble( val );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-linejoin" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.linejoin = LineJoin.valueOf( val.toUpperCase() );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-linecap" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.linecap = LineCap.valueOf( val.toUpperCase() );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-dasharray" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.dasharray = splitAsDoubles( val, " " );
                        }
                    }, contn );
                }

                if ( name.equals( "stroke-dashoffset" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.dashoffset = Double.parseDouble( val );
                        }
                    }, contn );
                }

                in.require( END_ELEMENT, SLDNS, null );
            }

            if ( in.getLocalName().equals( "GraphicFill" ) ) {
                in.nextTag();
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
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
                in.require( END_ELEMENT, SLDNS, "Graphic" );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "GraphicFill" );
            }

            if ( in.getLocalName().equals( "GraphicStroke" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "GraphicStroke" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Graphic" ) ) {
                        final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

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
                        }

                        in.require( END_ELEMENT, SLDNS, "Graphic" );
                    }

                    if ( in.getLocalName().equals( "InitialGap" ) ) {
                        contn = updateOrContinue( in, "InitialGap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeInitialGap = Double.parseDouble( val );
                            }
                        }, contn );
                        in.require( END_ELEMENT, SLDNS, "InitialGap" );
                    }

                    if ( in.getLocalName().equals( "Gap" ) ) {
                        contn = updateOrContinue( in, "Gap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeGap = Double.parseDouble( val );
                            }
                        }, contn );
                        in.require( END_ELEMENT, SLDNS, "Gap" );
                    }

                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "Stroke" );

        return new Pair<Stroke, Continuation<Stroke>>( base, contn );
    }

    // done and tested, same as SE
    private static Pair<Mark, Continuation<Mark>> parseMark( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Mark" );

        Mark base = new Mark();
        Continuation<Mark> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Mark" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "WellKnownName" ) ) {
                in.next();
                base.wellKnown = SimpleMark.valueOf( in.getText().toUpperCase() );
                in.nextTag();
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fill = parseFill( in );
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

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> stroke = parseStroke( in );
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
        }

        in.require( END_ELEMENT, SLDNS, "Mark" );

        return new Pair<Mark, Continuation<Mark>>( base, contn );
    }

    private static BufferedImage parseExternalGraphic( XMLStreamReader in )
                            throws IOException, XMLStreamException {
        // TODO inline content
        // TODO color replacement
        // TODO in case of svg, load/render it with batik

        in.require( START_ELEMENT, SLDNS, "ExternalGraphic" );

        String format = null;
        BufferedImage img = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "ExternalGraphic" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Format" ) ) {
                in.next();
                format = in.getText();
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Format" );
            }
            if ( in.getLocalName().equals( "OnlineResource" ) ) {
                String str = in.getAttributeValue( XLNNS, "href" );
                URL url = resolve( str, in );
                LOG.debug( "Loading external graphic from URL '{}'", url );
                img = ImageIO.read( url );
                in.nextTag();
            }
        }

        return img;
    }

    // done and tested, same as SE
    private static Pair<Graphic, Continuation<Graphic>> parseGraphic( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Graphic" );

        Graphic base = new Graphic();
        Continuation<Graphic> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Graphic" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Mark" ) ) {
                final Pair<Mark, Continuation<Mark>> pair = parseMark( in );
                in.nextTag();
                if ( pair != null ) {
                    base.mark = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Graphic>() {
                            @Override
                            public void updateStep( Graphic base, Feature f ) {
                                pair.second.evaluate( base.mark, f );
                            }
                        };
                    }
                }
            }
            if ( in.getLocalName().equals( "ExternalGraphic" ) ) {
                try {
                    base.image = parseExternalGraphic( in );
                } catch ( IOException e ) {
                    LOG.debug( "Stack trace", e );
                    LOG.warn( get( "R2D.EXTERNAL_GRAPHIC_NOT_LOADED" ),
                              new Object[] { in.getLocation().getLineNumber(), in.getLocation().getColumnNumber(),
                                            in.getLocation().getSystemId() } );
                }
            }

            if ( in.getLocalName().equals( "Opacity" ) ) {
                contn = updateOrContinue( in, "Opacity", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.opacity = Double.parseDouble( val );
                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Size" ) ) {
                contn = updateOrContinue( in, "Size", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.size = Double.parseDouble( val );
                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Rotation" ) ) {
                contn = updateOrContinue( in, "Rotation", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.rotation = Double.parseDouble( val );
                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "AnchorPoint" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "AnchorPoint" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "AnchorPointX" ) ) {
                        contn = updateOrContinue( in, "AnchorPointX", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.anchorPointX = Double.parseDouble( val );
                            }
                        }, contn );
                    }
                    if ( in.getLocalName().equals( "AnchorPointY" ) ) {
                        contn = updateOrContinue( in, "AnchorPointY", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.anchorPointY = Double.parseDouble( val );
                            }
                        }, contn );
                    }
                }
            }

            if ( in.getLocalName().equals( "Displacement" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                        contn = updateOrContinue( in, "DisplacementX", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.displacementX = Double.parseDouble( val );
                            }
                        }, contn );
                    }
                    if ( in.getLocalName().equals( "DisplacementY" ) ) {
                        contn = updateOrContinue( in, "DisplacementY", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.displacementY = Double.parseDouble( val );
                            }
                        }, contn );
                    }
                }
            }
        }
        in.require( END_ELEMENT, SLDNS, "Graphic" );

        return new Pair<Graphic, Continuation<Graphic>>( base, contn );
    }

    // done and tested, same as SE
    /**
     * @param in
     * @return a new symbolizer
     * @throws XMLStreamException
     */
    public static Symbolizer<PointStyling> parsePointSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "PointSymbolizer" );

        QName geometry = null;
        String name = null;
        PointStyling baseOrEvaluated = new PointStyling();

        while ( !( in.isEndElement() && in.getLocalName().equals( "PointSymbolizer" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Name" ) ) {
                in.next();
                name = in.getText();
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Name" );
            }
            if ( in.getLocalName().equals( "Geometry" ) ) {
                in.next();
                geometry = asQName( in, in.getText() );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Geometry" );
            }
            if ( in.getLocalName().equals( "Graphic" ) ) {
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

                if ( pair == null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, geometry, name );
                }

                baseOrEvaluated.graphic = pair.first;

                if ( pair.second != null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, new Continuation<PointStyling>() {
                        @Override
                        public void updateStep( PointStyling base, Feature f ) {
                            pair.second.evaluate( base.graphic, f );
                        }
                    }, geometry, null );
                }
            }
        }

        in.require( END_ELEMENT, SLDNS, "PointSymbolizer" );
        return new Symbolizer<PointStyling>( baseOrEvaluated, geometry, name );
    }

    /**
     * @param in
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public static Pair<Symbolizer<?>, Continuation<StringBuffer>> parseSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, null );
        if ( in.getLocalName().equals( "PointSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parsePointSymbolizer( in ), null );
        }
        if ( in.getLocalName().equals( "LineSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parseLineSymbolizer( in ), null );
        }
        if ( in.getLocalName().equals( "PolygonSymbolizer" ) ) {
            return new Pair<Symbolizer<?>, Continuation<StringBuffer>>( parsePolygonSymbolizer( in ), null );
        }
        if ( in.getLocalName().equals( "TextSymbolizer" ) ) {
            return (Pair) parseTextSymbolizer( in );
        }
        return null;
    }

    // done and tested, same as SE
    /**
     * @param in
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public static Symbolizer<LineStyling> parseLineSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "LineSymbolizer" );

        QName geom = null;
        String name = null;
        LineStyling baseOrEvaluated = new LineStyling();
        Continuation<LineStyling> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "LineSymbolizer" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Name" ) ) {
                in.next();
                name = in.getText();
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Name" );
            }

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( in );

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
            }

            if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<LineStyling>() {
                    @Override
                    public void update( LineStyling obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );
                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Geometry" ) ) {
                in.next();
                geom = asQName( in, in.getText() );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Geometry" );
            }
        }

        if ( contn == null ) {
            return new Symbolizer<LineStyling>( baseOrEvaluated, geom, name );
        }

        return new Symbolizer<LineStyling>( baseOrEvaluated, contn, geom, name );
    }

    /**
     * @param in
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public static Symbolizer<PolygonStyling> parsePolygonSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "PolygonSymbolizer" );

        QName geom = null;
        String name = null;
        PolygonStyling baseOrEvaluated = new PolygonStyling();
        Continuation<PolygonStyling> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "PolygonSymbolizer" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Name" ) ) {
                in.next();
                name = in.getText();
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Name" );
            }

            if ( in.getLocalName().equals( "Geometry" ) ) {
                in.next();
                geom = asQName( in, in.getText() );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Geometry" );
            }

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( in );

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
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );

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
            }

            if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<PolygonStyling>() {
                    @Override
                    public void update( PolygonStyling obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );
                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Displacement" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                        contn = updateOrContinue( in, "DisplacementX", baseOrEvaluated, new Updater<PolygonStyling>() {
                            @Override
                            public void update( PolygonStyling obj, String val ) {
                                obj.displacementX = Double.parseDouble( val );
                            }
                        }, contn );
                    }

                    if ( in.getLocalName().equals( "DisplacementY" ) ) {
                        contn = updateOrContinue( in, "DisplacementY", baseOrEvaluated, new Updater<PolygonStyling>() {
                            @Override
                            public void update( PolygonStyling obj, String val ) {
                                obj.displacementY = Double.parseDouble( val );
                            }
                        }, contn );
                    }
                }
            }
        }

        if ( contn == null ) {
            return new Symbolizer<PolygonStyling>( baseOrEvaluated, geom, name );
        }

        return new Symbolizer<PolygonStyling>( baseOrEvaluated, contn, geom, name );
    }

    private static <T> Continuation<T> updateOrContinue( XMLStreamReader in, String name, T obj,
                                                         final Updater<T> updater, Continuation<T> contn )
                            throws XMLStreamException {
        if ( in.getLocalName().endsWith( name ) ) {
            final LinkedList<Pair<String, Pair<Expression, String>>> text = new LinkedList<Pair<String, Pair<Expression, String>>>(); // no
            // real 'alternative', have we?
            boolean textOnly = true;
            while ( !( in.isEndElement() && in.getLocalName().endsWith( name ) ) ) {
                in.next();
                if ( in.isStartElement() ) {
                    Expression expr = null;
                    try {
                        in.nextTag();
                        expr = Filter110XMLDecoder.parseExpression( in );
                    } catch ( XMLStreamException e ) {
                        throw new XMLParsingException( in, e.getMessage() );
                    }
                    Pair<Expression, String> second;
                    second = new Pair<Expression, String>( expr, get( "R2D.LINE", in.getLocation().getLineNumber(),
                                                                      in.getLocation().getColumnNumber(),
                                                                      in.getLocation().getSystemId() ) );
                    text.add( new Pair<String, Pair<Expression, String>>( null, second ) );
                    textOnly = false;
                }
                if ( in.isCharacters() ) {
                    if ( textOnly && !text.isEmpty() ) { // concat text in case of multiple text nodes from
                        // beginning
                        String txt = text.removeLast().first;
                        text.add( new Pair<String, Pair<Expression, String>>( txt + in.getText(), null ) );
                    } else {
                        text.add( new Pair<String, Pair<Expression, String>>( in.getText(), null ) );
                    }
                }
            }
            in.require( END_ELEMENT, SLDNS, null );

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
                                    Object[] evald = p.second.first.evaluate( f );
                                    if ( evald.length == 0 ) {
                                        LOG.warn( get( "R2D.EXPRESSION_TO_NULL" ), p.second.second );
                                    } else {
                                        tmp += evald[0];
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

        return contn;
    }

    /**
     * @param in
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public static Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>> parseTextSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "TextSymbolizer" );

        QName geom = null;
        String name = null;
        TextStyling baseOrEvaluated = new TextStyling();
        Continuation<TextStyling> contn = null;
        Continuation<StringBuffer> label = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "TextSymbolizer" ) ) ) {
            in.nextTag();
            if ( in.getLocalName().equals( "Name" ) ) {
                in.next();
                name = in.getText();
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Name" );
            }

            if ( in.getLocalName().equals( "Geometry" ) ) {
                in.next();
                geom = asQName( in, in.getText() );
                in.nextTag();
                in.require( END_ELEMENT, SLDNS, "Geometry" );
            }

            if ( in.getLocalName().equals( "Label" ) ) {
                label = updateOrContinue( in, "Label", new StringBuffer(), new Updater<StringBuffer>() {
                    @Override
                    public void update( StringBuffer obj, String val ) {
                        obj.append( val );
                    }
                }, null );

            }

            if ( in.getLocalName().equals( "LabelPlacement" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equalsIgnoreCase( "LabelPlacement" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equalsIgnoreCase( "PointPlacement" ) ) {
                        while ( !( in.isEndElement() && in.getLocalName().equals( "PointPlacement" ) ) ) {
                            in.nextTag();
                            if ( in.getLocalName().equals( "AnchorPoint" ) ) {
                                while ( !( in.isEndElement() && in.getLocalName().equals( "AnchorPoint" ) ) ) {
                                    in.nextTag();
                                    if ( in.getLocalName().equals( "AnchorPointX" ) ) {
                                        contn = updateOrContinue( in, "AnchorPointX", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.anchorPointX = Double.parseDouble( val );
                                                                      }
                                                                  }, contn );
                                    }
                                    if ( in.getLocalName().equals( "AnchorPointY" ) ) {
                                        contn = updateOrContinue( in, "AnchorPointY", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.anchorPointY = Double.parseDouble( val );
                                                                      }
                                                                  }, contn );
                                    }
                                }
                            }

                            if ( in.getLocalName().equals( "Displacement" ) ) {
                                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                                    in.nextTag();
                                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                                        contn = updateOrContinue( in, "DisplacementX", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.displacementX = Double.parseDouble( val );
                                                                      }
                                                                  }, contn );
                                    }
                                    if ( in.getLocalName().equals( "DisplacementY" ) ) {
                                        contn = updateOrContinue( in, "DisplacementY", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.displacementY = Double.parseDouble( val );
                                                                      }
                                                                  }, contn );
                                    }
                                }
                            }

                            if ( in.getLocalName().equals( "Rotation" ) ) {
                                contn = updateOrContinue( in, "Rotation", baseOrEvaluated, new Updater<TextStyling>() {
                                    @Override
                                    public void update( TextStyling obj, String val ) {
                                        obj.rotation = Double.parseDouble( val );
                                    }
                                }, contn );
                            }

                        }
                    }

                    if ( in.getLocalName().equals( "LinePlacement" ) ) {
                        final Pair<LinePlacement, Continuation<LinePlacement>> pair = parseLinePlacement( in );
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
                    }
                }
            }

            if ( in.getLocalName().equals( "Halo" ) ) {
                final Pair<Halo, Continuation<Halo>> haloPair = parseHalo( in );
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
            }

            if ( in.getLocalName().equals( "Font" ) ) {
                final Pair<Font, Continuation<Font>> fontPair = parseFont( in );
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
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );
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
            }
        }

        if ( contn == null ) {
            Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, geom, name );
            return new Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>>( sym, label );
        }

        Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, contn, geom, name );
        return new Pair<Symbolizer<TextStyling>, Continuation<StringBuffer>>( sym, label );
    }

    private static Pair<Font, Continuation<Font>> parseFont( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Font" );

        Font baseOrEvaluated = new Font();
        Continuation<Font> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Font" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String name = in.getAttributeValue( null, "name" );
                if ( name.equals( "font-family" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.fontFamily.add( val );
                        }
                    }, contn );
                }
                if ( name.equals( "font-style" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.fontStyle = Style.valueOf( val.toUpperCase() );
                        }
                    }, contn );
                }
                if ( name.equals( "font-weight" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.bold = val.equalsIgnoreCase( "bold" );
                        }
                    }, contn );
                }
                if ( name.equals( "font-size" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.fontSize = Integer.parseInt( val );
                        }
                    }, contn );
                }
            }
        }

        return new Pair<Font, Continuation<Font>>( baseOrEvaluated, contn );

    }

    private static Pair<Halo, Continuation<Halo>> parseHalo( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "Halo" );

        Halo baseOrEvaluated = new Halo();
        Continuation<Halo> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Halo" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Radius" ) ) {
                contn = updateOrContinue( in, "Radius", baseOrEvaluated, new Updater<Halo>() {
                    @Override
                    public void update( Halo obj, String val ) {
                        obj.radius = Double.parseDouble( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );

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
            }
        }

        return new Pair<Halo, Continuation<Halo>>( baseOrEvaluated, contn );
    }

    private static Pair<LinePlacement, Continuation<LinePlacement>> parseLinePlacement( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, SLDNS, "LinePlacement" );

        LinePlacement baseOrEvaluated = new LinePlacement();
        Continuation<LinePlacement> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "LinePlacement" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "InitialGap" ) ) {
                contn = updateOrContinue( in, "InitialGap", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.initialGap = Double.parseDouble( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "Gap" ) ) {
                contn = updateOrContinue( in, "Gap", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.gap = Double.parseDouble( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "GeneralizeLine" ) ) {
                contn = updateOrContinue( in, "GeneralizeLine", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.generalizeLine = Boolean.parseBoolean( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "IsAligned" ) ) {
                contn = updateOrContinue( in, "IsAligned", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.isAligned = Boolean.parseBoolean( val );

                    }
                }, contn );
            }

            if ( in.getLocalName().equals( "IsRepeated" ) ) {
                contn = updateOrContinue( in, "IsRepeated", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.repeat = Boolean.parseBoolean( val );

                    }
                }, contn );
            }
        }

        return new Pair<LinePlacement, Continuation<LinePlacement>>( baseOrEvaluated, contn );
    }

    /**
     * @param in
     * @return null, if no symbolizer and no feature type style was found
     * @throws XMLStreamException
     */
    public static org.deegree.rendering.r2d.se.unevaluated.Style parse( XMLStreamReader in )
                            throws XMLStreamException {
        if ( in.getLocalName().endsWith( "Symbolizer" ) ) {
            Pair<Symbolizer<?>, Continuation<StringBuffer>> pair = parseSymbolizer( in );
            return new org.deegree.rendering.r2d.se.unevaluated.Style( pair.first, pair.second );
        }
        if ( in.getLocalName().equals( "FeatureTypeStyle" ) ) {
            return parseFeatureTypeStyle( in );
        }
        return null;
    }

    /**
     * @param in
     * @return a new style
     * @throws XMLStreamException
     */
    public static org.deegree.rendering.r2d.se.unevaluated.Style parseFeatureTypeStyle( XMLStreamReader in )
                            throws XMLStreamException {
        LinkedList<Continuation<LinkedList<Symbolizer<?>>>> result = new LinkedList<Continuation<LinkedList<Symbolizer<?>>>>();
        HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();
        // TODO name, description, ftname, semantictypeidentifier, online resource

        in.require( START_ELEMENT, SLDNS, "FeatureTypeStyle" );

        while ( !( in.isEndElement() && in.getLocalName().equals( "FeatureTypeStyle" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Rule" ) ) {
                Filter filter = null;
                LinkedList<Symbolizer<?>> syms = new LinkedList<Symbolizer<?>>();

                while ( !( in.isEndElement() && in.getLocalName().equals( "Rule" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Name" ) ) {
                        in.next();
                        // name = in.getText();
                        in.nextTag();
                        in.require( END_ELEMENT, SLDNS, "Name" );
                    }

                    if ( in.getLocalName().equals( "Filter" ) ) {
                        filter = Filter110XMLDecoder.parse( in );
                    }

                    if ( in.getLocalName().equals( "ElseFilter" ) ) {
                        filter = ELSEFILTER;
                        in.nextTag();
                    }

                    // TODO description, legendgraphic, scales
                    if ( in.getLocalName().endsWith( "Symbolizer" ) ) {
                        Pair<Symbolizer<?>, Continuation<StringBuffer>> parsedSym = parseSymbolizer( in );
                        if ( parsedSym.second != null ) {
                            labels.put( (Symbolizer) parsedSym.first, parsedSym.second );
                        }
                        syms.add( parsedSym.first );
                    }
                }

                result.add( new FilterContinuation( filter, syms ) );
            }
        }

        return new org.deegree.rendering.r2d.se.unevaluated.Style( result, labels );
    }

    static class ElseFilter implements Filter {
        @Override
        public boolean evaluate( MatchableObject object )
                                throws FilterEvaluationException {
            return false; // always to false, has to be checked differently, see FilterContinuation below
        }

        @Override
        public Type getType() {
            return null;
        }
    }

    static class FilterContinuation extends Continuation<LinkedList<Symbolizer<?>>> {
        private Filter filter;

        private LinkedList<Symbolizer<?>> syms;

        FilterContinuation( Filter filter, LinkedList<Symbolizer<?>> syms ) {
            this.filter = filter;
            this.syms = syms;
        }

        @Override
        public void updateStep( LinkedList<Symbolizer<?>> base, Feature f ) {
            try {
                if ( filter == null || filter.evaluate( f ) || ( base.isEmpty() && filter == ELSEFILTER ) ) {
                    base.addAll( syms );
                }
            } catch ( FilterEvaluationException e ) {
                LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), filter.toString() );
            }
        }

    }

}
