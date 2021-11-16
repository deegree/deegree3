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

import org.deegree.spring.TestContext.ContentBean;
import org.deegree.spring.TestContext.NoBean;
import org.deegree.spring.TestContext.SingleBean;

import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractSpringResourceBuilderTest {

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext( TestContext.class );

    ApplicationContextHolder contextHolder = new ApplicationContextHolder( null, context );

    Workspace workspace;

    @Before
    public void setUp() {
        workspace = mock( Workspace.class );
        when( workspace.getResource( ApplicationContextHolderProvider.class, "test" ) ).thenReturn( contextHolder );
    }

    @Test(expected = ResourceInitException.class)
    public void testGetNonExistingBeanType() {
        final AbstractSpringResourceBuilder<NoBean> builder = new AbstractSpringResourceBuilder<NoBean>(
                                                                                                                 workspace,
                                                                                                                 "test" ) {

            @Override
            public NoBean build() {
                return getBean( NoBean.class );
            }
        };
        
        builder.build();
    }
    
    @Test(expected = ResourceInitException.class)
    public void testGetNonExistingBeanName() {
        final AbstractSpringResourceBuilder<SingleBean> builder = new AbstractSpringResourceBuilder<SingleBean>(
                                                                                                                 workspace,
                                                                                                                 "test" ) {

            @Override
            public SingleBean build() {
                return getBean( SingleBean.class, "wrongName" );
            }
        };
        
        builder.build();
    }

    @Test
    public void testGetSingleBean() {
        final AbstractSpringResourceBuilder<SingleBean> builder = new AbstractSpringResourceBuilder<SingleBean>(
                                                                                                                 workspace,
                                                                                                                 "test" ) {

            @Override
            public SingleBean build() {
                return getBean( SingleBean.class );
            }
        };

        assertEquals( context.getBean( SingleBean.class ), builder.build() );
    }

    @Test(expected = ResourceInitException.class)
    public void testGetBeanNameMissing() {
        final AbstractSpringResourceBuilder<ContentBean> builder = new AbstractSpringResourceBuilder<ContentBean>(
                                                                                                                   workspace,
                                                                                                                   "test" ) {

            @Override
            public ContentBean build() {
                return getBean( ContentBean.class );
            }
        };

        builder.build();
    }

    @Test
    public void testGetNamedBean() {
        final AbstractSpringResourceBuilder<ContentBean> builder = new AbstractSpringResourceBuilder<ContentBean>(
                                                                                                                   workspace,
                                                                                                                   "test" ) {

            @Override
            public ContentBean build() {
                return getBean( ContentBean.class, "contentBean0" );
            }
        };

        final ContentBean bean = builder.build();
        assertNotNull( bean );
        assertEquals( "contentBean0", bean.getContent() );
    }

    @Test
    public void testGetConventionallyNamedBean() {
        final AbstractSpringResourceBuilder<ContentBean> builder = new AbstractSpringResourceBuilder<ContentBean>(
                                                                                                                   workspace,
                                                                                                                   "test" ) {

            @Override
            public ContentBean build() {
                return getBean( ContentBean.class, null, "contentBean" );
            }
        };

        final ContentBean bean = builder.build();
        assertNotNull( bean );
        assertEquals( "contentBean", bean.getContent() );
    }
    
    @Test(expected = ResourceInitException.class)
    public void testGetNonConventionallyNamedBean() {
        final AbstractSpringResourceBuilder<ContentBean> builder = new AbstractSpringResourceBuilder<ContentBean>(
                                                                                                                   workspace,
                                                                                                                   "test" ) {

            @Override
            public ContentBean build() {
                return getBean( ContentBean.class, null, "defaultContentBean" );
            }
        };

        builder.build();       
    }
}
