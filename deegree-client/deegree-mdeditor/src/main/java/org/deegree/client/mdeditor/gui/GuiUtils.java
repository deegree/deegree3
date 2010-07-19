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
package org.deegree.client.mdeditor.gui;

import static org.slf4j.LoggerFactory.getLogger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GuiUtils {

    private static final Logger LOG = getLogger( GuiUtils.class );

    public static final String FIELDPATH_ATT_KEY = "fieldPath";

    public static final String GROUPID_ATT_KEY = "groupId";

    public static final String CONF_ATT_KEY = "confId";

    public static final String GROUPREF_ATT_KEY = "grpReference";

    public static final String ACTION_ATT_KEY = "dgAction";

    public static enum ACTION_ATT_VALUES {
        SAVE, EDIT, NEW, DELETE, RESET
    }

    public static final String DG_ID_PARAM = "dataGroupId";

    public static final String IS_REFERENCED_PARAM = "isReferencedGrp";

    public static String getUniqueId() {
        return "id_" + UUID.randomUUID();
    }

    public static String getResourceText( FacesContext context, String bundleName, String key, Object... args ) {
        String text;
        try {
            Application app = context.getApplication();
            ResourceBundle bundle = app.getResourceBundle( context, bundleName );
            text = bundle.getString( key );
        } catch ( MissingResourceException e ) {
            LOG.error( "could not find resource '" + bundleName + "'", e );
            return "?" + key + "?";
        }
        if ( args != null ) {
            text = MessageFormat.format( text, args );
        }
        return text;
    }

    public static FacesMessage getFacesMessage( FacesContext context, FacesMessage.Severity severity, String key,
                                                Object... args ) {
        Locale loc = context.getViewRoot().getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle( context.getApplication().getMessageBundle(), loc );
        String msgDetail = key + "_Detail";
        try {
            msgDetail = bundle.getString( key + "_Detail" );
        } catch ( Exception e ) {
            LOG.warn( "detailed message for key " + key + " does not exist, set to empty string." );
        }
        String msgSummary = key;
        try {
            msgSummary = bundle.getString( key );
        } catch ( Exception e ) {
            LOG.warn( "message for key " + key + " does not exist, set to key." );
        }
        if ( args != null ) {
            msgDetail = MessageFormat.format( msgDetail, args );
            msgSummary = MessageFormat.format( msgSummary, args );
        }
        return new FacesMessage( severity, msgSummary, msgDetail );
    }

    public synchronized String getConfId()
                            throws MissingParameterException {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        if ( params.containsKey( CONF_ATT_KEY ) ) {
            throw new MissingParameterException( "Missing parameter with id: " + CONF_ATT_KEY );
        }
        return params.get( CONF_ATT_KEY );
    }

}
