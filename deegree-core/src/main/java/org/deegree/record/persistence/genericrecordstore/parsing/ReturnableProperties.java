//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.record.persistence.genericrecordstore.parsing;

import java.util.List;

/**
 * Properties that are just returnable by applicationprofiles such as ISO and Dublin Core. <br>
 * E.g. the "creator" can not be queried but is an element in Dublin Core Metadata profile. So it has to be extracted
 * from an insertable record.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ReturnableProperties {

    private String graphicOverview;

    private String creator;

    private String publisher;

    private String contributor;

    private List<String> rights;

    private String source;

    private List<String> relation;

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator
     *            the creator to set
     */
    public void setCreator( String creator ) {
        this.creator = creator;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher
     *            the publisher to set
     */
    public void setPublisher( String publisher ) {
        this.publisher = publisher;
    }

    /**
     * @return the contributor
     */
    public String getContributor() {
        return contributor;
    }

    /**
     * @param contributor
     *            the contributor to set
     */
    public void setContributor( String contributor ) {
        this.contributor = contributor;
    }

    /**
     * @return the graphicOverview
     */
    public String getGraphicOverview() {
        return graphicOverview;
    }

    /**
     * @param graphicOverview
     *            the graphicOverview to set
     */
    public void setGraphicOverview( String graphicOverview ) {
        this.graphicOverview = graphicOverview;
    }

    /**
     * @return the rights
     */
    public List<String> getRights() {
        return rights;
    }

    /**
     * @param rights
     *            the rights to set
     */
    public void setRights( List<String> rights ) {
        this.rights = rights;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource( String source ) {
        this.source = source;
    }

    /**
     * @return the relation
     */
    public List<String> getRelation() {
        return relation;
    }

    /**
     * @param relation
     *            the relation to set
     */
    public void setRelation( List<String> relation ) {
        this.relation = relation;
    }

}
