//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.metadata;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;

/**
 * Encapsulates metadata on a dataset (layer, feature type, etc.) served by an OGC web service (as reported in the
 * capabilities document).
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DatasetMetadata extends Description {

    private final QName name;

    private final String url;

    /**
     * Creates a new {@link DatasetMetadata} instance.
     * 
     * @param name
     *            name of the dataset, must not be <code>null</code>
     * @param titles
     *            titles for the dataset, may be <code>null</code> (no titles)
     * @param abstracts
     *            abstracts for the dataset, may be <code>null</code> (no titles)
     * @param keywords
     *            keywords for the dataset, may be <code>null</code> (no keywords)
     * @param url
     *            metadata url, may be <code>null</code> (no metadata url)
     */
    public DatasetMetadata( QName name, List<LanguageString> titles, List<LanguageString> abstracts,
                            List<Pair<List<LanguageString>, CodeType>> keywords, String url ) {
        super( name.getLocalPart(), titles, abstracts, keywords );
        this.name = name;
        this.url = url;
    }

    /**
     * Returns the qualified name of the dataset.
     * 
     * @return qualified name of the dataset, never <code>null</code>
     */
    public QName getQName() {
        return name;
    }

    /**
     * Returns the metadata url.
     * 
     * @return metadata url, may be <code>null</code> (no metadata url)
     */
    public String getUrl() {
        return url;
    }
}