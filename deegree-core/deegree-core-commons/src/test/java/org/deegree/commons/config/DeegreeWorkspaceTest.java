package org.deegree.commons.config;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DeegreeWorkspaceTest {

	@Test
	public void getWorkspaceRootFromProps() {
		System.setProperty(DeegreeWorkspace.VAR_WORKSPACE_ROOT, "/user/path/to/where");
		String pathtoworkspaceroot = DeegreeWorkspace.getWorkspaceRoot();
		assertEquals("/user/path/to/where", pathtoworkspaceroot);
		System.clearProperty(DeegreeWorkspace.VAR_WORKSPACE_ROOT);
	}

	@Test
	public void getWorkspaceRootFromEnv() {
		String pathtoworkspaceroot = DeegreeWorkspace.getWorkspaceRoot();
		assertThat(pathtoworkspaceroot, containsString(".deegree"));
	}

}