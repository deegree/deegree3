//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/commons/i18n/Messages.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.geometry.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the access to messages that are visible to the user.
 * <p>
 * Messages are read from the properties file <code>messages_LANG.properties</code> (LANG is always a lowercased ISO
 * 639 code), so internationalization is supported. If a certain property (or the property file) for the specific
 * default language of the system is not found, the message is taken from <code>messages_en.properties</code>.
 *
 * @see Locale#getLanguage()
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: rbezema $
 *
 * @version $Revision: 15508 $, $Date: 2009-01-06 12:08:22 +0100 (Di, 06. Jan 2009) $
 */
public class Messages {

    /* This definition allows Eclipse to display the content of referenced message keys. */
    @SuppressWarnings("unused")
    private static final String BUNDLE_NAME = "org.deegree.geometry.i18n.messages_en";

    private static Properties defaultProps = new Properties();

    private static Map<Locale, Properties> props = new HashMap<Locale, Properties>();

    private static String lang;

    private static Logger LOG = LoggerFactory.getLogger( Messages.class );

    /**
     * Initialization done at class loading time.
     */
    static {
        try {
            // load all messages from default file ("org/deegree/model/i18n/message_en.properties")
            String fileName = "messages_en.properties";
            InputStream is = Messages.class.getResourceAsStream( fileName );
            if ( is == null ) {
                LOG.error( "Error while initializing " + Messages.class.getName() + " : " + " default message file: '"
                           + fileName + " not found." );
            }
            is = Messages.class.getResourceAsStream( fileName );
            defaultProps.load( is );
            is.close();

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
        } catch ( IOException e ) {
            LOG.error( "Error while initializing " + Messages.class.getName() + " : " + e.getMessage(), e );
        }
    }

    private static void overrideMessages( String propertiesFile, Properties props )
                            throws IOException {
        InputStream is = Messages.class.getResourceAsStream( propertiesFile );
        if ( is != null ) {
            // override default messages
            Properties overrideProps = new Properties();
            overrideProps.load( is );
            is.close();
            Iterator<?> iter = overrideProps.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                props.put( key, overrideProps.get( key ) );
            }
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
}
