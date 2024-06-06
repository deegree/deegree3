package org.deegree.commons.font;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.io.File;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.Test;

public class WorkspaceFontTest {

	private static final File TEST_DIR = new File("src/test/resources/org/deegree/commons/fontworkspace");

	private static Workspace ws = new DefaultWorkspace(TEST_DIR);

	@Test
	public void testFontLoader() {
		WorkspaceFonts wsf = new WorkspaceFonts();
		wsf.init(ws);
		assertThat(WorkspaceFonts.PROCESSED_FILES, not(empty()));
	}

}
