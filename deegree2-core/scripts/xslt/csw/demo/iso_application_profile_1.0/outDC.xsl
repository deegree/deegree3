<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:deegreewfs="http://www.deegree.org/wfs" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:fo="http://www.w3.org/1999/XSL/Format" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:ogc="http://www.opengis.net/ogc" 
xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
xmlns:dc="http://purl.org/dc/elements/1.1/" 
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xmlns:app="http://www.deegree.org/app"
xmlns:ows="http://www.opengis.net/ows"
xmlns:java="java" xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor">
	<xsl:param name="REQUEST_ID"/>
	<xsl:param name="SEARCH_STATUS"/>
	<xsl:param name="TIMESTAMP"/>
	<xsl:param name="ELEMENT_SET"/>
	<xsl:param name="RECORD_SCHEMA"/>
	<xsl:param name="RECORDS_MATCHED"/>
	<xsl:param name="RECORDS_RETURNED"/>
	<xsl:param name="NEXT_RECORD"/>
    <xsl:param name="REQUEST_NAME" />
	
		<!-- ========================================================	
			root template
			===========================================================  -->
	<xsl:template match="wfs:FeatureCollection">
      <xsl:choose>
          <xsl:when test="$REQUEST_NAME = 'GetRecordById'">
            <csw:GetRecordByIdResponse xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" version="2.0.2">
                <xsl:for-each select="gml:featureMember/app:MD_Metadata">
  					<xsl:apply-templates select=".">
  						<xsl:with-param name="HLEVEL">
  							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
  						</xsl:with-param>
  					</xsl:apply-templates>
  				</xsl:for-each>
            </csw:GetRecordByIdResponse>
          </xsl:when>
          <xsl:otherwise>
    		<csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
    			<csw:RequestId>
    				<xsl:value-of select="$REQUEST_ID"/>
    			</csw:RequestId>
    			<csw:SearchStatus>
    				<xsl:attribute name="status"><xsl:value-of select="$SEARCH_STATUS"/></xsl:attribute>
    				<xsl:attribute name="timestamp"><xsl:value-of select="$TIMESTAMP"/></xsl:attribute>
    			</csw:SearchStatus>
    			<csw:SearchResults>
    				<xsl:attribute name="requestId"><xsl:value-of select="$REQUEST_ID"/></xsl:attribute>
    				<!--				<xsl:attribute name="elementSet"><xsl:value-of select="$ELEMENT_SET"/></xsl:attribute>-->
    				<xsl:attribute name="recordSchema"><xsl:value-of select="$RECORD_SCHEMA"/></xsl:attribute>
    				<xsl:attribute name="numberOfRecordsMatched"><xsl:value-of select="$RECORDS_MATCHED"/></xsl:attribute>
    				<xsl:attribute name="numberOfRecordsReturned"><xsl:value-of select="$RECORDS_RETURNED"/></xsl:attribute>
    				<xsl:attribute name="nextRecord"><xsl:value-of select="$NEXT_RECORD"/></xsl:attribute>
    				<xsl:for-each select="gml:featureMember/app:MD_Metadata">
    					<xsl:apply-templates select=".">
    						<xsl:with-param name="HLEVEL">
    							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
    						</xsl:with-param>
    					</xsl:apply-templates>
    				</xsl:for-each>
    			</csw:SearchResults>
    		</csw:GetRecordsResponse>
        </xsl:otherwise>
      </xsl:choose>
	</xsl:template>
	
		<!-- ========================================================	
			template for feature member
			===========================================================  -->
	<xsl:template match="app:MD_Metadata">
		<xsl:param name="HLEVEL" />
		<xsl:if test="$HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application'">
			<xsl:call-template name="ISO19115" />
		</xsl:if>
		<xsl:if test="$HLEVEL = 'service'">
			<xsl:call-template name="ISO19119" />
		</xsl:if>
	</xsl:template>
	
	<!-- ========================================================	
			template for dataset, series and application
			===========================================================  -->
	<xsl:template name="ISO19115">
		<csw:Record>
			<!-- title -->
			<xsl:apply-templates select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title"/>

			<!-- creator -->
			<xsl:for-each select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodeCreator" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodeCreator = 'originator' )">
					<xsl:call-template name="DUBLINCORE.CREATOR_ISO19115"/>
				</xsl:if>
			</xsl:for-each>

			<!-- subject -->
			<xsl:call-template name="DUBLINCORE.SUBJECT_ISO19115"/>
			
			<!-- description -->
			<xsl:apply-templates select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:abstract"/>

			<!-- publisher-->
			<xsl:for-each select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodePublisher" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodePublisher = 'publisher' )">
					<xsl:call-template name="DUBLINCORE.PUBLISHER_ISO19115"/>
				</xsl:if>
			</xsl:for-each>

			<!-- contributor -->
			<xsl:for-each select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodeAuthor" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodeAuthor = 'author' )">
					<xsl:call-template name="DUBLINCORE.CONTRIBUTOR_ISO19115"/>
				</xsl:if>
			</xsl:for-each>
			
			<!-- date -->
			<xsl:for-each select="app:dateStamp">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<!-- type -->
			<xsl:apply-templates select="app:hierarchyLevelCode"/>
			<!-- format -->
			<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format/app:name">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<!-- identifier -->
			<xsl:apply-templates select="app:fileidentifier"/>			
			<!--source not supported -->
			<!-- language -->
			<xsl:apply-templates select="app:language"/>			
			<!--relation: not supported by this csw MD_Metadata.identificationInfo.AbstractMD_Identification.aggregationInfo -->
            <!-- rights -->
            <xsl:for-each select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:legalConstraints/app:MD_LegalConstraints">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
			<!-- coverage -->
			<xsl:apply-templates select="app:dataIdentification/app:MD_DataIdentification/app:boundingBox/app:EX_GeogrBBOX"/>
		</csw:Record>
	</xsl:template>
	
		<!-- ========================================================	
			template for services
			===========================================================  -->
	<xsl:template name="ISO19119">
		<csw:Record>
			<!-- title -->
			<xsl:apply-templates select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title"/>

			<!-- creator -->
			<xsl:for-each select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodeCreator" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodeCreator = 'originator' )">
					<xsl:call-template name="DUBLINCORE.CREATOR_ISO19119"/>
				</xsl:if>
			</xsl:for-each>

			<!-- subject -->
			<xsl:call-template name="DUBLINCORE.SUBJECT_ISO19119"/>
			
			<!-- description -->
			<xsl:apply-templates select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:abstract"/>

			<!-- publisher-->
			<xsl:for-each select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodePublisher" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodePublisher = 'publisher' )">
					<xsl:call-template name="DUBLINCORE.PUBLISHER_ISO19119"/>
				</xsl:if>
			</xsl:for-each>

			<!-- contributor -->
			<xsl:for-each select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty">
				<xsl:variable name="roleCodeAuthor" select="app:role/app:CI_RoleCode/app:codelistvalue"/>
				<xsl:if test="boolean( $roleCodeAuthor = 'author' )">
					<xsl:call-template name="DUBLINCORE.CONTRIBUTOR_ISO19119"/>
				</xsl:if>
			</xsl:for-each>
			
			<!-- date -->
			<xsl:for-each select="app:dateStamp">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<!-- type -->
			<xsl:apply-templates select="app:hierarchyLevelCode"/>
			<!-- format -->
			<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format/app:name">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<!-- identifier -->
			<xsl:apply-templates select="app:fileidentifier"/>			
			<!--source not supported -->
			<!-- language -->
			<xsl:apply-templates select="app:language"/>			
			<!--relation: not supported by this csw MD_Metadata.identificationInfo.AbstractMD_Identification.aggregationInfo -->
			<!-- coverage -->
			<xsl:apply-templates select="app:serviceIdentification/app:CSW_ServiceIdentification/app:boundingBox/app:EX_GeogrBBOX"/>
			<!-- rights -->
			<xsl:for-each select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:legalConstraints/app:MD_LegalConstraints">
			<xsl:apply-templates select="."/>
			</xsl:for-each>
		</csw:Record>
	</xsl:template>
	
		<!-- ========================================================	
			template for titel
			===========================================================  -->
	<xsl:template match="app:title">
		<dc:title>
			<xsl:value-of select="."/>
		</dc:title>
	</xsl:template>
	
	<!-- ========================================================	
			templates for creator
			===========================================================  -->
	<xsl:template name="DUBLINCORE.CREATOR_ISO19115">
		<xsl:variable name="name" select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:creator>
				<xsl:value-of select="$name"/>
			</dc:creator>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="DUBLINCORE.CREATOR_ISO19119">
			<xsl:variable name="name" select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:creator>
				<xsl:value-of select="$name"/>
			</dc:creator>
		</xsl:if>
	</xsl:template>
	
	<!-- ========================================================	
			templates for subject
			===========================================================  -->
	<xsl:template name="DUBLINCORE.SUBJECT_ISO19115">
	
		<xsl:for-each select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:keyword/app:Keyword/app:keyword">
			<dc:subject>
				<xsl:value-of select="."/>
			</dc:subject>
		</xsl:for-each>

		<xsl:variable name="category" select="app:dataIdentification/app:MD_DataIdentification/app:topicCategory/app:MD_TopicCategoryCode/app:category"/>			
		<xsl:if test="boolean( $category != '' )">
			<dc:subject>
				<xsl:value-of select="$category"/>
			</dc:subject>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template name="DUBLINCORE.SUBJECT_ISO19119">
	
		<xsl:for-each select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:keyword/app:Keyword/app:keyword">
			<dc:subject>
				<xsl:value-of select="."/>
			</dc:subject>
		</xsl:for-each>

		<xsl:variable name="category" select="app:serviceIdentification/app:CSW_ServiceIdentification/app:topicCategory/app:MD_TopicCategoryCode/app:category"/>			
		<xsl:if test="boolean( $category != '' )">
			<dc:subject>
				<xsl:value-of select="$category"/>
			</dc:subject>
		</xsl:if>
		
	</xsl:template>
	
	<!-- ========================================================	
			templates for publisher
			===========================================================  -->
	<xsl:template name="DUBLINCORE.PUBLISHER_ISO19115">
		<xsl:variable name="name" select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:publisher>
				<xsl:value-of select="$name"/>
			</dc:publisher>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="DUBLINCORE.PUBLISHER_ISO19119">
	<xsl:variable name="name" select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:publisher>
				<xsl:value-of select="$name"/>
			</dc:publisher>
		</xsl:if>
	</xsl:template>
	
	<!-- ========================================================	
			template for contributor
			===========================================================  -->
	<xsl:template name="DUBLINCORE.CONTRIBUTOR_ISO19115">
		<xsl:variable name="name" select="app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:contributor>
				<xsl:value-of select="$name"/>
			</dc:contributor>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="DUBLINCORE.CONTRIBUTOR_ISO19119">
		<xsl:variable name="name" select="app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact/app:CI_RespParty/app:organisationname"/>
		<xsl:if test="boolean( $name != '' )">
			<dc:contributor>
				<xsl:value-of select="$name"/>
			</dc:contributor>
			</xsl:if>
	</xsl:template>
	
	<!-- ========================================================	
			template for date
			===========================================================  -->
	<xsl:template match="app:dateStamp">
		<dc:date>
			<xsl:value-of select="."/>
		</dc:date>
	</xsl:template>
	
	<!-- ========================================================	
			template for description
			===========================================================  -->
	<xsl:template match="app:abstract">
		<dc:description>
			<xsl:value-of select="."/>
		</dc:description>
	</xsl:template>
	
	<!-- ========================================================	
			template for type
			===========================================================  -->
	<xsl:template match="app:hierarchyLevelCode">
		<dc:type>
			<xsl:value-of select="."/>
		</dc:type>
	</xsl:template>
	
	<!-- ========================================================	
			template for format
			===========================================================  -->
	<xsl:template match="app:MD_Format/app:name">
		<dc:format>
			<xsl:value-of select="."/>
		</dc:format>
	</xsl:template>
	
	<!-- ========================================================	
			template for identifier
			===========================================================  -->
	<xsl:template match="app:fileidentifier">
		<dc:identifier>
			<xsl:value-of select="."/>
		</dc:identifier>
	</xsl:template>
	
	<!-- ========================================================	
			template for language
			===========================================================  -->
	<xsl:template match="app:language">
		<dc:language>
			<xsl:value-of select="."/>
		</dc:language>
	</xsl:template>
	
	<!-- ========================================================	
			template for rights
			===========================================================  -->
	<xsl:template match="app:MD_LegalConstraints">
		<dc:rights>
			<xsl:value-of select="app:accessConstraints/app:MD_RestrictionCode/app:codelistvalue"/>
		</dc:rights>
	</xsl:template>
	
	<!-- ========================================================	
			template for coverage
			===========================================================  -->
	<xsl:template match="app:EX_GeogrBBOX">
		<ows:BoundingBox crs="EPSG:4326">
			<xsl:variable name="xmax">
				<xsl:value-of select="minmax:getXMax( ./app:geom/child::*[1] )" />
			</xsl:variable>
			<xsl:variable name="xmin">
				<xsl:value-of select="minmax:getXMin( ./app:geom/child::*[1] )" />
			</xsl:variable>
			<xsl:variable name="ymax">
				<xsl:value-of select="minmax:getYMax( ./app:geom/child::*[1] )" />
			</xsl:variable>
			<xsl:variable name="ymin">
				<xsl:value-of select="minmax:getYMin( ./app:geom/child::*[1] )" />
			</xsl:variable>
            <ows:LowerCorner>
              <xsl:value-of select="concat($xmin, ' ', $ymin )"/>
            </ows:LowerCorner>
            <ows:UpperCorner>
              <xsl:value-of select="concat($xmax, ' ', $ymax )"/>
            </ows:UpperCorner>
		</ows:BoundingBox>
	</xsl:template>
<!--
  <xsl:template match="DUBLINCORE.ABSTRACT">
    <dc:abstract>
      <xsl:value-of select="."/>
    </dc:abstract>
  </xsl:template>
  -->
  
<!--
  <xsl:template match="DUBLINCORE.RELATION">
    <dc:relation>
      <xsl:value-of select="."/>
    </dc:relation>
  </xsl:template>
-->	

	<!--
  <xsl:template match="DUBLINCORE.SOURCE">
    <dc:source>
      <xsl:value-of select="."/>
    </dc:source>
  </xsl:template>
-->
	
</xsl:stylesheet>
