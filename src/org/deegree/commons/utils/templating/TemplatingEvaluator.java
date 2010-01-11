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
package org.deegree.commons.utils.templating;

import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.StringPair;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;

/**
 * <code>TemplatingEvaluator</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TemplatingEvaluator {

    private static void eval( StringBuilder sb, List<?> state, Map<String, ?> map, Object object, boolean oldOdd )
                            throws FilterEvaluationException {
        for ( Object o : state ) {
            if ( o instanceof String ) {
                sb.append( o );
            } else {
                if ( object == null ) {
                    continue;
                }
                StringPair p = (StringPair) o;
                String xpath = p.first;
                String tmplName = p.second;

                if ( ( oldOdd && xpath.equals( "odd" ) ) || ( !oldOdd && xpath.equals( "even" ) ) ) {
                    eval( sb, (List<?>) map.get( tmplName ), map, object, oldOdd );
                    continue;
                }

                if ( object instanceof FeatureCollection ) {
                    boolean odd = true;
                    for ( Feature f : (FeatureCollection) object ) {
                        if ( xpath.equals( f.getName().getLocalPart() ) || xpath.equals( "*" ) ) {
                            eval( sb, (List<?>) map.get( tmplName ), map, f, odd );
                            odd = !odd;
                        }
                    }
                } else if ( object instanceof Feature ) {
                    if ( tmplName.equals( "gmlid" ) ) {
                        sb.append( ( (Feature) object ).getId() );
                        continue;
                    }
                    if ( tmplName.equals( "name" ) ) {
                        sb.append( ( (Feature) object ).getName().getLocalPart() );
                        continue;
                    }
                    boolean odd = true;
                    for ( Property<?> prop : ( (Feature) object ).getProperties() ) {
                        if ( xpath.equals( prop.getName().getLocalPart() ) || xpath.equals( "*" ) ) {
                            if ( tmplName.equals( "text" ) ) {
                                sb.append( prop.getValue() );
                                continue;
                            }
                            if ( tmplName.equals( "name" ) ) {
                                sb.append( prop.getName().getLocalPart() );
                                continue;
                            }
                            Object mapOrList = map.get( tmplName );
                            if ( mapOrList instanceof List<?> ) {
                                eval( sb, (List<?>) map.get( tmplName ), map, prop, odd );
                            } else {
                                sb.append( ( (Map<?, ?>) mapOrList ).get( "" + prop.getValue() ) );
                            }
                            odd = !odd;
                        }
                    }
                } else if ( object instanceof Property<?> ) {
                    Property<?> prop = (Property<?>) object;
                    if ( tmplName.equals( "text" ) ) {
                        sb.append( prop.getValue() );
                        continue;
                    }
                    if ( tmplName.equals( "name" ) ) {
                        sb.append( prop.getName().getLocalPart() );
                        continue;
                    }
                    Object mapOrList = map.get( tmplName );
                    if ( mapOrList instanceof Map<?, ?> ) {
                        String key = null;
                        if ( xpath.equals( "name" ) ) {
                            key = prop.getName().getLocalPart();
                        }
                        if ( xpath.equals( "text" ) ) {
                            key = prop.getValue().toString();
                        }
                        sb.append( ( (Map<?, ?>) mapOrList ).get( key ) );
                    }
                }
            }
        }
    }

    /**
     * @param map
     * @param col
     * @return a string produced by evaluating the templates
     */
    public static String evalTemplating( Map<String, ?> map, MatchableObject col ) {
        if ( map.get( "start" ) == null ) {
            throw new IllegalArgumentException( "A template with name 'start' must be defined." );
        }

        StringBuilder sb = new StringBuilder();
        try {
            eval( sb, (List<?>) map.get( "start" ), map, col, true );
        } catch ( FilterEvaluationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sb.toString();
    }

}
