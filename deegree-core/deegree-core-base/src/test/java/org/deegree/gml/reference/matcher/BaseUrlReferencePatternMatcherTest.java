package org.deegree.gml.reference.matcher;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BaseUrlReferencePatternMatcherTest {

	private BaseUrlReferencePatternMatcher matcher = new BaseUrlReferencePatternMatcher(
			"http://deegree.org/documentation");

	@Test(expected = IllegalArgumentException.class)
	public void test_constructor_null() throws Exception {
		new BaseUrlReferencePatternMatcher(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_constructor_empty() throws Exception {
		new BaseUrlReferencePatternMatcher("");
	}

	@Test
	public void testIsMatching() throws Exception {
		assertThat(matcher.isMatching("http://deegree.org/documentation"), is(true));
		assertThat(matcher.isMatching("http://deegree.org/documentation/webservices"), is(true));

		assertThat(matcher.isMatching("https://deegree.org/documentation"), is(false));
		assertThat(matcher.isMatching("http://www.deegree.org/documentation"), is(false));
		assertThat(matcher.isMatching("http://deegree2.org/documentation"), is(false));

		assertThat(matcher.isMatching(null), is(false));
	}

}