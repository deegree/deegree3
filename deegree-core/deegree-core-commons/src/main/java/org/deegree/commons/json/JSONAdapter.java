package org.deegree.commons.json;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.XMLProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

public class JSONAdapter {
    private static final Logger LOG = LoggerFactory.getLogger( JSONAdapter.class );

    /**
     * Use this URL as SystemID only if the document content cannot be pinpointed to a URL - in this case it may not use
     * any relative references!
     */
    public static final String DEFAULT_URL = "http://www.deegree.org/unknownLocation";

    // physical source of the element (used for resolving relative URLs in the document)
    private String systemId;

    private DocumentContext jsonCtx;

    /**
     * Creates a new <code>JSONAdapter</code> which is not bound to a JSON element.
     */
    public JSONAdapter() {
        // nothing to do
    }

    /**
     * Creates a new instance that loads its content from the given <code>InputStream</code> using the default url.
     * 
     * @param in
     *            source of the json content
     * 
     * @throws XMLProcessingException
     */
    public JSONAdapter( InputStream in ) throws JSONParsingException {
        load( in, DEFAULT_URL );
    }

    /**
     * Returns the systemId (the physical location of the wrapped XML content).
     * 
     * @return the systemId
     */
    public String getSystemId() {
        return systemId;
    }

    public DocumentContext getJsonCtx() {
        return jsonCtx;
    }

    /**
     * Sets the systemId (the physical location of the wrapped XML content).
     * 
     * @param systemId
     *            systemId (physical location) to set
     */
    public void setSystemId( String systemId ) {
        this.systemId = systemId;
    }

    public void setJsonCtx( DocumentContext jsonCtx ) {
        this.jsonCtx = jsonCtx;
    }

    public JSONArray getJSONArrayForPath( String path ) {
        if ( path != null && !path.isEmpty() ) {
            Object arr = jsonCtx.read( path );
            if ( arr instanceof JSONArray )
                return (JSONArray) arr;
            else
                return new JSONArray();
        } else {
            Object json = jsonCtx.json();
            if ( json instanceof JSONArray ) {
                return (JSONArray) json;
            } else {
                LOG.warn( "the requested Object is no JSONArray" );
                return new JSONArray();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getMapArrayForPath( String path ) {
        List<Map<String, String>> result = new LinkedList<>();
        if ( path != null && !path.isEmpty() ) {
            Object arrObject = jsonCtx.read( path );
            if ( arrObject instanceof JSONArray ) {
                JSONArray arr = (JSONArray) arrObject;
                for ( Object mapObject : arr ) {
                    if ( mapObject instanceof Map<?, ?> ) {
                        result.add( (Map<String, String>) mapObject );
                    }
                }
            } else {
                // ignore
            }
        } else {
            Object json = jsonCtx.json();
            if ( json instanceof JSONArray ) {
                JSONArray arr = (JSONArray) json;
                for ( Object mapObject : arr ) {
                    if ( mapObject instanceof Map<?, ?> ) {
                        result.add( (Map<String, String>) mapObject );
                    }
                }
            } else {
                LOG.warn( "the requested Object is no JSONArray" );
            }
        }
        return result;
    }

    public String getRequiredNodeAsString( String xpath )
                            throws JSONParsingException {

        String value = getNodeAsString( xpath, null );
        if ( value == null ) {
            String msg = "Required element '" + xpath + "' is missing.";
            throw new JSONParsingException( msg );
        }
        return value;
    }

    public String getNodeAsString( String xpath, String defaultValue )
                            throws JSONParsingException {
        String value = defaultValue;
        Object node = jsonCtx.read( xpath );
        if ( node instanceof String )
            value = (String) node;
        else {
            try {
                value = String.valueOf( node );
            } catch ( Exception e ) {
                LOG.error( ">{}< can not cast to String. DefaultValue is used.", node );
                LOG.trace( "", e );
            }
        }
        return value;
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>InputStream</code>. Sets the
     * SystemId, too.
     * 
     * @param istream
     *            source of the json content
     * @param systemId
     *            cannot be null. This string should represent a URL that is related to the passed istream. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     */
    public void load( InputStream istream, String systemId )
                            throws JSONParsingException {
        if ( istream != null ) {

            // TODO evaluate correct encoding handling

            // PushbackInputStream pbis = new PushbackInputStream( istream, 1024 );
            // String encoding = determineEncoding( pbis );
            //
            // InputStreamReader isr;
            // try {
            // isr = new InputStreamReader( pbis, encoding );
            // } catch ( UnsupportedEncodingException e ) {
            // throw new XMLProcessingException( e.getMessage(), e );
            // }
            //
            setSystemId( systemId );

            // TODO the code below is used, because constructing from Reader causes an error if the
            // document contains a DOCTYPE definition

            jsonCtx = JsonPath.parse( istream );

            // load( isr, systemId );
        } else {
            throw new NullPointerException( "The stream may not be null." );
        }
    }

}
