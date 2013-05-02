//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.theme.persistence.remotewms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.theme.Themes.aggregateSpatialMetadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.struct.Tree;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.OldLayerStoreManager;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.theme.persistence.remotewms.jaxb.RemoteWMSThemes;
import org.deegree.theme.persistence.standard.StandardTheme;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class RemoteWMSThemeProvider implements ThemeProvider {

    private static final URL CONFIG_SCHEMA = RemoteWMSThemeProvider.class.getResource( "/META-INF/schemas/themes/remotewms/3.1.0/remotewms.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private Theme buildTheme( Tree<LayerMetadata> tree, LayerStore store ) {
        List<Theme> thms = new ArrayList<Theme>();
        List<Layer> lays = new ArrayList<Layer>();
        if ( tree.value.getName() != null ) {
            Layer l = store.get( tree.value.getName() );
            if ( l != null ) {
                lays.add( l );
            }
        }
        Theme thm = new StandardTheme( tree.value, thms, lays );
        for ( Tree<LayerMetadata> child : tree.children ) {
            thms.add( buildTheme( child, store ) );
        }
        return thm;
    }

    @Override
    public Theme create( URL configUrl )
                            throws ResourceInitException {
        try {
            RemoteWMSThemes cfg = (RemoteWMSThemes) unmarshall( "org.deegree.theme.persistence.remotewms.jaxb",
                                                                CONFIG_SCHEMA, configUrl, workspace );
            String id = cfg.getRemoteWMSId();
            RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );

            String lid = cfg.getLayerStoreId();
            OldLayerStoreManager lmgr = workspace.getSubsystemManager( OldLayerStoreManager.class );
            LayerStore store = lmgr.get( lid );
            if ( store == null ) {
                throw new ResourceInitException( "The layer store with id " + lid + " was not available." );
            }

            RemoteOWS ows = mgr.get( id );
            if ( !( ows instanceof RemoteWMS ) ) {
                throw new ResourceInitException( "The remote OWS store with id " + id
                                                 + " was not of type WMS or was not available." );
            }

            WMSClient client = ( (RemoteWMS) ows ).getClient();
            Tree<LayerMetadata> tree = client.getLayerTree();

            Theme theme = buildTheme( tree, store );
            aggregateSpatialMetadata( theme );
            return theme;
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse remote WMS theme config.", e );
        }
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { RemoteOWSManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/themes/remotewms";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

}
