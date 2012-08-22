<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" 
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:csw="http://www.opengis.net/cat/csw"
  xmlns:wfs="http://www.opengis.net/wfs" 
  xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:app="http://www.deegree.org/app"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" 
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


  <xsl:template name="add_wfs_get_feature_attributes">
    <xsl:param name="resultType"/>
    <xsl:param name="outputFormat"/>
    <xsl:param name="maxFeatures"/>
    <xsl:param name="traverseXlinkDepth"/>
    <xsl:param name="traverseXlinkExpiry"/>
    <!-- xsl:message>The resultType <xsl:value-of select="$resultType"/></xsl:message-->
    <xsl:choose>
      <xsl:when test="$resultType != 'brief' and $resultType != 'full' and $resultType !='results' and $resultType != 'RESULTS' ">
        <xsl:attribute name="resultType">hits</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="resultType">results</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$outputFormat != '' ">
        <xsl:attribute name="outputFormat"><xsl:value-of select="$outputFormat"/></xsl:attribute>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="outputFormat">text/xml; subtype=gml/3.1.1</xsl:attribute>        
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$maxFeatures != '' ">
      <xsl:attribute name="maxFeatures">
        <xsl:value-of select="$maxFeatures"/>
      </xsl:attribute>        
    </xsl:if>
    <xsl:if test="$traverseXlinkDepth != '' ">
      <xsl:attribute name="traverseXlinkDepth">
        <xsl:value-of select="$traverseXlinkDepth"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="$traverseXlinkExpiry != '' ">
      <xsl:attribute name="traverseXlinkExpiry">
        <xsl:value-of select="$traverseXlinkExpiry"/>
      </xsl:attribute>        
    </xsl:if>
  </xsl:template>

  <!-- *************************************
       * the getRecordById request mapping *
       *************************************-->
  <xsl:template match="csw:GetRecordById">
    <wfs:GetFeature>
      <xsl:call-template name="create_wfs_base_request"/>
      <xsl:call-template name="add_wfs_get_feature_attributes">
        <xsl:with-param name="resultType"><xsl:value-of select="csw:ElementSetName"/></xsl:with-param>
      </xsl:call-template>
      <wfs:Query typeName="app:RegistryObject">
        <ogc:Filter>
          <ogc:Or>
            <xsl:apply-templates select="csw:Id" />
          </ogc:Or>
        </ogc:Filter>
      </wfs:Query>
    </wfs:GetFeature>
  </xsl:template>

  <xsl:template match="csw:Id">
    <ogc:PropertyIsEqualTo>
      <xsl:attribute name="matchCase">false</xsl:attribute>
      <ogc:PropertyName>app:iduri</ogc:PropertyName>
      <ogc:Literal><xsl:value-of select="."/></ogc:Literal>
    </ogc:PropertyIsEqualTo>
    <ogc:PropertyIsEqualTo>
      <xsl:attribute name="matchCase">false</xsl:attribute>
      <ogc:PropertyName>app:externalIdentifier/app:ExternalIdentifier/app:value</ogc:PropertyName>
      <ogc:Literal><xsl:value-of select="."/></ogc:Literal>
    </ogc:PropertyIsEqualTo>
  </xsl:template>

  <!-- *************************************
       * the getRecords request mapping    *
       *************************************-->
  <xsl:template match="csw:GetRecords">
    <wfs:GetFeature>
      <xsl:call-template name="create_wfs_base_request">
        <xsl:with-param name="handle">
          <xsl:value-of select="@requestId"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="add_wfs_get_feature_attributes">
        <xsl:with-param name="resultType"><xsl:value-of select="@resultType"/></xsl:with-param>
        <xsl:with-param name="maxFeatures"><xsl:value-of select="@maxRecords"/></xsl:with-param>
      </xsl:call-template>
      <xsl:apply-templates select="csw:Query" />
    </wfs:GetFeature>
  </xsl:template>  
  
  <xsl:template match="csw:Query">
    <wfs:Query>
      <!-- adds the mapped typeName attribute -->
      <xsl:apply-templates select="@typeNames"/>

      <!-- the csw:ElementSetName and csw:ElementName elements are handled in inEbrim.xsl-->
      <xsl:apply-templates select="csw:ElementSetName"/>
      <xsl:apply-templates select="csw:ElementName"/>


      <xsl:apply-templates select="csw:Constraint"/>
      <xsl:apply-templates select="ogc:SortBy"/>
    </wfs:Query>
  </xsl:template>

  <xsl:template match="csw:Constraint">
    <!-- we can not handle the query language, therefore we only handle the 
         ogc-filter element if it exists -->
    <xsl:apply-templates select="ogc:Filter"/>
  </xsl:template>
  
  <xsl:template match="ogc:Filter">
    <ogc:Filter>
      <xsl:apply-templates select="ogc:And"/>
      <xsl:apply-templates select="ogc:Or"/>
      <xsl:apply-templates select="ogc:Not"/>
      <xsl:if test="local-name(*[1]) != 'And' and local-name(*[1])!='Or' and local-name(*[1])!='Not'">
        <xsl:for-each select="*">
          <xsl:call-template name="copyProperty"/>
        </xsl:for-each>
      </xsl:if>
    </ogc:Filter>
  </xsl:template>

  <!-- the recursive ogc:And, ogc:Or, ogc:Not conditions are copied of, 
       if their contents is a ogc:PropertyName it is mapped, otherwhise it too is copied-of.-->
  <xsl:template match="ogc:And | ogc:Or | ogc:Not">
    <xsl:copy>
      <xsl:apply-templates select="ogc:And"/>
      <xsl:apply-templates select="ogc:Or"/>
      <xsl:apply-templates select="ogc:Not"/>
      <xsl:for-each select="*">
        <xsl:if test="local-name(.) != 'And' and local-name(.)!='Or' and local-name(.)!='Not'">
          <xsl:call-template name="copyProperty"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <!-- this template copys all the ogc:PropertyName elements (inside a ogc:Filter element) 
       and maps them to the wfs equivalent propertyPaths. 
       All other elements are simply copied-of. -->
  <xsl:template name="copyProperty">
    <xsl:choose>
      <xsl:when test="ogc:PropertyName = 'AnyText' ">
        <xsl:message>
          The any text propertyName is still to be evaluated.
        </xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <!-- copy of all the attributes of the given element -->
          <xsl:copy-of select="@*" />
          
          <!-- find the gml mappings for the given ebrim property names -->
          <xsl:apply-templates select="ogc:PropertyName"/>

          <!-- just copy all other nodes which are not PropertyNames -->
          <xsl:for-each select="*">
            <xsl:if test="local-name(.) != 'PropertyName' ">
              <xsl:copy-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- the ogc:SortBy element of the csw:Query element can be copied to the wfs:Query element, 
       but... the propertyNames must be mapped accordingly -->
  <xsl:template match="ogc:SortBy">
    <ogc:SortBy>
      <xsl:apply-templates select="ogc:SortProperty"/>
    </ogc:SortBy>
  </xsl:template>

  <xsl:template match="ogc:SortProperty">
    <ogc:SortProperty>
      <!-- the ogc:PropertyName elements are handled in inEbrim.xsl-->
      <xsl:apply-templates select="ogc:PropertyName"/>
      <xsl:copy-of select="ogc:SortOrder"/>
    </ogc:SortProperty>
  </xsl:template>

  <!-- The mapping of the ogc:PropertyName and the csw:ElementName elements is 
       done in the inEbrim.xsl -->
  <xsl:template match="ogc:PropertyName | csw:ElementName">
    <!-- mapping property name value -->
    <ogc:PropertyName>
      <xsl:call-template name="map_propertyPath"/>
    </ogc:PropertyName>
  </xsl:template>


  <!-- *******************************************
       * the csw:DescribeRecord request mapping *
       *******************************************-->
  <xsl:template match="csw:DescribeRecord">
    <wfs:DescribeFeatureType>
      <xsl:call-template name="create_wfs_base_request"/>
       <xsl:choose> 
         <xsl:when test="@outputFormat != ''"> 
           <xsl:copy-of select="@outputFormat"/> 
         </xsl:when> 
         <xsl:otherwise> 
           <xsl:attribute name="outputFormat">text/xml; subtype=gml/3.1.1</xsl:attribute> 
         </xsl:otherwise> 
       </xsl:choose> 
      <xsl:apply-templates select="csw:TypeName"/>
    </wfs:DescribeFeatureType>
  </xsl:template>  

  <xsl:template match="csw:TypeName">
    <xsl:call-template name="map_TypeName_element"/>
  </xsl:template>

</xsl:stylesheet>
