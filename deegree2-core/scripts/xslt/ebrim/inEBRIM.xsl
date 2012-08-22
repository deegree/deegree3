<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml"
  xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:app="http://www.deegree.org/app" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:mapping="xalan://org.deegree.ogcwebservices.csw.iso_profile.ebrim.EBRIM_Mapping" xmlns:geomConv="org.deegree.framework.xml.GeometryUtils"
  exclude-result-prefixes="geomConv mapping java">

  <!-- xalan:component
    prefix="mapping"
    elements="init"
    functions="mapPropertyPath mapTypeNameElement mapElementSetName mapTypeNames">
    <xalan:script
    lang="javaclass"
    src="xalan://org.deegree.ogcwebservices.csw.iso_profile.ebrim.EBRIM_Mapping" />
    </xalan:component-->

  <xsl:output method="xml" indent="yes" />


  <!-- the transaction converter -->
  <xsl:include href="inCSW_Transaction.xsl" />

  <!-- the transaction converter -->
  <xsl:include href="inCSW_Discovery.xsl" />

  <!-- from ebrim to wfs mapping -->
  <xsl:include href="EBRIM_To_GML.xsl" />

  <!-- creating instances is in spirit of oop -->
  <!--xsl:variable name="mapper" select="mapping:new( )"/-->
  <xsl:variable name="mapper" select="mapping:new( )" />

  <xsl:template name="create_wfs_base_request">
    <xsl:param name="handle" />
    <!-- mapping:init /-->
    <xsl:attribute name="service">WFS</xsl:attribute>
    <xsl:attribute name="version">1.1.0</xsl:attribute>
    <xsl:if test="$handle != '' ">
      <xsl:attribute name="handle">
        <xsl:value-of select="$handle" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <!-- maps the given rim:propertyPath to the appropriate app:propertyPath-->
  <xsl:template name="map_propertyPath">
    <xsl:value-of select="mapping:mapPropertyPath( $mapper, . )" />
    <!-- xsl:value-of select="mapping:mapPropertyPath( . )" /-->
    <!-- xsl:value-of select="java:mapPropertyPath( . )" /-->
  </xsl:template>

  <!-- maps the given rim:FeatureType to the appropriate app:FeatuerType
    this function is using the required csw:TypeName/@targetNamespace to map to the rim: prefix.-->
  <xsl:template name="map_TypeName_element">
    <xsl:param name="mapped_name" select="mapping:mapTypeNameElement( $mapper, . )" />
    <!-- xsl:param name="mapped_name" select="mapper:mapTypeNameElement( . )" / -->
    <!-- select="mapping:mapTypeNameElement( . )" /-->
    <xsl:if test="$mapped_name != ''">
      <wfs:TypeName>
        <xsl:value-of select="$mapped_name" />
      </wfs:TypeName>
    </xsl:if>
  </xsl:template>

  <xsl:template match="csw:ElementSetName">
    <xsl:for-each select="mapping:mapElementSetName( $mapper, . )/*">
      <!-- 
        #### ATTENTION ####
        # The java-method 'mapElementSetName' will replace all referenced 
        # variables with the dereferenced propertyname, the aliases are therefore 
        # lost (blasted into oblivion).
        #### ATTENTION ###
      -->
      <!-- xsl:for-each select="mapping:mapElementSetName( . )/*"-->
      <!-- xsl:for-each select="mapper:mapElementSetName( . )/*"-->
      <xsl:copy-of select="." />
    </xsl:for-each>
  </xsl:template>


  <!-- adds the mapped typeName attribute, assuming the currentnode is the @typeName(s) attribute
  -->
  <xsl:template match="@typeName|@typeNames">
    <xsl:variable name="typeNamesAndAliases">
      <xsl:value-of select="mapping:mapTypeNames( $mapper, . )" />
      <!-- xsl:value-of select="mapping:mapTypeNames( . )" /-->
      <!--  xsl:value-of select="mapper:mapTypeNames( . )" /-->
    </xsl:variable>
    <!-- xsl:message>
      found result typename:
      <xsl:value-of select="$typeNamesAndAliases" />
    </xsl:message-->
    <xsl:if test="$typeNamesAndAliases != ''">
      <xsl:choose>
        <xsl:when test="substring-before($typeNamesAndAliases, 'aliases=') = ''">
          <xsl:attribute name="typeName">
            <xsl:value-of select="$typeNamesAndAliases" />
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="typeName">
            <xsl:value-of select="substring-before($typeNamesAndAliases, 'aliases')" />
          </xsl:attribute>
          <xsl:attribute name="aliases">
            <xsl:value-of select="substring-after($typeNamesAndAliases, 'aliases=')" />
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
