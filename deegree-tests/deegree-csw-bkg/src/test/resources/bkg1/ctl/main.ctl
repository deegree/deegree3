<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:xsd="http://www.w3.org/2001/XMLSchema">

  <suite name="csw:csw_2.0.2_ap_iso_1.0">
    <title>CSW 2.0.2 AP ISO 1.0 Compliance Test Suite</title>
    <description>
      Validates a CSW 2.0.2 catalogue implementation against the ISO 1.0
      application profile.
    </description>
    <starting-test>csw:csw-main</starting-test>
  </suite>

  <test name="csw:csw-main">
    <assertion>Run the CSW 2.0.2 AP ISO 1.0 compliance tests</assertion>
    <code>

          <!-- ***** Hard-wired the URL ***** -->
      <xsl:variable name="csw.capabilities.url" select="'${cswUrl}?service=CSW&amp;request=GetCapabilities'"/>

      <message>Capabilities request/URL used: <xsl:value-of select="$csw.capabilities.url" /></message>

      <!-- Attempt to retrieve capabilities document -->
      <xsl:variable name="csw.GetCapabilities.document">
        <request>
          <url>
            <xsl:value-of select="$csw.capabilities.url"/>
          </url>
          <method>GET</method>
        </request>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="not($csw.GetCapabilities.document/csw2:Capabilities)">
          <message>FAILURE: Did not receive a csw:Capabilities document! Skipping remaining tests.</message>
          <fail/>
        </xsl:when>
        <xsl:otherwise>
            <call-test name="csw:level1.1">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
            <call-test name="csw:level1.2">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
            <call-test name="csw:level1.3">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
            <call-test name="csw:level1.4">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
            <call-test name="csw:level1.5">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
            <call-test name="csw:level1.6">
              <with-param name="csw.GetCapabilities.document" select="$csw.GetCapabilities.document"/>
            </call-test>
        </xsl:otherwise>
      </xsl:choose>
    </code>
  </test>
</package>
