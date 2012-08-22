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
package org.deegree.graphics.sld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.StringTools;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterTools;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;

/**
 * Collects all property names used by a list of styles. E.g. this can be used to optimze GetFeature requests from a WMS
 * against a WFS.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class StyleUtils {

    /**
     * @return a list of all
     * 
     * @param featureType
     * @param styles
     * @param scaleDen
     * @throws PropertyPathResolvingException
     */
    public static List<PropertyPath> extractRequiredProperties( QualifiedName featureType, List<UserStyle> styles,
                                                                double scaleDen )
                            throws PropertyPathResolvingException {
        List<PropertyPath> pp = new ArrayList<PropertyPath>();

        for ( int i = 0; i < styles.size(); i++ ) {
            FeatureTypeStyle[] fts = styles.get( i ).getFeatureTypeStyles();
            for ( int j = 0; j < fts.length; j++ ) {
                Rule[] rules = fts[j].getRules();
                for ( int k = 0; k < rules.length; k++ ) {
//                    double minScale = rules[k].getMinScaleDenominator();
//                    double maxScale = rules[k].getMaxScaleDenominator();
                    //if ( minScale <= scaleDen && maxScale > scaleDen ) {
                        Filter filter = rules[k].getFilter();
                        List<PropertyPath> list = FilterTools.extractPropertyPaths( filter );
                        pp.addAll( list );
                        Symbolizer[] sym = rules[k].getSymbolizers();
                        for ( int d = 0; d < sym.length; d++ ) {
                            if ( sym[d] instanceof PointSymbolizer ) {
                                pp = extractPPFromPointSymbolizer( featureType, (PointSymbolizer) sym[d], pp );
                            } else if ( sym[d] instanceof LineSymbolizer ) {
                                pp = extractPPFromLineSymbolizer( (LineSymbolizer) sym[d], pp );
                            } else if ( sym[d] instanceof PolygonSymbolizer ) {
                                pp = extractPPFromPolygonSymbolizer( (PolygonSymbolizer) sym[d], pp );
                            } else if ( sym[d] instanceof TextSymbolizer ) {
                                pp = extractPPFromTextSymbolizer( (TextSymbolizer) sym[d], pp );
                            }
                        }
                    //}
                }
            }
        }

        List<PropertyPath> tmp = new ArrayList<PropertyPath>( pp.size() );
        for ( int i = 0; i < pp.size(); i++ ) {
            if ( !tmp.contains( pp.get( i ) ) ) {
                tmp.add( pp.get( i ) );
            }
        }

        return tmp;
    }

    private static List<PropertyPath> extractPPFromTextSymbolizer( TextSymbolizer symbolizer, List<PropertyPath> pp )
                            throws PropertyPathResolvingException {

        ParameterValueType[] bbox = symbolizer.getBoundingBox();
        if ( bbox != null ) {
            pp = extractPPFromParamValueType( bbox[0], pp );
            pp = extractPPFromParamValueType( bbox[1], pp );
            pp = extractPPFromParamValueType( bbox[2], pp );
            pp = extractPPFromParamValueType( bbox[3], pp );
        }

        Map<?, ?> css = null;
        if ( symbolizer.getFill() != null ) {
            css = symbolizer.getFill().getCssParameters();
            pp = extractPPFromCssParameter( css, pp );
        }
        if ( symbolizer.getFont() != null ) {
            css = symbolizer.getFont().getCssParameters();
            pp = extractPPFromCssParameter( css, pp );
        }
        if ( symbolizer.getGeometry() != null ) {
            pp.add( symbolizer.getGeometry().getPropertyPath() );
        }
        Halo halo = symbolizer.getHalo();
        ParameterValueType pvt = null;
        if ( halo != null ) {
            pvt = halo.getRadius();
            pp = extractPPFromParamValueType( pvt, pp );

            if ( halo.getFill() != null ) {
                css = halo.getFill().getCssParameters();
                pp = extractPPFromCssParameter( css, pp );
            }
            if ( halo.getStroke() != null ) {
                css = halo.getStroke().getCssParameters();
                pp = extractPPFromCssParameter( css, pp );
            }
        }
        pvt = symbolizer.getLabel();
        pp = extractPPFromParamValueType( pvt, pp );

        // collect property names from line placement
        LinePlacement lp = symbolizer.getLabelPlacement().getLinePlacement();
        if ( lp != null ) {
            pvt = lp.getGap();
            pp = extractPPFromParamValueType( pvt, pp );
            pvt = lp.getLineWidth();
            pp = extractPPFromParamValueType( pvt, pp );
            pvt = lp.getPerpendicularOffset();
            pp = extractPPFromParamValueType( pvt, pp );
        }

        // collect property names from line placement
        PointPlacement ppl = symbolizer.getLabelPlacement().getPointPlacement();
        if ( ppl != null ) {
            pvt = ppl.getRotation();
            pp = extractPPFromParamValueType( pvt, pp );
            ParameterValueType[] pvta = ppl.getAnchorPoint();
            if ( pvta != null ) {
                for ( int i = 0; i < pvta.length; i++ ) {
                    pp = extractPPFromParamValueType( pvta[i], pp );
                }
            }
            pvta = ppl.getDisplacement();
            if ( pvta != null ) {
                for ( int i = 0; i < pvta.length; i++ ) {
                    pp = extractPPFromParamValueType( pvta[i], pp );
                }
            }
        }

        return pp;
    }

    private static List<PropertyPath> extractPPFromPolygonSymbolizer( PolygonSymbolizer symbolizer,
                                                                      List<PropertyPath> pp )
                            throws PropertyPathResolvingException {

        Map<?, ?> css = null;
        if ( symbolizer != null ) {
            if ( symbolizer.getFill() != null ) {
                css = symbolizer.getFill().getCssParameters();
                pp = extractPPFromCssParameter( css, pp );
            }

            if ( symbolizer.getGeometry() != null ) {
                pp.add( symbolizer.getGeometry().getPropertyPath() );
            }

            if ( symbolizer.getStroke() != null ) {
                css = symbolizer.getStroke().getCssParameters();
                pp = extractPPFromCssParameter( css, pp );
            }
        }

        return pp;
    }

    private static List<PropertyPath> extractPPFromLineSymbolizer( LineSymbolizer symbolizer, List<PropertyPath> pp )
                            throws PropertyPathResolvingException {

        if ( symbolizer.getGeometry() != null ) {
            pp.add( symbolizer.getGeometry().getPropertyPath() );
        }

        Map<?, ?> css = symbolizer.getStroke().getCssParameters();
        pp = extractPPFromCssParameter( css, pp );

        return pp;
    }

    private static List<PropertyPath> extractPPFromPointSymbolizer( QualifiedName featureType,
                                                                    PointSymbolizer symbolizer, List<PropertyPath> pp )
                            throws PropertyPathResolvingException {

        Graphic graphic = symbolizer.getGraphic();
        if ( graphic != null ) {
            if ( graphic.getOpacity() != null ) {
                pp = extractPPFromParamValueType( graphic.getOpacity(), pp );
            }

            if ( graphic.getRotation() != null ) {
                pp = extractPPFromParamValueType( graphic.getRotation(), pp );
            }

            if ( graphic.getDisplacement() != null ) {
                pp = extractPPFromParamValueType( graphic.getDisplacement()[0], pp );
                pp = extractPPFromParamValueType( graphic.getDisplacement()[1], pp );
            }

            if ( graphic.getSize() != null ) {
                pp = extractPPFromParamValueType( graphic.getSize(), pp );
            }

            if ( symbolizer.getGeometry() != null ) {
                pp.add( symbolizer.getGeometry().getPropertyPath() );
            }

            if ( graphic.getMarksAndExtGraphics().length > 0 ) {
                for ( Object obj : graphic.getMarksAndExtGraphics() ) {
                    if ( obj instanceof ExternalGraphic ) {
                        String file = ( (ExternalGraphic) obj ).getOnlineResource().toExternalForm();
                        String[] tags = StringTools.extractStrings( file, "$", "$" );
                        if ( tags != null ) {
                            for ( int i = 0; i < tags.length; i++ ) {
                                String tag = tags[i].substring( 1, tags[i].length() - 1 );
                                QualifiedName pn = new QualifiedName( tag, featureType.getNamespace() );
                                pp.add( PropertyPathFactory.createPropertyPath( pn ) );
                            }
                        }
                    }
                }
            }

        }

        return pp;
    }

    private static List<PropertyPath> extractPPFromCssParameter( Map<?, ?> css, List<PropertyPath> pp )
                            throws PropertyPathResolvingException {
        if ( css != null ) {
            Iterator<?> iter = css.values().iterator();
            while ( iter.hasNext() ) {
                ParameterValueType pvt = ( (CssParameter) iter.next() ).getValue();
                pp = extractPPFromParamValueType( pvt, pp );
            }
        }
        return pp;
    }

    private static List<PropertyPath> extractPPFromParamValueType( ParameterValueType pvt, List<PropertyPath> pp )
                            throws PropertyPathResolvingException {
        if ( pvt != null ) {
            Object[] components = pvt.getComponents();
            for ( int i = 0; i < components.length; i++ ) {
                if ( components[i] instanceof Expression ) {
                    pp = FilterTools.extractPropertyPaths( (Expression) components[i], pp );
                }
            }
        }
        return pp;
    }

}
