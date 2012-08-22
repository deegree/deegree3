<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" 
xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" 
xmlns:wfs="http://www.opengis.net/wfs" xmlns:java="java" xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:variable name="HIERARCHY">
		<!-- Geodan-SF 10-11-2005: Added choose/when -->
		<xsl:if test="boolean( ./csw:Query/@typeNames )">
			<xsl:choose>
				<xsl:when test="substring( ./csw:Query/@typeNames,1,4)='csw:'">
					<xsl:value-of select="substring( ./csw:Query/@typeNames, 5 )"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="./csw:Query/@typeNames"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- 
			fallback:
			it seems that different XSLT-Processores have different opinions where
			to start an XPath for a global variable
			-->
		<xsl:if test="boolean( csw:GetRecords/csw:Query/@typeNames )">
			<xsl:choose>
				<xsl:when test="substring( csw:GetRecords/csw:Query/@typeNames,1,4)='csw:'">
					<xsl:value-of select="substring( csw:GetRecords/csw:Query/@typeNames, 5 )"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="csw:GetRecords/csw:Query/@typeNames"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:variable>
	<xsl:template match="csw:DescribeRecord">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="csw:GetRecords">
		<!-- will be used for GetRecords requests -->
		<wfs:GetFeature outputFormat="text/xml; subtype=gml/3.1.1" xmlns:gml="http://www.opengis.net/gml" xmlns:app="http://www.deegree.org/app">
			<xsl:if test="./@maxRecords != '' ">
				<xsl:attribute name="maxFeatures"><xsl:value-of select="./@maxRecords"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@startPosition != '' ">
				<xsl:attribute name="startPosition"><xsl:value-of select="./@startPosition"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@resultType = 'HITS' ">
				<xsl:attribute name="resultType">hits</xsl:attribute>
			</xsl:if>
			<xsl:if test="./@resultType = 'RESULTS' ">
				<xsl:attribute name="resultType">results</xsl:attribute>
			</xsl:if>
			<xsl:for-each select="./csw:Query">
				<wfs:Query>
					<xsl:attribute name="typeName">app:MD_Metadata</xsl:attribute>
					<xsl:apply-templates select="."/>
				</wfs:Query>
			</xsl:for-each>
		</wfs:GetFeature>
		<xsl:apply-templates select="csw:ResponseHandler"/>
	</xsl:template>
	<xsl:template match="csw:ResponseHandler"/>
	<xsl:template match="csw:Query">
		<xsl:apply-templates select="csw:ElementSetName"/>
		<xsl:for-each select="./child::*">
			<xsl:if test="local-name(.) = 'ElementName' ">
				<wfs:PropertyName>
					<xsl:apply-templates select="."/>
				</wfs:PropertyName>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="boolean( csw:Constraint ) = false">
			<xsl:call-template name="SETMINIMUMCONSTRAINT"/>
		</xsl:if>
		<xsl:apply-templates select="csw:Constraint"/>
		<xsl:apply-templates select="ogc:SortBy"/>
	</xsl:template>
	<xsl:template match="ogc:SortBy">
		<ogc:SortBy>
			<xsl:for-each select="./child::*">
				<ogc:SortProperty>
					<ogc:PropertyName>
						<xsl:value-of select="mapping:mapSortProperty( ./ogc:PropertyName, $HIERARCHY )"/>
					</ogc:PropertyName>
					<xsl:copy-of select="ogc:SortOrder"/>
				</ogc:SortProperty>
			</xsl:for-each>
		</ogc:SortBy>
	</xsl:template>
	<xsl:template match="csw:ElementSetName">
		<xsl:if test=". = 'brief' ">
			<wfs:PropertyName>app:fileidentifier</wfs:PropertyName>
			<wfs:PropertyName>app:hierarchyLevelCode</wfs:PropertyName>
			<wfs:PropertyName>app:contact</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:topicCategory/app:MD_TopicCategoryCode/app:category</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:boundingBox</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:verticalExtent</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:temportalExtent</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title</wfs:PropertyName>
			<wfs:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:boundingBox</wfs:PropertyName>
			<wfs:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title</wfs:PropertyName>
		</xsl:if>
		<xsl:if test=". = 'summary' ">
			<wfs:PropertyName>app:fileidentifier</wfs:PropertyName>
			<wfs:PropertyName>app:language</wfs:PropertyName>
			<wfs:PropertyName>app:characterSet</wfs:PropertyName>
			<wfs:PropertyName>app:parentidentifier</wfs:PropertyName>
			<wfs:PropertyName>app:hierarchyLevelCode</wfs:PropertyName>
			<wfs:PropertyName>app:hierarchyLevelName</wfs:PropertyName>
			<wfs:PropertyName>app:contact</wfs:PropertyName>
			<wfs:PropertyName>app:dateStamp</wfs:PropertyName>
			<wfs:PropertyName>app:metadataStandardName</wfs:PropertyName>
			<wfs:PropertyName>app:metadataStandardVersion</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:editiondate</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:revisiondate</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:creationdate</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:fileidentifier</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:identifier</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:citedResponsibleParty</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:abstract</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:pointOfContact</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:resourceConstraints</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:topicCategory/app:MD_TopicCategoryCode/app:category</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:boundingBox</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:verticalExtent</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:temportalExtent</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:spatialRepresentationType</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:spatialResolution</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:language</wfs:PropertyName>
			<wfs:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:characterSet</wfs:PropertyName>
			<wfs:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineagestatement</wfs:PropertyName>
			<wfs:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineagesourcedesc</wfs:PropertyName>
			<wfs:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineageprocessdesc</wfs:PropertyName>
			<wfs:PropertyName>app:referenceSystemInfo</wfs:PropertyName>
			<wfs:PropertyName>app:distributionInfo.digitalTransferOptions/app:MD_DigTransferOpt/app:offlineMediumName</wfs:PropertyName>
			<wfs:PropertyName>app:distributionInfo.digitalTransferOptions/app:MD_DigTransferOpt/app:offlineMediumFormat</wfs:PropertyName>
			<wfs:PropertyName>app:distributionInfo.digitalTransferOptions/app:MD_DigTransferOpt/app:onlineResource</wfs:PropertyName>
		</xsl:if>
		<xsl:if test=". = 'full' "/>
		<xsl:if test=". = 'hits' ">
			<wfs:PropertyName>_COUNT_</wfs:PropertyName>
		</xsl:if>
	</xsl:template>
	<xsl:template name="SETMINIMUMCONSTRAINT">
		<ogc:Filter>
			<ogc:PropertyIsEqualTo>
				<ogc:PropertyName>app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue</ogc:PropertyName>
				<ogc:Literal>
					<xsl:if test="$HIERARCHY = 'dataset'">dataset</xsl:if>
					<xsl:if test="$HIERARCHY = 'datasetcollection'">series</xsl:if>
					<xsl:if test="$HIERARCHY = 'service'">service</xsl:if>
					<xsl:if test="$HIERARCHY = 'application'">application</xsl:if>
				</ogc:Literal>
			</ogc:PropertyIsEqualTo>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="csw:Constraint">
		<ogc:Filter>
			<xsl:apply-templates select="ogc:Filter"/>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="ogc:Filter">
		<ogc:And>
			<ogc:PropertyIsEqualTo>
				<ogc:PropertyName>app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue</ogc:PropertyName>
				<ogc:Literal>
					<xsl:if test="$HIERARCHY = 'dataset'">dataset</xsl:if>
					<xsl:if test="$HIERARCHY = 'datasetcollection'">series</xsl:if>
					<xsl:if test="$HIERARCHY = 'service'">service</xsl:if>
					<xsl:if test="$HIERARCHY = 'application'">software</xsl:if>
				</ogc:Literal>
			</ogc:PropertyIsEqualTo>
			<xsl:apply-templates select="ogc:And"/>
			<xsl:apply-templates select="ogc:Or"/>
			<xsl:apply-templates select="ogc:Not"/>
			<xsl:if test="local-name(./child::*[1]) != 'And' and local-name(./child::*[1])!='Or' and local-name(./child::*[1])!='Not'">
				<xsl:for-each select="./child::*">
					<xsl:call-template name="copyProperty"/>
				</xsl:for-each>
			</xsl:if>
		</ogc:And>
	</xsl:template>
	<xsl:template match="ogc:And | ogc:Or | ogc:Not">
		<xsl:copy>
			<xsl:apply-templates select="ogc:And"/>
			<xsl:apply-templates select="ogc:Or"/>
			<xsl:apply-templates select="ogc:Not"/>
			<xsl:for-each select="./child::*">
				<xsl:if test="local-name(.) != 'And' and local-name(.)!='Or' and local-name(.)!='Not'">
					<xsl:call-template name="copyProperty"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template name="copyProperty">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName = 'AnyText' ">
				<xsl:choose>
					<xsl:when test="$HIERARCHY = 'service'">
						<xsl:call-template name="anytext_service">
							<xsl:with-param name="LITERAL" select="./ogc:Literal"/>
							<xsl:with-param name="MATCHCASE" select="./@matchCase"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="anytext_data">
							<xsl:with-param name="LITERAL" select="./ogc:Literal"/>
							<xsl:with-param name="MATCHCASE" select="./@matchCase"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:if test="local-name(.) = 'PropertyIsLike'">
						<xsl:attribute name="wildCard"><xsl:value-of select="./@wildCard"/></xsl:attribute>
						<xsl:attribute name="singleChar"><xsl:value-of select="./@singleChar"/></xsl:attribute>
						<xsl:attribute name="escape"><xsl:value-of select="./@escape"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="boolean( ./@matchCase )">
						<xsl:attribute name="matchCase"><xsl:value-of select="./@matchCase"/></xsl:attribute>
					</xsl:if>
					<ogc:PropertyName>
						<xsl:apply-templates select="ogc:PropertyName"/>
					</ogc:PropertyName>
					<xsl:for-each select="./child::*">
						<xsl:if test="local-name(.) != 'PropertyName' ">
							<xsl:copy-of select="."/>
						</xsl:if>
					</xsl:for-each>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="ogc:PropertyName | csw:ElementName">
		<!-- mapping property name value -->
		<xsl:value-of select="mapping:mapPropertyValue( ., $HIERARCHY )"/>
	</xsl:template>
	<xsl:template name="anytext_data">
		<xsl:param name="LITERAL"/>
		<xsl:param name="MATCHCASE"/>
		<xsl:variable name="CASE">
			<xsl:choose>
				<xsl:when test="$MATCHCASE != '' ">
					<xsl:value-of select="$MATCHCASE"/>
				</xsl:when>
				<xsl:otherwise>true</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<ogc:Or>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:topicCategory/app:MD_TopicCategoryCode/app:category</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:identifier</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:abstract</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:alternateTitle</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineagestatement</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineagesourcedesc</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataQualityInfo/app:DQ_DataQuality/app:lineageprocessdesc</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:keyword/app:Keyword/app:keyword</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:format/app:MD_Format/app:name</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:spatialRepresentationType/app:MD_SpatialRepTypeCode/app:codelistvalue</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:language</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:citedResponsibleParty/app:CI_RespParty/app:organisationname</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:hierarchyLevelName/app:HierarchyLevelName/app:name</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:dataIdentification/app:MD_DataIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:type/app:MD_KeywordTypeCode/app:codelistvalue</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
		</ogc:Or>
	</xsl:template>
	<xsl:template name="anytext_service">
		<xsl:param name="LITERAL"/>
		<xsl:param name="MATCHCASE"/>
		<xsl:variable name="CASE">
			<xsl:choose>
				<xsl:when test="$MATCHCASE != '' ">
					<xsl:value-of select="$MATCHCASE"/>
				</xsl:when>
				<xsl:otherwise>true</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<ogc:Or>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:identifier</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:abstract</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:alternateTitle</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:keyword/app:Keyword/app:keyword</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:language</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:citedResponsibleParty/app:CI_RespParty/app:organisationname</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:hierarchyLevelName/app:HierarchyLevelName/app:name</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords/app:type/app:MD_KeywordTypeCode/app:codelistvalue</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:fees</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:orderingInstructions</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:turnaround</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:resourceConstraints/app:MD_LegalConstraints/app:useLimitations</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:resourceConstraints/app:MD_LegalConstraints/app:otherConstraints</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:operationMetadata/app:SV_OperationMetadata/app:operationName/app:OperationNames/app:name</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:operationMetadata/app:SV_OperationMetadata/app:operationDescription</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
			<ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
				<xsl:attribute name="matchCase"><xsl:value-of select="$CASE"/></xsl:attribute>
				<ogc:PropertyName>app:serviceIdentification/app:CSW_ServiceIdentification/app:operationMetadata/app:SV_OperationMetadata/app:invocationName</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="$LITERAL"/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
		</ogc:Or>
	</xsl:template>
</xsl:stylesheet>
