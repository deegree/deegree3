<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- $HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/deegree3/contrib/deegree-wps/deegree-wps/src/main/webapp/wfs/index.jsp $
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
--%>
<%@ page import="java.util.*"%>
<%@ page import="org.deegree.services.controller.*"%>
<%@ page import="org.deegree.services.controller.wfs.WFSController"%>
<%@ page import="org.deegree.services.wfs.*"%>
<%@page import="org.deegree.feature.persistence.*"%>
<%@page import="org.deegree.feature.types.ApplicationSchema"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.IOException"%>
<%@page import="org.deegree.feature.persistence.query.Query"%>
<%@page import="org.deegree.feature.types.FeatureType"%>
<%!
    private void printFt (FeatureType ft, FeatureStore store, PrintWriter out, String indent) throws IOException {
        if (ft.isAbstract()) {
            out.println (indent + "- <i>" + ft.getName().getLocalPart() + " (abstract)</i><br/>");        
        } else {
            Query query = new Query( ft.getName(), null, null, 0, -1, -1 );
            int numInstances = -1;
            try {
                numInstances = store.queryHits(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
            out.println (indent + "- " + ft.getName().getLocalPart() + " [" + numInstances +  " instances]<br/>");            
        }
        FeatureType [] fts = ft.getSchema().getDirectSubtypes(ft);
        Arrays.sort(fts, new Comparator<FeatureType>() {
            public int compare(FeatureType a, FeatureType b)  {
                int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
                if (order == 0) {
                    order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());    
                }
                return order;
            }
        });
        for (FeatureType childType : fts) {
            printFt(childType, store, out, indent + "&nbsp;&nbsp;");
        }
    }
%>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <title>deegree 3 WFS configuration</title>
        <link rel="stylesheet" href="../styles.css" />
    </head>
    <body>
deegree 3 WFS configuration<br/>
---------------------------<br/><br/>
Protocol information<br/><br/>
<%
  WFSController controller = (WFSController) OGCFrontController.getServiceController(WFSController.class);
  out.println (" - active versions: " + controller.getOfferedVersionsString());  
%>
<br/><br/><br/>Configured feature stores<br/><br/>
<%
  WFService service = controller.getService();
  for (FeatureStore store : service.getStores()) {
      ApplicationSchema schema = store.getSchema();      
      FeatureType[] fts = schema.getRootFeatureTypes();
      
      // sort the types by name
      Arrays.sort(fts, new Comparator<FeatureType>() {
          public int compare(FeatureType a, FeatureType b)  {
              int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
              if (order == 0) {
                  order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());    
              }
              return order;
          }
      });
      
      for (FeatureType ft : fts) {
          printFt(ft, store, new PrintWriter(out), "");
          out.println ("<br/>");
      }
  }
%>
    </body>
</html>
