package org.deegree.tools.featurestoresql.loader;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderConfigurationTest {

	@Test
	public void testLoadApplicationContextAndInitializeBeans() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(GmlLoaderConfiguration.class);
		context.refresh();
		context.close();
	}

}
