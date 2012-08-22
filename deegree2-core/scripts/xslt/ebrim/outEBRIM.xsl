<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:app="http://www.deegree.org/app"
  xmlns:csw="http://www.opengis.net/cat/csw"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" 
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:wfs="http://www.opengis.net/wfs" 
  xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="wfs app fo xsi xsl ogc"
  >
  <!--  xmlns:java="java" 
       xmlns:minMaxExtract="org.deegree.framework.xml.MinMaxExtractor"-->
  <xsl:output method="xml" indent="yes"/>

  <!-- ############################################################
       # Global Parameters, which are set by the                  #
       # org.deegree.ogcwebservices.csw.discovery.Discovery#query #
       # method while invoking this script on the response of the #
       # GetFeature request.                                      #
       ############################################################ -->

  <!-- request id of the client-->
  <xsl:param name="REQUEST_ID"/>

  <!-- This element provides information about the status of the
       search request.
       - status    - one of, 
         * complete: The request was successfully completed and valid results are available.
         * subset (not supported): Partial, valid results are available
         * interim (not supported): Partial results available, not necessarily valid.
         * none: No results are available
         * processing (not supported): Request is still being processed.
       -->
  <xsl:param name="SEARCH_STATUS"/>
  
  <!-- the date and time when the result set was modified 
       (ISO 8601 format: YYYY-MM-DDThh:mm:ss[+|-]hh:mm).  -->
  <xsl:param name="TIMESTAMP"/>

  <!-- The element set returned (brief, summary or full). -->
  <xsl:param name="ELEMENT_SET"/>

  <!-- The element set returned (brief, summary or full). -->
  <xsl:param name="ELEMENT_SET_TYPENAMES"/>
  
  <!-- A reference to the type or schema of the records returned. -->
  <xsl:param name="RECORD_SCHEMA"/>

  <!-- Number of records found by the GetRecords operation. -->
  <xsl:param name="RECORDS_MATCHED"/>

  <!-- Number of records actually returned to client. This may not be the entire result. -->
  <xsl:param name="RECORDS_RETURNED"/>

  <!-- Start position of next record. A value of 0 means all records have been returned. -->
  <xsl:param name="NEXT_RECORD"/>

  <!-- the name of the request, either csw:GetRecords or csw:GetRecordsById -->
  <xsl:param name="REQUEST_NAME"/>

  <!-- the version of the csw-->
  <xsl:param name="VERSION"/>

  <!-- ###############################################
       # Some includes and the mapper instanciation  #
       ############################################### -->
  
  <!-- the Transaction converter -->
  <xsl:include href="outCSW_Transaction.xsl" />
  
  <!-- the Discovery converter -->
  <xsl:include href="outCSW_Discovery.xsl" />
  
  <!-- from wfs to ebrim mapping -->
  <xsl:include href="GML_TO_EBRIM.xsl" />

  <!-- The java mapper instance, creating instances is in spirit of oop 
       <xsl:variable name="mapper" select="mapping:new( )"/>
       -->  


  <!-- ##################################################################
       # Templates which use the mapper instance to convert paths etc.  #
       ################################################################## -->

  <!-- maps the given rim:propertyPath to the appropriate app:propertyPath
       <xsl:template name="map_propertyPath">
         <xsl:value-of select="mapping:mapPropertyPath( $mapper, . )"/>
       </xsl:template>
       -->


</xsl:stylesheet>
