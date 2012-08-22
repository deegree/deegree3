// $HeadURL$
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

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.model.crs.CRSFactory.create;

import java.net.URI;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;

/**
 * Represents a feature type in the context of an OGC-WFS 1.1.0 capabilities document.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSFeatureType {

    private static final ILogger LOG = getLogger( WFSFeatureType.class );

    private QualifiedName name;

    private String title;

    private String abstract_;

    private Keywords[] keywords;

    // null -> noSRS in document
    private URI defaultSrs;

    private URI[] otherSrs;

    private Operation[] operations;

    private FormatType[] outputFormats;

    private Envelope[] wgs84BoundingBoxes;

    private MetadataURL[] metadataUrls;

    /**
     * Creates a new instance of <code>WFSFeatureType</code> from the given parameters.
     *
     * @param name
     * @param title
     * @param abstract_
     * @param keywords
     * @param defaultSrs
     * @param otherSrs
     * @param operations
     * @param outputFormats
     * @param wgs84BoundingBoxes
     * @param metadataUrls
     */
    public WFSFeatureType( QualifiedName name, String title, String abstract_, Keywords[] keywords, URI defaultSrs,
                           URI[] otherSrs, Operation[] operations, FormatType[] outputFormats,
                           Envelope[] wgs84BoundingBoxes, MetadataURL[] metadataUrls ) {
        this.name = name;
        this.title = title;
        if (title == null || title.length() == 0) {
            this.title = name.getLocalName();
        }
        this.abstract_ = abstract_;
        this.keywords = keywords;
        this.defaultSrs = defaultSrs;
        this.otherSrs = otherSrs;
        this.operations = operations;
        this.outputFormats = outputFormats;
        this.wgs84BoundingBoxes = wgs84BoundingBoxes;
        this.metadataUrls = metadataUrls;
    }

    /**
     * @return Returns the abstract.
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * @param abstract_
     *            The abstract to set.
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * @return Returns the name.
     */
    public QualifiedName getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( QualifiedName name ) {
        this.name = name;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return Returns the keywords.
     */
    public Keywords[] getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            The keywords to set.
     */
    public void setKeywords( Keywords[] keywords ) {
        this.keywords = keywords;
    }

    /**
     * @return Returns the defaultSrs.
     */
    public URI getDefaultSRS() {
        return defaultSrs;
    }

    /**
     * @param defaultSrs
     *            The defaultSrs to set.
     */
    public void setDefaultSrs( URI defaultSrs ) {
        this.defaultSrs = defaultSrs;
    }

    /**
     * @return Returns the otherSrs.
     */
    public URI[] getOtherSrs() {
        return otherSrs;
    }

    /**
     * @param otherSrs
     *            The otherSrs to set.
     */
    public void setOtherSrs( URI[] otherSrs ) {
        this.otherSrs = otherSrs;
    }

    /**
     * @return Returns the operations.
     */
    public Operation[] getOperations() {
        return operations;
    }

    /**
     * @param operations
     *            The operations to set.
     */
    public void setOperations( Operation[] operations ) {
        this.operations = operations;
    }

    /**
     * @return Returns the outputFormats.
     */
    public FormatType[] getOutputFormats() {
        return outputFormats;
    }

    /**
     * Returns the outputFormat with the given name.
     *
     * @param name
     *            name of requested format
     * @return the outputFormat with the given name if it exists, null otherwise
     */
    public FormatType getOutputFormat( String name ) {
        FormatType formatType = null;
        for ( FormatType outputFormat : this.outputFormats ) {
            if ( outputFormat.getValue().equals( name ) ) {
                formatType = outputFormat;
            }
        }
        return formatType;
    }

    /**
     * @param outputFormats
     *            The outputFormats to set.
     */
    public void setOutputFormats( FormatType[] outputFormats ) {
        this.outputFormats = outputFormats;
    }

    /**
     * @return Returns the wgs84BoundingBoxes.
     */
    public Envelope[] getWgs84BoundingBoxes() {
        return wgs84BoundingBoxes;
    }

    /**
     * @param wgs84BoundingBoxes
     *            The wgs84BoundingBoxes to set.
     */
    public void setWgs84BoundingBoxes( Envelope[] wgs84BoundingBoxes ) {
        this.wgs84BoundingBoxes = wgs84BoundingBoxes;
    }

    /**
     * @return Returns the metadataUrls.
     */
    public MetadataURL[] getMetadataUrls() {
        return metadataUrls;
    }

    /**
     * @param metadataUrls
     *            The metadataUrls to set.
     */
    public void setMetadataUrls( MetadataURL[] metadataUrls ) {
        this.metadataUrls = metadataUrls;
    }

    /**
     * Returns whether the feature type definition is virtual, i.e. all of it's output formats are processed using an
     * (input) XSLT-script.
     *
     * @return true, if all formats are processed using an XSL input script
     */
    public boolean isVirtual() {

        boolean isVirtual = true;
        FormatType[] outputFormats = getOutputFormats();

        for ( int i = 0; i < outputFormats.length; i++ ) {
            if ( !outputFormats[i].isVirtual() ) {
                isVirtual = false;
                break;
            }
        }
        return isVirtual;
    }

    /**
     * Returns whether the feature type supports the given spatial reference system.
     *
     * @param srsName
     *            name of the srs, usually <code>EPSG:xyz</code>
     * @return true, if srs is supported, false otherwise
     */
    public boolean supportsSrs( String srsName ) {
        try {
            CoordinateSystem other = create( srsName );

            if ( this.defaultSrs != null ) {
                CoordinateSystem crs = create( defaultSrs.toString() );
                if ( other.equals( crs ) ) {
                    return true;
                }
                if ( this.otherSrs != null ) {
                    for ( URI srs : this.otherSrs ) {
                        CoordinateSystem crs2 = create( srs.toString() );
                        if ( crs2.equals( other ) ) {
                            return true;
                        }
                    }
                }
            }
        } catch ( UnknownCRSException e ) {
            LOG.logError( "Unknown error", e ); // they really should exist
        }
        return false;
    }
}
