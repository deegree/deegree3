//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.cataloguemanager.model;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ServiceMetadataBean {

    private String version;

    private String type;

    private Operation[] operations;

    private Resource[] resources;

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion( String version ) {
        this.version = version;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType( String type ) {
        this.type = type;
    }

    /**
     * @return the operations
     */
    public Operation[] getOperations() {
        return operations;
    }

    /**
     * @param operations
     *            the operations to set
     */
    public void setOperations( Operation[] operations ) {
        this.operations = operations;
    }

    /**
     * @return the resources
     */
    public Resource[] getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources( Resource[] resources ) {
        this.resources = resources;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // inner classes
    // ////////////////////////////////////////////////////////////////////////////////////

    public static class Operation {
        private String name;

        private String dcp_get;

        private String dcp_post;

        private String dcp_soap;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName( String name ) {
            this.name = name;
        }

        /**
         * @return the dcp_get
         */
        public String getDcp_get() {
            return dcp_get;
        }

        /**
         * @param dcpGet
         *            the dcp_get to set
         */
        public void setDcp_get( String dcpGet ) {
            dcp_get = dcpGet;
        }

        /**
         * @return the dcp_post
         */
        public String getDcp_post() {
            return dcp_post;
        }

        /**
         * @param dcpPost
         *            the dcp_post to set
         */
        public void setDcp_post( String dcpPost ) {
            dcp_post = dcpPost;
        }

        /**
         * @return the dcp_soap
         */
        public String getDcp_soap() {
            return dcp_soap;
        }

        /**
         * @param dcpSoap
         *            the dcp_soap to set
         */
        public void setDcp_soap( String dcpSoap ) {
            dcp_soap = dcpSoap;
        }

    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class Resource {
        private String name;

        private String namespace;

        private String title;

        private String[] operations;

        private String resourceIdentifier;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName( String name ) {
            this.name = name;
        }

        /**
         * @return the namespace
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * @param namespace
         *            the namespace to set
         */
        public void setNamespace( String namespace ) {
            this.namespace = namespace;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title
         *            the title to set
         */
        public void setTitle( String title ) {
            this.title = title;
        }

        /**
         * @return the operations
         */
        public String[] getOperations() {
            return operations;
        }

        /**
         * @param operations
         *            the operations to set
         */
        public void setOperations( String[] operations ) {
            this.operations = operations;
        }

        /**
         * @return the resourceIdentifier
         */
        public String getResourceIdentifier() {
            return resourceIdentifier;
        }

        /**
         * @param resourceIdentifier
         *            the resourceIdentifier to set
         */
        public void setResourceIdentifier( String resourceIdentifier ) {
            this.resourceIdentifier = resourceIdentifier;
        }

    }

}
