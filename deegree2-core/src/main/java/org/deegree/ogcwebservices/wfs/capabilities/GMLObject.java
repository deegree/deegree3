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

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.metadata.iso19115.Keywords;

/**
 * <code>GMLObject</code> encapsulate a gml object
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class GMLObject {

    private QualifiedName name;

    private String title;

    private String abstract_;

    private Keywords[] keywords;

    private FormatType[] outputFormats;

    /**
     * @param name of the object
     * @param title
     * @param abstract_
     * @param keywords
     * @param outputFormats
     */
    public GMLObject(QualifiedName name, String title, String abstract_,
            Keywords[] keywords, FormatType[] outputFormats) {
        this.name = name;
        this.title = title;
        this.abstract_ = abstract_;
        this.keywords = keywords;
        this.outputFormats = outputFormats;
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
    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
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
    public void setKeywords(Keywords[] keywords) {
        this.keywords = keywords;
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
    public void setName(QualifiedName name) {
        this.name = name;
    }

    /**
     * @return Returns the outputFormats.
     */
    public FormatType[] getOutputFormats() {
        return outputFormats;
    }

    /**
     * @param outputFormats
     *            The outputFormats to set.
     */
    public void setOutputFormats(FormatType[] outputFormats) {
        this.outputFormats = outputFormats;
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
    public void setTitle(String title) {
        this.title = title;
    }
}
