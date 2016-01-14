package org.deegree.workspace.graph;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ResourceGraphTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testResourceGraph() {
        final ResourceIdentifier id1 = new DefaultResourceIdentifier( ResourceProvider.class, "md1" );
        DefaultResourceLocation loc1 = new DefaultResourceLocation( new File( "/tmp/" ), id1 );
        final ResourceIdentifier id2 = new DefaultResourceIdentifier( ResourceProvider.class, "md2" );
        DefaultResourceLocation loc2 = new DefaultResourceLocation( new File( "/tmp/" ), id2 );
        final ResourceIdentifier id3 = new DefaultResourceIdentifier( ResourceProvider.class, "md3" );
        DefaultResourceLocation loc3 = new DefaultResourceLocation( new File( "/tmp/" ), id3 );
        final ResourceIdentifier id4 = new DefaultResourceIdentifier( ResourceProvider.class, "md4" );
        DefaultResourceLocation loc4 = new DefaultResourceLocation( new File( "/tmp/" ), id4 );
        final ResourceIdentifier id5 = new DefaultResourceIdentifier( ResourceProvider.class, "md5" );
        DefaultResourceLocation loc5 = new DefaultResourceLocation( new File( "/tmp/" ), id5 );
        final ResourceIdentifier id6 = new DefaultResourceIdentifier( ResourceProvider.class, "md6" );
        DefaultResourceLocation loc6 = new DefaultResourceLocation( new File( "/tmp/" ), id6 );
        final ResourceIdentifier id7 = new DefaultResourceIdentifier( ResourceProvider.class, "md7" );
        DefaultResourceLocation loc7 = new DefaultResourceLocation( new File( "/tmp/" ), id7 );
        final ResourceIdentifier id8 = new DefaultResourceIdentifier( ResourceProvider.class, "md8" );
        DefaultResourceLocation loc8 = new DefaultResourceLocation( new File( "/tmp/" ), id8 );

        AbstractResourceMetadata md4 = new AbstractResourceMetadata( null, loc4, null ) {
            @Override
            public ResourceBuilder prepare() {
                return null;
            }
        };
        final AbstractResourceMetadata md3 = new AbstractResourceMetadata( null, loc3, null ) {
            @Override
            public ResourceBuilder prepare() {
                dependencies.add( id4 );
                return null;
            }
        };
        final AbstractResourceMetadata md2 = new AbstractResourceMetadata( null, loc2, null ) {
            @Override
            public ResourceBuilder prepare() {
                dependencies.add( id4 );
                return null;
            }
        };
        final AbstractResourceMetadata md1 = new AbstractResourceMetadata( null, loc1, null ) {
            @Override
            public ResourceBuilder prepare() {
                dependencies.add( id2 );
                dependencies.add( id3 );
                return null;
            }
        };

        AbstractResourceMetadata md5 = new AbstractResourceMetadata( null, loc5, null ) {
            @Override
            public ResourceBuilder prepare() {
                dependencies.add( id8 );
                return null;
            }
        };
        final AbstractResourceMetadata md6 = new AbstractResourceMetadata( null, loc6, null ) {
            @Override
            public ResourceBuilder prepare() {
                return null;
            }
        };
        final AbstractResourceMetadata md7 = new AbstractResourceMetadata( null, loc7, null ) {
            @Override
            public ResourceBuilder prepare() {
                return null;
            }
        };
        final AbstractResourceMetadata md8 = new AbstractResourceMetadata( null, loc8, null ) {
            @Override
            public ResourceBuilder prepare() {
                dependencies.add( id7 );
                dependencies.add( id6 );
                return null;
            }
        };

        prepare( md4, md3, md2, md1, md5, md6, md7, md8 );

        final ResourceGraph graph = new ResourceGraph();
        graph.insertNode( md1 );
        graph.insertNode( md4 );
        graph.insertNode( md2 );
        graph.insertNode( md3 );
        graph.insertNode( md5 );
        graph.insertNode( md7 );
        graph.insertNode( md8 );
        graph.insertNode( md6 );

        Iterator<ResourceIdentifier<?>> traverseGraphFromBottomToTop = graph.traverseGraphFromBottomToTop();
        assertThat( traverseGraphFromBottomToTop, hasOrder( id4, id7, id6, id2, id3, id8, id1, id5 ) );

        Iterator<ResourceIdentifier<?>> traverseGraphFromTopToBottom = graph.traverseGraphFromTopToBottom();
        // TODO: 2/3 and 6/7 may be switched
        assertThat( traverseGraphFromTopToBottom, hasOrder( id1, id3, id2, id4, id5, id8, id7, id6 ) );

        ResourceGraph subgraph = graph.getSubgraph( id8 );
        Iterator<ResourceIdentifier<?>> traverseSubGraphFromBottomToTop = subgraph.traverseGraphFromBottomToTop();
        // TODO: 6/7 may be switched
        assertThat( traverseSubGraphFromBottomToTop, hasOrder( id6, id7, id8, id5 ) );

        Iterator<ResourceIdentifier<?>> traverseSubGraphFromTopToBottom = subgraph.traverseGraphFromTopToBottom();
        // // TODO fix order! should be 5-> 8 -> 6 -> 7 (6/7 may be switched)
        assertThat( traverseSubGraphFromTopToBottom, hasOrder( id8, id5, id6, id7 ) );

        List<ResourceIdentifier<?>> dependents = graph.getDependents( id8 );
        assertThat( dependents.size(), is( 1 ) );
        assertThat( dependents.get( 0 ), is( id5 ) );

        List<ResourceIdentifier<?>> dependencies = graph.getDependencies( id8 );
        assertThat( dependencies.size(), is( 2 ) );
        assertThat( dependencies.get( 0 ), is( id7 ) );
        assertThat( dependencies.get( 1 ), is( id6 ) );
    }

    @SuppressWarnings({ "rawtypes" })
    private void prepare( AbstractResourceMetadata md4, final AbstractResourceMetadata md3,
                          final AbstractResourceMetadata md2, final AbstractResourceMetadata md1,
                          AbstractResourceMetadata md5, final AbstractResourceMetadata md6,
                          final AbstractResourceMetadata md7, final AbstractResourceMetadata md8 ) {
        md1.prepare();
        md2.prepare();
        md3.prepare();
        md4.prepare();
        md5.prepare();
        md6.prepare();
        md7.prepare();
        md8.prepare();
    }

    private Matcher<Iterator<ResourceIdentifier<?>>> hasOrder( final ResourceIdentifier<?>... expectedResourceIdentifierOrder ) {
        return new BaseMatcher<Iterator<ResourceIdentifier<?>>>() {

            @Override
            public boolean matches( Object item ) {
                @SuppressWarnings("unchecked")
                Iterator<ResourceIdentifier<?>> iterator = (Iterator<ResourceIdentifier<?>>) item;
                for ( ResourceIdentifier<?> expectedResourceIdentifier : expectedResourceIdentifierOrder ) {
                    if ( !iterator.hasNext() )
                        return false;
                    ResourceIdentifier<?> resourceIdentifier = (ResourceIdentifier<?>) iterator.next();
                    if ( !expectedResourceIdentifier.equals( resourceIdentifier ) )
                        return false;
                }
                if ( iterator.hasNext() )
                    return false;
                return true;
            }

            @Override
            public void describeTo( Description description ) {
                description.appendText( "order is not as expected" );
            }
        };
    }

}