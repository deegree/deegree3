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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueRewriter {

    private static final Logger LOG = LoggerFactory.getLogger( KeyValueRewriter.class );

    private interface Handler {
        public void handle( WMSRequestType req, Map<String, String> map, HttpServletRequest request );
    }

    private final List<Handler> list;

    private KeyValueRewriter( List<Handler> list ) {
        this.list = list;
    }

    public static KeyValueRewriter parse( List<KeyValueRewriteType> config ) {
        ArrayList<Handler> lst = new ArrayList<>();

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

    private static void addHandler( List<Handler> lst, final WMSRequestType type, KeyValueRewriteType.Remove cfg ) {
        final String key = cfg.getKey();
        LOG.debug( "Adding Remove [key={}]", key );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }
            
            map.remove( key );
        } );
    }

    private static void addHandler( List<Handler> lst, final WMSRequestType type, KeyValueRewriteType.Default cfg ) {
        final String key = cfg.getKey();
        final String nval = cfg.getValue();
        LOG.debug( "Adding Default" );
        lst.add( ( req, map, request ) -> {
            if ( type != req ) {
                return;
            }
            
            if ( !map.containsKey( key ) ) {
                map.put( key, nval );
            }
        } );
    }

    private static void addHandler( List<Handler> lst, final WMSRequestType type, KeyValueRewriteType.HeaderMatch cfg ) {
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

        LOG.debug( "Adding HeaderMatch" );
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
                    map.put( key, m.replaceAll( replacement ) );
                }
            } else {
                // simple match
                if ( ( ignoreCase == false && match.equals( hdrValue ) )
                     || ( ignoreCase && match.equalsIgnoreCase( hdrValue ) ) ) {
                    map.put( key, replacement );
                }
            }
        } );
    }

    private static void addHandler( List<Handler> lst, final WMSRequestType type, KeyValueRewriteType.ParameterMatch cfg ) {
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

        LOG.debug( "Adding ParameterMatch" );
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
                    map.put( key, m.replaceAll( replacement ) );
                }
            } else {
                // simple match
                if ( ( ignoreCase == false && match.equals( keyValue ) )
                     || ( ignoreCase && match.equalsIgnoreCase( keyValue ) ) ) {
                    map.put( key, replacement );
                }
            }
        } );
    }

    public void rewrite( WMSRequestType req, Map<String, String> map, HttpServletRequest request ) {
        for ( Handler hdl : list ) {
            hdl.handle( req, map, request );
        }
    }
}
