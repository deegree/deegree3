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

package org.deegree.protocol.wfs.transaction;

import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.persistence.FeatureStoreTransaction.IDGenMode;

/**
 * Represents a WFS <code>Insert</code> operation (part of a {@link Transaction} request).
 * <p>
 * NOTE: Due to the possible size of <code>Insert</code> operations, this...
 * </p>
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Insert extends TransactionOperation {

    private IDGenMode idGenMode;

    private String inputFormat;

    private String srsName;

    private XMLStreamReader encodedFeatures;

    /**
     * Creates a new {@link Insert} instance.
     * 
     * @param handle
     *            identifier for the operation, can be null
     * @param idGenMode
     *            controls how identifiers for newly inserted feature instances are generated, can be null (unspecified)
     * @param inputFormat
     *            the format of encoded feature instances, may be null (unspecified)
     * @param srsName
     *            the coordinate references system used for the geometries, may be null (unspecified)
     * @param encodedFeatures
     *            provides access to the XML encoded features, cursor must point at the <code>START_ELEMENT</code> event
     *            of the first <code>Feature</code> to be inserted, must not be null
     */
    public Insert( String handle, IDGenMode idGenMode, String inputFormat, String srsName,
                   XMLStreamReader encodedFeatures ) {
        super( handle );
        this.idGenMode = idGenMode;
        this.inputFormat = inputFormat;
        this.srsName = srsName;
        this.encodedFeatures = encodedFeatures;
    }

    /**
     * Always returns {@link TransactionOperation.Type#INSERT}.
     * 
     * @return {@link TransactionOperation.Type#INSERT}
     */
    @Override
    public Type getType() {
        return Type.INSERT;
    }

    /**
     * Returns the mode for the generation of feature identifiers.
     * 
     * @return the mode for id generation, can be null (unspecified)
     */
    public IDGenMode getIdGen() {
        return idGenMode;
    }

    /**
     * Returns the format of encoded feature instances.
     * 
     * @return the format of encoded feature instances, may be null (unspecified)
     */
    public String getInputFormat() {
        return inputFormat;
    }

    /**
     * Returns the specified coordinate system for the geometries to be inserted.
     * 
     * @return the specified coordinate system, can be null (unspecified)
     */
    public String getSRSName() {
        return srsName;
    }

    /**
     * Returns an <code>XMLStreamReader</code> that provides access to the encoded features to be inserted.
     * <p>
     * NOTE: The client <b>must</b> read this stream exactly once and exactly up to the next tag event after the closing
     * element of the feature/feature collection, i.e. the END_ELEMENT of the surrounding <code>Insert</code> element.
     * </p>
     * 
     * @return <code>XMLStreamReader</code> that provides access to the XML encoded features, cursor must point at the
     *         <code>START_ELEMENT</code> event of the first <code>Feature</code> to be inserted, never null
     */
    public XMLStreamReader getFeatures() {
        return encodedFeatures;
    }
}
