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

package org.deegree.ogcwebservices.wass.saml;

import java.util.ArrayList;
import java.util.Date;

/**
 * Encapsulated data: Assertion element
 *
 * Namespace: http://urn:oasis:names:tc.SAML:1.0:assertion
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Assertion {

    private Conditions conditions = null;

    private ArrayList<Assertion> advices = null;

    private String[] adviceIDs = null;

    private ArrayList<Statement> statements = null;

    private int majorVersion = 0;

    private int minorVersion = 0;

    private String assertionID = null;

    private String issuer = null;

    private Date issueInstant = null;

    /**
     * @param conditions
     * @param advices
     * @param adviceIDs
     * @param statements
     * @param majorVersion
     * @param minorVersion
     * @param assertionID
     * @param issuer
     * @param issueInstant
     */
    public Assertion( Conditions conditions, ArrayList<Assertion> advices, String[] adviceIDs,
                     ArrayList<Statement> statements, int majorVersion, int minorVersion,
                     String assertionID, String issuer, Date issueInstant ) {
        this.conditions = conditions;
        this.advices = advices;
        this.adviceIDs = adviceIDs;
        this.statements = statements;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.assertionID = assertionID;
        this.issuer = issuer;
        this.issueInstant = issueInstant;
    }

    /**
     * @return Returns the adviceIDs.
     */
    public String[] getAdviceIDs() {
        return adviceIDs;
    }

    /**
     * @return Returns the advices.
     */
    public ArrayList<Assertion> getAdvices() {
        return advices;
    }

    /**
     * @return Returns the assertionID.
     */
    public String getAssertionID() {
        return assertionID;
    }

    /**
     * @return Returns the conditions.
     */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * @return Returns the issueInstant.
     */
    public Date getIssueInstant() {
        return issueInstant;
    }

    /**
     * @return Returns the issuer.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @return Returns the majorVersion.
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * @return Returns the minorVersion.
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * @return Returns the statements.
     */
    public ArrayList<Statement> getStatements() {
        return statements;
    }

}
