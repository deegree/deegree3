package org.deegree.gml.reference;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlDocumentIdContextTest {

	@Test
	public void getObject_skippedUrl() throws Exception {
		GmlDocumentIdContext gmlDocumentIdContext = new GmlDocumentIdContext(GMLVersion.GML_32);
		gmlDocumentIdContext.setReferencePatternMatcher(mockMatcher(true));

		GMLObject uri = gmlDocumentIdContext.getObject("uri", null);
		assertThat(uri, is(nullValue()));
	}

	@Test(expected = Exception.class)
	public void getObject_notSkippedUrl_butInvalid() throws Exception {
		GmlDocumentIdContext gmlDocumentIdContext = new GmlDocumentIdContext(GMLVersion.GML_32);
		gmlDocumentIdContext.setReferencePatternMatcher(mockMatcher(false));

		gmlDocumentIdContext.getObject("uri", null);
	}

	@Test(expected = Exception.class)
	public void getObject_noMatcher_invalidUrl() throws Exception {
		GmlDocumentIdContext gmlDocumentIdContext = new GmlDocumentIdContext(GMLVersion.GML_32);

		gmlDocumentIdContext.getObject("uri", null);
	}

	private ReferencePatternMatcher mockMatcher(boolean isMatching) {
		ReferencePatternMatcher mock = mock(ReferencePatternMatcher.class);
		when(mock.isMatching(anyString())).thenReturn(isMatching);
		return mock;
	}

}