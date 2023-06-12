package org.deegree.workspace.standard;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DefaultResourceLocationTest {

	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	private static File resourceFile;

	private static File resourceFileInSubfolder;

	@BeforeClass
	public static void initFolder() throws IOException {
		resourceFile = folder.newFile("test.xml");
		folder.newFolder("test");
		resourceFileInSubfolder = folder.newFile("test/test.xml");
	}

	@Test
	public void testActivate() {
		DefaultResourceLocation<Resource> resourceDefaultResourceLocation = new DefaultResourceLocation<>(
				resourceFileInSubfolder, createIdentifier("test"));

		File resourceFile = resourceDefaultResourceLocation.getAsFile();
		MatcherAssert.assertThat(resourceFile, is(resourceFileInSubfolder));
		resourceDefaultResourceLocation.activate();

		File activatedResourceFile = resourceDefaultResourceLocation.getAsFile();
		MatcherAssert.assertThat(activatedResourceFile, is(resourceFileInSubfolder));
	}

	@Test
	public void testActivate_InSubDirectory() {
		DefaultResourceLocation<Resource> resourceDefaultResourceLocation = new DefaultResourceLocation<>(
				resourceFileInSubfolder, createIdentifier("test/test"));

		File resourceFile = resourceDefaultResourceLocation.getAsFile();
		MatcherAssert.assertThat(resourceFile, is(resourceFileInSubfolder));
		resourceDefaultResourceLocation.activate();

		File activatedResourceFile = resourceDefaultResourceLocation.getAsFile();
		MatcherAssert.assertThat(activatedResourceFile, is(resourceFileInSubfolder));
	}

	private DefaultResourceIdentifier<Resource> createIdentifier(String identifier) {
		return new DefaultResourceIdentifier<>((Class<? extends ResourceProvider<Resource>>) providerClass().getClass(),
				identifier);
	}

	private ResourceProvider<Resource> providerClass() {
		return new ResourceProvider<Resource>() {

			@Override
			public String getNamespace() {
				return null;
			}

			@Override
			public ResourceMetadata<Resource> read(Workspace workspace, ResourceLocation<Resource> location) {
				return null;
			}

			@Override
			public List<ResourceMetadata<Resource>> getAdditionalResources(Workspace workspace) {
				return null;
			}
		};
	}

}
