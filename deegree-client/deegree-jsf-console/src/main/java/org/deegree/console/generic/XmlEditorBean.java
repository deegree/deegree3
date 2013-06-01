/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.generic;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

@ManagedBean
@ViewScoped
public class XmlEditorBean implements Serializable {

    private static final long serialVersionUID = -2345424266499294734L;

    private String id;

    private String fileName;

    private String schemaUrl;

    private String nextView;

    private String content;

    private String schemaAsText;

    private String resourceProviderClass;

    public String getFileName() {
        return fileName;
    }

    public void setFileName( String fileName ) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl( String schemaUrl ) {
        this.schemaUrl = schemaUrl;
        if ( schemaUrl != null ) {
            try {
                schemaAsText = IOUtils.toString( new URL( schemaUrl ).openStream(), "UTF-8" );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public String getNextView() {
        return nextView;
    }

    public void setNextView( String nextView ) {
        this.nextView = nextView;
    }

    public String getContent()
                            throws IOException, ClassNotFoundException {
        if ( content == null ) {
            Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
            Class<?> cls = workspace.getModuleClassLoader().loadClass( resourceProviderClass );
            ResourceMetadata<?> md = workspace.getResourceMetadata( (Class) cls, id );
            content = IOUtils.toString( md.getLocation().getAsStream() );
        }
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public String getSchemaAsText() {
        return schemaAsText;
    }

    public void setSchemaAsText( String schemaAsText ) {
        this.schemaAsText = schemaAsText;
    }

    public String cancel() {
        return nextView;
    }

    public String save() {
        activate();
        return nextView;
    }

    public String getResourceProviderClass() {
        return resourceProviderClass;
    }

    public void setResourceProviderClass( String providerClass ) {
        this.resourceProviderClass = providerClass;
    }

    private void activate() {
        try {
            Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
            Class<?> cls = workspace.getModuleClassLoader().loadClass( resourceProviderClass );
            ResourceMetadata<?> md = workspace.getResourceMetadata( (Class) cls, id );

            workspace.destroy( md.getIdentifier() );

            md.getLocation().setContent( IOUtils.toInputStream( content ) );
            workspace.getLocationHandler().persist( md.getLocation() );

            workspace.getLocationHandler().activate( md.getLocation() );
            workspace.prepare( md.getIdentifier() );
            workspace.init( md.getIdentifier(), null );
        } catch ( Exception t ) {
            t.printStackTrace();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to activate resource: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
    }

}
