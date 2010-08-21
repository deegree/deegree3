<%@page import="org.deegree.feature.persistence.FeatureStoreManager"%><%@page import="org.deegree.feature.persistence.postgis.PostGISFeatureStore"%><%@ page language="java" contentType="text/plain; charset=UTF-8" pageEncoding="UTF-8" %><%
  PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.get("inspire-postgis");
  String [] sql = fs.getDDL();
  for ( String string : sql ) {
      out.println (string + ";<br/>");
  }
%>
