//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.portal.context;

import java.awt.Rectangle;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.ImageURL;

/**
 * The class encapsulates the general informations common to all types of contexts
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class General {
    private CitedResponsibleParty contactInformation = null;

    private Point[] boundingBox = null;

    private GeneralExtension extension = null;

    private String abstract_ = null;

    private String title = null;

    private BaseURL descriptionURL = null;

    private ImageURL logoURL = null;

    private String[] keywords = null;

    private Rectangle window = null;

    /**
     * Creates a new General object.
     *
     * @param title
     *            title of the context
     * @param abstract_
     *            short description
     * @param window
     * @param contactInformation
     *            informations about creator of the context
     * @param boundingBox
     *            bounding box of the map/data
     * @param descriptionURL
     *            reference to a webpage which contains relevant information to the view.
     * @param logoURL
     *            A reference to an image that might be attached to the context document.
     * @param keywords
     * @param extension
     *            The Extension element is a container tag in which arbitrary vendor specific information can be
     *            included without compromising the ability of other clients to enforce schema validation.
     *
     * @throws ContextException
     */
    public General( String title, String abstract_, Rectangle window, CitedResponsibleParty contactInformation,
                    Point[] boundingBox, BaseURL descriptionURL, ImageURL logoURL, String[] keywords,
                    GeneralExtension extension ) throws ContextException {
        setTitle( title );
        setAbstract( abstract_ );
        setWindow( window );
        setContactInformation( contactInformation );
        setBoundingBox( boundingBox );
        setDescriptionURL( descriptionURL );
        setLogoURL( logoURL );
        setKeywords( keywords );
        setExtension( extension );
    }

    /**
     * An element Window presenting the size in pixels of the map the Context document describes. Negotiation between
     * Context defined aspect ratio and typical client aspect ratio (according to the client's vendor) is left to the
     * client.
     *
     * @param window
     */
    public void setWindow( Rectangle window ) {
        this.window = window;
    }

    /**
     * BoundingBox formatted as defined in the WMS 1.1.1 Specification. It represents the geographic extent that should
     * be presented by the client1.
     *
     * @param boundingBox
     * @throws ContextException
     *             if the bbox is <code>null</code>
     */
    public void setBoundingBox( Point[] boundingBox )
                            throws ContextException {
        if ( boundingBox == null ) {
            throw new ContextException( "A context's bounding box isn't allowed to be null" );
        }

        this.boundingBox = boundingBox;
    }

    /**
     * BoundingBox formatted as defined in the WMS 1.1.1 Specification. It represents the geographic extent that should
     * be presented by the client1.
     *
     * @param boundingBox
     * @throws ContextException
     *             if the bbox is <code>null</code>
     */
    public void setBoundingBox( Envelope boundingBox )
                            throws ContextException {
        if ( boundingBox == null ) {
            throw new ContextException( "A context's bounding box isn't allowed to be null" );
        }

        CoordinateSystem cs = this.boundingBox[0].getCoordinateSystem();
        Point p0 = GeometryFactory.createPoint( boundingBox.getMin().getX(), boundingBox.getMin().getY(), cs );
        Point p1 = GeometryFactory.createPoint( boundingBox.getMax().getX(), boundingBox.getMax().getY(), cs );
        this.boundingBox = new Point[] { p0, p1 };
    }

    /**
     * An element KeywordList that contains one or more Keyword elements which allow search across context collections.
     *
     * @param keywords
     */
    public void setKeywords( String[] keywords ) {
        this.keywords = keywords;
    }

    /**
     * An element Title that contains a human readable title of the Context.
     *
     * @param title
     * @throws ContextException
     *             if the title is <code>null</code>
     */
    public void setTitle( String title )
                            throws ContextException {
        if ( title == null ) {
            throw new ContextException( "A context's title isn't allowed to be null" );
        }

        this.title = title;
    }

    /**
     * An element Abstract that contains a description for the Context document describing its content.
     *
     * @param abstract_
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * A reference to an image that might be attached to the Context document. It can be, for instance, the logo of the
     * project for which the context has been setup, or an overview of the map the context describes. This element
     * contains a link to the image as well as the dimension of the image (in pixels) and its format.
     *
     * @param logoURL
     */
    public void setLogoURL( ImageURL logoURL ) {
        this.logoURL = logoURL;
    }

    /**
     * A URL reference to a webpage which contains relevant information to the view.
     *
     * @param descriptionURL
     */
    public void setDescriptionURL( BaseURL descriptionURL ) {
        this.descriptionURL = descriptionURL;
    }

    /**
     * An element ContactInformation that presents contact information of the creator of the Context document. Contact
     * is described as defined in WMS 1.1.1 Specification.
     *
     * @param contactInformation
     */
    public void setContactInformation( CitedResponsibleParty contactInformation ) {
        this.contactInformation = contactInformation;
    }

    /**
     * The Extension element is a container tag in which arbitrary vendor specific information can be included without
     * compromising the ability of other clients to enforce schema validation.<p/> This tag should not be used to
     * introduce new candidate elements that are intended to promote interoperability. Content in an <Extension> element
     * should not be expected to be preserved in transfers of ViewContext documents between different systems.
     *
     * @param extension
     */
    public void setExtension( GeneralExtension extension ) {
        this.extension = extension;
    }

    /**
     *
     *
     * @return the window
     */
    public Rectangle getWindow() {
        return window;
    }

    /**
     *
     * @return the boundingbox
     */
    public Point[] getBoundingBox() {
        return boundingBox;
    }

    /**
     *
     * @return the keywords
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     *
     * @return the titles
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return the abstract
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     *
     * @return the url to the logo
     */
    public ImageURL getLogoURL() {
        return logoURL;
    }

    /**
     *
     * @return the url to the description
     */
    public BaseURL getDescriptionURL() {
        return descriptionURL;
    }

    /**
     *
     * @return the contactinformation
     */
    public CitedResponsibleParty getContactInformation() {
        return contactInformation;
    }

    /**
     *
     * @return the extension.
     */
    public GeneralExtension getExtension() {
        return extension;
    }

}
