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
>
  <!-- the xmlns:java and xmlns:mapping defines are necessary for else they might appear in inner nodes 
       
       The following templates used here are found in inEbrim.xsl 
       1) create_wfs_base_request
       2) map_propertyPath
       3) map_typeNames
       3) csw:Constraint
       
       All ebrim transformations are done in EBRIM_To_GML.xsd
       -->

  <xsl:output method="xml" indent="yes"/>



  <xsl:template match="csw:Transaction">
    <wfs:Transaction>
      <xsl:call-template name="create_wfs_base_request">
        <xsl:with-param name="handle"><xsl:value-of select="@requestId" /></xsl:with-param>
      </xsl:call-template>
      <xsl:apply-templates select="*" />
    </wfs:Transaction>
  </xsl:template>

  <xsl:template match="csw:Insert">
    <!--xsl:message>creating ***********: <xsl:value-of select="name( . )"/></xsl:message-->
    <wfs:Insert inputFormat="text/xml; subtype=gml/3.1.1" >
      <xsl:if test="@handle != '' ">
        <xsl:attribute name="handle">
          <xsl:value-of select="@handle" />
        </xsl:attribute>
      </xsl:if>
      <wfs:FeatureCollection>
        <xsl:for-each select="*">
          <gml:featureMember>
            <xsl:apply-templates select="."/>
          </gml:featureMember>          
        </xsl:for-each>
      </wfs:FeatureCollection>

    </wfs:Insert>
  </xsl:template>

  <!-- maps the csw:Value element of a csw:Property to a wfs:Value -->
  <xsl:template match="csw:Value">
    <wfs:Value><xsl:value-of select="."/></wfs:Value>
  </xsl:template>

  <!-- maps the csw:Name element of a csw:Property to a wfs:Name -->
  <xsl:template match="csw:Name">
    <wfs:Name><xsl:call-template name="map_propertyPath"/></wfs:Name>
  </xsl:template>

  <xsl:template match="csw:Update">
    <!--xsl:message>creating ***********: <xsl:value-of select="name( . )"/></xsl:message-->
    <wfs:Update inputFormat="x-application/gml:3" typeName="app:RegistryObject">
      <xsl:if test="@handle != '' ">
        <xsl:attribute name="handle">
          <xsl:value-of select="@handle" />
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="namespace-uri( * ) != namespace-uri( . ) ">
          <!-- xsl:message>
            <xsl:text>We want to upate an ebrim object, to be handled later.</xsl:text>
          </xsl:message-->
          <!-- only one element is allowed, so just take the first. -->
          <xsl:apply-templates select="*[1]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="csw:RecordProperty">
            <wfs:Property>
              <xsl:apply-templates select="csw:Name"/>
              <xsl:apply-templates select="csw:Value"/>
            </wfs:Property>
          </xsl:for-each> 
          <xsl:apply-templates select="csw:Constraint"/>
        </xsl:otherwise>
      </xsl:choose>
    </wfs:Update>
  </xsl:template>
  
  <xsl:template match="csw:Delete">
    <wfs:Delete>
      <xsl:if test="@handle != '' ">
        <xsl:attribute name="handle">
          <xsl:value-of select="@handle" />
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="@typeName != '' ">
          <xsl:apply-templates select="@typeName"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="typeName">app:RegistryObject</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="csw:Constraint"/>
    </wfs:Delete>
  </xsl:template>



</xsl:stylesheet>
