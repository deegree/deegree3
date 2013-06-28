package org.deegree.spring;

import org.deegree.spring.annotation.InjectMetadata;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContext {
    
    public static class TestResource implements Resource {
        
        @InjectMetadata
        private ResourceMetadata<? extends Resource> metadata;

        @Override
        public void destroy() {

        }

        @Override
        public ResourceMetadata<? extends Resource> getMetadata() {
            return metadata;
        }

        @Override
        public void init() {

        }
    }

    public static class NoBean extends TestResource {
    }

    public static class SingleBean extends TestResource {
    }

    public static class ContentBean extends TestResource {

        final String content;

        ContentBean( final String content ) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    @Bean
    public SingleBean singleBean() {
        return new SingleBean();
    }

    @Bean
    public ContentBean contentBean0() {
        return new ContentBean( "contentBean0" );
    }

    @Bean
    public ContentBean contentBean1() {
        return new ContentBean( "contentBean1" );
    }

    @Bean
    public ContentBean contentBean() {
        return new ContentBean( "contentBean" );
    }
}
