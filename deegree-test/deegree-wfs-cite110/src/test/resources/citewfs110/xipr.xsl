<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: xipr.xsl 297 2007-04-04 01:58:59Z dret $ -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- XInclude Processor (XIPr) - Erik Wilde (http://dret.net/netdret/) - http://dret.net/projects/xipr/ -->
<!-- XIPr is licensed under the GNU Lesser General Public License (LGPL). See http://creativecommons.org/licenses/LGPL/2.1/ for licensing details. -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- XIPr Instructions: Include xipr.xsl in your XSLT 2.0 stylesheet using <xsl:include href=".../xipr.xsl"> and initiate the XInclude process at any node (only document and element nodes are reasonable node kinds, though) of a given XML document with the following instruction: <xsl:apply-templates select="$node" mode="xipr"/> -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- XInclude Instructions: For instructions on how to use XInclude, please look at the XInclude specification (http://www.w3.org/TR/xinclude/) or other resources available on the Web. Please remember that the XInclude elements <xi:include> and <xi:fallback> must use the XInclude namespace (http://www.w3.org/2001/XInclude), otherwise they will not be recognized as XInclude elements. -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xipr="http://dret.net/projects/xipr/">
	<xsl:param name="fix-xml-base">yes</xsl:param>
	<xsl:template match="/*">
		<!-- if there is no other template handling the document element, this template initiates xinclude processing at the document element of the input document. -->
		<xsl:apply-templates select="." mode="xipr"/>
	</xsl:template>
	<xsl:template match="@* | node()" mode="xipr">
		<xsl:apply-templates select="." mode="xipr-internal">
			<!-- the sequences of included uri/xpointer values need to be initialized with the starting document of the xinclude processing (required for detecting inclusion loops). -->
			<xsl:with-param name="uri-history" select="document-uri(/)" tunnel="yes"/>
			<xsl:with-param name="xpointer-history" select="''" tunnel="yes"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="@* | node()" mode="xipr-internal">
		<!-- this template handles all nodes which do not require xinclude processing. -->
		<xsl:copy>
			<!-- the xinclude process recursively processes the document until it finds an xinclude node. -->
			<xsl:apply-templates select="@* | node()" mode="xipr-internal"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="xi:include" mode="xipr-internal">
		<!-- the two parameters are required for detecting inclusion loops, they contain the complete history of href and xpointer attributes as sequences. -->
		<xsl:param name="uri-history" tunnel="yes"/>
		<xsl:param name="xpointer-history" tunnel="yes"/>
		<!-- REC: The children property of the xi:include element may include a single xi:fallback element; the appearance of more than one xi:fallback element, an xi:include element, or any other element from the XInclude namespace is a fatal error. -->
		<xsl:if test="count(xi:fallback) > 1 or exists(xi:include) or exists(xi:*[local-name() ne 'fallback'])">
			<xsl:sequence select="xipr:message('xi:include elements may only have no or one single xi:fallback element as their only xi:* child', 'fatal')"/>
		</xsl:if>
		<xsl:if test="not(matches(@accept, '^[&#x20;-&#x7E;]*$'))">
			<!-- SPEC: Values containing characters outside the range #x20 through #x7E must be flagged as fatal errors. -->
			<xsl:sequence select="xipr:message('accept contains illegal character(s)', 'fatal')"/>
		</xsl:if>
		<xsl:if test="not(matches(@accept-language, '^[&#x20;-&#x7E;]*$'))">
			<!-- SPEC: Values containing characters outside the range #x20 through #x7E are disallowed in HTTP headers, and must be flagged as fatal errors. -->
			<xsl:sequence select="xipr:message('accept-language contains illegal character(s)', 'fatal')"/>
		</xsl:if>
		<xsl:if test="exists(@accept)">
			<xsl:sequence select="xipr:message('XIPr does not support the accept attribute', 'info')"/>
		</xsl:if>
		<xsl:if test="exists(@accept-language)">
			<xsl:sequence select="xipr:message('XIPr does not support the accept-language attribute', 'info')"/>
		</xsl:if>
		<xsl:variable name="include-uri" select="resolve-uri(@href, document-uri(/))"/>
		<xsl:choose>
			<xsl:when test="@parse eq 'xml' or empty(@parse)">
				<!-- SPEC: This attribute is optional. When omitted, the value of "xml" is implied (even in the absence of a default value declaration). -->
				<xsl:if test="empty(@href | @xpointer)">
					<!-- SPEC: If the href attribute is absent when parse="xml", the xpointer attribute must be present. -->
					<xsl:sequence select="xipr:message('For parse=&quot;xml&quot;, at least one the href or xpointer attributes must be present', 'fatal')"/>
				</xsl:if>
				<xsl:if test="( index-of($uri-history, $include-uri ) = index-of($xpointer-history, string(@xpointer)) )">
					<!-- SPEC: When recursively processing an xi:include element, it is a fatal error to process another xi:include element with an include location and xpointer attribute value that have already been processed in the inclusion chain. -->
					<xsl:sequence select="xipr:message(concat('Recursive inclusion (same href/xpointer) of ', @href), 'fatal')"/>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="doc-available($include-uri)">
						<xsl:variable name="include-doc" select="doc($include-uri)"/>
						<xsl:choose>
							<xsl:when test="empty(@xpointer)">
								<!-- SPEC: The inclusion target might be a document information item (for instance, no specified xpointer attribute, or an XPointer specifically locating the document root.) In this case, the set of top-level included items is the children of the acquired infoset's document information item, except for the document type declaration information item child, if one exists. -->
								<xsl:for-each select="$include-doc/node()">
									<xsl:choose>
										<xsl:when test="self::*">
											<!-- for elements, copy the element and perform base uri fixup. -->
											<xsl:copy-of select="xipr:include(., $include-uri, @xpointer, $uri-history, $xpointer-history)"/>
										</xsl:when>
										<xsl:otherwise>
											<!-- copy everything else (i.e., everything which is not an element). -->
											<xsl:copy/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<!-- there is an xpointer attribute... -->
								<xsl:variable name="xpointer-node">
									<xsl:choose>
										<!-- xpointer uses a shorthand pointer (formerly known as barename), NCName regex copied from the schema for schemas. -->
										<xsl:when test="matches(@xpointer, '^[\i-[:]][\c-[:]]*$')">
											<xsl:copy-of select="xipr:include(id(@xpointer, $include-doc), $include-uri, @xpointer, $uri-history, $xpointer-history)"/>
										</xsl:when>
										<!-- xpointer uses the element() scheme; regex derived from XPointer element() scheme spec: http://www.w3.org/TR/xptr-element/#NT-ElementSchemeData (NCName regex copied from the schema for schemas). -->
										<xsl:when test="matches(@xpointer, '^element\([\i-[:]][\c-[:]]*((/[1-9][0-9]*)+)?|(/[1-9][0-9]*)+\)$')">
											<xsl:variable name="element-pointer" select="replace(@xpointer, 'element\((.*)\)', '$1')"/>
											<xsl:choose>
												<xsl:when test="not(contains($element-pointer, '/'))">
													<!-- the pointer is a simple id, which can be located using the id() function. -->
													<xsl:copy-of select="xipr:include(id($element-pointer, $include-doc), $include-uri, @xpointer, $uri-history, $xpointer-history)"/>
												</xsl:when>
												<xsl:otherwise>
													<!-- child sequence evaluation starts from the root or from an element identified by a NCName. -->
													<xsl:copy-of select="xipr:include(xipr:child-sequence( if ( starts-with($element-pointer, '/') ) then $include-doc else id(substring-before($element-pointer, '/'), $include-doc), substring-after($element-pointer, '/')), $include-uri, @xpointer, $uri-history, $xpointer-history)"/>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:when>
										<xsl:otherwise>
											<!-- xpointer uses none of the schemes covered in the preceding branches. -->
											<xsl:sequence select="xipr:message('XIPr only supports the XPointer element() scheme (skipping...)', 'warning')"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<xsl:choose>
									<xsl:when test="exists($xpointer-node/node())">
										<!-- xpointer evaluation returned a node. -->
										<xsl:copy-of select="$xpointer-node/node()"/>
									</xsl:when>
									<xsl:otherwise>
										<!-- the xpointer did not return a result, a message is produced and fallback processing is initiated. -->
										<xsl:sequence select="xipr:message(concat('Evaluation of xpointer ', @xpointer, ' returned nothing'), 'resource')"/>
										<xsl:call-template name="fallback"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!-- this branch is executed when the doc-available() function returned false(), a message is produced and fallback processing is initiated. -->
						<xsl:sequence select="xipr:message(concat('Could not read document ', $include-uri), 'resource')"/>
						<xsl:call-template name="fallback"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="@parse eq 'text'">
				<xsl:if test="exists(@xpointer)">
					<!-- SPEC: The xpointer attribute must not be present when parse="text". -->
					<xsl:sequence select="xipr:message('The xpointer attribute is not allowed for parse=&quot;text&quot;', 'warning')"/>			
				</xsl:if>
				<xsl:choose>
					<xsl:when test="unparsed-text-available($include-uri)">
						<xsl:value-of select="if ( empty(@encoding) ) then unparsed-text($include-uri) else unparsed-text($include-uri, string(@encoding))"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- this branch is executed when the unparsed-text-available() function returned false(), a message is produced and fallback processing is initiated. -->
						<xsl:sequence select="xipr:message(concat('Could not read document ', $include-uri), 'resource')"/>
						<xsl:call-template name="fallback"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- SPEC: Values other than "xml" and "text" are a fatal error. -->
				<xsl:sequence select="xipr:message(concat('Unknown xi:include attribute value parse=&quot;', @parse ,'&quot;'), 'fatal')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:function name="xipr:include">
		<xsl:param name="context"/>
		<xsl:param name="include-uri"/>
		<xsl:param name="xpointer"/>
		<xsl:param name="uri-history"/>
		<xsl:param name="xpointer-history"/>
		<xsl:for-each select="$context">
			<xsl:choose>
				<xsl:when test="$fix-xml-base = 'yes'">
					<xsl:copy>
						<xsl:attribute name="xml:base" select="$include-uri"/>
						<!-- SPEC: If an xml:base attribute information item is already present, it is replaced by the new attribute. -->
						<xsl:apply-templates select="@*[name() ne 'xml:base'] | node()" mode="xipr-internal">
							<xsl:with-param name="uri-history" select="($uri-history, $include-uri)" tunnel="yes"/>
							<xsl:with-param name="xpointer-history" select="($xpointer-history, string($xpointer))" tunnel="yes"/>
						</xsl:apply-templates>
					</xsl:copy>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy>
						<xsl:apply-templates select="@*| node()" mode="xipr-internal">
							<xsl:with-param name="uri-history" select="($uri-history, $include-uri)" tunnel="yes"/>
							<xsl:with-param name="xpointer-history" select="($xpointer-history, string($xpointer))" tunnel="yes"/>
						</xsl:apply-templates>
					</xsl:copy>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:function>
	<xsl:function name="xipr:child-sequence">
		<xsl:param name="context"/>
		<xsl:param name="path"/>
		<xsl:choose>
			<!-- if this is the last path segment, return the node. -->
			<xsl:when test="not(contains($path, '/'))">
				<xsl:sequence select="$context/*[number($path)]"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- go one step along the child sequence by selecting the next node and trimming the path. -->
				<xsl:sequence select="xipr:child-sequence($context/*[number(substring-before($path, '/'))], substring-after($path, '/'))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:template name="fallback">
		<xsl:if test="exists(xi:fallback[empty(parent::xi:include)])">
			<!-- SPEC: It is a fatal error for an xi:fallback  element to appear in a document anywhere other than as the direct child of the xi:include (before inclusion processing on the contents of the element). -->
			<xsl:sequence select="xipr:message('xi:fallback is only allowed as the direct child of xi:include', 'fatal')"/>
		</xsl:if>
		<xsl:if test="exists(xi:fallback[count(xi:include) ne count(xi:*)])">
			<!-- SPEC: It is a fatal error  for the xi:fallback element to contain any elements from the XInclude namespace other than xi:include. -->
			<xsl:sequence select="xipr:message('xi:fallback may not contain other xi:* elements than xi:include', 'fatal')"/>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="count(xi:fallback) = 1">
				<xsl:apply-templates select="xi:fallback/*" mode="xipr-internal"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- SPEC: It is a fatal error if there is zero or more than one xi:fallback element. -->
				<xsl:sequence select="xipr:message('No xi:fallback for resource error', 'fatal')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:function name="xipr:message">
		<xsl:param name="message"/>
		<xsl:param name="level"/>
		<xsl:choose>
			<xsl:when test="$level eq 'info'">
				<xsl:message terminate="no">
					<xsl:value-of select="concat('INFO: ', $message)"/>
				</xsl:message>
			</xsl:when>
			<xsl:when test="$level eq 'warning'">
				<xsl:message terminate="no">
					<xsl:value-of select="concat('WARNING: ', $message)"/>
				</xsl:message>
			</xsl:when>
			<xsl:when test="$level eq 'resource'">
				<xsl:message terminate="no">
					<xsl:value-of select="concat('RESOURCE ERROR: ', $message)"/>
				</xsl:message>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message terminate="yes">
					<xsl:value-of select="concat('FATAL ERROR: ', $message)"/>
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
</xsl:stylesheet>
