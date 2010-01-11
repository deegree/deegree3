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
package org.deegree.record.persistence.genericrecordstore;

/**
 * Keyword representation for records. This class encapsulates the data for representation only. 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class Keyword {
    
    private String keywordType;
    
    private String keyword;
    
    private String thesaurus;

    /**
     * @return the keywordType
     */
    public String getKeywordType() {
        return keywordType;
    }

    /**
     * @param keywordType the keywordType to set
     */
    public void setKeywordType( String keywordType ) {
        this.keywordType = keywordType;
    }

    /**
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword the keyword to set
     */
    public void setKeyword( String keyword ) {
        this.keyword = keyword;
    }

    /**
     * @return the thesaurus
     */
    public String getThesaurus() {
        return thesaurus;
    }

    /**
     * @param thesaurus the thesaurus to set
     */
    public void setThesaurus( String thesaurus ) {
        this.thesaurus = thesaurus;
    }
    
    

}
