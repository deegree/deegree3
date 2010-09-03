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
package org.deegree.client.core.component;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.deegree.client.core.model.BBox;
import org.deegree.client.core.utils.MessageUtils;

/**
 * <code>HtmlInputBBox</code>
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */

@FacesComponent(value = "HtmlInputBBox")
public class HtmlInputBBox extends UISelectOne {

    private String styleClass;

    private String crsLabel;

    private int crsSize;

    private String minxLabel;

    private String minyLabel;

    private String maxxLabel;

    private String maxyLabel;

    public HtmlInputBBox() {
        setRendererType( "org.deegree.InputBBox" );
    }

    public void setStyleClass( String styleClass ) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getCrsLabel() {
        return crsLabel;
    }

    public void setCrsLabel( String crsLabel ) {
        this.crsLabel = crsLabel;
    }

    public String getMinxLabel() {
        return minxLabel;
    }

    public void setMinxLabel( String minxLabel ) {
        this.minxLabel = minxLabel;
    }

    public String getMinyLabel() {
        return minyLabel;
    }

    public void setMinyLabel( String minyLabel ) {
        this.minyLabel = minyLabel;
    }

    public String getMaxxLabel() {
        return maxxLabel;
    }

    public void setMaxxLabel( String maxxLabel ) {
        this.maxxLabel = maxxLabel;
    }

    public String getMaxyLabel() {
        return maxyLabel;
    }

    public void setMaxyLabel( String maxyLabel ) {
        this.maxyLabel = maxyLabel;
    }

    public void setCrsSize( int crsSize ) {
        this.crsSize = crsSize;
    }

    public int getCrsSize() {
        return crsSize;
    }

    @Override
    public BBox getValue() {
        Object value = super.getValue();
        if ( value == null ) {
            return null;
        }
        if ( !( value instanceof BBox ) ) {
            throw new FacesException( "value of HtmlInputBBox must be a org.deegree.client.core.model.BBox" );
        }
        return (BBox) super.getValue();
    }

    @Override
    protected void validateValue( FacesContext context, Object value ) {

        if ( !isValid() ) {
            return;
        }

        if ( isRequired() && isBBoxEmpty( value ) ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputBBox.REQUIRED",
                                                                 getClientId() );
            context.addMessage( getClientId( context ), message );
            setValid( false );
            return;
        }

        BBox bbox = (BBox) value;

        if ( bbox.getCrs() == null ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputBBox.INVALID_CRS",
                                                                 getClientId() );
            context.addMessage( getClientId( context ), message );
            setValid( false );
        }
        if ( bbox.getLower() == null || bbox.getLower().length != 2 || Double.isNaN( bbox.getLower()[0] )
             || Double.isNaN( bbox.getLower()[1] ) ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputBBox.INVALID_MINVALUES",
                                                                 getClientId() );
            context.addMessage( getClientId( context ), message );
            setValid( false );
        }
        if ( bbox.getUpper() == null || bbox.getUpper().length != 2 || Double.isNaN( bbox.getUpper()[0] )
             || Double.isNaN( bbox.getUpper()[1] ) ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputBBox.INVALID_MAXVALUES",
                                                                 getClientId() );
            context.addMessage( getClientId( context ), message );
            setValid( false );
        }

        if ( bbox.getLower() != null && bbox.getLower().length == 2 && bbox.getUpper() != null
             && bbox.getUpper().length == 2 && bbox.getLower()[0] < bbox.getUpper()[0]
             && bbox.getLower()[1] < bbox.getUpper()[1] ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputBBox.INVALID_BBOX",
                                                                 getClientId() );
            context.addMessage( getClientId( context ), message );
            setValid( false );
        }
    }

    private boolean isBBoxEmpty( Object value ) {
        if ( value == null ) {
            return true;
        }
        if ( !( value instanceof BBox ) ) {
            return true;
        }
        BBox bbox = (BBox) value;
        if ( ( bbox.getLower() == null || bbox.getLower().length == 0 || ( Double.isNaN( bbox.getLower()[0] ) && Double.isNaN( bbox.getLower()[1] ) ) )
             && ( bbox.getUpper() == null || bbox.getUpper().length == 0 || ( Double.isNaN( bbox.getUpper()[0] ) && Double.isNaN( bbox.getUpper()[1] ) ) ) ) {
            return true;
        }
        return false;
    }

}
