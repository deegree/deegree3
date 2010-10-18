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
package org.deegree.metadata.persistence.iso;

import java.net.URL;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TstConstants {

    private TstConstants() {

    }

    static final URL configURL = TstConstants.class.getResource( "configdocs/iso19115_SetUpTables.xml" );

    static final URL configURL_REJECT_FI_FALSE = TstConstants.class.getResource( "configdocs/iso19115_Reject_FI_FALSE.xml" );

    static final URL configURL_REJECT_FI_TRUE = TstConstants.class.getResource( "configdocs/iso19115_Reject_FI_TRUE.xml" );

    static final URL configURL_RS_GEN_FALSE = TstConstants.class.getResource( "configdocs/iso19115_RS_Available_Generate_FALSE.xml" );

    static final URL configURL_RS_GEN_TRUE = TstConstants.class.getResource( "configdocs/iso19115_RS_Available_Generate_TRUE.xml" );

    static final URL tst_1 = TstConstants.class.getResource( "metadatarecords/1.xml" );

    static final URL tst_2 = TstConstants.class.getResource( "metadatarecords/2.xml" );

    static final URL tst_3 = TstConstants.class.getResource( "metadatarecords/3.xml" );

    static final URL tst_4 = TstConstants.class.getResource( "metadatarecords/4.xml" );

    static final URL tst_5 = TstConstants.class.getResource( "metadatarecords/5.xml" );

    static final URL tst_6 = TstConstants.class.getResource( "metadatarecords/6.xml" );

    static final URL tst_7 = TstConstants.class.getResource( "metadatarecords/7.xml" );

    static final URL tst_8 = TstConstants.class.getResource( "metadatarecords/8.xml" );

    static final URL tst_9 = TstConstants.class.getResource( "metadatarecords/9.xml" );

    static final URL tst_10 = TstConstants.class.getResource( "metadatarecords/10.xml" );

    static final URL tst_11 = TstConstants.class.getResource( "metadatarecords/11.xml" );

    static final URL fullRecord = TstConstants.class.getResource( "metadatarecords/filterTstFull.xml" );

    static final URL summaryRecord = TstConstants.class.getResource( "metadatarecords/filterTstSummary.xml" );

    static final URL briefRecord = TstConstants.class.getResource( "metadatarecords/filterTstBrief.xml" );

    static final URL propEqualToID = TstConstants.class.getResource( "filter/propEqualToID.xml" );

}