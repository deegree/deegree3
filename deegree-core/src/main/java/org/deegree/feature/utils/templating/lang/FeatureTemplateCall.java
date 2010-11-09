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
package org.deegree.feature.utils.templating.lang;

import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.deegree.feature.utils.templating.lang.Util.getMatchingObjects;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.slf4j.Logger;

/**
 * <code>FeatureTemplateCall</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureTemplateCall {

    private static final Logger LOG = getLogger( FeatureTemplateCall.class );

    private String name;

    private List<String> patterns;

    private HashSet<Object> visited = new HashSet<Object>();

    private boolean negate;

    /**
     * @param name
     * @param patterns
     * @param negate
     */
    public FeatureTemplateCall( String name, List<String> patterns, boolean negate ) {
        this.name = name;
        this.patterns = patterns;
        this.negate = negate;
    }

    private void eval( StringBuilder sb, HashMap<String, Object> defs, Feature f, TemplateDefinition t,
                       List<Feature> list, boolean geometries ) {
        if ( visited.contains( f ) ) {
            // TODO add link?
            return;
        }
        visited.add( f );
        for ( Object o : t.body ) {
            if ( o instanceof String ) {
                sb.append( o );
            }
            if ( o instanceof MapCall ) {
                ( (MapCall) o ).eval( sb, defs, f );
            }
            if ( o instanceof FeatureTemplateCall ) {
                ( (FeatureTemplateCall) o ).eval( sb, defs, f, geometries );
            }
            if ( o instanceof PropertyTemplateCall ) {
                ( (PropertyTemplateCall) o ).eval( sb, defs, f, geometries );
            }
            if ( o instanceof Name ) {
                ( (Name) o ).eval( sb, f );
            }
            if ( o instanceof Value ) {
                ( (Value) o ).eval( sb, f );
            }
            if ( o instanceof Link ) {
                ( (Link) o ).eval( sb, f );
            }
            if ( o instanceof Index ) {
                ( (Index) o ).eval( sb, f, list );
            }
            if ( o instanceof OddEven ) {
                ( (OddEven) o ).eval( sb, defs, f, 1 + list.indexOf( f ), geometries );
            }
            if ( o instanceof GMLId ) {
                ( (GMLId) o ).eval( sb, f, null );
            }
        }
    }

    /**
     * @param sb
     * @param defs
     * @param obj
     * @param geometries
     */
    public void eval( StringBuilder sb, HashMap<String, Object> defs, Object obj, boolean geometries ) {
        if ( obj instanceof Feature ) {
            LOG.debug( "Feature template call '{}' with featureid '{}'", name, ( (Feature) obj ).getId() );
        } else {
            LOG.debug( "Feature template call '{}' with '{}'", name, obj );
        }

        Object def = defs.get( name );
        if ( def == null ) {
            LOG.warn( "No template definition with name '{}'.", name );
            return;
        }
        TemplateDefinition t = (TemplateDefinition) def;

        if ( obj instanceof FeatureCollection ) {
            Feature[] fs = new Feature[( (FeatureCollection) obj ).size()];
            List<Feature> list = getMatchingObjects( ( (FeatureCollection) obj ).toArray( fs ), patterns, negate,
                                                     geometries );
            for ( Feature feat : list ) {
                eval( sb, defs, feat, t, list, geometries );
            }
            return;
        }
        if ( obj instanceof Feature ) {
            List<Feature> feats = getMatchingObjects( new Feature[] { (Feature) obj }, patterns, negate, geometries );
            for ( Feature f : feats ) {
                eval( sb, defs, f, t, feats, geometries );
            }
        }
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
