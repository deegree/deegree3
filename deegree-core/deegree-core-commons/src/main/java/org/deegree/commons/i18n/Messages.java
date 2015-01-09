//$HeadURL$
package org.deegree.commons.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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

    private static final ResourceBundle bundle = ResourceBundle.getBundle( "org.deegree.commons.i18n.messages" );

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
        return getMessage( key, arguments );
    }

    /**
     * Short version for lazy people.
     * 
     * @param key
     * @param arguments
     * @return the same as #getMessage
     */
    public static String get( String key, Object... arguments ) {
        try {
            if ( key != null )
                return MessageFormat.format( bundle.getString( key ), arguments );
        } catch ( MissingResourceException e ) {
        }
        return "$Message with key: " + key + " not found$";
    }

}