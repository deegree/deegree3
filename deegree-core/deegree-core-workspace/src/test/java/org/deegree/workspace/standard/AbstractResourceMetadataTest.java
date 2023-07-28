/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import java.io.File;
import java.util.List;

import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.graph.ResourceGraph;
import org.deegree.workspace.graph.ResourceNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for resource metadata.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class AbstractResourceMetadataTest {

	@Test
	public void testSorting() {
		final ResourceIdentifier id1 = new DefaultResourceIdentifier(ResourceProvider.class, "md1");
		DefaultResourceLocation loc1 = new DefaultResourceLocation(new File("/tmp/"), id1);
		final ResourceIdentifier id2 = new DefaultResourceIdentifier(ResourceProvider.class, "md2");
		DefaultResourceLocation loc2 = new DefaultResourceLocation(new File("/tmp/"), id2);
		final ResourceIdentifier id3 = new DefaultResourceIdentifier(ResourceProvider.class, "md3");
		DefaultResourceLocation loc3 = new DefaultResourceLocation(new File("/tmp/"), id3);
		final ResourceIdentifier id4 = new DefaultResourceIdentifier(ResourceProvider.class, "md4");
		DefaultResourceLocation loc4 = new DefaultResourceLocation(new File("/tmp/"), id4);
		final AbstractResourceMetadata md4 = new AbstractResourceMetadata(null, loc4, null) {
			@Override
			public ResourceBuilder prepare() {
				return null;
			}
		};
		final AbstractResourceMetadata md3 = new AbstractResourceMetadata(null, loc3, null) {
			@Override
			public ResourceBuilder prepare() {
				dependencies.add(id4);
				return null;
			}
		};
		final AbstractResourceMetadata md2 = new AbstractResourceMetadata(null, loc2, null) {
			@Override
			public ResourceBuilder prepare() {
				dependencies.add(id4);
				return null;
			}
		};
		final AbstractResourceMetadata md1 = new AbstractResourceMetadata(null, loc1, null) {
			@Override
			public ResourceBuilder prepare() {
				dependencies.add(id2);
				dependencies.add(id3);
				return null;
			}
		};

		md1.prepare();
		md2.prepare();
		md3.prepare();
		md4.prepare();

		ResourceGraph graph = new ResourceGraph();

		graph.insertNode(md1);
		ResourceNode node1 = graph.getNode(md1.getIdentifier());

		graph.insertNode(md4);
		ResourceNode node4 = graph.getNode(md4.getIdentifier());

		graph.insertNode(md2);
		ResourceNode node2 = graph.getNode(md2.getIdentifier());

		graph.insertNode(md3);
		ResourceNode node3 = graph.getNode(md3.getIdentifier());

		List deps = node1.getDependencies();
		Assert.assertEquals(2, deps.size());
		Assert.assertTrue(deps.contains(node2));
		Assert.assertTrue(deps.contains(node3));

		deps = node2.getDependencies();
		Assert.assertEquals(1, deps.size());
		Assert.assertTrue(deps.contains(node4));

		deps = node3.getDependencies();
		Assert.assertEquals(1, deps.size());
		Assert.assertTrue(deps.contains(node4));

		deps = node4.getDependencies();
		Assert.assertTrue(deps.isEmpty());
	}

}
