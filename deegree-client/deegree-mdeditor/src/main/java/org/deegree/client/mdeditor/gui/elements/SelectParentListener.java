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
package org.deegree.client.mdeditor.gui.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.gui.EditorBean;
import org.deegree.client.mdeditor.gui.listener.SelectItemsCreator;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.Dataset;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.commons.utils.StringPair;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SelectParentListener extends SelectItemsCreator {

    @Override
    protected List<StringPair> getItems( Map<String, Object> attributes ) {
        List<StringPair> items = new ArrayList<StringPair>();
        try {
            List<Dataset> datasets = DataHandler.getInstance().getDatasets();

            FacesContext fc = FacesContext.getCurrentInstance();
            EditorBean editorBean = (EditorBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null,
                                                                                               "editorBean" );
            String confId = editorBean.getConfId();
            FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( confId );

            if ( configuration != null && configuration.getPathToIdentifier() != null ) {
                String selectedId = "";

                String pathToIdentifier = configuration.getPathToIdentifier().toString();
                if ( editorBean.getFormFields().get( pathToIdentifier ) != null
                     && editorBean.getFormFields().get( pathToIdentifier ).getValue() != null ) {
                    selectedId = editorBean.getFormFields().get( pathToIdentifier ).getValue().toString();
                }

                for ( Dataset dataset : datasets ) {
                    Map<String, Object> values = dataset.getValues();
                    if ( values.containsKey( pathToIdentifier ) ) {

                        String id = values.get( pathToIdentifier ).toString();
                        String label = id;
                        String describtion = null;

                        if ( configuration.getPathToTitle() != null
                             && values.get( configuration.getPathToTitle().toString() ) != null ) {
                            String title = values.get( configuration.getPathToTitle().toString() ).toString();
                            label = title + " (" + id + ")";
                            describtion = title;
                        }
                        if ( configuration.getPathToDescription() != null
                             && values.get( configuration.getPathToDescription().toString() ) != null ) {
                            String desc = values.get( configuration.getPathToDescription().toString() ).toString();
                            describtion = describtion != null ? ": " : "" + desc;
                        }
                        if ( !selectedId.equals( id ) ) {
                            items.add( new StringPair( id, label ) );
                        }
                    }
                }
            }
        } catch ( ConfigurationException e ) {
            e.printStackTrace();
        } catch ( DataIOException e ) {
            e.printStackTrace();
        }
        return items;
    }
}
