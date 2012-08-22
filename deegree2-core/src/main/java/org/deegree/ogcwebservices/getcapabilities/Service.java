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

import org.deegree.datatypes.CodeList;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.ogcbase.Description;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class Service extends Description {

    private static final long serialVersionUID = -7968109788130292737L;

    private Keywords[] keywords = new Keywords[0];

    private CitedResponsibleParty citedResponsibleParty = null;

    private CodeList fees = null;

    private CodeList[] accessConstraints = new CodeList[0];

    private String version = null;

    private String updateSequence = null;

    /**
     * @param name
     * @param label
     * @param fees
     * @param accessConstraints
     * @throws OGCException
     * @throws OGCWebServiceException
     */
    public Service( String name, String label, CodeList fees, CodeList[] accessConstraints ) throws OGCException,
                            OGCWebServiceException {
        super( name, label );
        setFees( fees );
        this.accessConstraints = accessConstraints;
    }

    /**
     * @param name
     * @param label
     * @param description
     * @param citedResponsibleParty
     * @param fees
     * @param accessConstraints
     * @throws OGCException
     * @throws OGCWebServiceException
     */
    public Service( String name, String label, String description, CitedResponsibleParty citedResponsibleParty,
                    CodeList fees, CodeList[] accessConstraints ) throws OGCException, OGCWebServiceException {
        super( name, label, description, null );
        this.citedResponsibleParty = citedResponsibleParty;
        setFees( fees );
        setAccessConstraints( accessConstraints );
    }

    /**
     * @param description
     * @param name
     * @param metadataLink
     * @param label
     * @param keywords
     * @param citedResponsibleParty
     * @param fees
     * @param accessConstraints
     * @param version
     * @param updateSequence
     * @throws OGCException
     * @throws OGCWebServiceException
     */
    public Service( String description, String name, MetadataLink metadataLink, String label, Keywords[] keywords,
                    CitedResponsibleParty citedResponsibleParty, CodeList fees, CodeList[] accessConstraints,
                    String version, String updateSequence ) throws OGCException, OGCWebServiceException {
        super( name, label, description, metadataLink );
        setKeywords( keywords );
        this.citedResponsibleParty = citedResponsibleParty;
        setFees( fees );
        setAccessConstraints( accessConstraints );
        this.version = version;
        this.updateSequence = updateSequence;
    }

    /**
     * @return Returns the accessConstraints.
     *
     */
    public CodeList[] getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * @param accessConstraints
     *            The accessConstraints to set.
     *
     */
    public void setAccessConstraints( CodeList[] accessConstraints ) {
        if ( accessConstraints == null ) {
            accessConstraints = new CodeList[0];
        }
        this.accessConstraints = accessConstraints;
    }

    /**
     * @return Returns the citedResponsibleParty.
     *
     */
    public CitedResponsibleParty getCitedResponsibleParty() {
        return citedResponsibleParty;
    }

    /**
     * @param citedResponsibleParty
     *            The citedResponsibleParty to set.
     *
     */
    public void setCitedResponsibleParty( CitedResponsibleParty citedResponsibleParty ) {
        this.citedResponsibleParty = citedResponsibleParty;
    }

    /**
     * @return Returns the fees.
     *
     */
    public CodeList getFees() {
        return fees;
    }

    /**
     * @param fees
     *            The fees to set.
     * @throws OGCWebServiceException
     *
     */
    public void setFees( CodeList fees )
                            throws OGCWebServiceException {
        if ( fees == null ) {
            throw new OGCWebServiceException( "fees must be <> null for Service" );
        }
        this.fees = fees;
    }

    /**
     * @return Returns the keywords.
     *
     */
    public Keywords[] getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            The keywords to set.
     *
     */
    public void setKeywords( Keywords[] keywords ) {
        if ( keywords == null ) {
            keywords = new Keywords[0];
        }
        this.keywords = keywords;
    }

    /**
     * @return Returns the updateSequence.
     *
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * @param updateSequence
     *            The updateSequence to set.
     *
     */
    public void setUpdateSequence( String updateSequence ) {
        this.updateSequence = updateSequence;
    }

    /**
     * @return Returns the version.
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     *
     */
    public void setVersion( String version ) {
        this.version = version;
    }

}
