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
package org.deegree.ogcwebservices.getcapabilities;

import java.net.URL;
import java.util.ArrayList;

import org.deegree.ogcbase.ContactInformation;

/**
 * The interface provides acces to the &lt;CapabilitiesService&gt; element of the Capabilities XML
 * providing general metadata for the service as a whole. It shall include a Name, Title, and Online
 * Resource URL. Optionally, Abstract, Keyword List, Contact Information, Fees, and Access
 * Constraints may be provided. The meaning of most of these elements is defined in [ISO 19115]. The
 * CapabilitiesService Name shall be "ogc:WMS" in the case of a Web Map CapabilitiesService.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */

public class CapabilitiesService {

    private ArrayList<String> keywordList = null;

    private ContactInformation contactInformation = null;

    private String abstract_ = null;

    private String accessConstraints = null;

    private String fees = null;

    private String name = null;

    private String title = null;

    private URL onlineResource = null;

    /**
     * constructor initializing the class with the OGCWebServiceCapabilities
     *
     * @param name
     * @param title
     * @param abstract_
     * @param keywords
     * @param onlineResource
     * @param contactInformation
     * @param fees
     * @param accessConstraints
     */
    public CapabilitiesService( String name, String title, String abstract_, String[] keywords, URL onlineResource,
                                ContactInformation contactInformation, String fees, String accessConstraints ) {
        keywordList = new ArrayList<String>();
        setName( name );
        setTitle( title );
        setAbstract( abstract_ );
        setKeywordList( keywords );
        setOnlineResource( onlineResource );
        setContactInformation( contactInformation );
        setFees( fees );
        setAccessConstraints( accessConstraints );
    }

    /**
     * returns the name of the service. Typically, the Name is a single word used for
     * machine-to-machine communication.
     *
     * @return name of the service
     *
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the service. Typically, the Name is a single word used for
     * machine-to-machine communication.
     *
     * @param name
     *
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Returns the title of the service. The Title is for the benefit of humans. The
     * CapabilitiesService Title is at the discretion of the provider, and should be brief yet
     * descriptive enough to identify this server in a menu with other servers.
     *
     * @see #getName()
     * @return title of the service
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the service. The Title is for the benefit of humans. The
     * CapabilitiesService Title is at the discretion of the provider, and should be brief yet
     * descriptive enough to identify this server in a menu with other servers.
     *
     * @param title
     *
     * @see #getName()
     *
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * The Abstract element allows a descriptive narrative providing more information about the
     * enclosing object.
     *
     * @return the abstract
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * Sets the abstract element
     *
     * @param abstract_
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * A list of keywords or keyword phrases should be included to help catalog searching.
     * Currently, no controlled vocabulary has been defined.
     *
     * @return the keyword list
     *
     */
    public String[] getKeywordList() {
        return keywordList.toArray( new String[keywordList.size()] );
    }

    /**
     * adds the keywordList
     *
     * @param keyword
     */
    public void addKeyword( String keyword ) {
        this.keywordList.add( keyword );
    }

    /**
     * sets the keywordList
     *
     * @param keywordList
     */
    public void setKeywordList( String[] keywordList ) {
        this.keywordList.clear();

        if ( keywordList != null ) {
            for ( int i = 0; i < keywordList.length; i++ ) {
                this.keywordList.add( keywordList[i] );
            }
        }
    }

    /**
     * The OnlineResource element within the CapabilitiesService element can be used, for example,
     * to point to the web site of the service provider. There are other OnlineResource elements
     * used for the URL prefix of each supported operation.
     *
     * @return the link
     *
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * sets URL prefix for get HTTP request method.
     *
     * @param onlineResource
     *
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
    }

    /**
     * Returns informations who to contact for questions about the service. This method returns
     * <tt>null</tt> if no contact informations are available.
     *
     * @return informations who to contact for questions about the service. This method returns
     *         <tt>null</tt> if no contact informations are available.
     *
     */
    public ContactInformation getContactInformation() {
        return contactInformation;
    }

    /**
     * Sets informations who to contact for questions about the service. This method returns
     * <tt>null</tt> if no contact informations are available.
     *
     * @param contactInformation
     *
     */
    public void setContactInformation( ContactInformation contactInformation ) {
        this.contactInformation = contactInformation;
    }

    /**
     * Returns fees assigned to the service. If no fees defined "none" will be returned.
     *
     * @return fees assigned to the service. If no fees defined "none" will be returned.
     *
     */
    public String getFees() {
        return fees;
    }

    /**
     * Sets fees assigned to the service. If no fees defined "none" will be returned.
     *
     * @param fees
     *
     */
    public void setFees( String fees ) {
        this.fees = fees;
    }

    /**
     * Returns access constraints assigned to the service. If no access constraints are defined
     * "none" will be returned.
     *
     * @return the constraints
     *
     */
    public String getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * Sets access constraints assigned to the service. If no access constraints are defined "none"
     * will be returned.
     *
     * @param accessConstraints
     *
     */
    public void setAccessConstraints( String accessConstraints ) {
        this.accessConstraints = accessConstraints;
    }

}
