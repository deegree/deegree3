//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.jsf;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.deegree.client.core.utils.SQLExecution;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.metadata.persistence.iso.ISOMetadataStore;
import org.deegree.metadata.persistence.iso.ISOMetadataStoreProvider;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class MetadataStoreConfig implements Serializable {

    private static final long serialVersionUID = -6283943364868062095L;

    private String id;

    public String getId() {
        return id;
    }

    public void updateId( ActionEvent evt ) {
        id = ( (HtmlCommandButton) evt.getComponent() ).getAlt();
    }

    public String openImporter()
                            throws Exception {
        MetadataStore ms = MetadataStoreManager.get( getId() );
        if ( ms == null ) {
            throw new Exception( "No metadata store with id '" + getId() + "' known / active." );
        }
        MetadataImporter msImporter = new MetadataImporter( ms );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "msConfig", this );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "msImporter", msImporter );
        return "/console/metadatastore/importer";
    }

    public String createTables()
                            throws MetadataStoreException {
        ISOMetadataStore ms = (ISOMetadataStore) MetadataStoreManager.get( getId() );
        String connId = ms.getConnId();
        String[] sql = null;
        try {
            sql = new ISOMetadataStoreProvider().getDefaultCreateStatements();
        } catch ( UnsupportedEncodingException e ) {
            String msg = "Unsupported: " + e.getMessage();
            throw new MetadataStoreException( msg );
        } catch ( IOException e ) {
            String msg = "IOException: " + e.getMessage();
            throw new MetadataStoreException( msg );
        }
        SQLExecution execution = new SQLExecution( connId, sql );

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "execution", execution );
        return "/console/generic/sql.jsf?faces-redirect=true";
    }

}
