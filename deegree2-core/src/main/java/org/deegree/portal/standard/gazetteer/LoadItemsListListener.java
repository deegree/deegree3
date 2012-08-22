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
package org.deegree.portal.standard.gazetteer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Parameter;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoadItemsListListener extends AbstractGazetteerListener {
    
    private static final ILogger LOG = LoggerFactory.getLogger( LoadItemsListListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        Map<String, Object> param = event.getParameter();
        int index = ( (Number) param.get( "level" ) ).intValue();
        int hierarchyIndex = Integer.parseInt( (String) param.get( "hierarchyIndex" ) );
        String geographicIdentifier = (String) param.get( "geographicIdentifier" );

        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        Module[] modules = vc.getGeneral().getExtension().getFrontend().getModulesByName( "Gazetteer" );
        Parameter[] parameters = modules[0].getParameter().getParameters();
        // just one gazetteer module is allowed to be contained in an instance of iGeoPortal
        Hierarchy hierarchy;
        try {
            hierarchy = loadHierarchy( (String) parameters[hierarchyIndex].getValue() );
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: can not load hierarchy list" );
            return;
        }
        HierarchyNode node = hierarchy.getRoot();
        int k = 0;
        while ( node != null && k < index ) {
            node = node.getChildNode();
            k++;
        }
        QualifiedName ft = node.getFeatureType();
        FindChildrenCommand cmd = new FindChildrenCommand(hierarchy.getGazetteerAddress(),ft,node.getProperties(),geographicIdentifier);
        List<GazetteerItem> items;
        try {
            items = cmd.execute();
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: could not find/load items for geographicIdentifier: " + geographicIdentifier );
            return;
        }        
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, items );

    }

}
