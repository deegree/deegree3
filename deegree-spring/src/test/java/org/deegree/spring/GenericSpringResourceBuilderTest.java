//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by:

 IDgis bv

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/ 

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;

import org.deegree.spring.TestContext.SingleBean;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GenericSpringResourceBuilderTest {

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext( TestContext.class );

    ApplicationContextHolder contextHolder = new ApplicationContextHolder( null, context );

    Workspace workspace;

    static class TestProvider extends AbstractResourceProvider<SingleBean> {

        @Override
        public String getNamespace() {
            return "http://www.deegree.org/spring/test";
        }

        @Override
        public ResourceMetadata<SingleBean> createFromLocation( Workspace workspace,
                                                                ResourceLocation<SingleBean> location ) {
            return new GenericSpringResourceMetadata<SingleBean>( workspace, location, this,
                                                                  "org.deegree.spring.test.jaxb", SingleBean.class );
        }

        @Override
        public URL getSchema() {
            return null;
        }
    }

    @Before
    public void setUp() {
        workspace = mock( Workspace.class );
        when( workspace.getResource( ApplicationContextHolderProvider.class, "test" ) ).thenReturn( contextHolder );
        when( workspace.getModuleClassLoader() ).thenReturn( GenericSpringResourceBuilderTest.class.getClassLoader() );
    }

    @Test
    public void testInjectMetadata() {

        final TestProvider provider = new TestProvider();

        final InputStream testConfig = GenericSpringResourceBuilderTest.class.getClassLoader().getResourceAsStream( "org/deegree/spring/test.xml" );
        assertNotNull( testConfig );

        @SuppressWarnings("unchecked")
        final ResourceLocation<SingleBean> location = mock( ResourceLocation.class );
        when( location.getAsStream() ).thenReturn( testConfig );
        when( location.getIdentifier() ).thenReturn( new DefaultResourceIdentifier<SingleBean>( TestProvider.class,
                                                                                                "test" ) );

        final ResourceMetadata<SingleBean> metadata = provider.createFromLocation( workspace, location );
        assertNotNull( metadata );

        final ResourceBuilder<SingleBean> builder = metadata.prepare();
        assertNotNull( builder );

        final SingleBean beanFromComtext = context.getBean( SingleBean.class );
        assertNull( beanFromComtext.getMetadata() );

        final SingleBean beanFromBuilder = builder.build();
        assertNotNull( beanFromBuilder );
        assertEquals( beanFromComtext, beanFromBuilder );

        assertEquals( metadata, beanFromBuilder.getMetadata() );
    }
}
