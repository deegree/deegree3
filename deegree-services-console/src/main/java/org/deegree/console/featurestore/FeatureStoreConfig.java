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
package org.deegree.console.featurestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.context.FacesContext;

import org.deegree.console.ManagedXMLConfig;
import org.deegree.console.SQLExecution;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureStoreConfig extends ManagedXMLConfig {

    private static final long serialVersionUID = 6752472497206455251L;

    public FeatureStoreConfig( String id, boolean active, boolean ignore, FeatureStoreConfigManager manager, FeatureStoreProvider provider ) {
        super( id, active, ignore, manager, provider.getConfigSchema(), provider.getConfigTemplate() );
    }

    public boolean getSql () {
        if ( !isActive() ) {
            return false;
        }
        return FeatureStoreManager.get( getId() ) instanceof SQLFeatureStore;
    }
    
    public String createTables() {
        if ( !isActive() ) {
            throw new RuntimeException();
        }

        SQLFeatureStore fs = (SQLFeatureStore) FeatureStoreManager.get( getId() );
        String connId = fs.getConnId();
        String[] sql = fs.getDDL();
        SQLExecution execution = new SQLExecution( connId, sql );

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "execution", execution );
        return "console/generic/sql.jsf?faces-redirect=true";
    }

    public String showInfo() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "editConfig", this );
        return "console/feature/index";
    }

    public List<NamespaceBinding> getNamespaces() {
        Set<NamespaceBinding> namespaces = new TreeSet<NamespaceBinding>();
        FeatureStore fs = FeatureStoreManager.get( getId() );
        ApplicationSchema schema = fs.getSchema();
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            String prefix = ft.getName().getPrefix();
            String ns = ft.getName().getNamespaceURI();
            namespaces.add( new NamespaceBinding( prefix, ns ) );
        }
        return new ArrayList<NamespaceBinding>( namespaces );
    }

    public String getNumFtsTotal() {
        FeatureStore fs = FeatureStoreManager.get( getId() );
        ApplicationSchema schema = fs.getSchema();
        int numFtsTotal = schema.getFeatureTypes().length;
        return "" + numFtsTotal;
    }

    public String getNumFtsAbstract() {
        FeatureStore fs = FeatureStoreManager.get( getId() );
        ApplicationSchema schema = fs.getSchema();
        int numFtsTotal = schema.getFeatureTypes().length;
        int numFtsConcrete = schema.getFeatureTypes( null, false, false ).size();
        return "" + ( numFtsTotal - numFtsConcrete );
    }

    public String getNumFtsConcrete() {
        FeatureStore fs = FeatureStoreManager.get( getId() );
        ApplicationSchema schema = fs.getSchema();
        int numFtsConcrete = schema.getFeatureTypes( null, false, false ).size();
        return "" + numFtsConcrete;
    }

    public String getFtInfo()
                            throws IOException {
        StringBuffer sb = new StringBuffer();
        FeatureStore fs = FeatureStoreManager.get( getId() );
        ApplicationSchema schema = fs.getSchema();
        FeatureType[] fts = schema.getRootFeatureTypes();

        // sort the types by name
        Arrays.sort( fts, new Comparator<FeatureType>() {
            public int compare( FeatureType a, FeatureType b ) {
                int order = a.getName().getNamespaceURI().compareTo( b.getName().getNamespaceURI() );
                if ( order == 0 ) {
                    order = a.getName().getLocalPart().compareTo( b.getName().getLocalPart() );
                }
                return order;
            }
        } );

        for ( FeatureType ft : fts ) {
            appendFtInfo( ft, fs, sb, "" );
            sb.append( "<br/>" );
        }
        return sb.toString();
    }

    private void appendFtInfo( FeatureType ft, FeatureStore store, StringBuffer sb, String indent )
                            throws IOException {
        if ( ft.isAbstract() ) {
            sb.append( indent + "- <i>" + ft.getName().getLocalPart() + " (abstract)</i><br/>" );
        } else {
            Query query = new Query( ft.getName(), null, null, 0, -1, -1 );
            int numInstances = -1;
            try {
                numInstances = store.queryHits( query );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            sb.append( indent + "- " + ft.getName().getLocalPart() + " (" + numInstances + " instances)<br/>" );
        }
        FeatureType[] fts = ft.getSchema().getDirectSubtypes( ft );
        Arrays.sort( fts, new Comparator<FeatureType>() {
            public int compare( FeatureType a, FeatureType b ) {
                int order = a.getName().getNamespaceURI().compareTo( b.getName().getNamespaceURI() );
                if ( order == 0 ) {
                    order = a.getName().getLocalPart().compareTo( b.getName().getLocalPart() );
                }
                return order;
            }
        } );
        for ( FeatureType childType : fts ) {
            appendFtInfo( childType, store, sb, indent + "&nbsp;&nbsp;" );
        }
    }
}
