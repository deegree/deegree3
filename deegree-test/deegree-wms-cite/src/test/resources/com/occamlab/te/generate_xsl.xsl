<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:te="http://www.occamlab.com/te"
 xmlns:saxon="http://saxon.sf.net/"
 extension-element-prefixes="saxon"
 version="2.0">
 
 	<!--
         The input XML for this stylesheet should be a CTL source file.
 	     An outdir parameter must also be supplied.
 	     The stylesheet generates executable XSL stylesheets for each
 	     test and CTL function, and writes them to files in outdir.
 	     The output XML is an index of meta-information about each
 	     suite, test, function, and parser objects in the CTL file,
 	     including the name of the file that was generated for
 	     test and CTL function objects.
 	     Includes are supported.  The source filename and any included
 	     filenames are also written to the index as dependecies.
 	-->

	<xsl:strip-space elements="*"/>
	<xsl:output indent="yes"/>
	
	<!-- Elements in the txsl namespace aren't executed by this stylesheet.
	     They are written into the generated xsl as xsl instructions. -->
	<xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>
	
	<xsl:param name="outdir"/>
	<xsl:param name="xsl-ver">1.0</xsl:param>
	<xsl:variable name="namespaces">
		<namespaces/> <!-- Placeholder element to hold default namespaces -->
	</xsl:variable>

	<!-- Stack for current filename.  Initially the source CTL file.  Include files are added to the stack as they are processed -->
<!-- 	<xsl:variable name="filename-stack" select="stack:new()"/> -->
	<xsl:variable name="stack" saxon:assignable="yes">
		<filename>top</filename>filename>
	</xsl:variable>


	<!-- Supporting functions/templates -->
	
	<xsl:template name="push">
		<xsl:param name="filename"/>
		<saxon:assign name="stack">
			<filename>
				<xsl:value-of select="$filename"/>
			</filename>
			<xsl:copy-of select="$stack/filename"/>
		</saxon:assign>
	</xsl:template>

	<xsl:template name="pop">
		<xsl:value-of select="$stack[1]"/>
		<saxon:assign name="stack">
			<xsl:copy-of select="$stack[position() &gt; 1]"/>
		</saxon:assign>
	</xsl:template>

	<xsl:template name="peek">
		<xsl:value-of select="$stack[1]"/>
	</xsl:template>

	<!-- Returns a string containing the destination filename for a test or function. -->
	<!-- Called by templates match="ctl:test" and match="ctl:function" -->
	<!-- Calls parse-qname -->
	<xsl:function name="te:get-filename" xmlns:file="java:java.io.File" xmlns:uri="java:java.net.URI">
		<xsl:param name="file-type"/>      <!-- "tst" or "fn" -->
		<xsl:param name="prefix"/>
		<xsl:param name="local-name"/>
		<xsl:param name="namespace-uri"/>
		<xsl:param name="seqno"/>          <!-- For recursive calls only -->

		<xsl:variable name="file" select="file:new(concat($outdir, '/', $prefix, $seqno, '$', $local-name, '.', $file-type))"/>	<!-- Example "/workdir/ns$name.fn" -->
		<xsl:choose>
			<xsl:when test="file:exists($file)">
<!--
				<xsl:variable name="qname">
					<xsl:for-each select="saxon:discard-document(document(uri:toString(file:toURI($file))))/xsl:transform/xsl:template">	<!- Get existing template from disk ->
						<xsl:call-template name="parse-qname"/>
					</xsl:for-each>
				</xsl:variable>
 -->
				<xsl:choose>
