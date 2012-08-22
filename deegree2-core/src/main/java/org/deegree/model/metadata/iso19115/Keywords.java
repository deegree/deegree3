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
package org.deegree.model.metadata.iso19115;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Keywords implements Serializable {

    private static final long serialVersionUID = -2140118359320160159L;

    private ArrayList<String> keywords;

    private String thesaurusname;

    private TypeCode typecode;

    /**
     * Creates a new instance of Keywords
     *
     */
    private Keywords() {
        this.keywords = new ArrayList<String>();
    }

    /**
     * Creates a new instance of Keywords
     *
     * @param keywords
     */
    public Keywords( String[] keywords ) {
        this();
        this.setKeywords( keywords );
    }

    /**
     * Creates a new instance of Keywords
     *
     * @param keywords
     * @param thesaurusname
     * @param typecode
     */
    public Keywords( String[] keywords, String thesaurusname, TypeCode typecode ) {
        this( keywords );
        this.setThesaurusName( thesaurusname );
        this.setTypeCode( typecode );
    }

    /**
     * @return keywords
     *
     */
    public String[] getKeywords() {
        return keywords.toArray( new String[keywords.size()] );
    }

    /**
     * @see #getKeywords()
     * @param keyword
     */
    public void addKeyword( String keyword ) {
        this.keywords.add( keyword );
    }

    /**
     * @see #getKeywords()
     * @param keywords
     */
    public void setKeywords( String[] keywords ) {
        this.keywords.clear();
        for ( int i = 0; i < keywords.length; i++ ) {
            this.keywords.add( keywords[i] );
        }
    }

    /**
     * @return thesaurus name
     *
     */
    public String getThesaurusName() {
        return thesaurusname;
    }

    /**
     * @see #getThesaurusName()
     * @param thesaurusname
     */
    public void setThesaurusName( String thesaurusname ) {
        this.thesaurusname = thesaurusname;
    }

    /**
     * @return type code
     *
     */
    public TypeCode getTypeCode() {
        return typecode;
    }

    /**
     * @see #getTypeCode()
     * @param typecode
     */
    public void setTypeCode( TypeCode typecode ) {
        this.typecode = typecode;
    }

    /**
     * to String method
     *
     * @return string representation
     */
    public String toString() {
        String ret = null;
        ret = "keywords = " + keywords + "\n";
        ret += "thesaurusname = " + thesaurusname + "\n";
        ret += "typecode = " + typecode + "\n";
        return ret;
    }
}
