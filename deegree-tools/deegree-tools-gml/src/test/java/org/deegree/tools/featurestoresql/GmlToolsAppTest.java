package org.deegree.tools.featurestoresql;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlToolsAppTest {

	@Test
	public void testMain_Empty() throws Exception {
		String[] args = new String[] {};
		GmlToolsApp.main(args);
	}

	@Test
	public void testMain_H() throws Exception {
		String[] args = new String[] { "-h" };
		GmlToolsApp.main(args);
	}

	@Test
	public void testMain_Help() throws Exception {
		String[] args = new String[] { "-help" };
		GmlToolsApp.main(args);
	}

	@Test
	public void testMain_Help2() throws Exception {
		String[] args = new String[] { "--help" };
		GmlToolsApp.main(args);
	}

	@Test
	public void testMain_GmlLoader() throws Exception {
		String[] args = new String[] { "gmlLoader", "--help" };
		GmlToolsApp.main(args);
	}

	@Test
	public void testMain_FeatureStoreConfigLoader() throws Exception {
		String[] args = new String[] { "sqlFeatureStoreConfigCreator", "--help" };
		GmlToolsApp.main(args);
	}

}