<!-- 
					<xsl:when test="$qname/namespace-uri = $namespace-uri">
						<!- Duplicate object ->
						<xsl:message>Warning: Overwriting <xsl:value-of select="uri:toString(file:toURI($file))"/></xsl:message>
						<xsl:value-of select="uri:toString(file:toURI($file))"/>
					</xsl:when>
 -->
					<xsl:when test="$prefix = ''">
						<!-- Assign a prefix and try again -->
						<xsl:value-of select="te:get-filename($file-type, 'ns', $local-name, $namespace-uri, '')"/>
					</xsl:when>
					<xsl:when test="string($seqno) = ''">
						<!-- Assign seqno=1 and try again -->
						<xsl:value-of select="te:get-filename($file-type, $prefix, $local-name, $namespace-uri, 1)"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- Increment seqno and try again -->
						<xsl:value-of select="te:get-filename($file-type, $prefix, $local-name, $namespace-uri, $seqno + 1)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="uri:toString(file:toURI($file))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- Creates a marker loc attribute to tie the generated code to the line number in the source CTL file -->
	<xsl:template name="loc">
		<xsl:variable name="filename">
			<xsl:call-template name="peek"/>
		</xsl:variable>
		<xsl:attribute name="loc" namespace="http://www.occamlab.com/te">
<!-- 			<xsl:value-of select="concat(saxon:line-number(.), ',', stack:peek($filename-stack))"/> -->	<!-- Example: loc=10,file:/file.ctl -->
			<xsl:value-of select="concat(saxon:line-number(.), ',', $filename)"/>	<!-- Example: loc=10,file:/file.ctl -->
		</xsl:attribute>
	</xsl:template>

	<!-- Contains a dummy instruction with a marker loc attribute -->
	<!-- Called by ctl instruction templates -->
	<xsl:template name="loc-element">
		<txsl:if test="false()">
			<xsl:call-template name="loc"/>
		</txsl:if>
	</xsl:template>

	<!-- Parses a qname string (ns:name) into name, local-name, prefix, and namespace-uri elements.
	     The @name attribute of the context node is used as the qname by default. -->
	<xsl:template name="parse-qname">
		<xsl:param name="qname" select="@name"/>
		<name>
			<xsl:value-of select="$qname"/>
		</name>
		<xsl:choose>
			<xsl:when test="contains($qname, ':')">
				<!-- Prefix qualified qname -->
				<xsl:variable name="prefix" select="substring-before($qname, ':')"/>
				<local-name>
					<xsl:value-of select="substring-after($qname, ':')"/>
				</local-name>
				<prefix>
					<xsl:value-of select="$prefix"/>
				</prefix>
				<namespace-uri>
					<xsl:value-of select="namespace::*[name()=$prefix]"/>
				</namespace-uri>
			</xsl:when>
			<xsl:otherwise>
				<!-- qname uses the default namespace -->
				<local-name>
					<xsl:value-of select="$qname"/>
				</local-name>
				<prefix/>
				<namespace-uri>
