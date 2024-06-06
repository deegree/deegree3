package org.deegree.commons.font;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.io.File;
import org.deegree.commons.utils.TunableParameter;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WorkspaceFontTest {

	private static final File TEST_DIR = new File("src/test/resources/org/deegree/commons/fontworkspace");

	private static Workspace ws = new DefaultWorkspace(TEST_DIR);

	@BeforeClass
	public static void before() {
		TunableParameter.resetCache();
		System.setProperty("deegree.workspace.allow-font-loading", "true");
	}

	@AfterClass
	public static void after() {
		System.setProperty("deegree.workspace.allow-font-loading", "");
		TunableParameter.resetCache();
	}

	@Test
	public void testFontLoader() {
		WorkspaceFonts wsf = new WorkspaceFonts();
		wsf.init(ws);
		assertThat(WorkspaceFonts.PROCESSED_FILES, not(empty()));
	}

}
