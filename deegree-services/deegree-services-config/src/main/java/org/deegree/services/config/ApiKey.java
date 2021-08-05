package org.deegree.services.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle access to an API key file containing a token
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @see org.deegree.console.security.PasswordFile
 */
public class ApiKey {

    private static final Logger LOG = LoggerFactory.getLogger( ApiKey.class );

    private static final String API_TOKEN_FILE = "config.apikey";

    /**
     * Token to be checked
     * 
     * Every value matches this token if the value is "*". No value matches this token if the value is null or an empty
     * string.
     */
    class Token {

        final boolean allowAll;

        private final String key;

        public Token( String value ) {
            this.allowAll = value != null && "*".equals( value.trim() );
            this.key = value != null && value.trim().length() > 0 ? value.trim() : value;
        }

        public Token() {
            this.allowAll = false;
            this.key = null;
        }

        public boolean matches( String value ) {
            if ( allowAll )
                return true;

            if ( key == null )
                return false;

            return key.matches( value != null ? value.trim() : value );
        }

        public boolean isAnyAllowed() {
            return allowAll;
        }
    }

    private Path getPasswordFile() {
        String workspace = DeegreeWorkspace.getWorkspaceRoot();
        return Paths.get( workspace, API_TOKEN_FILE );
    }

    private String generateRandomApiKey() {
        try {
            MessageDigest md = DigestUtils.getSha1Digest();
            // add some random data
            Random rnd = new Random();
            byte[] data = new byte[128];
            rnd.nextBytes( data );
            // add random data
            md.update( data );
            md.update( new Date().toString().getBytes() );
            byte[] digest = md.digest();

            return Hex.encodeHexString( digest );
        } catch ( Exception ex ) {
            LOG.warn( "Could not generate random key with SHA-1: {}", ex.getMessage() );
            LOG.trace( "Exception", ex );
        }
        return null;
    }

    public Token getCurrentToken()
                            throws SecurityException {
        Path file = getPasswordFile();
        Token token = null;

        try {
            if ( Files.isReadable( file ) ) {
                List<String> lines = Files.readAllLines( file );
                if ( lines.size() != 1 ) {
                    LOG.warn( "API Key file ({}) has incorrect format.", file );
                    throw new IOException( "API Key file has incorrect format." );
                }
                token = new Token( lines.get( 0 ) );
            } else if ( !Files.exists( file ) ) {
                // create new one, if no file exists
                String apikey = generateRandomApiKey();
                Files.write( file, Collections.singleton( apikey ) );
                token = new Token( apikey );
                LOG.warn( "Create API Key file ({}) with the generated value of \"{}\"", file, apikey );
            } else {
                LOG.info( "API Key file ({}) is not a regular file or not readable, access prohibited " );
            }
        } catch ( IOException ioe ) {
            throw new SecurityException( "API key file could not be accessed", ioe );
        }

        if ( token == null ) {
            token = new Token();
        }

        return token;
    }

    public void validate( HttpServletRequest req )
                            throws SecurityException {
        String tmp, value = null;
        // check for headers
        if ( value == null ) {
            value = req.getHeader( "X-API-Key" );
        }
        if ( value == null ) {
            tmp = req.getHeader( "Authorization" );
            if ( tmp != null && tmp.toLowerCase().startsWith( "bearer " ) ) {
                value = tmp.substring( 7 );
            } else if ( tmp != null && tmp.toLowerCase().startsWith( "basic " ) ) {
                tmp = tmp.substring( 6 );
                final byte[] decoded = Base64.getDecoder().decode( tmp );
                final String credentials = new String( decoded, StandardCharsets.UTF_8 );
                // credentials = username:password
                final String[] values = credentials.split( ":", 2 );
                if ( values.length == 2 && values[1] != null ) {
                    value = values[1];
                }
            }
        }

        // check for parameter
        if ( value == null ) {
            Enumeration<?> keys = req.getParameterNames();
            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( "token".equalsIgnoreCase( key ) || "api_key".equalsIgnoreCase( key ) ) {
                    value = req.getParameter( key );
                    break;
                }
            }
        }
        
        // initialize early to allow creation of apikey/token
        Token token = getCurrentToken();

        if ( token.isAnyAllowed() ) {
            // no API Key required
            return;
        }
        
        if ( value == null || value.trim().length() == 0 ) {
            throw new SecurityException( "Please specify API Key" );
        }

        if ( !token.matches( value ) ) {
            throw new SecurityException( "Invalid API Key specified" );
        }
    }
}
