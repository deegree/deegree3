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
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.IOException;
import java.net.URL;

import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.owscommon.OWSCommonCapabilities;
import org.xml.sax.SAXException;

/**
 * Represents the capabilities of an OGC-WFS 1.1.0 compliant service instance.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSCapabilities extends OWSCommonCapabilities {

    private static final long serialVersionUID = -8126209663124432256L;

    private FeatureTypeList featureTypeList;

    private GMLObject[] servesGMLObjectTypeList;

    private GMLObject[] supportsGMLObjectTypeList;

    private FilterCapabilities filterCapabilities;

    /**
     * Creates WFSCapabilities from a URL.
     *
     * @param url
     *            location of the capabilities file
     * @return catalog capabilities
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCapabilitiesException
     */
    public static OGCCapabilities createCapabilities( URL url )
                            throws IOException, SAXException, InvalidCapabilitiesException {

        WFSCapabilitiesDocument capabilitiesDoc = new WFSCapabilitiesDocument();
        capabilitiesDoc.load( url );
        return capabilitiesDoc.parseCapabilities();
    }

    /**
     * Generates a new WFSCapabilities instance from the given parameters.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param featureTypeList
     * @param servesGMLObjectTypeList
     * @param supportsGMLObjectTypeList
     * @param contents
     *            TODO field not verified! Check spec.
     * @param filterCapabilities
     */
    public WFSCapabilities( String version, String updateSequence, ServiceIdentification serviceIdentification,
                            ServiceProvider serviceProvider, OperationsMetadata operationsMetadata,
                            FeatureTypeList featureTypeList, GMLObject[] servesGMLObjectTypeList,
                            GMLObject[] supportsGMLObjectTypeList, Contents contents,
                            FilterCapabilities filterCapabilities ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents );
        this.featureTypeList = featureTypeList;
        this.servesGMLObjectTypeList = servesGMLObjectTypeList;
        this.supportsGMLObjectTypeList = supportsGMLObjectTypeList;
        this.filterCapabilities = filterCapabilities;
    }

    /**
     * Returns the FilterCapabilites section of the capabilities.
     *
     * @return the FilterCapabilites section of the capabilities.
     *
     */
    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * @return Returns the featureTypeList.
     */
    public FeatureTypeList getFeatureTypeList() {
        return this.featureTypeList;
    }

    /**
     * @param featureTypeList
     *            The featureTypeList to set.
     */
    public void setFeatureTypeList( FeatureTypeList featureTypeList ) {
        this.featureTypeList = featureTypeList;
    }

    /**
     * @return Returns the servesGMLObjectTypeList.
     */
    public GMLObject[] getServesGMLObjectTypeList() {
        return servesGMLObjectTypeList;
    }

    /**
     * @param servesGMLObjectTypeList
     *            The servesGMLObjectTypeList to set.
     */
    public void setServesGMLObjectTypeList( GMLObject[] servesGMLObjectTypeList ) {
        this.servesGMLObjectTypeList = servesGMLObjectTypeList;
    }

    /**
     * @return Returns the supportsGMLObjectTypeList.
     */
    public GMLObject[] getSupportsGMLObjectTypeList() {
        return supportsGMLObjectTypeList;
    }

    /**
     * @param supportsGMLObjectTypeList
     *            The supportsGMLObjectTypeList to set.
     */
    public void setSupportsGMLObjectTypeList( GMLObject[] supportsGMLObjectTypeList ) {
        this.supportsGMLObjectTypeList = supportsGMLObjectTypeList;
    }
}
