/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.services.wms.utils;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.jaxb.wms.KeyValueRewriteType;
import org.deegree.services.wms.controller.plugins.KeyValueRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueRewriter {

    private static final Logger LOG = LoggerFactory.getLogger( KeyValueRewriter.class );

    private final List<KeyValueRewrite> list;

    private KeyValueRewriter( List<KeyValueRewrite> list ) {
        this.list = list;
    }

    public static KeyValueRewriter parse( List<KeyValueRewriteType> config ) {
        ArrayList<KeyValueRewrite> lst = new ArrayList<>();

        for ( KeyValueRewriteType rewrites : config ) {
            WMSRequestType type = null;

            switch ( rewrites.getType() ) {
            case DESCRIBE_LAYER:
                type = WMSRequestType.DescribeLayer;
                break;
            case GET_CAPABILITIES:
                type = WMSRequestType.GetCapabilities;
                break;
            case CAPABILITIES:
                type = WMSRequestType.capabilities;
                break;
            case GET_FEATURE_INFO:
                type = WMSRequestType.GetFeatureInfo;
                break;
            case GET_MAP:
                type = WMSRequestType.GetMap;
                break;
            case MAP:
                type = WMSRequestType.map;
                break;
            case GET_FEATURE_INFO_SCHEMA:
                type = WMSRequestType.GetFeatureInfoSchema;
                break;
            case GET_LEGEND_GRAPHIC:
                type = WMSRequestType.GetLegendGraphic;
                break;
            case DTD:
                type = WMSRequestType.DTD;
                break;
            case GET_STYLES:
                type = WMSRequestType.GetStyles;
                break;
            case PUT_STYLES:
                type = WMSRequestType.PutStyles;
                break;
            }

            for ( Object rule : rewrites.getRemoveOrRemoveMatchOrDefault() ) {
                if ( rule == null ) {
                    // ignore
                } else if ( rule instanceof KeyValueRewriteType.Remove ) {
                    addHandler( lst, type, (KeyValueRewriteType.Remove) rule );
                } else if ( rule instanceof KeyValueRewriteType.Default ) {
                    addHandler( lst, type, (KeyValueRewriteType.Default) rule );
                } else if ( rule instanceof KeyValueRewriteType.HeaderMatch ) {
                    addHandler( lst, type, (KeyValueRewriteType.HeaderMatch) rule );
                } else if ( rule instanceof KeyValueRewriteType.ParameterMatch ) {
                    addHandler( lst, type, (KeyValueRewriteType.ParameterMatch) rule );
                } else if ( rule instanceof KeyValueRewriteType.RemoveMatch ) {
                    addHandler( lst, type, (KeyValueRewriteType.RemoveMatch) rule );
                } else if ( rule instanceof KeyValueRewriteType.Custom ) {
                    addHandler( lst, type, (KeyValueRewriteType.Custom) rule );
                } else {
                    LOG.warn( "Unknwon KeyValueRewriter of type {} ignored", rule.getClass() );
                }
            }
        }

        if ( lst.size() > 0 ) {
            return new KeyValueRewriter( lst );
        } else {
            return new KeyValueRewriter( emptyList() );
        }
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.Remove cfg ) {
        final String key = cfg.getKey();
        LOG.debug( "[{}] Remove {}", type, key );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            LOG.trace( "Remove {}", key );
            map.remove( key );
        } );
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.Default cfg ) {
        final String key = cfg.getKey();
        final String nval = cfg.getValue();
        LOG.debug( "[{}] Default  {}={}", type, key, nval );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            if ( !map.containsKey( key ) ) {
                LOG.trace( "Defatult set {} to {}", key, nval );
                map.put( key, nval );
            }
        } );
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.HeaderMatch cfg ) {
        final String key = cfg.getKey();
        final String hdrName = cfg.getHeader();
        final String match = cfg.getValue();
        final String replacement = cfg.getReplacement();
        final boolean regex = cfg.isRegex();
        final boolean ignoreCase = cfg.isIgnoreCase();
        final Pattern pattern;
        if ( regex ) {
            pattern = Pattern.compile( match, ignoreCase ? Pattern.CASE_INSENSITIVE : 0 );
        } else {
            pattern = null;
        }

        LOG.debug( "[{}] HeaderMatch {}={} when {} {} {} [{}]", type, key, replacement, hdrName, regex ? "=~" : "==",
                   match, ignoreCase );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            String hdrValue = request.getHeader( hdrName );
            if ( hdrValue == null || match == null ) {
                return;
            }
            if ( regex ) {
                Matcher m = pattern.matcher( hdrValue );
                if ( m.matches() ) {
                    String out = m.replaceAll( replacement );
                    LOG.trace( "ParameterMatch set {} to {}", key, out );
                    map.put( key, out );
                }
            } else {
                // simple match
                if ( ( ignoreCase == false && match.equals( hdrValue ) )
                     || ( ignoreCase && match.equalsIgnoreCase( hdrValue ) ) ) {
                    LOG.trace( "ParameterMatch set {} to {}", key, replacement );
                    map.put( key, replacement );
                }
            }
        } );
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.ParameterMatch cfg ) {
        final String key = cfg.getKey();
        final String keyMatch = cfg.getMatch();
        final String match = cfg.getValue();
        final String replacement = cfg.getReplacement();
        final boolean regex = cfg.isRegex();
        final boolean ignoreCase = cfg.isIgnoreCase();
        final Pattern pattern;
        if ( regex ) {
            pattern = Pattern.compile( match, ignoreCase ? Pattern.CASE_INSENSITIVE : 0 );
        } else {
            pattern = null;
        }

        LOG.debug( "[{}] ParameterMatch {}={} when {} {} {} [{}]", type, key, replacement, keyMatch,
                   regex ? "=~" : "==", match, ignoreCase );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            String keyValue = map.get( keyMatch );
            if ( keyValue == null || match == null ) {
                return;
            }
            if ( regex ) {
                Matcher m = pattern.matcher( keyValue );
                if ( m.matches() ) {
                    String out = m.replaceAll( replacement );
                    LOG.trace( "ParameterMatch set {} to {}", key, out );
                    map.put( key, out );
                }
            } else {
                // simple match
                if ( ( ignoreCase == false && match.equals( keyValue ) )
                     || ( ignoreCase && match.equalsIgnoreCase( keyValue ) ) ) {
                    LOG.trace( "ParameterMatch set {} to {}", key, replacement );
                    map.put( key, replacement );
                }
            }
        } );
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.RemoveMatch cfg ) {
        final String key = cfg.getKey();
        final String keyMatch = cfg.getMatch();
        final String match = cfg.getValue();
        final boolean regex = cfg.isRegex();
        final boolean ignoreCase = cfg.isIgnoreCase();
        final Pattern pattern;
        if ( regex ) {
            pattern = Pattern.compile( match, ignoreCase ? Pattern.CASE_INSENSITIVE : 0 );
        } else {
            pattern = null;
        }

        LOG.debug( "[{}] RemoveMatch {} when {} {} {} [{}]", type, key, keyMatch,
                   regex ? "=~" : "==", match, ignoreCase );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            String keyValue = map.get( keyMatch );
            if ( keyValue == null || match == null ) {
                return;
            }
            if ( regex ) {
                Matcher m = pattern.matcher( keyValue );
                if ( m.matches() ) {
                    LOG.trace( "RemoveMatch {}", key );
                    map.remove( key );
                }
            } else {
                // simple match
                if ( ( ignoreCase == false && match.equals( keyValue ) )
                     || ( ignoreCase && match.equalsIgnoreCase( keyValue ) ) ) {
                    LOG.trace( "RemoveMatch {}", key );
                    map.remove( key );
                }
            }
        } );
    }

    private static void addHandler( List<KeyValueRewrite> lst, final WMSRequestType type,
                                    KeyValueRewriteType.Custom cfg ) {
        final KeyValueRewrite hdl;
        try {
            Class<?> cls = Class.forName( cfg.getJavaClass() );
            Object obj = cls.newInstance();
            if ( obj instanceof KeyValueRewrite ) {
                hdl = (KeyValueRewrite) obj;
            } else {
                LOG.warn( "[{}] Custom {} ignored, as it is not a rewriter." );
                return;
            }
        } catch ( Exception ex ) {
            LOG.error( "Failed to initialize {}: {}", cfg.getJavaClass(), ex.getMessage() );
            LOG.trace( "Exception", ex );
            return;
        }

        LOG.debug( "[{}] Custom {}", type );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }

            hdl.handle( req, map, request );
        } );
    }

    public void rewrite( WMSRequestType req, Map<String, String> map, HttpServletRequest request ) {
        for ( KeyValueRewrite hdl : list ) {
            hdl.handle( req, map, request );
        }
    }
}
