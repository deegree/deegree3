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

import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.TYPE1_FONT;
import static java.awt.Font.createFont;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.commons.xml.CommonNamespaces.SENS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsBoolean;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsQName;
import static org.deegree.commons.xml.stax.StAXParsingHelper.resolve;
import static org.deegree.commons.xml.stax.StAXParsingHelper.skipElement;
import static org.deegree.filter.xml.Filter110XMLDecoder.parseExpression;
import static org.deegree.rendering.i18n.Messages.get;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;
import static org.deegree.rendering.r2d.styling.components.Stroke.LineCap.BUTT;
import static org.deegree.rendering.r2d.styling.components.Stroke.LineJoin.ROUND;
import static org.deegree.rendering.r2d.styling.components.UOM.Foot;
import static org.deegree.rendering.r2d.styling.components.UOM.Metre;
import static org.deegree.rendering.r2d.styling.components.UOM.Pixel;
import static org.deegree.rendering.r2d.styling.components.UOM.mm;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.impl.dv.util.Base64;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.custom.se.Categorize;
import org.deegree.filter.expression.custom.se.Interpolate;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.se.unevaluated.Continuation.Updater;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.RasterChannelSelection;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.RasterStyling.ContrastEnhancement;
import org.deegree.rendering.r2d.styling.RasterStyling.Overlap;
import org.deegree.rendering.r2d.styling.RasterStyling.ShadedRelief;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Font;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.Halo;
import org.deegree.rendering.r2d.styling.components.LinePlacement;
import org.deegree.rendering.r2d.styling.components.Mark;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.UOM;
import org.deegree.rendering.r2d.styling.components.Font.Style;
import org.deegree.rendering.r2d.styling.components.Mark.SimpleMark;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType.Substraction;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType.Type;
import org.deegree.rendering.r2d.styling.components.Stroke.LineCap;
import org.deegree.rendering.r2d.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>SymbologyParser</code> parses the SE part of 1.1.0 and the corresponding SLD 1.0.0 part.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SymbologyParser {

    private boolean collectXMLSnippets = false;

    static final Logger LOG = getLogger( SymbologyParser.class );

    /**
     * A static elsefilter instance (think of it as a marker).
     */
    public static final ElseFilter ELSEFILTER = new ElseFilter();

    /**
     * A default instance.
     */
    public static final SymbologyParser INSTANCE = new SymbologyParser();

    /**
     * Constructs one which does not collect source snippets.
     */
    public SymbologyParser() {
        // default values
    }

    /**
     * @param collectXMLSnippets
     *            if true, some source snippets are collected (which can be used for re-export)
     */
    public SymbologyParser( boolean collectXMLSnippets ) {
        this.collectXMLSnippets = collectXMLSnippets;
    }

    private static boolean require( XMLStreamReader in, String elementName ) {
        if ( !( in.getLocalName().equals( elementName ) && in.isStartElement() ) ) {
            Location loc = in.getLocation();
            LOG.error( "Expected a '{}' element at line {} column {}.", new Object[] { elementName,
                                                                                      loc.getLineNumber(),
                                                                                      loc.getColumnNumber() } );
            return false;
        }
        return true;
    }

    /**
     * @param in
     * @return the resolved href attribute
     * @throws XMLStreamException
     * @throws MalformedURLException
     */
    public static URL parseOnlineResource( XMLStreamReader in )
                            throws XMLStreamException, MalformedURLException {
        if ( !require( in, "OnlineResource" ) ) {
            return null;
        }
        String url = in.getAttributeValue( null, "href" );
        URL resolved = StAXParsingHelper.resolve( url, in );
        in.nextTag();
        in.require( END_ELEMENT, null, "OnlineResource" );
        return resolved;
    }

    private static void checkCommon( Common common, XMLStreamReader in )
                            throws XMLStreamException {
        if ( in.getLocalName().equals( "Name" ) ) {
            common.name = in.getElementText();
        }
        Location l = in.getLocation();
        if ( in.getLocalName().equals( "Geometry" ) ) {
            common.loc = l.getSystemId();
            common.line = l.getLineNumber();
            common.col = l.getColumnNumber();
            in.nextTag();
            common.geometry = parseExpression( in );
            in.nextTag();
            in.require( END_ELEMENT, null, "Geometry" );
        }
        if ( in.getLocalName().equals( "Description" ) ) {
            while ( !( in.isEndElement() && in.getLocalName().equals( "Description" ) ) ) {
                in.nextTag();
                if ( in.getLocalName().equals( "Title" ) ) {
                    common.title = in.getElementText();
                } else if ( in.getLocalName().equals( "Abstract" ) ) {
                    common.abstract_ = in.getElementText();
                } else if ( in.isStartElement() ) {
                    Location loc = l;
                    LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                               new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                    skipElement( in );
                }
            }
        }
        // in case of SLD 1.0.0:
        if ( in.getLocalName().equals( "Title" ) ) {
            common.title = in.getElementText();
            in.nextTag();
        }
        if ( in.getLocalName().equals( "Abstract" ) ) {
            common.abstract_ = in.getElementText();
            in.nextTag();
        }
    }

    private Pair<Fill, Continuation<Fill>> parseFill( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Fill" );

        Fill base = new Fill();
        Continuation<Fill> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Fill" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "GraphicFill" ) ) {
                in.nextTag();
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
                if ( pair != null ) {
                    base.graphic = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Fill>( contn ) {
                            @Override
                            public void updateStep( Fill base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                pair.second.evaluate( base.graphic, f, evaluator );
                            }
                        };
                    }
                }
                in.nextTag();
            } else if ( in.getLocalName().endsWith( "Parameter" ) ) {
                String cssName = in.getAttributeValue( null, "name" );
                if ( cssName.equals( "fill" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep alpha value
                            int alpha = obj.color.getAlpha();
                            obj.color = decodeWithAlpha( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn ).second;
                }

                if ( cssName.equals( "fill-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Fill>() {
                        @Override
                        public void update( Fill obj, String val ) {
                            // keep original color
                            float alpha = max( 0, min( 1, parseFloat( val ) ) );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn ).second;
                }
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }

        }

        in.require( END_ELEMENT, null, "Fill" );

        return new Pair<Fill, Continuation<Fill>>( base, contn );
    }

    private Pair<Stroke, Continuation<Stroke>> parseStroke( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Stroke" );

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
                            obj.color = decodeWithAlpha( val );
                            obj.color = new Color( obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha );
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-opacity" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // keep original color
                            float alpha = Float.parseFloat( val );
                            float[] cols = obj.color.getRGBColorComponents( null );
                            obj.color = new Color( cols[0], cols[1], cols[2], alpha );
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-width" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.width = Double.parseDouble( val );
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-linejoin" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            try {
                                obj.linejoin = LineJoin.valueOf( val.toUpperCase() );
                            } catch ( IllegalArgumentException e ) {
                                LOG.warn( "Used invalid value '{}' for line join.", val );
                                obj.linejoin = ROUND;
                            }
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-linecap" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            try {
                                obj.linecap = LineCap.valueOf( val.toUpperCase() );
                            } catch ( IllegalArgumentException e ) {
                                LOG.warn( "Used invalid value '{}' for line cap.", val );
                                obj.linecap = BUTT;
                            }
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-dasharray" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            // , is not strictly allowed, but we don't lose anything by being flexible
                            if ( val.contains( "," ) ) {
                                obj.dasharray = splitAsDoubles( val, "," );
                            } else {
                                obj.dasharray = splitAsDoubles( val, " " );
                            }
                        }
                    }, contn ).second;
                } else if ( name.equals( "stroke-dashoffset" ) ) {
                    contn = updateOrContinue( in, "Parameter", base, new Updater<Stroke>() {
                        @Override
                        public void update( Stroke obj, String val ) {
                            obj.dashoffset = Double.parseDouble( val );
                        }
                    }, contn ).second;
                } else {
                    Location loc = in.getLocation();
                    LOG.error( "Found unknown parameter '{}' at line {}, column {}, skipping.",
                               new Object[] { name, loc.getLineNumber(), loc.getColumnNumber() } );
                    skipElement( in );
                }

                in.require( END_ELEMENT, null, null );
            } else if ( in.getLocalName().equals( "GraphicFill" ) ) {
                in.nextTag();
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );
                if ( pair != null ) {
                    base.fill = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Stroke>( contn ) {
                            @Override
                            public void updateStep( Stroke base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                pair.second.evaluate( base.fill, f, evaluator );
                            }
                        };
                    }
                }
                in.require( END_ELEMENT, null, "Graphic" );
                in.nextTag();
                in.require( END_ELEMENT, null, "GraphicFill" );
            } else if ( in.getLocalName().equals( "GraphicStroke" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "GraphicStroke" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Graphic" ) ) {
                        final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

                        if ( pair != null ) {
                            base.stroke = pair.first;
                            if ( pair.second != null ) {
                                contn = new Continuation<Stroke>( contn ) {
                                    @Override
                                    public void updateStep( Stroke base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                        pair.second.evaluate( base.stroke, f, evaluator );
                                    }
                                };
                            }
                        }

                        in.require( END_ELEMENT, null, "Graphic" );
                    } else if ( in.getLocalName().equals( "InitialGap" ) ) {
                        contn = updateOrContinue( in, "InitialGap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeInitialGap = Double.parseDouble( val );
                            }
                        }, contn ).second;
                        in.require( END_ELEMENT, null, "InitialGap" );
                    } else if ( in.getLocalName().equals( "Gap" ) ) {
                        contn = updateOrContinue( in, "Gap", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.strokeGap = Double.parseDouble( val );
                            }
                        }, contn ).second;
                        in.require( END_ELEMENT, null, "Gap" );
                    } else if ( in.getLocalName().equals( "PositionPercentage" ) ) {
                        contn = updateOrContinue( in, "PositionPercentage", base, new Updater<Stroke>() {
                            @Override
                            public void update( Stroke obj, String val ) {
                                obj.positionPercentage = Double.parseDouble( val );
                            }
                        }, contn ).second;
                        in.require( END_ELEMENT, null, "PositionPercentage" );
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }

                }
            } else if ( in.isStartElement() ) {
                LOG.error( "Found unknown element '{}', skipping.", in.getLocalName() );
                skipElement( in );
            }
        }

        in.require( END_ELEMENT, null, "Stroke" );

        return new Pair<Stroke, Continuation<Stroke>>( base, contn );
    }

    private Pair<Mark, Continuation<Mark>> parseMark( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Mark" );

        Mark base = new Mark();
        Continuation<Mark> contn = null;

        in.nextTag();

        while ( !( in.isEndElement() && in.getLocalName().equals( "Mark" ) ) ) {
            if ( in.isEndElement() ) {
                in.nextTag();
            }

            if ( in.getLocalName().equals( "WellKnownName" ) ) {
                String wkn = in.getElementText();
                try {
                    base.wellKnown = SimpleMark.valueOf( wkn.toUpperCase() );
                } catch ( IllegalArgumentException e ) {
                    LOG.warn( "Specified unsupported WellKnownName of '{}', using square instead.", wkn );
                    base.wellKnown = SimpleMark.SQUARE;
                }
            } else
                sym: if ( in.getLocalName().equals( "OnlineResource" ) || in.getLocalName().equals( "InlineContent" ) ) {
                    LOG.debug( "Loading mark from external file." );
                    Triple<InputStream, String, Continuation<StringBuffer>> pair = getOnlineResourceOrInlineContent( in );
                    if ( pair == null ) {
                        in.nextTag();
                        break sym;
                    }
                    InputStream is = pair.first;
                    in.nextTag();

                    in.require( START_ELEMENT, null, "Format" );
                    String format = in.getElementText();
                    in.require( END_ELEMENT, null, "Format" );

                    in.nextTag();
                    if ( in.getLocalName().equals( "MarkIndex" ) ) {
                        base.markIndex = Integer.parseInt( in.getElementText() );
                    }

                    if ( is != null ) {
                        try {
                            java.awt.Font font = null;
                            if ( format.equalsIgnoreCase( "ttf" ) ) {
                                font = createFont( TRUETYPE_FONT, is );
                            }
                            if ( format.equalsIgnoreCase( "type1" ) ) {
                                font = createFont( TYPE1_FONT, is );
                            }

                            if ( format.equalsIgnoreCase( "svg" ) ) {
                                base.shape = RenderHelper.getShapeFromSvg( is, pair.second );
                            }

                            if ( font == null && base.shape == null ) {
                                LOG.warn( "Mark was not loaded, because the format '{}' is not supported.", format );
                                break sym;
                            }

                            if ( font != null && base.markIndex >= font.getNumGlyphs() - 1 ) {
                                LOG.warn( "The font only contains {} glyphs, but the index given was {}.",
                                          font.getNumGlyphs(), base.markIndex );
                                break sym;
                            }

                            base.font = font;
                        } catch ( FontFormatException e ) {
                            LOG.debug( "Stack trace:", e );
                            LOG.warn( "The file was not a valid '{}' file: '{}'", format, e.getLocalizedMessage() );
                        } catch ( IOException e ) {
                            LOG.debug( "Stack trace:", e );
                            LOG.warn( "The file could not be read: '{}'.", e.getLocalizedMessage() );
                        }
                    }
                } else if ( in.getLocalName().equals( "Fill" ) ) {
                    final Pair<Fill, Continuation<Fill>> fill = parseFill( in );
                    base.fill = fill.first;
                    if ( fill.second != null ) {
                        contn = new Continuation<Mark>( contn ) {
                            @Override
                            public void updateStep( Mark base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                fill.second.evaluate( base.fill, f, evaluator );
                            }
                        };
                    }
                } else if ( in.getLocalName().equals( "Stroke" ) ) {
                    final Pair<Stroke, Continuation<Stroke>> stroke = parseStroke( in );
                    base.stroke = stroke.first;
                    if ( stroke.second != null ) {
                        contn = new Continuation<Mark>( contn ) {
                            @Override
                            public void updateStep( Mark base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                stroke.second.evaluate( base.stroke, f, evaluator );
                            }
                        };
                    }
                } else if ( in.isStartElement() ) {
                    Location loc = in.getLocation();
                    LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                               new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                    skipElement( in );
                }
        }

        in.require( END_ELEMENT, null, "Mark" );

        return new Pair<Mark, Continuation<Mark>>( base, contn );
    }

    private Triple<InputStream, String, Continuation<StringBuffer>> getOnlineResourceOrInlineContent( XMLStreamReader in )
                            throws XMLStreamException {
        if ( in.getLocalName().equals( "OnlineResource" ) ) {
            String str = in.getAttributeValue( XLNNS, "href" );

            if ( str == null ) {
                Continuation<StringBuffer> contn = updateOrContinue( in, "OnlineResource", new StringBuffer(),
                                                                     SBUPDATER, null ).second;
                return new Triple<InputStream, String, Continuation<StringBuffer>>( null, null, contn );
            }

            String strUrl = null;
            try {
                URL url = resolve( str, in );
                strUrl = url.toExternalForm();
                LOG.debug( "Loading from URL '{}'", url );
                in.nextTag();
                return new Triple<InputStream, String, Continuation<StringBuffer>>( url.openStream(), strUrl, null );
            } catch ( IOException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Could not retrieve content at URL '{}'.", str );
                return null;
            }
        } else if ( in.getLocalName().equals( "InlineContent" ) ) {
            String format = in.getAttributeValue( null, "encoding" );
            if ( format.equalsIgnoreCase( "base64" ) ) {
                ByteArrayInputStream bis = new ByteArrayInputStream( Base64.decode( in.getElementText() ) );
                return new Triple<InputStream, String, Continuation<StringBuffer>>( bis, null, null );
            }
            if ( format.equalsIgnoreCase( "xml" ) ) {
                // TODO
            }
        } else if ( in.isStartElement() ) {
            Location loc = in.getLocation();
            LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                       new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
            skipElement( in );
        }

        return null;
    }

    private Triple<BufferedImage, String, Continuation<List<BufferedImage>>> parseExternalGraphic(
                                                                                                   final XMLStreamReader in )
                            throws IOException, XMLStreamException {
        // TODO color replacement

        in.require( START_ELEMENT, null, "ExternalGraphic" );

        String format = null;
        BufferedImage img = null;
        String url = null;
        Triple<InputStream, String, Continuation<StringBuffer>> pair = null;
        Continuation<List<BufferedImage>> contn = null; // needs to be list to be updateable by reference...

        while ( !( in.isEndElement() && in.getLocalName().equals( "ExternalGraphic" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Format" ) ) {
                format = in.getElementText();
            } else if ( in.getLocalName().equals( "OnlineResource" ) || in.getLocalName().equals( "InlineContent" ) ) {
                pair = getOnlineResourceOrInlineContent( in );
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        if ( pair != null ) {
            if ( pair.first != null && format != null && !format.equalsIgnoreCase( "image/svg" ) ) {
                img = ImageIO.read( pair.first );
            }
            url = pair.second;

            final Continuation<StringBuffer> sbcontn = pair.third;

            if ( pair.third != null ) {
                final LinkedHashMap<String, BufferedImage> cache = new LinkedHashMap<String, BufferedImage>( 256 ) {
                    private static final long serialVersionUID = -6847956873232942891L;

                    @Override
                    protected boolean removeEldestEntry( Map.Entry<String, BufferedImage> eldest ) {
                        return size() > 256; // yeah, hardcoded max size... TODO
                    }
                };
                contn = new Continuation<List<BufferedImage>>() {
                    @Override
                    public void updateStep( List<BufferedImage> base, Feature f, XPathEvaluator<Feature> evaluator ) {
                        StringBuffer sb = new StringBuffer();
                        sbcontn.evaluate( sb, f, evaluator );
                        String file = sb.toString();
                        if ( cache.containsKey( file ) ) {
                            base.add( cache.get( file ) );
                            return;
                        }
                        try {
                            BufferedImage i = ImageIO.read( resolve( file, in ) );
                            base.add( i );
                            cache.put( file, i );
                        } catch ( MalformedURLException e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch ( IOException e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };
            }
        }

        return new Triple<BufferedImage, String, Continuation<List<BufferedImage>>>( img, url, contn );
    }

    private Pair<Graphic, Continuation<Graphic>> parseGraphic( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Graphic" );

        Graphic base = new Graphic();
        Continuation<Graphic> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Graphic" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Mark" ) ) {
                final Pair<Mark, Continuation<Mark>> pair = parseMark( in );

                if ( pair != null ) {
                    base.mark = pair.first;
                    if ( pair.second != null ) {
                        contn = new Continuation<Graphic>( contn ) {
                            @Override
                            public void updateStep( Graphic base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                pair.second.evaluate( base.mark, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "ExternalGraphic" ) ) {
                try {
                    final Triple<BufferedImage, String, Continuation<List<BufferedImage>>> p = parseExternalGraphic( in );
                    if ( p.third != null ) {
                        contn = new Continuation<Graphic>( contn ) {
                            @Override
                            public void updateStep( Graphic base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                LinkedList<BufferedImage> list = new LinkedList<BufferedImage>();
                                p.third.evaluate( list, f, evaluator );
                                base.image = list.poll();
                            }
                        };
                    } else {
                        base.image = p.first;
                        base.imageURL = p.second;
                    }
                } catch ( IOException e ) {
                    LOG.debug( "Stack trace", e );
                    LOG.warn( get( "R2D.EXTERNAL_GRAPHIC_NOT_LOADED" ),
                              new Object[] { in.getLocation().getLineNumber(), in.getLocation().getColumnNumber(),
                                            in.getLocation().getSystemId() } );
                }
            } else if ( in.getLocalName().equals( "Opacity" ) ) {
                contn = updateOrContinue( in, "Opacity", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.opacity = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.getLocalName().equals( "Size" ) ) {
                contn = updateOrContinue( in, "Size", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.size = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.getLocalName().equals( "Rotation" ) ) {
                contn = updateOrContinue( in, "Rotation", base, new Updater<Graphic>() {
                    public void update( Graphic obj, String val ) {
                        obj.rotation = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.getLocalName().equals( "AnchorPoint" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "AnchorPoint" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "AnchorPointX" ) ) {
                        contn = updateOrContinue( in, "AnchorPointX", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.anchorPointX = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.getLocalName().equals( "AnchorPointY" ) ) {
                        contn = updateOrContinue( in, "AnchorPointY", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.anchorPointY = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }
                }
            } else if ( in.getLocalName().equals( "Displacement" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                        contn = updateOrContinue( in, "DisplacementX", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.displacementX = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.getLocalName().equals( "DisplacementY" ) ) {
                        contn = updateOrContinue( in, "DisplacementY", base, new Updater<Graphic>() {
                            public void update( Graphic obj, String val ) {
                                obj.displacementY = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }
                }
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }
        in.require( END_ELEMENT, null, "Graphic" );

        return new Pair<Graphic, Continuation<Graphic>>( base, contn );
    }

    /**
     * @param in
     * @param uom
     * @return a new symbolizer
     * @throws XMLStreamException
     */
    public Symbolizer<PointStyling> parsePointSymbolizer( XMLStreamReader in, UOM uom )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "PointSymbolizer" );

        Common common = new Common( in.getLocation() );
        PointStyling baseOrEvaluated = new PointStyling();
        baseOrEvaluated.uom = uom;

        while ( !( in.isEndElement() && in.getLocalName().equals( "PointSymbolizer" ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            if ( in.getLocalName().equals( "Graphic" ) ) {
                final Pair<Graphic, Continuation<Graphic>> pair = parseGraphic( in );

                if ( pair == null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, common.geometry, common.name, common.loc,
                                                         common.line, common.col );
                }

                baseOrEvaluated.graphic = pair.first;

                if ( pair.second != null ) {
                    return new Symbolizer<PointStyling>( baseOrEvaluated, new Continuation<PointStyling>() {
                        @Override
                        public void updateStep( PointStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                            pair.second.evaluate( base.graphic, f, evaluator );
                        }
                    }, common.geometry, null, common.loc, common.line, common.col );
                }
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        in.require( END_ELEMENT, null, "PointSymbolizer" );
        return new Symbolizer<PointStyling>( baseOrEvaluated, common.geometry, common.name, common.loc, common.line,
                                             common.col );
    }

    static UOM getUOM( String uom ) {
        if ( uom != null ) {
            String u = uom.toLowerCase();
            if ( u.endsWith( "metre" ) || u.endsWith( "meter" ) ) {
                return Metre;
            } else if ( u.endsWith( "mm" ) ) {
                return mm;
            } else if ( u.endsWith( "foot" ) ) {
                return Foot;
            } else if ( !u.endsWith( "pixel" ) ) {
                LOG.warn( "Unknown unit of measure '{}', using pixel instead.", uom );
            }
        }

        return Pixel;
    }

    /**
     * @param in
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public Triple<Symbolizer<?>, Continuation<StringBuffer>, String> parseSymbolizer( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, null );
        if ( in.getLocalName().endsWith( "Symbolizer" ) ) {
            UOM uom = getUOM( in.getAttributeValue( null, "uom" ) );

            if ( in.getLocalName().equals( "PointSymbolizer" ) ) {
                return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>( parsePointSymbolizer( in, uom ),
                                                                                      null, null );
            }
            if ( in.getLocalName().equals( "LineSymbolizer" ) ) {
                return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>( parseLineSymbolizer( in, uom ),
                                                                                      null, null );
            }
            if ( in.getLocalName().equals( "PolygonSymbolizer" ) ) {
                return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>(
                                                                                      parsePolygonSymbolizer( in, uom ),
                                                                                      null, null );
            }
            if ( in.getLocalName().equals( "RasterSymbolizer" ) ) {
                return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>( parseRasterSymbolizer( in, uom ),
                                                                                      null, null );
            }
            if ( in.getLocalName().equals( "TextSymbolizer" ) ) {
                return (Triple) parseTextSymbolizer( in, uom );
            }
            Location loc = in.getLocation();
            LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                       new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
            skipElement( in );
        }
        return null;
    }

    /**
     * @param in
     * @param uom
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public Symbolizer<RasterStyling> parseRasterSymbolizer( XMLStreamReader in, UOM uom )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "RasterSymbolizer" );

        Common common = new Common( in.getLocation() );
        RasterStyling baseOrEvaluated = new RasterStyling();
        baseOrEvaluated.uom = uom;
        Continuation<RasterStyling> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "RasterSymbolizer" ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            if ( in.getLocalName().equals( "Opacity" ) ) {
                contn = updateOrContinue( in, "Opacity", baseOrEvaluated, new Updater<RasterStyling>() {
                    @Override
                    public void update( RasterStyling obj, String val ) {
                        obj.opacity = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.getLocalName().equals( "ChannelSelection" ) ) {
                String red = null, green = null, blue = null, gray = null;
                HashMap<String, ContrastEnhancement> enhancements = new HashMap<String, ContrastEnhancement>( 10 );

                while ( !( in.isEndElement() && in.getLocalName().equals( "ChannelSelection" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "RedChannel" ) ) {
                        in.nextTag();
                        in.require( START_ELEMENT, null, "SourceChannelName" );
                        red = in.getElementText();
                        in.nextTag();
                        ContrastEnhancement enh = parseContrastEnhancement( in );
                        if ( enh != null ) {
                            enhancements.put( "red", enh );
                        }
                        in.nextTag();
                    } else if ( in.getLocalName().equals( "GreenChannel" ) ) {
                        in.nextTag();
                        in.require( START_ELEMENT, null, "SourceChannelName" );
                        green = in.getElementText();
                        in.nextTag();
                        ContrastEnhancement enh = parseContrastEnhancement( in );
                        if ( enh != null ) {
                            enhancements.put( "green", enh );
                        }
                        in.nextTag();
                    } else if ( in.getLocalName().equals( "BlueChannel" ) ) {
                        in.nextTag();
                        in.require( START_ELEMENT, null, "SourceChannelName" );
                        blue = in.getElementText();
                        in.nextTag();
                        ContrastEnhancement enh = parseContrastEnhancement( in );
                        if ( enh != null ) {
                            enhancements.put( "blue", enh );
                        }
                        in.nextTag();
                    } else if ( in.getLocalName().equals( "GrayChannel" ) ) {
                        in.nextTag();
                        in.require( START_ELEMENT, null, "SourceChannelName" );
                        gray = in.getElementText();
                        in.nextTag();
                        ContrastEnhancement enh = parseContrastEnhancement( in );
                        if ( enh != null ) {
                            enhancements.put( "gray", enh );
                        }
                        in.nextTag();
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }
                }

                baseOrEvaluated.channelSelection = new RasterChannelSelection( red, green, blue, gray, enhancements );
            } else if ( in.getLocalName().equals( "OverlapBehavior" ) ) {
                // actual difference between SLD 1.0.0/SE 1.1.0
                if ( in.getNamespaceURI().equals( SENS ) ) {
                    baseOrEvaluated.overlap = Overlap.valueOf( in.getElementText() );
                } else {
                    in.nextTag();
                    baseOrEvaluated.overlap = Overlap.valueOf( in.getLocalName() );
                    in.nextTag();
                    in.nextTag();
                }
            } else if ( in.getLocalName().equals( "ColorMap" ) ) {
                if ( in.getNamespaceURI().equals( SENS ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Categorize" ) ) {
                        baseOrEvaluated.categorize = new Categorize().parse( in );
                    } else if ( in.getLocalName().equals( "Interpolate" ) ) {
                        baseOrEvaluated.interpolate = new Interpolate().parse( in );
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }

                    in.nextTag();
                } else {
                    baseOrEvaluated.interpolate = Interpolate.parseSLD100( in );
                }
            } else if ( in.getLocalName().equals( "ContrastEnhancement" ) ) {
                baseOrEvaluated.contrastEnhancement = parseContrastEnhancement( in );
            } else if ( in.getLocalName().equals( "ShadedRelief" ) ) {
                baseOrEvaluated.shaded = new ShadedRelief();
                while ( !( in.isEndElement() && in.getLocalName().equals( "ShadedRelief" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "BrightnessOnly" ) ) {
                        baseOrEvaluated.shaded.brightnessOnly = getElementTextAsBoolean( in );
                    }
                    if ( in.getLocalName().equals( "ReliefFactor" ) ) {
                        baseOrEvaluated.shaded.reliefFactor = parseDouble( in.getElementText() );
                    }
                    if ( in.getLocalName().equals( "AzimuthAngle" ) ) {
                        baseOrEvaluated.shaded.azimuthAngle = parseDouble( in.getElementText() );
                    }
                    if ( in.getLocalName().equals( "IlluminationAngle" ) ) {
                        baseOrEvaluated.shaded.alt = parseDouble( in.getElementText() );
                    }
                }
            } else if ( in.getLocalName().equals( "ImageOutline" ) ) {
                in.nextTag();
                if ( in.getLocalName().equals( "LineSymbolizer" ) ) {
                    baseOrEvaluated.imageOutline = parseLineSymbolizer( in,
                                                                        getUOM( in.getAttributeValue( null, "uom" ) ) );
                }
                if ( in.getLocalName().equals( "PolygonSymbolizer" ) ) {
                    baseOrEvaluated.imageOutline = parsePolygonSymbolizer( in, getUOM( in.getAttributeValue( null,
                                                                                                             "uom" ) ) );
                }
                in.nextTag();
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        in.require( END_ELEMENT, null, "RasterSymbolizer" );
        return new Symbolizer<RasterStyling>( baseOrEvaluated, contn, common.geometry, common.name, common.loc,
                                              common.line, common.col );
    }

    private ContrastEnhancement parseContrastEnhancement( XMLStreamReader in )
                            throws XMLStreamException {
        if ( !in.getLocalName().equals( "ContrastEnhancement" ) ) {
            return null;
        }

        ContrastEnhancement base = new ContrastEnhancement();

        while ( !( in.isEndElement() && in.getLocalName().equals( "ContrastEnhancement" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "Normalize" ) ) {
                in.nextTag();
                base.normalize = true;
            } else if ( in.getLocalName().equals( "Histogram" ) ) {
                base.histogram = true;
            } else if ( in.getLocalName().equals( "GammaValue" ) ) {
                base.gamma = parseDouble( in.getElementText() );
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        return base;
    }

    /**
     * @param in
     * @param uom
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public Symbolizer<LineStyling> parseLineSymbolizer( XMLStreamReader in, UOM uom )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "LineSymbolizer" );

        Common common = new Common( in.getLocation() );
        LineStyling baseOrEvaluated = new LineStyling();
        baseOrEvaluated.uom = uom;
        Continuation<LineStyling> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "LineSymbolizer" ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( in );

                if ( pair != null ) {
                    baseOrEvaluated.stroke = pair.first;

                    if ( pair.second != null ) {
                        contn = new Continuation<LineStyling>( contn ) {
                            @Override
                            public void updateStep( LineStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                pair.second.evaluate( base.stroke, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType( in );
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<LineStyling>() {
                    @Override
                    public void update( LineStyling obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        if ( contn == null ) {
            return new Symbolizer<LineStyling>( baseOrEvaluated, common.geometry, common.name, common.loc, common.line,
                                                common.col );
        }

        return new Symbolizer<LineStyling>( baseOrEvaluated, contn, common.geometry, common.name, common.loc,
                                            common.line, common.col );
    }

    /**
     * @param in
     * @param uom
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public Symbolizer<PolygonStyling> parsePolygonSymbolizer( XMLStreamReader in, UOM uom )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "PolygonSymbolizer" );

        Common common = new Common( in.getLocation() );
        PolygonStyling baseOrEvaluated = new PolygonStyling();
        baseOrEvaluated.uom = uom;
        Continuation<PolygonStyling> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "PolygonSymbolizer" ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            if ( in.getLocalName().equals( "Stroke" ) ) {
                final Pair<Stroke, Continuation<Stroke>> pair = parseStroke( in );

                if ( pair != null ) {
                    baseOrEvaluated.stroke = pair.first;

                    if ( pair.second != null ) {
                        contn = new Continuation<PolygonStyling>( contn ) {
                            @Override
                            public void updateStep( PolygonStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                pair.second.evaluate( base.stroke, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );

                if ( fillPair != null ) {
                    baseOrEvaluated.fill = fillPair.first;

                    if ( fillPair.second != null ) {
                        contn = new Continuation<PolygonStyling>( contn ) {
                            @Override
                            public void updateStep( PolygonStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                fillPair.second.evaluate( base.fill, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType( in );
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<PolygonStyling>() {
                    @Override
                    public void update( PolygonStyling obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );
                    }
                }, contn ).second;
            } else if ( in.getLocalName().equals( "Displacement" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                        contn = updateOrContinue( in, "DisplacementX", baseOrEvaluated, new Updater<PolygonStyling>() {
                            @Override
                            public void update( PolygonStyling obj, String val ) {
                                obj.displacementX = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.getLocalName().equals( "DisplacementY" ) ) {
                        contn = updateOrContinue( in, "DisplacementY", baseOrEvaluated, new Updater<PolygonStyling>() {
                            @Override
                            public void update( PolygonStyling obj, String val ) {
                                obj.displacementY = Double.parseDouble( val );
                            }
                        }, contn ).second;
                    } else if ( in.isStartElement() ) {
                        Location loc = in.getLocation();
                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                   new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                        skipElement( in );
                    }
                }
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        if ( contn == null ) {
            return new Symbolizer<PolygonStyling>( baseOrEvaluated, common.geometry, common.name, common.loc,
                                                   common.line, common.col );
        }

        return new Symbolizer<PolygonStyling>( baseOrEvaluated, contn, common.geometry, common.name, common.loc,
                                               common.line, common.col );
    }

    /**
     * @param <T>
     * @param in
     * @param name
     * @param obj
     * @param updater
     * @param contn
     * @return either contn, or a new continuation which updates obj, also the XML snippet (w/ filter expressions
     *         re-exported) which was parsed (or null, if none was parsed)
     * @throws XMLStreamException
     */
    public <T> Pair<String, Continuation<T>> updateOrContinue( XMLStreamReader in, String name, T obj,
                                                               final Updater<T> updater, Continuation<T> contn )
                            throws XMLStreamException {
        StringBuilder xmlText = collectXMLSnippets ? new StringBuilder() : null;

        if ( in.getLocalName().endsWith( name ) ) {
            final LinkedList<Pair<String, Pair<Expression, String>>> text = new LinkedList<Pair<String, Pair<Expression, String>>>(); // no
            // real 'alternative', have we?
            boolean textOnly = true;
            while ( !( in.isEndElement() && in.getLocalName().endsWith( name ) ) ) {
                in.next();
                if ( in.isStartElement() ) {
                    Expression expr = parseExpression( in );
                    if ( collectXMLSnippets ) {
                        StringWriter sw = new StringWriter();
                        XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( sw );
                        Filter110XMLEncoder.export( expr, out );
                        xmlText.append( sw.toString() );
                    }
                    Pair<Expression, String> second;
                    second = new Pair<Expression, String>( expr, get( "R2D.LINE", in.getLocation().getLineNumber(),
                                                                      in.getLocation().getColumnNumber(),
                                                                      in.getLocation().getSystemId() ) );
                    text.add( new Pair<String, Pair<Expression, String>>( null, second ) );
                    textOnly = false;
                }
                if ( in.isCharacters() ) {
                    if ( collectXMLSnippets ) {
                        xmlText.append( in.getText() );
                    }
                    if ( textOnly && !text.isEmpty() ) { // concat text in case of multiple text nodes from
                        // beginning
                        String txt = text.removeLast().first;
                        text.add( new Pair<String, Pair<Expression, String>>( txt + in.getText().trim(), null ) );
                    } else {
                        text.add( new Pair<String, Pair<Expression, String>>( in.getText().trim(), null ) );
                    }
                }
            }
            in.require( END_ELEMENT, null, null );

            if ( textOnly ) {
                if ( text.isEmpty() ) {
                    LOG.warn( "Expression was empty at line {}, column {}.", in.getLocation().getLineNumber(),
                              in.getLocation().getColumnNumber() );
                }
                updater.update( obj, text.isEmpty() ? "" : text.getFirst().first );
            } else {
                contn = new Continuation<T>( contn ) {
                    @Override
                    public void updateStep( T base, Feature f, XPathEvaluator<Feature> evaluator ) {
                        String tmp = "";
                        for ( Pair<String, Pair<Expression, String>> p : text ) {
                            if ( p.first != null ) {
                                tmp += p.first;
                            }
                            if ( p.second != null ) {
                                try {
                                    TypedObjectNode[] evald = p.second.first.evaluate( f, evaluator );
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

        return new Pair<String, Continuation<T>>( collectXMLSnippets ? xmlText.toString().trim() : null, contn );
    }

    /**
     * @param in
     * @param uom
     * @return the symbolizer
     * @throws XMLStreamException
     */
    public Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String> parseTextSymbolizer( XMLStreamReader in,
                                                                                                    UOM uom )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "TextSymbolizer" );

        Common common = new Common( in.getLocation() );
        TextStyling baseOrEvaluated = new TextStyling();
        baseOrEvaluated.uom = uom;
        Continuation<TextStyling> contn = null;
        Continuation<StringBuffer> label = null;
        String xmlText = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "TextSymbolizer" ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            if ( in.getLocalName().equals( "Label" ) ) {
                Pair<String, Continuation<StringBuffer>> res = updateOrContinue( in, "Label", new StringBuffer(),
                                                                                 new Updater<StringBuffer>() {
                                                                                     @Override
                                                                                     public void update(
                                                                                                         StringBuffer obj,
                                                                                                         String val ) {
                                                                                         obj.append( val );
                                                                                     }
                                                                                 }, null );
                xmlText = res.first;
                label = res.second;
            } else if ( in.getLocalName().equals( "LabelPlacement" ) ) {
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
                                                                  }, contn ).second;
                                    } else if ( in.getLocalName().equals( "AnchorPointY" ) ) {
                                        contn = updateOrContinue( in, "AnchorPointY", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.anchorPointY = Double.parseDouble( val );
                                                                      }
                                                                  }, contn ).second;
                                    } else if ( in.isStartElement() ) {
                                        Location loc = in.getLocation();
                                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                                   new Object[] { in.getLocalName(), loc.getLineNumber(),
                                                                 loc.getColumnNumber() } );
                                        skipElement( in );
                                    }
                                }
                            } else if ( in.getLocalName().equals( "Displacement" ) ) {
                                while ( !( in.isEndElement() && in.getLocalName().equals( "Displacement" ) ) ) {
                                    in.nextTag();
                                    if ( in.getLocalName().equals( "DisplacementX" ) ) {
                                        contn = updateOrContinue( in, "DisplacementX", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.displacementX = Double.parseDouble( val );
                                                                      }
                                                                  }, contn ).second;
                                    } else if ( in.getLocalName().equals( "DisplacementY" ) ) {
                                        contn = updateOrContinue( in, "DisplacementY", baseOrEvaluated,
                                                                  new Updater<TextStyling>() {
                                                                      @Override
                                                                      public void update( TextStyling obj, String val ) {
                                                                          obj.displacementY = Double.parseDouble( val );
                                                                      }
                                                                  }, contn ).second;
                                    } else if ( in.isStartElement() ) {
                                        Location loc = in.getLocation();
                                        LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                                                   new Object[] { in.getLocalName(), loc.getLineNumber(),
                                                                 loc.getColumnNumber() } );
                                        skipElement( in );
                                    }
                                }
                            } else if ( in.getLocalName().equals( "Rotation" ) ) {
                                contn = updateOrContinue( in, "Rotation", baseOrEvaluated, new Updater<TextStyling>() {
                                    @Override
                                    public void update( TextStyling obj, String val ) {
                                        obj.rotation = Double.parseDouble( val );
                                    }
                                }, contn ).second;
                            } else if ( in.isStartElement() ) {
                                Location loc = in.getLocation();
                                LOG.error(
                                           "Found unknown element '{}' at line {}, column {}, skipping.",
                                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                                skipElement( in );
                            }

                        }
                    }

                    if ( in.getLocalName().equals( "LinePlacement" ) ) {
                        final Pair<LinePlacement, Continuation<LinePlacement>> pair = parseLinePlacement( in );
                        if ( pair != null ) {
                            baseOrEvaluated.linePlacement = pair.first;

                            if ( pair.second != null ) {
                                contn = new Continuation<TextStyling>( contn ) {
                                    @Override
                                    public void updateStep( TextStyling base, Feature f,
                                                            XPathEvaluator<Feature> evaluator ) {
                                        pair.second.evaluate( base.linePlacement, f, evaluator );
                                    }
                                };
                            }
                        }
                    }
                }
            } else if ( in.getLocalName().equals( "Halo" ) ) {
                final Pair<Halo, Continuation<Halo>> haloPair = parseHalo( in );
                if ( haloPair != null ) {
                    baseOrEvaluated.halo = haloPair.first;

                    if ( haloPair.second != null ) {
                        contn = new Continuation<TextStyling>( contn ) {
                            @Override
                            public void updateStep( TextStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                haloPair.second.evaluate( base.halo, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "Font" ) ) {
                final Pair<Font, Continuation<Font>> fontPair = parseFont( in );
                if ( fontPair != null ) {
                    baseOrEvaluated.font = fontPair.first;

                    if ( fontPair.second != null ) {
                        contn = new Continuation<TextStyling>( contn ) {
                            @Override
                            public void updateStep( TextStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                fontPair.second.evaluate( base.font, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );
                if ( fillPair != null ) {
                    baseOrEvaluated.fill = fillPair.first;

                    if ( fillPair.second != null ) {
                        contn = new Continuation<TextStyling>( contn ) {
                            @Override
                            public void updateStep( TextStyling base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                fillPair.second.evaluate( base.fill, f, evaluator );
                            }
                        };
                    }
                }
            } else if ( in.isStartElement() ) {
                Location loc = in.getLocation();
                LOG.error( "Found unknown element '{}' at line {}, column {}, skipping.",
                           new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() } );
                skipElement( in );
            }
        }

        if ( contn == null ) {
            Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, common.geometry, common.name,
                                                                       common.loc, common.line, common.col );
            return new Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String>( sym, label, xmlText );
        }

        Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>( baseOrEvaluated, contn, common.geometry,
                                                                   common.name, common.loc, common.line, common.col );
        return new Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String>( sym, label, xmlText );
    }

    private Pair<Font, Continuation<Font>> parseFont( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Font" );

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
                    }, contn ).second;
                } else if ( name.equals( "font-style" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.fontStyle = Style.valueOf( val.toUpperCase() );
                        }
                    }, contn ).second;
                } else if ( name.equals( "font-weight" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.bold = val.equalsIgnoreCase( "bold" );
                        }
                    }, contn ).second;
                } else if ( name.equals( "font-size" ) ) {
                    contn = updateOrContinue( in, "Parameter", baseOrEvaluated, new Updater<Font>() {
                        @Override
                        public void update( Font obj, String val ) {
                            obj.fontSize = Integer.parseInt( val );
                        }
                    }, contn ).second;
                } else if ( name.equals( "font-color" ) ) {
                    skipElement( in );
                    LOG.warn( "The non-standard font-color Svg/CssParameter is not supported any more. Use a standard Fill element instead." );
                } else {
                    in.getElementText();
                    LOG.warn( "The non-standard '{}' Svg/CssParameter is not supported.", name );
                }
            }
        }

        return new Pair<Font, Continuation<Font>>( baseOrEvaluated, contn );

    }

    private Pair<Halo, Continuation<Halo>> parseHalo( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Halo" );

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
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "Fill" ) ) {
                final Pair<Fill, Continuation<Fill>> fillPair = parseFill( in );

                if ( fillPair != null ) {
                    baseOrEvaluated.fill = fillPair.first;

                    if ( fillPair.second != null ) {
                        contn = new Continuation<Halo>( contn ) {
                            @Override
                            public void updateStep( Halo base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                fillPair.second.evaluate( base.fill, f, evaluator );
                            }
                        };
                    }
                }
            }
        }

        return new Pair<Halo, Continuation<Halo>>( baseOrEvaluated, contn );
    }

    private static PerpendicularOffsetType getPerpendicularOffsetType( XMLStreamReader in ) {
        PerpendicularOffsetType tp = new PerpendicularOffsetType();
        String type = in.getAttributeValue( null, "type" );
        if ( type != null ) {
            try {
                tp.type = Type.valueOf( type );
            } catch ( IllegalArgumentException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "The value '{}' is not a valid type for perpendicular offsets. Valid types are: {}", type,
                          Arrays.toString( Type.values() ) );
            }
        }
        String substraction = in.getAttributeValue( null, "substraction" );
        if ( substraction != null ) {
            try {
                tp.substraction = Substraction.valueOf( substraction );
            } catch ( IllegalArgumentException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "The value '{}' is not a valid substraction type for perpendicular offsets."
                          + " Valid types are: {}", substraction, Arrays.toString( Substraction.values() ) );
            }
        }
        return tp;
    }

    private Pair<LinePlacement, Continuation<LinePlacement>> parseLinePlacement( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "LinePlacement" );

        LinePlacement baseOrEvaluated = new LinePlacement();
        Continuation<LinePlacement> contn = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "LinePlacement" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "PerpendicularOffset" ) ) {
                baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType( in );
                contn = updateOrContinue( in, "PerpendicularOffset", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.perpendicularOffset = Double.parseDouble( val );

                    }
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "InitialGap" ) ) {
                contn = updateOrContinue( in, "InitialGap", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.initialGap = Double.parseDouble( val );

                    }
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "Gap" ) ) {
                contn = updateOrContinue( in, "Gap", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.gap = Double.parseDouble( val );

                    }
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "GeneralizeLine" ) ) {
                contn = updateOrContinue( in, "GeneralizeLine", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.generalizeLine = Boolean.parseBoolean( val );

                    }
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "IsAligned" ) ) {
                contn = updateOrContinue( in, "IsAligned", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.isAligned = Boolean.parseBoolean( val );

                    }
                }, contn ).second;
            }

            if ( in.getLocalName().equals( "IsRepeated" ) ) {
                contn = updateOrContinue( in, "IsRepeated", baseOrEvaluated, new Updater<LinePlacement>() {
                    @Override
                    public void update( LinePlacement obj, String val ) {
                        obj.repeat = Boolean.parseBoolean( val );

                    }
                }, contn ).second;
            }
        }

        return new Pair<LinePlacement, Continuation<LinePlacement>>( baseOrEvaluated, contn );
    }

    /**
     * @param in
     * @return null, if no symbolizer and no Feature type style was found
     * @throws XMLStreamException
     */
    public org.deegree.rendering.r2d.se.unevaluated.Style parse( XMLStreamReader in )
                            throws XMLStreamException {
        if ( in.getEventType() == START_DOCUMENT ) {
            in.nextTag();
        }
        if ( in.getLocalName().endsWith( "Symbolizer" ) ) {
            Triple<Symbolizer<?>, Continuation<StringBuffer>, String> pair = parseSymbolizer( in );
            return new org.deegree.rendering.r2d.se.unevaluated.Style( pair.first, pair.second, pair.first.getName(),
                                                                       pair.third );
        }
        if ( in.getLocalName().equals( "FeatureTypeStyle" ) ) {
            return parseFeatureTypeOrCoverageStyle( in );
        }
        LOG.warn( "Symbology file '{}' did not contain symbolizer or feature type style.",
                  in.getLocation().getSystemId() );
        return null;
    }

    /**
     * @param in
     * @return a new style
     * @throws XMLStreamException
     */
    public org.deegree.rendering.r2d.se.unevaluated.Style parseFeatureTypeOrCoverageStyle( XMLStreamReader in )
                            throws XMLStreamException {
        if ( in.getLocalName().equals( "OnlineResource" ) ) {
            try {
                URL url = SymbologyParser.parseOnlineResource( in );
                XMLStreamReader newReader = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                                 url.openStream() );
                return parseFeatureTypeOrCoverageStyle( newReader );
            } catch ( MalformedURLException e ) {
                LOG.warn( "An URL referencing a FeatureType or CoverageStyle could not be resolved." );
                LOG.debug( "Stack trace:", e );
            } catch ( FactoryConfigurationError e ) {
                LOG.warn( "An URL referencing a FeatureType or CoverageStyle could not be read." );
                LOG.debug( "Stack trace:", e );
            } catch ( IOException e ) {
                LOG.warn( "An URL referencing a FeatureType or CoverageStyle could not be read." );
                LOG.debug( "Stack trace:", e );
            }
        }

        LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> result = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();
        HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();
        HashMap<Symbolizer<TextStyling>, String> labelXMLTexts = collectXMLSnippets ? new HashMap<Symbolizer<TextStyling>, String>()
                                                                                   : null;
        Common common = new Common( in.getLocation() );
        QName featureTypeName = null;

        while ( !( in.isEndElement() && ( in.getLocalName().equals( "FeatureTypeStyle" ) || in.getLocalName().equals(
                                                                                                                      "CoverageStyle" ) ) ) ) {
            in.nextTag();

            checkCommon( common, in );

            // TODO unused
            if ( in.getLocalName().equals( "SemanticTypeIdentifier" ) ) {
                in.getElementText(); // AndThrowItAwayImmediately
            }

            if ( in.getLocalName().equals( "FeatureTypeName" ) ) {
                featureTypeName = getElementTextAsQName( in );
            }

            // TODO unused
            if ( in.getLocalName().equals( "CoverageName" ) ) {
                in.getElementText(); // AndThrowItAwayImmediately
            }

            if ( in.getLocalName().equals( "Rule" ) || in.getLocalName().equals( "OnlineResource" ) ) {
                XMLStreamReader localReader = in;
                if ( in.getLocalName().equals( "OnlineResource" ) ) {
                    try {
                        URL url = parseOnlineResource( in );
                        localReader = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                           url.openStream() );
                    } catch ( IOException e ) {
                        LOG.warn( "Error '{}' while resolving/accessing remote Rule document.", e.getLocalizedMessage() );
                        LOG.debug( "Stack trace:", e );
                    }
                }

                Common ruleCommon = new Common( in.getLocation() );
                double minScale = NEGATIVE_INFINITY;
                double maxScale = POSITIVE_INFINITY;

                Filter filter = null;
                LinkedList<Symbolizer<?>> syms = new LinkedList<Symbolizer<?>>();

                while ( !( localReader.isEndElement() && localReader.getLocalName().equals( "Rule" ) ) ) {
                    localReader.nextTag();

                    checkCommon( ruleCommon, localReader );

                    if ( localReader.getLocalName().equals( "Filter" ) ) {
                        filter = Filter110XMLDecoder.parse( localReader );
                    }

                    if ( localReader.getLocalName().equals( "ElseFilter" ) ) {
                        filter = ELSEFILTER;
                        localReader.nextTag();
                    }

                    if ( localReader.getLocalName().equals( "MinScaleDenominator" ) ) {
                        minScale = parseDouble( localReader.getElementText() );
                    }
                    if ( localReader.getLocalName().equals( "MaxScaleDenominator" ) ) {
                        maxScale = parseDouble( localReader.getElementText() );
                    }

                    // TODO legendgraphic
                    if ( localReader.isStartElement() && localReader.getLocalName().endsWith( "Symbolizer" ) ) {

                        Triple<Symbolizer<?>, Continuation<StringBuffer>, String> parsedSym = parseSymbolizer( localReader );
                        if ( parsedSym.second != null ) {
                            labels.put( (Symbolizer) parsedSym.first, parsedSym.second );
                        }
                        if ( collectXMLSnippets && parsedSym.third != null ) {
                            labelXMLTexts.put( (Symbolizer) parsedSym.first, parsedSym.third );
                        }
                        syms.add( parsedSym.first );
                    }
                }

                FilterContinuation contn = new FilterContinuation( filter, syms, ruleCommon );
                DoublePair scales = new DoublePair( minScale, maxScale );
                result.add( new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>( contn, scales ) );
            }
        }

        return new org.deegree.rendering.r2d.se.unevaluated.Style( result, labels, labelXMLTexts, common.name,
                                                                   featureTypeName );
    }

    static class ElseFilter implements Filter {
        @Override
        public <T> boolean evaluate( T object, XPathEvaluator<T> evaluator )
                                throws FilterEvaluationException {
            return false; // always to false, has to be checked differently, see FilterContinuation below
        }

        @Override
        public Type getType() {
            return null;
        }
    }

    /**
     * <code>FilterContinuation</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class FilterContinuation extends Continuation<LinkedList<Symbolizer<?>>> {
        /***/
        public Filter filter;

        private LinkedList<Symbolizer<?>> syms;

        /** Contains description and so on. */
        public Common common;

        FilterContinuation( Filter filter, LinkedList<Symbolizer<?>> syms, Common common ) {
            this.filter = filter;
            this.syms = syms;
            this.common = common;
        }

        @Override
        public void updateStep( LinkedList<Symbolizer<?>> base, Feature f, XPathEvaluator<Feature> evaluator ) {
            try {
                if ( filter == null || f == null || filter.evaluate( f, evaluator )
                     || ( base.isEmpty() && filter == ELSEFILTER ) ) {
                    base.addAll( syms );
                }
            } catch ( FilterEvaluationException e ) {
                LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), filter.toString() );
                LOG.debug( "Stack trace:", e );
            }
        }

    }

    /**
     * <code>Common</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class Common {
        Common( Location loc ) {
            this.loc = loc.getSystemId();
            line = loc.getLineNumber();
            col = loc.getColumnNumber();
        }

        /***/
        public String name;

        /***/
        public String title;

        /***/
        public String abstract_;

        Expression geometry;

        String loc;

        int line, col;
    }
}