//$HeadURL$
package org.deegree.commons.i18n;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the access to messages that are visible to the user.
 * <p>
 * Messages are read from the properties file <code>messages_LANG.properties</code> (LANG is always a lowercased ISO 639
 * code), so internationalization is supported. If a certain property (or the property file) for the specific default
 * language of the system is not found, the message is taken from <code>messages_en.properties</code>.
 * 
 * @see Locale#getLanguage()
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Messages {

    /* This definition allows Eclipse to display the content of referenced message keys. */
    @SuppressWarnings("unused")
    private static final String BUNDLE_NAME = "org.deegree.commons.i18n.messages_en";

    private static Properties defaultProps = new Properties();

    private static Map<Locale, Properties> props = new HashMap<Locale, Properties>();

    private static String lang;

    private static Logger LOG = LoggerFactory.getLogger( Messages.class );

    /**
     * Initialization done at class loading time.
     */
    static {
        InputStream is = null;
        try {
            // load all messages from default file ("org/deegree/model/i18n/message_en.properties")
            String fileName = "messages_en.properties";
            is = Messages.class.getResourceAsStream( fileName );
            if ( is == null ) {
                LOG.error( "Error while initializing " + Messages.class.getName() + " : " + " default message file: '"
                           + fileName + " not found." );
            } else {
                defaultProps.load( is );

                // override messages using file "/message_en.properties"
                fileName = "/messages_en.properties";
                overrideMessages( fileName, defaultProps );

                lang = Locale.getDefault().getLanguage();
                if ( !"".equals( lang ) && !"en".equals( lang ) ) {
                    // override messages using file "org/deegree/i18n/message_LANG.properties"
                    fileName = "messages_" + lang + ".properties";
                    overrideMessages( fileName, defaultProps );
                    // override messages using file "/message_LANG.properties"
                    fileName = "/messages_" + lang + ".properties";
                    overrideMessages( fileName, defaultProps );
                }
            }
        } catch ( IOException e ) {
            LOG.error( "Error while initializing " + Messages.class.getName() + " : " + e.getMessage(), e );
        } finally {
            closeQuietly( is );
        }
    }

    private static void overrideMessages( String propertiesFile, Properties props )
                            throws IOException {
        InputStream is = null;
        try {
            is = Messages.class.getResourceAsStream( propertiesFile );
            if ( is != null ) {
                // override default messages
                Properties overrideProps = new Properties();
                overrideProps.load( is );
                is.close();
                for ( Entry<?, ?> e : overrideProps.entrySet() ) {
                    props.put( e.getKey(), e.getValue() );
                }
            }
        } finally {
            closeQuietly( is );
        }
    }

    private static String get( Properties props, String key, Object... args ) {
        String s = (String) props.get( key );
        if ( s != null ) {
            return MessageFormat.format( s, args );
        }

        return "$Message with key: " + key + " not found$";
    }

    /**
     * @param loc
     *            the locale to be used
     * @param key
     *            to get
     * @param arguments
     *            to fill in the message
     * @return the localized message
     */
    public static synchronized String getMessage( Locale loc, String key, Object... arguments ) {
        if ( loc.getLanguage().equals( lang ) ) {
            return getMessage( key, arguments );
        }

        if ( !props.containsKey( loc ) ) {
            Properties p = new Properties();

            String l = loc.getLanguage();

            if ( !"".equals( l ) ) {
                try {
                    // override messages in this order:
                    // messages_en.properties
                    // /messages_en.properties
                    // messages_lang.properties
                    // /messages_lang.properties
                    String fileName = "messages_en.properties";
                    overrideMessages( fileName, p );
                    fileName = "/messages_en.properties";
                    overrideMessages( fileName, p );
                    fileName = "messages_" + l + ".properties";
                    overrideMessages( fileName, p );
                    fileName = "/messages_" + l + ".properties";
                    overrideMessages( fileName, p );
                } catch ( IOException e ) {
                    LOG.error( "Error loading language file for language '" + l + "': ", e );
                }
            }

            props.put( loc, p );
        }

        return get( props.get( loc ), key, arguments );
    }

    /**
     * Returns the message assigned to the passed key. If no message is assigned, an error message will be returned that
     * indicates the missing key.
     * 
     * @see MessageFormat for conventions on string formatting and escape characters.
     * 
     * @param key
     * @param arguments
     * @return the message assigned to the passed key
     */
    public static String getMessage( String key, Object... arguments ) {
        return get( defaultProps, key, arguments );
    }

    /**
     * Short version for lazy people.
     * 
     * @param key
     * @param arguments
     * @return the same as #getMessage
     */
    public static String get( String key, Object... arguments ) {
        return getMessage( key, arguments );
    }

}
