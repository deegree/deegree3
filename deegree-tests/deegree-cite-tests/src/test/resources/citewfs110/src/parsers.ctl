<?xml version="1.0" encoding="UTF-8"?>
<ctl:package
 xmlns="http://www.w3.org/2001/XMLSchema"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:parsers="http://www.occamlab.com/te/parsers"
 xmlns:p="http://teamengine.sourceforge.net/parsers"
 xmlns:saxon="http://saxon.sf.net/"
 xmlns:wfs="http://www.opengis.net/wfs"
 xmlns:ows="http://www.opengis.net/ows"
 xmlns:xi="http://www.w3.org/2001/XInclude"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema">

	<!--=========-->
	<!-- PARSERS -->
	<!--=========-->
	
	<!-- Used to call the schematron validator outside the request element, i.e.: -->
	<!--<ctl:call-test name="ctl:SchematronValidatingParser">
		<ctl:with-param name="doc" select="$cap-doc"/>
		<ctl:with-param name="schematronFile">sch/wfs/1.1.0/WFSCapabilities.sch</ctl:with-param>
		<ctl:with-param name="phase">Default</ctl:with-param>
	</ctl:call-test>-->
	<ctl:test name="ctl:SchematronValidatingParser">
			<ctl:param name="doc"/>
			<ctl:param name="schema"/>
			<ctl:param name="phase"/>
			<ctl:assertion>Validate an XML instance against a Schematron schema using the given phase.</ctl:assertion>
			<ctl:code>
				<xsl:choose>
					<xsl:when test="not($doc)">
						<ctl:message>Error: Null input document.</ctl:message>
						<ctl:fail/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="results">
							<ctl:call-function name="ctl:SchematronValidator">
								<ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param>
								<ctl:with-param name="schema" select="string($schema)"/>
								<ctl:with-param name="phase" select="string($phase)"/>
							</ctl:call-function>
						</xsl:variable>
						<xsl:if test="count($results/*) > 0">
							<ctl:message>Total number of errors detected: <xsl:copy-of select="count($results/*)"/></ctl:message>
							<xsl:for-each select="$results/*">
								<ctl:message><xsl:value-of select="concat('Error ', position())"/>: <xsl:copy-of select="."/></ctl:message>
							</xsl:for-each>
							<ctl:fail/>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</ctl:code>
	</ctl:test>	
	<ctl:function name="ctl:SchematronValidator">
		<ctl:param name="doc"/>
		<ctl:param name="schema"/>
		<ctl:param name="phase"/>
		<ctl:return>A list of errors (NodeList of "error" elements).</ctl:return>
		<ctl:description>Invokes the Schematon validator.</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" 
                  method="validate" 
                  initialized="true"/>
	</ctl:function>	
	
	<!-- Used to call the xml validator outside the request element (after using parsers:HTTPParser in this case), i.e.: -->
	<!--<ctl:call-test name="ctl:XMLValidatingParser">
			<ctl:with-param name="doc"><xsl:copy-of select="$response//content/*"/></ctl:with-param>
			<ctl:with-param name="instruction">
				<parsers:schemas>
					<parsers:schema type="resource">xsd/ogc/wfs/1.1.0/wfs.xsd</parsers:schema>
				</parsers:schemas>
			</ctl:with-param>
	</ctl:call-test>-->
  <ctl:test name="ctl:XMLValidatingParser">
    <ctl:param name="doc"/>
    <ctl:param name="instruction"/>
    <ctl:assertion>Validates the XML instance against the set of XML Schemas specified using the given instruction parameter.</ctl:assertion>
    <ctl:code>
		<xsl:choose>
			<xsl:when test="not($doc)">
				<ctl:message>Error: Null input document.</ctl:message>
				<ctl:fail/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="results">
					<ctl:call-function name="ctl:XMLValidator">
						<ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param>
						<ctl:with-param name="instruction"><xsl:copy-of select="$instruction"/></ctl:with-param>
					</ctl:call-function>
				</xsl:variable>
				<xsl:if test="count($results/*) > 0">
					<ctl:message>Total number of errors detected: <xsl:copy-of select="count($results/*)"/></ctl:message>
					<xsl:for-each select="$results/*">
						<ctl:message><xsl:value-of select="concat('Error ', position())"/>: <xsl:copy-of select="."/></ctl:message>
					</xsl:for-each>
					<ctl:fail/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
    </ctl:code>
  </ctl:test>
  <ctl:function name="ctl:XMLValidator">
		<ctl:param name="doc"/>
		<ctl:param name="instruction"/>
		<ctl:return>A list of errors (NodeList of "error" elements).</ctl:return>
		<ctl:description>Invokes the XML Schema validator.</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" 
                method="validate" 
                initialized="true"/>
  </ctl:function>

	<!-- Sample usage:
    <ctl:call-test name="ctl:XMLValidatingParser.WFS">
			<ctl:with-param name="doc"><xsl:copy-of select="$response//content/*"/></ctl:with-param>
	</ctl:call-test>
    -->
	<ctl:test name="ctl:XMLValidatingParser.GMLSF2">
			<ctl:param name="doc"/>
			<ctl:assertion>Validate response entity against WFS schemas.</ctl:assertion>
			<ctl:code>
				<ctl:call-test name="ctl:XMLValidatingParser">
					<ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param>
					<ctl:with-param name="instruction">				
						<parsers:schemas>
							<parsers:schema type="resource">xsd/ogc/xlink/1.0.0/xlinks.xsd</parsers:schema>
							<parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema>					
							<parsers:schema type="resource">xsd/ogc/cite/gmlsf2.xsd</parsers:schema>					
							<parsers:schema type="resource">xsd/ogc/filter/1.1.0/filter-1.1.0.xsd</parsers:schema>
							<parsers:schema type="resource">xsd/ogc/cite/cite-gmlsf2.xsd</parsers:schema>				
							<parsers:schema type="resource">xsd/ogc/cite/wfs.xsd</parsers:schema>				
						</parsers:schemas>
					</ctl:with-param>
				</ctl:call-test>
			</ctl:code>			
	</ctl:test>	

	<ctl:test name="ctl:XMLValidatingParser.GMLSF1">
			<ctl:param name="doc"/>
			<ctl:assertion>Validate response entity against WFS schemas.</ctl:assertion>
			<ctl:code>
				<ctl:call-test name="ctl:XMLValidatingParser">
					<ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param>
					<ctl:with-param name="instruction">				
						<parsers:schemas>
							<parsers:schema type="resource">xsd/ogc/xlink/1.0.0/xlinks.xsd</parsers:schema>
							<parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema>					
							<parsers:schema type="resource">xsd/ogc/cite/gmlsf.xsd</parsers:schema>					
							<parsers:schema type="resource">xsd/ogc/filter/1.1.0/filter-1.1.0.xsd</parsers:schema>
							<parsers:schema type="resource">xsd/ogc/cite/cite-gmlsf1.xsd</parsers:schema>				
							<parsers:schema type="resource">xsd/ogc/cite/wfs.xsd</parsers:schema>
						</parsers:schemas>
					</ctl:with-param>
				</ctl:call-test>
			</ctl:code>			
	</ctl:test>	
	
	<!-- XML validating parsers, defined for various response types to be reused by multiple tests -->
	<ctl:parser name="p:XMLValidatingParser.GMLSF2">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true">
			<ctl:with-param name="schemas_links">
				<parsers:schemas>
					<parsers:schema type="resource">xsd/ogc/xlink/1.0.0/xlinks.xsd</parsers:schema>
					<parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema>					
					<parsers:schema type="resource">xsd/ogc/cite/gmlsf2.xsd</parsers:schema>					
					<parsers:schema type="resource">xsd/ogc/filter/1.1.0/filter-1.1.0.xsd</parsers:schema>
					<parsers:schema type="resource">xsd/ogc/cite/cite-gmlsf2.xsd</parsers:schema>				
					<parsers:schema type="resource">xsd/ogc/cite/wfs.xsd</parsers:schema>					
				</parsers:schemas>
			</ctl:with-param>
		</ctl:java>
	</ctl:parser>	

	<ctl:parser name="p:XMLValidatingParser.GMLSF1">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true">
			<ctl:with-param name="schemas_links">
				<parsers:schemas>
					<parsers:schema type="resource">xsd/ogc/xlink/1.0.0/xlinks.xsd</parsers:schema>
					<parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema>					
					<parsers:schema type="resource">xsd/ogc/cite/gmlsf.xsd</parsers:schema>					
					<parsers:schema type="resource">xsd/ogc/filter/1.1.0/filter-1.1.0.xsd</parsers:schema>
					<parsers:schema type="resource">xsd/ogc/cite/cite-gmlsf1.xsd</parsers:schema>				
					<parsers:schema type="resource">xsd/ogc/cite/wfs.xsd</parsers:schema>
				</parsers:schemas>
			</ctl:with-param>
		</ctl:java>
	</ctl:parser>	

	<ctl:parser name="p:XMLValidatingParser.OWS">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true">
			<ctl:with-param name="schemas_links">
				<parsers:schemas>
					<parsers:schema type="resource">xsd/ogc/xlink/1.0.0/xlinks.xsd</parsers:schema>				
					<parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema>
				</parsers:schemas>
			</ctl:with-param>
		</ctl:java>
	</ctl:parser>	
	
	<ctl:parser name="p:XMLValidatingParser.XMLSchema">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true">
			<ctl:with-param name="schemas_links">
				<parsers:schemas>
					<parsers:schema type="resource">xsd/w3c/xmlschema/1.0/XMLSchema.xsd</parsers:schema>
				</parsers:schemas>
			</ctl:with-param>
		</ctl:java>
	</ctl:parser>		
	
	<!-- Schematron validator used in request element, pass in information for schematron schema to use, i.e.: -->
	<!--<p:SchematronValidatingParser>
			<parsers:schemas>
				<parsers:schema type="resource" phase="Default">sch/wfs/1.1.0/WFSCapabilities.sch</parsers:schema>
			</parsers:schemas>
		</p:SchematronValidatingParser>-->
	<ctl:parser name="p:SchematronValidatingParser">
		<ctl:param name="schema_link"/>
		<ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" method="parse" initialized="true"/>
	</ctl:parser>		
	
	<!-- Schematron validator used in request element, uses the given schema, i.e.: -->
	<!--<p:SchematronValidatingParser.WFSCapabilities/>-->
	<ctl:parser name="p:SchematronValidatingParser.WFSCapabilities">
		<ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" method="parse" initialized="true">
			<ctl:with-param name="schema_link">
					<parsers:schemas>
						<parsers:schema type="resource" phase="Default">sch/wfs/1.1.0/WFSCapabilities.sch</parsers:schema>
					</parsers:schemas>
			</ctl:with-param>
		</ctl:java>
	</ctl:parser>	
	
</ctl:package>