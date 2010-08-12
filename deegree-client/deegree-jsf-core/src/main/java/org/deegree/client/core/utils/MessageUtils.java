//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.core.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * <code>MessageUtils</code>
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MessageUtils {

    private static final String DEEGREE_RESOURCE_BUNDLE = "org.deegree.client.core.i18n.messages";

    private static String JSF_BUNDLE_BASENAME = "javax.faces.Messages";

    private static final String DETAIL_SUFFIX = "_detail";

    /**
     * @param bundleName
     *            the name of the resource bundle, must not be <code>null</code>
     * @param key
     *            the message key, must not be <code>null</code>
     * @param args
     *            a list of arguments
     * @return the formated text, or <code>?key?<code>, if the resource bundle or key does not exist
     */
    public static String getResourceText( String bundleName, String key, Object... args ) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle userBundle = context.getApplication().getResourceBundle( context, bundleName );
            String text = userBundle.getString( key );
            if ( args != null ) {
                return MessageFormat.format( text, args );
            }
            return text;
        } catch ( MissingResourceException e ) {
            // LOG.error( "could not find resource '" + bundleName + "'", e );
            return "?" + key + "?";
        }
    }

    /**
     * 
     * @param bundleName
     *            the name of the resource bundle, can be <code>null</code>, in this case the deegree properties or JSF
     *            properties will be used as fallback
     * @param severity
     *            the severity level of the message, must not be <code>null</code>
     * @param key
     *            the message key, must not be <code>null</code>
     * @param args
     *            a list of arguments
     * @return A faces message with the given severity. The message summary will be created from the key in the message
     *         bundles of the application. The message detail from key 'KEY_detail'.
     */
    public static FacesMessage getFacesMessage( String bundleName, FacesMessage.Severity severity, String key,
                                                Object... args ) {
        FacesContext context = FacesContext.getCurrentInstance();
        Locale locale = context.getViewRoot().getLocale();
        ResourceBundle bundle = null;
        String msgSummary = null;
        String msgDetail = null;
        if ( bundleName != null ) {
            bundle = context.getApplication().getResourceBundle( context, bundleName );
            try {
                msgSummary = bundle.getString( key );
            } catch ( Exception e ) {
                // nothing to do - try to get from deegree bundle
            }
        }

        if ( msgSummary == null ) {
            try {
                bundle = ResourceBundle.getBundle( DEEGREE_RESOURCE_BUNDLE, locale );
                msgSummary = bundle.getString( key );
            } catch ( MissingResourceException e ) {
                // nothing to do - try to get from jsf bundle
            }
        }

        if ( msgSummary == null ) {
            try {
                bundle = ResourceBundle.getBundle( JSF_BUNDLE_BASENAME, locale );
                msgSummary = bundle.getString( key );
            } catch ( MissingResourceException e ) {
                // nothing to do - set key as summary
            }
        }

        if ( msgSummary == null ) {
            return new FacesMessage( severity, key, null );
        }

        try {
            msgDetail = bundle.getString( key + DETAIL_SUFFIX );
            msgDetail = MessageFormat.format( msgDetail, args );
        } catch ( Exception e ) {
            // Nothing to do - detail is null
        }
        msgSummary = MessageFormat.format( msgSummary, args );
        return new FacesMessage( severity, msgSummary, msgDetail );
    }

}
