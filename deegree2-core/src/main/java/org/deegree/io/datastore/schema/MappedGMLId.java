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
package org.deegree.io.datastore.schema;

import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.io.datastore.idgenerator.IdGenerator;
import org.deegree.io.datastore.schema.content.MappingField;

/**
 * Defines how values for "gml:id" attributes for a certain feature type are generated and
 * which columns are used to store it.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedGMLId {

    /**
     * Used to represent the 'identityPart' information.
     */
    public static enum IDPART_INFO {

        /**
         * No 'identityPart' information available.
         */
        noIDInfo,

        /**
         * Feature id determines feature identity solely.
         */
        isIDPart,

        /**
         * Feature id is not part of the feature identity -> property values determine if
         * two features are 'equal'.
         */
        notIDPart
    }

    private String prefix;

    private MappingField[] idFields;

    private String separator;

    private IdGenerator idGenerator;

    private IDPART_INFO idPartInfo;

    private boolean isIdentityPart;

    /**
     * Creates a new instance of <code>MappedGMLId</code> from the given parameters.
     *
     * @param prefix
     * @param separator
     * @param idFields
     * @param idGenerator
     * @param idPartInfo
     */
    public MappedGMLId( String prefix, String separator, MappingField[] idFields,
                       IdGenerator idGenerator, IDPART_INFO idPartInfo ) {
        this.prefix = prefix;
        this.separator = separator;
        this.idFields = idFields;
        this.idGenerator = idGenerator;
        this.idPartInfo = idPartInfo;
        if (this.idPartInfo == IDPART_INFO.isIDPart) {
        	setIdentityPart(true);
        }
    }

    /**
     * Returns the number of <code>MappingField</code>s that constitute the "gml:id".
     *
     * @return the number of MappingFields
     */
    public int getKeySize() {
        return this.idFields.length;
    }

    /**
     * Returns the <code>MappingField</code>s that are used to build the "gml:id".
     *
     * @return the id fields
     */
    public MappingField[] getIdFields() {
        return this.idFields;
    }

    /**
     * Returns the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Returns the separator.
     *
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Returns whether the configuration explicitly defines that the id has to be considered when
     * two features are checked for equality.
     *
     * @return 'identityPart' information
     */
    public IDPART_INFO getIdPartInfo () {
        return this.idPartInfo;
    }

    /**
     * Returns whether the id has to be considered when two features are checked for equality.
     *
     * @return true, if the feature id is part of the feature's identity
     */
    public boolean isIdentityPart () {
        return this.isIdentityPart;
    }

    /**
     * Sets the 'identiyPart' information.
     *
     * @param isIdentityPart
     *          set to true, if feature id should determine feature's identity solely
     */
    public void setIdentityPart (boolean isIdentityPart) {
        this.isIdentityPart = isIdentityPart;
    }

    /**
     * TODO remove this. Just a quick hack to make ParentIdGenerator work...
     *
     * @return IdGenerator that is used to generate new feature ids
     */
    public IdGenerator getIdGenerator () {
        return this.idGenerator;
    }

    /**
     * Generates a new and unique feature identifier.
     *
     * @param ft
     * @param ta
     * @return a new and unique feature identifier.
     * @throws IdGenerationException
     */
    FeatureId generateFid (MappedFeatureType ft, DatastoreTransaction ta) throws IdGenerationException {
        return this.idGenerator.getNewId(ft, ta);
    }
}
