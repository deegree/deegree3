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
package org.deegree.ogcwebservices.wcs.describecoverage;

import java.io.Serializable;

import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.SupportedFormats;
import org.deegree.ogcwebservices.SupportedSRSs;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.deegree.ogcwebservices.wcs.SupportedInterpolations;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.configuration.Extension;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class CoverageOffering extends CoverageOfferingBrief implements Cloneable, Serializable {

    private static final long serialVersionUID = -2280508956895529051L;

    private DomainSet domainSet = null;

    private RangeSet rangeSet = null;

    private SupportedSRSs supportedCRSs = null;

    private SupportedFormats supportedFormats = null;

    private SupportedInterpolations supportedInterpolations = new SupportedInterpolations();

    private Extension extension = null;

    /**
     * @param name
     * @param label
     * @param description
     * @param metadataLink
     * @param lonLatEnvelope
     * @param keywords
     * @param domainSet
     * @param rangeSet
     * @param supportedCRSs
     * @param supportedFormats
     * @param supportedInterpolations
     * @throws OGCException
     * @throws WCSException
     */
    public CoverageOffering( String name, String label, String description, MetadataLink metadataLink,
                             LonLatEnvelope lonLatEnvelope, Keywords[] keywords, DomainSet domainSet,
                             RangeSet rangeSet, SupportedSRSs supportedCRSs, SupportedFormats supportedFormats,
                             SupportedInterpolations supportedInterpolations, Extension extension )
                            throws OGCException, WCSException {
        super( name, label, description, metadataLink, lonLatEnvelope, keywords );
        setDomainSet( domainSet );
        setRangeSet( rangeSet );
        setSupportedCRSs( supportedCRSs );
        setSupportedFormats( supportedFormats );
        setSupportedInterpolations( supportedInterpolations );
        setExtension( extension );
    }

    /**
     * @return Returns the domainSet.
     *
     */
    public DomainSet getDomainSet() {
        return domainSet;
    }

    /**
     * @param domainSet
     *            The domainSet to set.
     */
    public void setDomainSet( DomainSet domainSet )
                            throws WCSException {
        if ( domainSet == null ) {
            throw new WCSException( "domainSet must be <> null for CoverageOffering" );
        }
        this.domainSet = domainSet;
    }

    /**
     * @return Returns the rangeSet.
     */
    public RangeSet getRangeSet() {
        return rangeSet;
    }

    /**
     * @param rangeSet
     *            The rangeSet to set.
     */
    public void setRangeSet( RangeSet rangeSet )
                            throws WCSException {
        if ( rangeSet == null ) {
            throw new WCSException( "rangeSet must be <> null for CoverageOffering" );
        }
        this.rangeSet = rangeSet;
    }

    /**
     * @return Returns the supportedCRSs.
     */
    public SupportedSRSs getSupportedCRSs() {
        return supportedCRSs;
    }

    /**
     * @param supportedCRSs
     *            The supportedCRSs to set.
     */
    public void setSupportedCRSs( SupportedSRSs supportedCRSs )
                            throws WCSException {
        if ( supportedCRSs == null ) {
            throw new WCSException( "supportedCRSs must be <> null for CoverageOffering" );
        }
        this.supportedCRSs = supportedCRSs;
    }

    /**
     * @return Returns the supportedFormats.
     */
    public SupportedFormats getSupportedFormats() {
        return supportedFormats;
    }

    /**
     * @param supportedFormats
     *            The supportedFormats to set.
     */
    public void setSupportedFormats( SupportedFormats supportedFormats )
                            throws WCSException {
        if ( supportedFormats == null ) {
            throw new WCSException( "supportedFormatss must be <> null for CoverageOffering" );
        }
        this.supportedFormats = supportedFormats;
    }

    /**
     * @return Returns the supportedInterpolations.
     */
    public SupportedInterpolations getSupportedInterpolations() {
        return supportedInterpolations;
    }

    /**
     * If <tt>null</tt> will be passed supportedInterpolations will be set to its default.
     *
     * @param supportedInterpolations
     *            The supportedInterpolations to set.
     */
    public void setSupportedInterpolations( SupportedInterpolations supportedInterpolations ) {
        if ( supportedCRSs != null ) {
            this.supportedInterpolations = supportedInterpolations;
        }
    }

    /**
     * @return Returns the extension.
     */
    public Extension getExtension() {
        return extension;
    }

    /**
     * @param extension
     *            The extension to set.
     */
    public void setExtension( Extension extension ) {
        this.extension = extension;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            DomainSet domainSet_ = (DomainSet) domainSet.clone();
            RangeSet rangeSet_ = null;
            if ( rangeSet != null ) {
                rangeSet_ = (RangeSet) rangeSet.clone();
            }

            LonLatEnvelope llenv = (LonLatEnvelope) getLonLatEnvelope().clone();

            return new CoverageOffering( getName(), getLabel(), getDescription(), getMetadataLink(), llenv,
                                         getKeywords(), domainSet_, rangeSet_, supportedCRSs, supportedFormats,
                                         supportedInterpolations, extension );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

}
