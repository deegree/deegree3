//$HeadURL$
/*
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
public class Citation {

    private ArrayList<String> alternatetitle = null;

    private ArrayList<CitedResponsibleParty> citedresponsibleparty = null;

    private ArrayList<Date> date = null;

    private String edition = null;

    private String editiondate = null;

    private ArrayList<String> identifier = null;

    private String isbn = null;

    private String issn = null;

    private String issueidentification = null;

    private String seriesname = null;

    private String title = null;

    /**
     *
     * @param alternatetitle
     * @param citedresponsibleparty
     * @param date
     * @param edition
     * @param editiondate
     * @param identifier
     * @param isbn
     * @param issn
     * @param issueidentification
     * @param seriesname
     * @param title
     */
    public Citation( String[] alternatetitle, CitedResponsibleParty[] citedresponsibleparty, Date[] date,
                     String edition, String editiondate, String[] identifier, String isbn, String issn,
                     String issueidentification, String seriesname, String title ) {

        this.alternatetitle = new ArrayList<String>();
        this.citedresponsibleparty = new ArrayList<CitedResponsibleParty>();
        this.date = new ArrayList<Date>();
        this.identifier = new ArrayList<String>();

        setAlternateTitle( alternatetitle );
        setCitedResponsibleParty( citedresponsibleparty );
        setDate( date );
        setEdition( edition );
        setEditionDate( editiondate );
        setIdentifier( identifier );
        setIsbn( isbn );
        setIssn( issn );
        setIssueIdentification( issueidentification );
        setSeriesName( seriesname );
        setTitle( title );
    }

    /**
     * @return aleternate titles
     *
     */
    public String[] getAlternateTitle() {
        return alternatetitle.toArray( new String[alternatetitle.size()] );
    }

    /**
     * @param alternatetitle
     * @see Citation#getAlternateTitle()
     */
    public void addAlternateTitle( String alternatetitle ) {
        this.alternatetitle.add( alternatetitle );
    }

    /**
     * @param alternatetitle
     * @see Citation#getAlternateTitle()
     */
    public void setAlternateTitle( String[] alternatetitle ) {
        this.alternatetitle.clear();
        for ( int i = 0; i < alternatetitle.length; i++ ) {
            this.alternatetitle.add( alternatetitle[i] );
        }
    }

    /**
     * @return Cited Responsible Parties
     */
    public CitedResponsibleParty[] getCitedResponsibleParty() {
        return citedresponsibleparty.toArray( new CitedResponsibleParty[citedresponsibleparty.size()] );
    }

    /**
     * @see Citation#getCitedResponsibleParty()
     * @param citedresponsibleparty
     */
    public void addCitedResponsibleParty( CitedResponsibleParty citedresponsibleparty ) {
        this.citedresponsibleparty.add( citedresponsibleparty );
    }

    /**
     * @see Citation#getCitedResponsibleParty()
     * @param citedresponsibleparty
     */
    public void setCitedResponsibleParty( CitedResponsibleParty[] citedresponsibleparty ) {
        this.citedresponsibleparty.clear();
        for ( int i = 0; i < citedresponsibleparty.length; i++ ) {
            this.citedresponsibleparty.add( citedresponsibleparty[i] );
        }
    }

    /**
     * @return dates
     *
     */
    public Date[] getDate() {
        return date.toArray( new Date[date.size()] );
    }

    /**
     * @param date
     * @see Citation#getDate()
     */
    public void addDate( Date date ) {
        this.date.add( date );
    }

    /**
     * @param date
     * @see Citation#getDate()
     */
    public void setDate( Date[] date ) {
        this.date.clear();
        for ( int i = 0; i < date.length; i++ ) {
            this.date.add( date[i] );
        }
    }

    /**
     * @return String
     *
     */
    public String getEdition() {
        return edition;
    }

    /**
     * @see Citation#getEdition()
     * @param edition
     */
    public void setEdition( String edition ) {
        this.edition = edition;
    }

    /**
     * @return edition date
     */
    public String getEditionDate() {
        return editiondate;
    }

    /**
     * @see Citation#getEditionDate()
     * @param editiondate
     */
    public void setEditionDate( String editiondate ) {
        this.editiondate = editiondate;
    }

    /**
     * @return identifiers
     *
     */
    public String[] getIdentifier() {
        return identifier.toArray( new String[identifier.size()] );
    }

    /**
     * @see Citation#getIdentifier()
     * @param identifier
     */
    public void addIdentifier( String identifier ) {
        this.identifier.add( identifier );
    }

    /**
     * @see Citation#getIdentifier()
     * @param identifier
     */
    public void setIdentifier( String[] identifier ) {
        this.identifier.clear();
        for ( int i = 0; i < identifier.length; i++ ) {
            this.identifier.add( identifier[i] );
        }
    }

    /**
     *
     * @return isbn
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * @see Citation#getIsbn()
     * @param isbn
     */
    public void setIsbn( String isbn ) {
        this.isbn = isbn;
    }

    /**
     * @return issn
     *
     */
    public String getIssn() {
        return issn;
    }

    /**
     * @see Citation#getIssn()
     * @param issn
     */
    public void setIssn( String issn ) {
        this.issn = issn;
    }

    /**
     * @return Issue Identification
     *
     */
    public String getIssueIdentification() {
        return issueidentification;
    }

    /**
     * @see Citation#getIssueIdentification()
     * @param issueidentification
     */
    public void setIssueIdentification( String issueidentification ) {
        this.issueidentification = issueidentification;
    }

    /**
     * @return sereis name
     *
     */
    public String getSeriesName() {
        return seriesname;
    }

    /**
     * @see Citation#getSeriesName()
     * @param seriesname
     */
    public void setSeriesName( String seriesname ) {
        this.seriesname = seriesname;
    }

    /**
     * @return title
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * @see Citation#getTitle()
     *
     * @param title
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "alternatetitle = " + alternatetitle + "\n";
        ret += "citedresponsibleparty = " + citedresponsibleparty + "\n";
        ret += "date = " + date + "\n";
        ret += "edition = " + edition + "\n";
        ret += "editiondate = " + editiondate + "\n";
        ret += "identifier = " + identifier + "\n";
        ret += "isbn = " + isbn + "\n";
        ret += "issn = " + issn + "\n";
        ret += "issueidentification = " + issueidentification + "\n";
        ret += "seriesname = " + seriesname + "\n";
        ret += "title = " + title + "\n";
        return ret;
    }

}
