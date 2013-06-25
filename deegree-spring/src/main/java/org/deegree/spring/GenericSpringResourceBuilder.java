package org.deegree.spring;

import org.deegree.spring.jaxb.SingleBeanRef;

import org.deegree.workspace.Resource;
import org.deegree.workspace.Workspace;

public class GenericSpringResourceBuilder<T extends Resource> extends AbstractSpringResourceBuilder<T> {

    private final Class<T> clazz;

    private final String beanName;

    public GenericSpringResourceBuilder( final Workspace workspace, final SingleBeanRef singleBeanRef,
                                         final Class<T> clazz ) {
        super( workspace, singleBeanRef.getApplicationContextHolder() );

        this.clazz = clazz;
        this.beanName = singleBeanRef.getBeanName();
    }

    @Override
    public T build() {
        return getBean( clazz, beanName );
    }
}
