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
package org.deegree.client.mdeditor.gui.listener;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.SelectFormField;
import org.deegree.commons.utils.StringPair;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GroupReferenceListCreator extends SelectItemsCreator {

    @Override
    protected List<StringPair> getItems( Map<String, Object> attributes ) {
        String grpReference = (String) attributes.get( GuiUtils.GROUPREF_ATT_KEY );
        String confId = (String) attributes.get( GuiUtils.CONF_ATT_KEY );
        FormFieldPath path = (FormFieldPath) attributes.get( GuiUtils.FIELDPATH_ATT_KEY );

        try {
            String referenceLabel = null;
            FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( confId );
            FormField formField = configuration.getFormField( path );
            if ( formField instanceof SelectFormField ) {
                referenceLabel = ( (SelectFormField) formField ).getReferenceText();
            }
            return DataHandler.getInstance().getItems( grpReference, referenceLabel );
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

}