<!-- 					<xsl:value-of select="namespace::*[name()='']"/>  -->
				</namespace-uri>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="template-includes">
		<xsl:param name="qname"/>
	 	<xsl:variable name="includes">
		 	<xsl:for-each select=".//xsl:call-template">
				<xsl:variable name="inc-qname">
					<xsl:call-template name="parse-qname"/>
				</xsl:variable>
				<xsl:if test="not($inc-qname/namespace-uri = $qname/namespace-uri and $inc-qname/local-name = $qname/local-name)">
					<xsl:variable name="inc-filename" select="te:get-filename('template', $inc-qname/prefix, $inc-qname/local-name, $inc-qname/namespace-uri, '')"/>
					<txsl:include href="{$inc-filename}"/>
				</xsl:if>
		 	</xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="$includes/*">
			<xsl:variable name="href" select="@href"/>
			<xsl:if test="not(following-sibling::*/@href=$href)">
				<xsl:copy-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- Generates an XSLT stylesheet for the test or ctl function body.
	     The context node should be a ctl:test or ctl:function element -->
	<!-- Called by templates match="ctl:test" and match="ctl:function" -->
	<xsl:template name="make-sub-stylesheet">
		<xsl:param name="qname"/>
		<xsl:param name="filename"/>

		<xsl:result-document href="{$filename}">
			<txsl:transform
			 xmlns:te="http://www.occamlab.com/te"
			 xmlns:saxon="http://saxon.sf.net/"
			 xmlns:xs="http://www.w3.org/2001/XMLSchema"
			 xmlns:tec="java:com.occamlab.te.TECore"
			 version="{$xsl-ver}">
				<xsl:namespace name="saxon" select="'http://saxon.sf.net/'"/>
				<xsl:for-each select="namespace::*">
					<xsl:variable name="prefix" select="name()"/>
					<xsl:if test="not($namespaces/namespaces/namespace::*[name()=$prefix])">
						<xsl:copy/>
					</xsl:if>
				</xsl:for-each>
				<!-- xsl:copy-of select="namespace::*"/ -->	<!-- Copy namespaces from source CTL -->
				<xsl:call-template name="template-includes">
					<xsl:with-param name="qname" select="$qname"/>
				</xsl:call-template>
				<txsl:param name="te:core"/>
				<txsl:param name="te:params"/>
				<xsl:if test="ctl:param">
					<!-- Not used for tests that don't indicate they use CTL version 1, because in older versions of TEAM Engine documents were passed to tests as elements -->
					<xsl:if test="not(self::ctl:test) or number(@version) &gt;= 1 or ancestor::ctl:package[number(@version) &gt;= 1]">
						<!-- Generate a function for parsing parameters -->
						<txsl:function name="te:param-value" xmlns:xs="http://www.w3.org/2001/XMLSchema">
							<txsl:param name="local-name"/>
							<txsl:param name="namespace-uri"/>
							<txsl:for-each select="$te:params/params/param[@local-name=$local-name and @namespace-uri=$namespace-uri]">
								<txsl:choose>
									<txsl:when test="value/node() and starts-with(@type, 'xs:')">
										<!-- Values cast to saved type -->
										<txsl:copy-of select="saxon:evaluate(concat('$p1 cast as ', @type), value/node())"/>
									</txsl:when>
									<txsl:when test="starts-with(@type, 'xs:')">
										<!-- Empty string cast to saved type -->
										<txsl:copy-of select="saxon:evaluate(concat('&quot;&quot; cast as ', @type))"/>
									</txsl:when>
									<txsl:when test="value/@*">
										<!-- Attributes -->
										<txsl:copy-of select="value/@*"/>
									</txsl:when>
									<txsl:when test="starts-with(@type, 'document-node')">
										<!-- Document node -->
										<txsl:document>
											<txsl:copy-of select="value/node()"/>
										</txsl:document>
									</txsl:when>
									<txsl:otherwise>
										<!-- Element or text nodes -->
	 									<txsl:copy-of select="value/node()"/>
									</txsl:otherwise>
								</txsl:choose>
							</txsl:for-each>
							<xsl:if test="ctl:code/xsl:param">
								<txsl:if test="not($te:params/params/param[@local-name=$local-name and @namespace-uri=$namespace-uri])">
									<txsl:choose>
										<xsl:for-each select="ctl:code/xsl:param">
											<xsl:variable name="param-qname">
												<xsl:call-template name="parse-qname"/>
											</xsl:variable>
											<txsl:when test="$local-name='{$param-qname/local-name}' and $namespace-uri='{$param-qname/namespace-uri}'">
												<xsl:choose>
													<xsl:when test="@select">
														<txsl:copy-of>
															<xsl:apply-templates select="@select"/>
														</txsl:copy-of>
													</xsl:when>
													<xsl:otherwise>
														<xsl:apply-templates select="*"/>
													</xsl:otherwise>
												</xsl:choose>
											</txsl:when>
										</xsl:for-each>
									</txsl:choose>
								</txsl:if>
							</xsl:if>
						</txsl:function>
					</xsl:if>
				</xsl:if>

				<!-- Generate main template -->
				<txsl:template match="/" name="{@name}">
					<xsl:if test="$qname/prefix != ''">
						<!-- Make sure namespace attribute is in scope -->
						<xsl:namespace name="{$qname/prefix}" select="$qname/namespace-uri"/>
					</xsl:if>
					<xsl:call-template name="loc"/>

					<!-- Create a variable for each of the parameters -->
					<xsl:for-each select="ctl:param">
						<xsl:variable name="param-qname">
							<xsl:call-template name="parse-qname"/>
						</xsl:variable>
						<xsl:choose>
							<xsl:when test="not(parent::ctl:test) or number(parent::ctl:test/@version) &gt;= 1 or ancestor::ctl:package[number(@version) &gt;= 1]">
								<!-- Parameter style for all functions and for tests version 1 or higher.  Keeps the type of the parameter -->
								<txsl:param name="{@name}" select="te:param-value('{$param-qname/local-name}', '{$param-qname/namespace-uri}')"/>
							</xsl:when>
							<xsl:otherwise>
								<!-- Old parameter style for tests lower than version 1.  Parameter type is lost.  Documents are passed as their root element -->
								<txsl:variable name="te:param-{position()}">
									<txsl:copy-of select="$te:params/params/param[@local-name='{$param-qname/local-name}' and @namespace-uri='{$param-qname/namespace-uri}']/value"/>
								</txsl:variable>
								<txsl:variable name="{@name}" select="$te:param-{position()}/value/node()|$te:param-{position()}/value/@*"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>

					<txsl:for-each select="node()|@*">
						<!-- Handle all the code nodes except any xsl:param elements which we already handled -->
						<xsl:apply-templates select="ctl:code/node()[not(self::xsl:param)]"/>
					</txsl:for-each>
				</txsl:template>
			</txsl:transform>
		</xsl:result-document>
	</xsl:template>

	<!-- Generates code to make a variable containing an xml representation of parameters.
	     The context node is a ctl:call-test or ctl:call-function element -->
	<!-- Called by match="ctl:call-test" and match="ctl:call-function" templates -->
	<xsl:template name="make-params-var">
		<txsl:variable name="te:params">
			<params>
				<xsl:for-each select="ctl:with-param|xsl:with-param">
					<xsl:variable name="qname">
						<xsl:call-template name="parse-qname"/>
					</xsl:variable>
					<!-- Create a variable to hold parameter value -->
					<txsl:variable name="te:param-value">
						<xsl:choose>
							<xsl:when test="@select">
								<xsl:apply-templates select="@select"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates/>
							</xsl:otherwise>
						</xsl:choose>
					</txsl:variable>
					<!-- Create XML for the parameter -->
					<param local-name="{$qname/local-name}" namespace-uri="{$qname/namespace-uri}" prefix="{$qname/prefix}" type="{{te:get-type($te:param-value)}}">
						<xsl:choose>
							<!-- Copy label value to the XML -->
							<xsl:when test="@label-expr">
								<txsl:attribute name="label">
									<txsl:value-of select="{@label-expr}"/>
								</txsl:attribute>
							</xsl:when>
							<xsl:when test="@label">
								<txsl:attribute name="label">
									<xsl:value-of select="@label"/>
								</txsl:attribute>
							</xsl:when>
						</xsl:choose>
						<value>
							<txsl:copy-of select="$te:param-value"/>
						</value>
					</param>
				</xsl:for-each>
				<!-- Include context XML if applicable -->
