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
package org.deegree.client.mdeditor;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.gui.GuiUtils;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MDEditorExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler parent;

    public MDEditorExceptionHandler( ExceptionHandler parent ) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.parent;
    }

    @Override
    public void handle()
                            throws FacesException {

        Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
        while ( i.hasNext() ) {

            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();

            if ( ( t instanceof ConfigurationException )
                 || ( t.getCause() != null && t.getCause() instanceof ConfigurationException ) ) {
                FacesContext fc = FacesContext.getCurrentInstance();
                NavigationHandler nav = fc.getApplication().getNavigationHandler();
                try {
                    FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR",
                                                                 t.getMessage() );
                    fc.addMessage( null, msg );
                    nav.handleNavigation( fc, null, "/page/globalErrorPage.xhtml" );
                    fc.renderResponse();
                } finally {
                    i.remove();
                }
            }
        }
        getWrapped().handle();

    }
}
