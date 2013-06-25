package org.deegree.spring;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationContextHolder implements Resource {

    private final ResourceMetadata<ApplicationContextHolder> metadata;

    private final ConfigurableApplicationContext applicationContext;

    public ApplicationContextHolder( final ResourceMetadata<ApplicationContextHolder> metadata,
                                     final ConfigurableApplicationContext applicationContext ) {
        this.metadata = metadata;
        this.applicationContext = applicationContext;
    }

    @Override
    public ResourceMetadata<ApplicationContextHolder> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        applicationContext.close();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