<!--
				<xsl:if test="ctl:context">
					<context>
						<txsl:copy-of select="."/>
					</context>
				</xsl:if>
-->
			</params>
		</txsl:variable>
	</xsl:template>

	<!-- Generates a child java element for function and parser index entries -->
	<!-- Called by match="ctl:function" and match="ctl:parser" -->
	<xsl:template name="java-entry">
		<xsl:for-each select="ctl:java">
			<java>
				<xsl:copy-of select="@*"/>
				<xsl:for-each select="ctl:with-param">
					<with-param>
<!--
						<xsl:attribute name="name">
							<xsl:choose>
								<xsl:when test="@name">
									<xsl:value-of select="@name"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>param</xsl:text>
									<xsl:value-of select="position()"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
-->
						<xsl:choose>
							<xsl:when test="@select">
								<!-- This form should probably be considered deprecated -->
								<xsl:value-of select="saxon:evaluate(@select)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:copy-of select="text()|*"/>
							</xsl:otherwise>
						</xsl:choose>
					</with-param>
				</xsl:for-each>
			</java>
		</xsl:for-each>
	</xsl:template>


	<!-- Handle package object -->

	<xsl:template match="ctl:package">
		<xsl:apply-templates select="*"/>  <!-- "*" currently selects ctl:test, ctl:function, ctl:parser, ctl:suite -->
	</xsl:template>


	<!-- Handle includes -->

	<xsl:template match="ctl:include">
		<!-- matches the "main" template -->
		<xsl:apply-templates select="document(@href)" mode="include"/>
	</xsl:template>


	<!-- Handle package level objects -->

	<!-- Calls parse-qname -->
	<xsl:template match="ctl:suite">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<xsl:variable name="starting-test">
			<xsl:for-each select="ctl:starting-test">
				<xsl:call-template name="parse-qname">
					<xsl:with-param name="qname" select="."/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:variable>
		<suite prefix="{$qname/prefix}" namespace-uri="{$qname/namespace-uri}" local-name="{$qname/local-name}">
			<title>
				<xsl:value-of select="ctl:title"/>
			</title>
			<description>
				<xsl:value-of select="ctl:description"/>
			</description>
			<starting-test prefix="{$starting-test/prefix}" namespace-uri="{$starting-test/namespace-uri}" local-name="{$starting-test/local-name}"/>
			<xsl:copy-of select="ctl:form"/>
		</suite>
	</xsl:template>

	<!-- Calls parse-qname -->
	<xsl:template match="ctl:profile">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<profile prefix="{$qname/prefix}" namespace-uri="{$qname/namespace-uri}" local-name="{$qname/local-name}">
			<title>
				<xsl:value-of select="ctl:title"/>
			</title>
			<description>
				<xsl:value-of select="ctl:description"/>
			</description>
			<xsl:variable name="base">
				<xsl:for-each select="ctl:base">
					<xsl:call-template name="parse-qname">
						<xsl:with-param name="qname" select="."/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:variable>
			<base prefix="{$base/prefix}" namespace-uri="{$base/namespace-uri}" local-name="{$base/local-name}"/>
			<xsl:for-each select="ctl:exclude">
				<xsl:variable name="exclude" select="."/>
				<exclude>
					<xsl:for-each select="fn:tokenize(fn:substring-after(., '/'), '/')" xmlns:fn="http://www.w3.org/2005/xpath-functions">
						<xsl:variable name="token" select="."/>
						<xsl:variable name="test">
							<xsl:for-each select="$exclude">
								<xsl:call-template name="parse-qname">
									<xsl:with-param name="qname" select="$token"/>
								</xsl:call-template>
							</xsl:for-each>
						</xsl:variable>
						<test prefix="{$test/prefix}" namespace-uri="{$test/namespace-uri}" local-name="{$test/local-name}"/>
					</xsl:for-each>
				</exclude>
			</xsl:for-each>
			<xsl:variable name="starting-test">
				<xsl:for-each select="ctl:starting-test">
					<xsl:call-template name="parse-qname">
						<xsl:with-param name="qname" select="."/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:variable>
			<starting-test prefix="{$starting-test/prefix}" namespace-uri="{$starting-test/namespace-uri}" local-name="{$starting-test/local-name}"/>
			<xsl:copy-of select="ctl:form"/>
		</profile>
	</xsl:template>

	<!-- Calls get-filename, make-sub-stylesheet -->
	<xsl:template match="ctl:test">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<xsl:variable name="filename" select="te:get-filename('test', $qname/prefix, $qname/local-name, $qname/namespace-uri, '')"/>
		<test prefix="{$qname/prefix}" namespace-uri="{$qname/namespace-uri}" local-name="{$qname/local-name}" file="{$filename}">
			<xsl:attribute name="uses-context">
				<xsl:value-of select="boolean(ctl:context)"/>
			</xsl:attribute>
			<xsl:for-each select="ctl:param">
				<xsl:variable name="param-qname">
					<xsl:call-template name="parse-qname"/>
				</xsl:variable>
				<param prefix="{$param-qname/prefix}" namespace-uri="{$param-qname/namespace-uri}" local-name="{$param-qname/local-name}"/>
			</xsl:for-each>
			<assertion>
				<xsl:value-of select="ctl:assertion"/>
			</assertion>
		</test>
		<xsl:call-template name="make-sub-stylesheet">
			<xsl:with-param name="qname" select="$qname"/>
			<xsl:with-param name="filename" select="$filename"/>
		</xsl:call-template>
	</xsl:template>

	<!-- Calls get-filename, make-sub-stylesheet -->
	<xsl:template name="function" match="ctl:function">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>

		<function prefix="{$qname/prefix}" namespace-uri="{$qname/namespace-uri}" local-name="{$qname/local-name}">
			<xsl:choose>
				<xsl:when test="ctl:code">
					<xsl:variable name="filename" select="te:get-filename('fn', $qname/prefix, $qname/local-name, $qname/namespace-uri, '')"/>
					<xsl:call-template name="make-sub-stylesheet">
						<xsl:with-param name="qname" select="$qname"/>
						<xsl:with-param name="filename" select="$filename"/>
					</xsl:call-template>
					<xsl:attribute name="type">xsl</xsl:attribute>
					<xsl:attribute name="file">
						<xsl:value-of select="$filename"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="type">java</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="uses-context">
				<xsl:value-of select="boolean(ctl:context)"/>
			</xsl:attribute>
			<xsl:for-each select="ctl:param">
				<xsl:variable name="param-qname">
					<xsl:call-template name="parse-qname"/>
				</xsl:variable>
				<param prefix="{$param-qname/prefix}" namespace-uri="{$param-qname/namespace-uri}" local-name="{$param-qname/local-name}"/>
			</xsl:for-each>
			<xsl:for-each select="ctl:var-params">
				<var-params min="{@min}" max="{@max}"/>
			</xsl:for-each>
			<xsl:call-template name="java-entry"/>
		</function>
	</xsl:template>

	<!-- Calls parse-qname -->
 	<xsl:template match="ctl:parser">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<parser prefix="{$qname/prefix}" namespace-uri="{$qname/namespace-uri}" local-name="{$qname/local-name}">
			<xsl:call-template name="java-entry"/>
		</parser>
	</xsl:template>
	
	<!-- Calls template "function" -->
