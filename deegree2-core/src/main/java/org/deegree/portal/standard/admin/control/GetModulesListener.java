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
package org.deegree.portal.standard.admin.control;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.Constants;
import org.deegree.portal.context.GUIArea;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.admin.model.ExtJsAreaBean;
import org.deegree.portal.standard.admin.model.ExtJsGUIBean;
import org.deegree.portal.standard.admin.model.ExtJsModuleBean;

/**
 * Reads all modules registered to current WMC and returns them ordered by area to requesting client as list of JSON
 * object. Where each JSON object represent/describes one module.
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetModulesListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( GetModulesListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler resp )
                            throws IOException {

        LOG.logDebug( "parameters", event.getParameter() );

        List areas = new ArrayList();

        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        appendArea( areas, vc.getGeneral().getExtension().getFrontend().getNorth() );
        appendArea( areas, vc.getGeneral().getExtension().getFrontend().getSouth() );
        appendArea( areas, vc.getGeneral().getExtension().getFrontend().getEast() );
        appendArea( areas, vc.getGeneral().getExtension().getFrontend().getWest() );
        appendArea( areas, vc.getGeneral().getExtension().getFrontend().getCenter() );

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        resp.setContentType( "application/json; charset=" + charEnc );
        ExtJsGUIBean gui = new ExtJsGUIBean();
        gui.setChildren( areas );
        Object[] o = new Object[] { gui };
        resp.writeAndClose( false, o );
    }

    /**
     * 
     * @param areas
     * @param area
     */
    @SuppressWarnings("unchecked")
    private void appendArea( List areas, GUIArea area ) {

        List mList = new ArrayList();
        Module[] modules = area.getModules();
        for ( Module module : modules ) {
            ExtJsModuleBean modelBean = new ExtJsModuleBean();
            modelBean.setText( module.getTitle() );
            modelBean.setId( module.getName() );
            mList.add( modelBean );
        }
        ExtJsAreaBean areaBean = new ExtJsAreaBean();
        areaBean.setChildren( mList );
        switch ( area.getArea() ) {
        case GUIArea.CENTER: {
            areaBean.setText( "center" );
            areaBean.setId( "center" );
            break;
        }
        case GUIArea.WEST: {
            areaBean.setText( "west" );
            areaBean.setId( "west" );
            break;
        }
        case GUIArea.EAST: {
            areaBean.setText( "east" );
            areaBean.setId( "east" );
            break;
        }
        case GUIArea.SOUTH: {
            areaBean.setText( "south" );
            areaBean.setId( "south" );
            break;
        }
        case GUIArea.NORTH: {
            areaBean.setText( "north" );
            areaBean.setId( "north" );
            break;
        }
        }
        areas.add( areaBean );
    }

}
