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
package org.deegree.portal.cataloguemanager.control;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SearchResultBean {

    private String modified;

    private String title;

    private String abstr;

    private String csw;

    private String id;

    private String hierarchyLevel;

    private String bbox;

    private String overview;

    /**
     * @return the modified
     */
    public String getModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified( String modified ) {
        this.modified = modified;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return the abstr
     */
    public String getAbstr() {
        return abstr;
    }

    /**
     * @param abstr
     *            the abstr to set
     */
    public void setAbstr( String abstr ) {
        this.abstr = abstr;
    }

    /**
     * @return the csw
     */
    public String getCsw() {
        return csw;
    }

    /**
     * @param csw
     *            the csw to set
     */
    public void setCsw( String csw ) {
        this.csw = csw;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId( String id ) {
        this.id = id;
    }

    /**
     * @return the hierarchyLevel
     */
    public String getHierarchyLevel() {
        return hierarchyLevel;
    }

    /**
     * @param hierarchyLevel
     *            the hierarchyLevel to set
     */
    public void setHierarchyLevel( String hierarchyLevel ) {
        this.hierarchyLevel = hierarchyLevel;
    }

    /**
     * @return the bbox
     */
    public String getBbox() {
        return bbox;
    }

    /**
     * @param bbox
     *            the bbox to set
     */
    public void setBbox( String bbox ) {
        this.bbox = bbox;
    }

    /**
     * @return the overview
     */
    public String getOverview() {
        return overview;
    }

    /**
     * @param overview
     *            the overview to set
     */
    public void setOverview( String overview ) {
        this.overview = overview;
    }

}
