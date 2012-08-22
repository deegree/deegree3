<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  xmlns:deerim="http://www.deegree.org/csw-ebrim"
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
  <!-- the xmlns:java and xmlns:mapping defines are necessary for else they will appear in inner nodes 
       
       The following templates used here are found in inEbrim.xsl 
       1) create_wfs_base_request
       2) map_typeNames
       3) map_propertyPath
       4) map_TypeName_element
       
       All ebrim transformations are done in EBRIM_To_GML.xsd
       -->
  
  <xsl:output method="xml" indent="yes"/>

  <!-- ################################################
       # GetRecordsResponse and GetRecordByIdResponse #
       # and the FeatureCollection response mapping   #
       ################################################-->

  <xsl:template match="wfs:FeatureCollection">
    <xsl:if test="$REQUEST_NAME = ''">
      <xsl:message terminate="yes">outCSW_Discovery.xsl: The request name is not set, exiting!</xsl:message>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="$REQUEST_NAME = 'GetRecordById'">
        <xsl:call-template name="create_get_records_by_id_response"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- the request was a GetRecords-->
        <xsl:call-template name="create_get_records_response"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  

  <xsl:template name="check_id_response_variables">
    <xsl:if test="$ELEMENT_SET = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Element set variable (full, brief or summary) is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
  </xsl:template>

  <xsl:template name="check_response_variables">
    <xsl:if test="$VERSION != '' ">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Version variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$REQUEST_ID != '' ">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Requestid variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$SEARCH_STATUS = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) search status variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$ELEMENT_SET = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Element set variable (full, brief or summary) is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$RECORD_SCHEMA = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Record Schema variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$RECORDS_MATCHED = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Records matched variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$RECORDS_RETURNED = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Records returned variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
    <xsl:if test="$NEXT_RECORD = ''">
      <xsl:message terminate="yes">
        <xsl:text>outCSW_Discovery.xsl: The (internal) Next Records variable is not set, exiting!</xsl:text>
      </xsl:message>
    </xsl:if>   
  </xsl:template>

  <xsl:template name="create_get_records_by_id_response">
    <!-- first findout if any of the needed variables are not set-->
    <xsl:call-template name="check_id_response_variables"/>
    <csw:GetRecordByIdResponse>
      <xsl:if test="$ELEMENT_SET = 'brief'">
        <xsl:call-template name="brief_record_by_id"/>
      </xsl:if>
      <xsl:if test="$ELEMENT_SET = 'summary'">
        
      </xsl:if>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:apply-templates select="gml:featureMember"/>              
      </xsl:if>
    </csw:GetRecordByIdResponse>
  </xsl:template>

  <xsl:template name="brief_record_by_id">
    
  </xsl:template>

  <xsl:template name="create_get_records_response">
    <!-- first findout if any of the needed variables are not set-->
    <xsl:call-template name="check_id_response_variables"/>
    <csw:GetRecordsResponse>
      <xsl:attribute name="version">
        <xsl:value-of select="$VERSION"/>
      </xsl:attribute>
      <xsl:if test="$REQUEST_ID != ''">
        <csw:RequestId>
          <xsl:value-of select="$REQUEST_ID"/>
        </csw:RequestId>
      </xsl:if>
      <csw:SearchStatus>
        <xsl:attribute name="status">
          <xsl:value-of select="$SEARCH_STATUS"/>          
        </xsl:attribute>
        <!-- use the set variable instead of the featurecollection attribute -->
        <xsl:if test="$TIMESTAMP != ''">
          <xsl:attribute name="timestamp">
            <xsl:value-of select="$TIMESTAMP"/>
          </xsl:attribute>
        </xsl:if>
      </csw:SearchStatus>
      <csw:SearchResults>
        <!-- Not supported at the moment
             <xsl:if test="">
               <xsl:attribute name="resultSetId">
               </xsl:attribute>
             </xsl:if>
             -->
        <xsl:attribute name="elementSet">
          <xsl:value-of select="$ELEMENT_SET"/>
        </xsl:attribute>

        <xsl:attribute name="recordSchema">
          <xsl:value-of select="$RECORD_SCHEMA"/>
        </xsl:attribute>

        <!-- required -->
        <xsl:attribute name="numberOfRecordsMatched">
          <xsl:value-of select="$RECORDS_MATCHED"/>
        </xsl:attribute>
        <!-- required -->
        <xsl:attribute name="numberOfRecordsReturned">
          <xsl:value-of select="$RECORDS_RETURNED"/>
        </xsl:attribute>
        <xsl:attribute name="nextRecord">
          <xsl:value-of select="$NEXT_RECORD"/>
        </xsl:attribute>
        <!-- Not supported at the moment
             <xsl:if test="">
               <xsl:attribute name="expires">
               </xsl:attribute>
             </xsl:if>
             -->
         <xsl:apply-templates select="gml:featureMember"/>      
         <xsl:apply-templates select="gml:featureTuple"/>      
      </csw:SearchResults>
    </csw:GetRecordsResponse>
  </xsl:template>

  <xsl:template match="gml:featureMember">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="gml:featureTuple">
    <deerim:RecordTuple>
      <xsl:apply-templates select="*"/>
    </deerim:RecordTuple>
  </xsl:template>


  <!-- ################################################
       # DescribeRecordsResponse is not handled       #
       ################################################-->  


       
</xsl:stylesheet>
