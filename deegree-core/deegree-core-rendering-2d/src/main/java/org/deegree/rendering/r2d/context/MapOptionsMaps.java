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
            options.put( e.getKey(), new MapOptions( e.getValue(), null, null, -1 ) );
        }
        for ( Entry<String, Interpolation> e : interpolations.entrySet() ) {
            if ( options.get( e.getKey() ) != null ) {
                options.get( e.getKey() ).setInterpolation( e.getValue() );
            } else {
                options.put( e.getKey(), new MapOptions( null, e.getValue(), null, -1 ) );
            }
        }
        for ( Entry<String, Antialias> e : antialiases.entrySet() ) {
            if ( options.get( e.getKey() ) != null ) {
                options.get( e.getKey() ).setAntialias( e.getValue() );
            } else {
                options.put( e.getKey(), new MapOptions( null, null, e.getValue(), -1 ) );
            }
        }
        for ( Entry<String, Integer> e : maxFeatures.entrySet() ) {
            if ( options.get( e.getKey() ) != null ) {
                options.get( e.getKey() ).setMaxFeatures( e.getValue() );
            } else {
                options.put( e.getKey(), new MapOptions( null, null, null, e.getValue() ) );
            }
        }
    }

    public Integer getMaxFeatures( String layer ) {
        return options.get( layer ).getMaxFeatures();
    }

    public Map<String, Quality> getQualities() {
        Map<String, Quality> qualities = new HashMap<String, Quality>();
        for ( Entry<String, MapOptions> e : options.entrySet() ) {
            qualities.put( e.getKey(), e.getValue().getQuality() );
        }
        return qualities;
    }

    public Map<String, Interpolation> getInterpolations() {
        Map<String, Interpolation> interpolations = new HashMap<String, Interpolation>();
        for ( Entry<String, MapOptions> e : options.entrySet() ) {
            interpolations.put( e.getKey(), e.getValue().getInterpolation() );
        }
        return interpolations;
    }

    public Map<String, Antialias> getAntialiases() {
        Map<String, Antialias> antialiases = new HashMap<String, Antialias>();
        for ( Entry<String, MapOptions> e : options.entrySet() ) {
            antialiases.put( e.getKey(), e.getValue().getAntialias() );
        }
        return antialiases;
    }

    public Map<String, Integer> getMaxFeatures() {
        Map<String, Integer> maxFeatures = new HashMap<String, Integer>();
        for ( Entry<String, MapOptions> e : options.entrySet() ) {
            maxFeatures.put( e.getKey(), e.getValue().getMaxFeatures() );
        }
        return maxFeatures;
    }

}
