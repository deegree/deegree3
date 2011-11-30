//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.rendering.r2d.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class MapOptionsMaps {

    private Map<String, MapOptions> options;

    public MapOptionsMaps() {
        options = new HashMap<String, MapOptions>();
    }

    public MapOptionsMaps( Map<String, Quality> qualities, Map<String, Interpolation> interpolations,
                           Map<String, Antialias> antialiases, Map<String, Integer> maxFeatures ) {
        options = new HashMap<String, MapOptions>();
        for ( Entry<String, Quality> e : qualities.entrySet() ) {
            options.put( e.getKey(), new MapOptions( e.getValue(), null, null, -1, -1 ) );
        }
        for ( Entry<String, Interpolation> e : interpolations.entrySet() ) {
            if ( options.get( e.getKey() ) != null ) {
                options.get( e.getKey() ).setInterpolation( e.getValue() );
            } else {
                options.put( e.getKey(), new MapOptions( null, e.getValue(), null, -1, -1 ) );
            }
        }
        for ( Entry<String, Antialias> e : antialiases.entrySet() ) {
            if ( options.get( e.getKey() ) != null ) {
                options.get( e.getKey() ).setAntialias( e.getValue() );
            } else {
                options.put( e.getKey(), new MapOptions( null, null, e.getValue(), -1, -1 ) );
            }
        }
        for ( Entry<String, Integer> e : maxFeatures.entrySet() ) {
            setMaxFeatures( e.getKey(), e.getValue() );
        }
    }

    public int getMaxFeatures( String layer ) {
        MapOptions opts = options.get( layer );
        return opts == null ? -1 : opts.getMaxFeatures();
    }

    public int getFeatureInfoRadius( String layer ) {
        MapOptions opts = options.get( layer );
        return opts == null ? -1 : opts.getFeatureInfoRadius();
    }

    public Quality getQuality( String layer ) {
        MapOptions opts = options.get( layer );
        return opts == null ? null : opts.getQuality();
    }

    public Antialias getAntialias( String layer ) {
        MapOptions opts = options.get( layer );
        return opts == null ? null : opts.getAntialias();
    }

    public Interpolation getInterpolation( String layer ) {
        MapOptions opts = options.get( layer );
        return opts == null ? null : opts.getInterpolation();
    }

    public void setMaxFeatures( String layer, int maxFeatures ) {
        if ( options.get( layer ) == null ) {
            options.put( layer, new MapOptions( null, null, null, maxFeatures, -1 ) );
        } else {
            options.get( layer ).setMaxFeatures( maxFeatures );
        }
    }

    public void setFeatureInfoRadius( String layer, int radius ) {
        if ( options.get( layer ) == null ) {
            options.put( layer, new MapOptions( null, null, null, -1, radius ) );
        } else {
            options.get( layer ).setFeatureInfoRadius( radius );
        }
    }

    public void setQuality( String layer, Quality q ) {
        if ( options.get( layer ) == null ) {
            options.put( layer, new MapOptions( q, null, null, -1, -1 ) );
        } else {
            options.get( layer ).setQuality( q );
        }
    }

    public void setInterpolation( String layer, Interpolation interpol ) {
        if ( options.get( layer ) == null ) {
            options.put( layer, new MapOptions( null, interpol, null, -1, -1 ) );
        } else {
            options.get( layer ).setInterpolation( interpol );
        }
    }

    public void setAntialias( String layer, Antialias alias ) {
        if ( options.get( layer ) == null ) {
            options.put( layer, new MapOptions( null, null, alias, -1, -1 ) );
        } else {
            options.get( layer ).setAntialias( alias );
        }
    }

    public MapOptions get( String layer ) {
        return new MapOptions( getQuality( layer ), getInterpolation( layer ), getAntialias( layer ),
                               getMaxFeatures( layer ), getFeatureInfoRadius( layer ) );
    }

}
