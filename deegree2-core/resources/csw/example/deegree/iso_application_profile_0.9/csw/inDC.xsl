<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:include href="inDC_WFSMapping.xsl"/>
    <xsl:template match="csw:GetRecords">
        <wfs:GetFeature outputFormat="GML2" xmlns:gml="http://www.opengis.net/gml">
            <xsl:if test="/@maxRecords != '' ">
                <xsl:attribute name="maxFeature"><xsl:value-of select="/@maxRecords"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="/@startPosition != '' ">
                <xsl:attribute name="startPosition"><xsl:value-of select="/@startPosition"/></xsl:attribute>
            </xsl:if>
            <xsl:for-each select="./csw:Query">
                <xsl:variable name="TYPENAME">
                    <xsl:value-of select="../@outputSchema"/>:<xsl:value-of select="./@typeName"/>
                </xsl:variable>
                <!--        <xsl:if test="$TYPENAME = 'DublinCore:csw:Record' or $TYPENAME = 'OGCCORE:Product'  ">-->
                <wfs:Query>
                    <xsl:attribute name="typeName">csw:Record</xsl:attribute>
                    <xsl:apply-templates select="."/>
                </wfs:Query>
                <!--        </xsl:if>-->
            </xsl:for-each>
        </wfs:GetFeature>
        <xsl:apply-templates select="csw:ResponseHandler"/>
    </xsl:template>
    <xsl:template match="csw:ResponseHandler"/>
    <xsl:template match="csw:Query">
        <xsl:if test="./csw:ElementSetName = 'brief' ">
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_identifier"/>
            </wfs:PropertyName>
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_title"/>
            </wfs:PropertyName>
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_subject"/>
            </wfs:PropertyName>
        </xsl:if>
        <xsl:if test="./csw:ElementSetName = 'summary' ">
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_identifier"/>
            </wfs:PropertyName>
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_title"/>
            </wfs:PropertyName>
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_subject"/>
            </wfs:PropertyName>
            <wfs:PropertyName>
                <xsl:value-of select="$WFS_description"/>
            </wfs:PropertyName>
        </xsl:if>
        <xsl:if test="./csw:ElementSetName = 'full' "/>
        <xsl:if test="./csw:ElementSetName = 'hits' ">
            <wfs:PropertyName>_COUNT_</wfs:PropertyName>
        </xsl:if>
        <!--xsl:apply-templates select="csw:ElementName"/-->
        <xsl:for-each select="./child::*">
            <xsl:if test="local-name(.) = 'ElementName' ">
                <wfs:PropertyName>
                    <xsl:apply-templates select="."/>
                </wfs:PropertyName>
            </xsl:if>
        </xsl:for-each>
        <xsl:apply-templates select="csw:Constraint"/>
    </xsl:template>
    <xsl:template match="csw:Constraint">
        <ogc:Filter>
            <xsl:apply-templates select="ogc:Filter"/>
        </ogc:Filter>
    </xsl:template>
    <xsl:template match="ogc:Filter">
        <xsl:apply-templates select="ogc:And"/>
        <xsl:apply-templates select="ogc:Or"/>
        <xsl:apply-templates select="ogc:Not"/>
        <xsl:if test="local-name(./child::*[1]) != 'And' and local-name(./child::*[1])!='Or' and local-name(./child::*[1])!='Not'">
            <xsl:for-each select="./child::*">
                <xsl:call-template name="copyProperty"/>
            </xsl:for-each>
        </xsl:if>
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
        <xsl:copy>
            <xsl:if test="local-name(.) = 'PropertyIsLike'">
                <xsl:attribute name="wildCard"><xsl:value-of select="./@wildCard"/></xsl:attribute>
                <xsl:attribute name="singleChar"><xsl:value-of select="./@singleChar"/></xsl:attribute>
                <xsl:attribute name="escape"><xsl:value-of select="./@escape"/></xsl:attribute>
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
    </xsl:template>
    <xsl:template match="ogc:PropertyName | csw:ElementName">
        <!-- mapping property name value -->
        <xsl:variable name="VAL">
            <xsl:value-of select="."/>
        </xsl:variable>
        <xsl:if test="$VAL = 'title'">
            <xsl:value-of select="$WFS_title"/>
        </xsl:if>
        <xsl:if test="$VAL = 'creator'">
            <xsl:value-of select="$WFS_creator"/>
        </xsl:if>
        <xsl:if test="$VAL = 'subject'">
            <xsl:value-of select="$WFS_subject"/>
        </xsl:if>
        <xsl:if test="$VAL = 'description'">
            <xsl:value-of select="$WFS_description"/>
        </xsl:if>
        <xsl:if test="$VAL = 'publisher'">
            <xsl:value-of select="$WFS_publisher"/>
        </xsl:if>
        <xsl:if test="$VAL = 'contributor'">
            <xsl:value-of select="$WFS_contributor"/>
        </xsl:if>
        <xsl:if test="$VAL = 'date'">
            <xsl:value-of select="$WFS_date"/>
        </xsl:if>
        <xsl:if test="$VAL = 'type'">
            <xsl:value-of select="$WFS_type"/>
        </xsl:if>
        <xsl:if test="$VAL = 'format'">
            <xsl:value-of select="$WFS_format"/>
        </xsl:if>
        <xsl:if test="$VAL = 'identifier'">
            <xsl:value-of select="$WFS_identifier"/>
        </xsl:if>
        <xsl:if test="$VAL = 'source'">
            <xsl:value-of select="$WFS_source"/>
        </xsl:if>
        <xsl:if test="$VAL = 'language'">
            <xsl:value-of select="$WFS_language"/>
        </xsl:if>
        <xsl:if test="$VAL = 'relation'">
            <xsl:value-of select="$WFS_relation"/>
        </xsl:if>
        <xsl:if test="$VAL = 'coverage'">
            <xsl:value-of select="$WFS_coverage"/>
        </xsl:if>
        <xsl:if test="$VAL = 'rights'">
            <xsl:value-of select="$WFS_rights"/>
        </xsl:if>
        <xsl:value-of select="VErdamgtmtwse"/>
    </xsl:template>
</xsl:stylesheet>