<!--
	<xsl:template match="xsl:template">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<xsl:variable name="function">
			<ctl:function name="{@name}">
				<xsl:if test="$qname/prefix != ''">
					<xsl:namespace name="{$qname/prefix}" select="$qname/namespace-uri"/>
				</xsl:if>
				<xsl:for-each select="xsl:param">
					<ctl:param name="{@name}"/>
				</xsl:for-each>
				<ctl:context/>
				<ctl:code>
					<xsl:copy-of select="*"/>
				</ctl:code>
			</ctl:function>
		</xsl:variable>
		<xsl:for-each select="$function/ctl:function">
			<xsl:call-template name="function"/>
		</xsl:for-each>
	</xsl:template>
-->
	<xsl:template match="xsl:template">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<xsl:variable name="filename" select="te:get-filename('template', $qname/prefix, $qname/local-name, $qname/namespace-uri, '')"/>
		<xsl:result-document href="{$filename}">
			<txsl:transform
			 version="{$xsl-ver}">
				<xsl:copy-of select="namespace::*"/>	<!-- Copy namespaces from source CTL -->
				<xsl:call-template name="template-includes">
					<xsl:with-param name="qname" select="$qname"/>
				</xsl:call-template>
			 	<xsl:copy-of select="."/>
			</txsl:transform>
		</xsl:result-document>
	</xsl:template>

	<!-- Handle CTL instructions -->

	<!-- Calls parse-qname, make-params-var -->
	<xsl:template match="ctl:call-test">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>

		<!-- TODO: Raise error unless this is inside a test -->

		<xsl:call-template name="make-params-var"/>

		<txsl:value-of select="tec:callTest($te:core, '{$qname/local-name}', '{$qname/namespace-uri}', $te:params, concat('{generate-id()}_', position()))"/> <!-- Last param is the log file directory name -->
	</xsl:template>

	<!-- Calls parse-qname, make-params-var -->
	<xsl:template name="call-function" match="ctl:call-function">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>

		<xsl:call-template name="make-params-var"/>

		<txsl:copy-of select="tec:callFunction($te:core, '{$qname/local-name}', '{$qname/namespace-uri}', $te:params)"/>
	</xsl:template>

	<!-- Calls template "function" -->
