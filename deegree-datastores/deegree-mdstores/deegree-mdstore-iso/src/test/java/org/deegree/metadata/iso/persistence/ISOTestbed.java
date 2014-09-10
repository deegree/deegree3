//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/test/java/org/deegree/metadata/iso/persistence/ISOTestbed.java $
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

import org.deegree.metadata.iso.persistence.inspectors.InspectorCouplingTest;
import org.deegree.metadata.iso.persistence.inspectors.InspectorIdentifierTest;
import org.deegree.metadata.iso.persistence.parsing.ParseISOTest;
import org.deegree.metadata.iso.persistence.sql.AnyTextHelperTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Testsuite for all the ISO testCases.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: lbuesching $
 * 
 * @version $Revision: 30725 $, $Date: 2011-05-09 15:04:20 +0200 (Mo, 09. Mai 2011) $
 */
@RunWith(Suite.class)
@SuiteClasses({ InspectorIdentifierTest.class, ParseISOTest.class, InspectorCouplingTest.class,
               AnyTextHelperTest.class, ISORecordSerializeTest.class, ISOMetadatStoreTransactionTest.class })
public class ISOTestbed {

}
