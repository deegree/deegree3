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
package org.deegree.metadata.iso.persistence;

import java.net.URL;

/**
 * Constants
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class TstConstants {

	private TstConstants() {

	}

	private final static String CONFIG_DIR = "configdocs/";

	private final static String DATA_DIR = "metadatarecords/";

	private final static String FILTER_DIR = "filter/";

	public static final URL configURL = TstConstants.class.getResource(CONFIG_DIR + "iso19115_SetUpTables.xml");

	public static final URL configURL_REJECT_FI_FALSE = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_Reject_FI_FALSE.xml");

	public static final URL configURL_REJECT_FI_TRUE = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_Reject_FI_TRUE.xml");

	public static final URL configURL_RS_GEN_FALSE = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_RS_Available_Generate_FALSE.xml");

	public static final URL configURL_RS_GEN_TRUE = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_RS_Available_Generate_TRUE.xml");

	public static final URL configURL_ANYTEXT_ALL = TstConstants.class.getResource(CONFIG_DIR + "anyText_All.xml");

	public static final URL configURL_ANYTEXT_CORE = TstConstants.class.getResource(CONFIG_DIR + "anyText_Core.xml");

	public static final URL configURL_ANYTEXT_CUSTOM = TstConstants.class
		.getResource(CONFIG_DIR + "anyText_Custom.xml");

	public static final URL configURL_COUPLING_ACCEPT = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_coupling_accept.xml");

	public static final URL configURL_COUPLING_Ex_AWARE = TstConstants.class
		.getResource(CONFIG_DIR + "iso19115_coupling_reject.xml");

	public static final URL tst_1 = TstConstants.class.getResource(DATA_DIR + "1.xml");

	public static final URL tst_2 = TstConstants.class.getResource(DATA_DIR + "2.xml");

	public static final URL tst_3 = TstConstants.class.getResource(DATA_DIR + "3.xml");

	public static final URL tst_4 = TstConstants.class.getResource(DATA_DIR + "4.xml");

	public static final URL tst_5 = TstConstants.class.getResource(DATA_DIR + "5.xml");

	public static final URL tst_6 = TstConstants.class.getResource(DATA_DIR + "6.xml");

	public static final URL tst_7 = TstConstants.class.getResource(DATA_DIR + "7.xml");

	public static final URL tst_8 = TstConstants.class.getResource(DATA_DIR + "8.xml");

	public static final URL tst_9 = TstConstants.class.getResource(DATA_DIR + "9.xml");

	public static final URL tst_10 = TstConstants.class.getResource(DATA_DIR + "10.xml");

	public static final URL tst_11 = TstConstants.class.getResource(DATA_DIR + "11.xml");

	public static final URL tst_12 = TstConstants.class.getResource(DATA_DIR + "12_data.xml");

	public static final URL tst_12_2 = TstConstants.class.getResource(DATA_DIR + "12_2_data.xml");

	public static final URL tst_13 = TstConstants.class.getResource(DATA_DIR + "13_service.xml");

	public static final URL fullRecord = TstConstants.class.getResource(DATA_DIR + "filterTstFull.xml");

	public static final URL summaryRecord = TstConstants.class.getResource(DATA_DIR + "filterTstSummary.xml");

	public static final URL briefRecord = TstConstants.class.getResource(DATA_DIR + "filterTstBrief.xml");

	public static final URL propEqualToID = TstConstants.class.getResource(FILTER_DIR + "propEqualToID.xml");

}