<!--
	<xsl:template match="xsl:call-template">
		<xsl:variable name="qname">
			<xsl:call-template name="parse-qname"/>
		</xsl:variable>
		<xsl:variable name="call-function">
			<ctl:call-function name="{@name}">
				<xsl:if test="$qname/prefix != ''">
					<xsl:namespace name="{$qname/prefix}" select="$qname/namespace-uri"/>
				</xsl:if>
				<xsl:for-each select="xsl:with-param">
					<ctl:with-param>
						<xsl:copy-of select="@name"/>
					    <ignore>
					    	<xsl:choose>
					    		<xsl:when test="@select">
					    			<txsl:copy-of select="{@select}"/>
					    		</xsl:when>
					    		<xsl:otherwise>
									<xsl:copy-of select="*"/>
								</xsl:otherwise>
							</xsl:choose>
						</ignore>
					</ctl:with-param>
				</xsl:for-each>
			</ctl:call-function>
		</xsl:variable>
		<xsl:for-each select="$call-function/ctl:call-function">
			<xsl:call-template name="call-function"/>
		</xsl:for-each>
	</xsl:template>
-->

	<xsl:template match="ctl:fail">
		<txsl:value-of select="tec:fail($te:core)"/>
	</xsl:template>

	<xsl:template match="ctl:warning">
		<txsl:value-of select="tec:warning($te:core)"/>
	</xsl:template>

	<xsl:template match="ctl:form">
		<!-- Expand any child CTL instructions into XSL instructions and store in generated variable -->
		<txsl:variable name="te:form-xhtml">
			<xsl:copy>
				<xsl:apply-templates select="@*"/>
				<xsl:apply-templates/>
			</xsl:copy>
		</txsl:variable>
		<!-- Generate form method call -->
		<txsl:copy-of select="tec:form($te:core, $te:form-xhtml, concat('{generate-id()}_', position()))"/>
	</xsl:template>

	<xsl:template name="request" match="ctl:request">
		<!-- Expand any child CTL instructions into XSL instructions and store in generated variable -->
		<txsl:variable name="te:request-xml">
			<xsl:copy>
				<xsl:apply-templates select="@*"/>
				<xsl:apply-templates/>
			</xsl:copy>
		</txsl:variable>
		<!-- Generate request method call -->
		<txsl:copy-of select="tec:request($te:core, $te:request-xml, concat('{generate-id()}_', position()))/node()"/>
	</xsl:template>
	
	<xsl:template match="ctl:parse">
		<xsl:choose>
			<xsl:when test="parent::ctl:form">
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<!-- Expand any child CTL instructions into XSL instructions and store in generated variable -->
				<txsl:variable name="te:parse-xml">
					<xsl:copy>
					<xsl:apply-templates select="@*"/>
						<xsl:apply-templates/>
					</xsl:copy>
				</txsl:variable>
				<!-- Generate parse method call -->
				<txsl:copy-of select="tec:parse($te:core, $te:parse-xml, '{$xsl-ver}')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="ctl:for-each">
		<!-- Generate a for-each instruction and call the setContextLabel method -->
		<txsl:for-each>
			<xsl:call-template name="loc"/>
			<xsl:apply-templates select="@select"/>
			<xsl:choose>
				<xsl:when test="@label-expr">
					<txsl:copy-of select="tec:setContextLabel($te:core, {@label-expr})"/>
				</xsl:when>
				<xsl:when test="@label">
					<txsl:copy-of select="tec:setContextLabel($te:core, '{@label}')"/>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates/>
		</txsl:for-each>
	</xsl:template>

	<xsl:template match="ctl:message">
		<txsl:variable name="te:message-var">
			<xsl:call-template name="loc"/>
			<xsl:choose>
				<xsl:when test="@select">
					<!-- Copy select attribute -->
					<xsl:copy-of select="@select"/>
				</xsl:when>
				<xsl:otherwise>
					<!-- Expand any child CTL instructions into XSL instructions -->
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</txsl:variable>
		<txsl:value-of select="tec:message($te:core, $te:message-var, concat('{generate-id()}_', position()))"/>
	</xsl:template>

	<!-- Currently undocumented and untested.  Serializes an XML value -->
	<xsl:template match="ctl:out">
		<xsl:choose>
			<xsl:when test="@select">
				<txsl:value-of select="tec:copy($te:core, {@select})">
					<xsl:call-template name="loc"/>
				</txsl:value-of>
			</xsl:when>
			<xsl:otherwise>
				<txsl:variable name="te:output">
					<xsl:call-template name="loc"/>
					<xsl:apply-templates/>
				</txsl:variable>
				<txsl:value-of select="tec:copy($te:core, $te:output)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template match="xsl:variable">
		<xsl:copy>
			<xsl:call-template name="loc"/>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
		<!-- Use variables after they are set to ensure they are evaluated in document order -->
		<txsl:if test="string-length(substring(${@name},1,0))=1">
			<xsl:if test="contains(@name, ':')">
				<xsl:variable name="prefix" select="substring-before(@name, ':')"/>
				<xsl:copy-of select="namespace::*[name()=$prefix]"/>
			</xsl:if>
			<xsl:text>x</xsl:text>
		</txsl:if>
	</xsl:template>
	
	<!-- Handle generic xsl instructions -->

	<!-- A loc attribute is added to XSL instructions -->
	<xsl:template match="xsl:*">
		<xsl:copy>
			<xsl:call-template name="loc"/>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>


	<!-- Handle other nodes and attributes -->

	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:copy-of select="."/>
	</xsl:template>


	<!-- Main templates -->

	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="$outdir=''">
				<xsl:message terminate="yes">Error: no outdir parameter</xsl:message>
			</xsl:when>
			<xsl:when test="not(file:isDirectory(file:new(string($outdir))))" xmlns:file="java:java.io.File">
				<xsl:message terminate="yes">Error: outdir parameter is not a valid directory</xsl:message>
			</xsl:when>
			<xsl:otherwise>
				<index>
					<xsl:call-template name="main"/>
				</index>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="main" match="/" mode="include">
		<!-- Push current filename onto stack.  Output 0 characters -->
<!--   		<xsl:value-of select="substring(stack:push($filename-stack, document-uri(.)), 1, 0)"/> -->
		<xsl:call-template name="push">
			<xsl:with-param name="filename" select="document-uri(.)"/>
		</xsl:call-template>
		<!-- Create an index entry for the current filename -->
<!-- 	<dependency file="{stack:peek($filename-stack)}"/> -->
		<dependency>
			<xsl:attribute name="file">
				<xsl:call-template name="peek"/>
			</xsl:attribute>
		</dependency>
		<!-- Process the file -->
		<xsl:apply-templates/>
		<!-- Pop the filename back off of the stack.  Output 0 characters -->
<!-- 		<xsl:value-of select="substring(stack:pop($filename-stack), 1, 0)"/> -->
		<xsl:call-template name="pop"/>
	</xsl:template>
</xsl:transform>
