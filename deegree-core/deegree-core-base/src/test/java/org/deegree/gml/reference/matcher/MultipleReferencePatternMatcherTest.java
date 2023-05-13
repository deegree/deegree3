package org.deegree.gml.reference.matcher;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MultipleReferencePatternMatcherTest {

	@Test
	public void testIsMatching_oneTrue() throws Exception {
		MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();
		matcher.addMatcherToApply(mockMatcher(true));
		matcher.addMatcherToApply(mockMatcher(false));

		assertThat(matcher.isMatching("test"), is(true));
	}

	@Test
	public void testIsMatching_allTrue() throws Exception {
		MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();
		matcher.addMatcherToApply(mockMatcher(true));
		matcher.addMatcherToApply(mockMatcher(true));

		assertThat(matcher.isMatching("test"), is(true));
	}

	@Test
	public void testIsMatching_allFalse() throws Exception {
		MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();
		matcher.addMatcherToApply(mockMatcher(false));
		matcher.addMatcherToApply(mockMatcher(false));

		assertThat(matcher.isMatching("test"), is(false));
	}

	@Test
	public void testIsMatching_noMatchers() throws Exception {
		MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();

		assertThat(matcher.isMatching("test"), is(false));
	}

	private ReferencePatternMatcher mockMatcher(boolean isMatching) {
		ReferencePatternMatcher mock = mock(ReferencePatternMatcher.class);
		when(mock.isMatching(anyString())).thenReturn(isMatching);
		return mock;
	}

}