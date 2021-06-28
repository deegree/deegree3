package org.deegree.commons.xml.schema;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
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

        assertThat(redirected, endsWith("/META-INF/SCHEMAS_OPENGIS_NET/csw/2.0.2/record.xsd"));
    }

    @Test
    public void test_redirect_inspire_http_to_https() {
        String systemId = "http://inspire.ec.europa.eu/schemas/base/3.3/BaseTypes.xsd";
        String redirected = redirectingEntityResolver.redirect(systemId);

        assertThat(redirected, is("https://inspire.ec.europa.eu/schemas/base/3.3/BaseTypes.xsd"));
    }

}
