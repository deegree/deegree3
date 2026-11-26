package org.deegree.commons.xml.schema;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.deegree.commons.utils.TunableParameter;
import org.deegree.commons.xml.schema.resolver.AuditingSchemaResolver;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RedirectingEntityResolverTest {

	private RedirectingEntityResolver redirectingEntityResolver = new RedirectingEntityResolver();

	@Test
	public void test_redirect_schemasOpengisNet_to_local() {
		String systemId = "http://schemas.opengis.net/csw/2.0.2/record.xsd";
		String redirected = redirectingEntityResolver.redirect(systemId);

		assertThat(redirected, endsWith("/META-INF/schema-cache/schemas.opengis.net/csw/2.0.2/record.xsd"));
	}

	@Test
	public void test_redirect_inspire_http_to_https() {
		String systemId = "http://inspire.ec.europa.eu/schemas/base/3.3/BaseTypes.xsd";
		String redirected = redirectingEntityResolver.redirect(systemId);

		assertThat(redirected, is("https://inspire.ec.europa.eu/schemas/base/3.3/BaseTypes.xsd"));
	}

	@Test
	public void testBlockedSchemaAccess() throws IOException {
		String systemId = "https://example.com/unwanted/non-existing-schema.xsd";
		URL systemIdURL = new URL(systemId);
		TestableXMLResourceIdentifier xmlResourceIdentifier = new TestableXMLResourceIdentifier();
		xmlResourceIdentifier.setExpandedSystemId(systemId);

		// disabled by default (enforce service loader to be already populated)
		assertThat(systemId, is(equalTo(redirectingEntityResolver.redirect(systemId))));
		XMLInputSource resolvedEntity = redirectingEntityResolver.resolveEntity(xmlResourceIdentifier);
		assertThat(resolvedEntity, is(notNullValue()));
		assertThat(resolvedEntity.getSystemId(), is(equalTo(systemId)));

		System.setProperty("deegree.commons.xml.schema.block-non-cached-schemas", "true");
		TunableParameter.resetCache();

		final AuditingSchemaResolver resolverA = new AuditingSchemaResolver();

		Assert.assertThrows(IllegalArgumentException.class, () -> resolverA.resolve(systemIdURL));

		// reset to default
		System.setProperty("deegree.commons.xml.schema.block-non-cached-schemas", "false");
		TunableParameter.resetCache();
		AuditingSchemaResolver resolverB = new AuditingSchemaResolver();

		assertThat(resolverB.resolve(systemIdURL), is(nullValue()));
	}

	static class TestableXMLResourceIdentifier implements XMLResourceIdentifier {

		private String expandedSystemId;

		@Override
		public void setPublicId(String publicId) {
			throw new IllegalArgumentException();
		}

		@Override
		public String getPublicId() {
			throw new IllegalArgumentException();
		}

		@Override
		public void setExpandedSystemId(String systemId) {
			expandedSystemId = systemId;
		}

		@Override
		public String getExpandedSystemId() {
			return expandedSystemId;
		}

		@Override
		public void setLiteralSystemId(String systemId) {
			throw new IllegalArgumentException();
		}

		@Override
		public String getLiteralSystemId() {
			throw new IllegalArgumentException();
		}

		@Override
		public void setBaseSystemId(String systemId) {
			throw new IllegalArgumentException();
		}

		@Override
		public String getBaseSystemId() {
			throw new IllegalArgumentException();
		}

		@Override
		public void setNamespace(String namespace) {
			throw new IllegalArgumentException();
		}

		@Override
		public String getNamespace() {
			throw new IllegalArgumentException();
		}
	}

}
