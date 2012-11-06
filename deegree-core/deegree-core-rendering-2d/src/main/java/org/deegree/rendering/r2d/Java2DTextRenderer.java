//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.rendering.r2d;

import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.Font.PLAIN;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.util.Collection;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Font.Style;
import org.slf4j.Logger;

/**
 * <code>Java2DTextRenderer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs usage of text rendering with unsupported geometry types", debug = "logs rendering of null/zero length texts/null geometries")
public class Java2DTextRenderer implements TextRenderer {

    private static final Logger LOG = getLogger( Java2DTextRenderer.class );

    private Java2DRenderer renderer;

    private LabelRenderer labelRenderer;

    /**
     * @param renderer
     */
    public Java2DTextRenderer( Java2DRenderer renderer ) {
        this.renderer = renderer;
        this.labelRenderer = new LabelRenderer( renderer );
    }

    @Override
    public void render( TextStyling styling, String text, Collection<Geometry> geoms ) {
        for ( Geometry g : geoms ) {
            render( styling, text, g );
        }
    }

    @Override
    public void render( TextStyling styling, String text, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }
        if ( text == null || text.length() == 0 ) {
            LOG.debug( "Trying to render null or zero length text." );
            return;
        }

        geom = renderer.rendererContext.clipper.clipGeometry( geom );

        Font font = convertFont( styling );

        handleGeometryTypes( styling, text, font, geom );
    }

    private void handleGeometryTypes( TextStyling styling, String text, Font font, Geometry geom ) {
        if ( geom instanceof Point ) {
            labelRenderer.render( styling, font, text, (Point) geom );
        } else if ( geom instanceof Surface && styling.linePlacement != null ) {
            render( styling, font, text, (Surface) geom );
        } else if ( geom instanceof Curve && styling.linePlacement != null ) {
            labelRenderer.render( styling, font, text, (Curve) geom );
        } else if ( geom instanceof GeometricPrimitive ) {
            labelRenderer.render( styling, font, text, geom.getCentroid() );
        } else {
            handleMultiGeometryTypes( styling, text, font, geom );
        }
    }

    private void handleMultiGeometryTypes( TextStyling styling, String text, Font font, Geometry geom ) {
        if ( geom instanceof MultiPoint ) {
            handleMultiGeometry( styling, text, font, (MultiPoint) geom );
        } else if ( geom instanceof MultiCurve<?> && styling.linePlacement != null ) {
            handleMultiGeometry( styling, text, font, (MultiCurve<?>) geom );
        } else if ( geom instanceof MultiLineString && styling.linePlacement != null ) {
            handleMultiGeometry( styling, text, font, (MultiLineString) geom );
        } else if ( geom instanceof MultiGeometry<?> ) {
            for ( Geometry g : (MultiGeometry<?>) geom ) {
                render( styling, text, g );
            }
        } else {
            LOG.warn( "Trying to use unsupported geometry type '{}' for text rendering.",
                      geom.getClass().getSimpleName() );
        }
    }

    private <T extends Geometry> void handleMultiGeometry( TextStyling styling, String text, Font font,
                                                           MultiGeometry<T> geom ) {
        for ( T g : geom ) {
            handleGeometryTypes( styling, text, font, g );
        }
    }

    private void render( TextStyling styling, Font font, String text, Surface surface ) {
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( patch instanceof PolygonPatch ) {
                PolygonPatch polygonPatch = (PolygonPatch) patch;
                for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                    labelRenderer.render( styling, font, text, curve );
                }
            } else {
                throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
            }
        }
    }

    private Font convertFont( TextStyling styling ) {
        AffineTransform shear = null;

        int style = styling.font.bold ? BOLD : PLAIN;
        switch ( styling.font.fontStyle ) {
        case ITALIC:
            style += ITALIC;
            break;
        case NORMAL:
            style += PLAIN; // yes, it's zero, but the code looks nicer this way
            break;
        case OBLIQUE:
            // Shear the font horizontally to achieve obliqueness
            shear = new AffineTransform();
            shear.shear( -0.2, 0 );
            break;
        }

        // use the first matching name, or Dialog, if none was found
        int size = round( renderer.considerUOM( styling.font.fontSize, styling.uom ) );
        Font font = new Font( "", style, size );
        for ( String name : styling.font.fontFamily ) {
            font = new Font( name, style, size );
            if ( !font.getFamily().equalsIgnoreCase( "dialog" ) ) {
                break;
            }
        }

        if ( styling.font.fontStyle == Style.OBLIQUE && shear != null )
            font = font.deriveFont( shear );
        return font;
    }

}
