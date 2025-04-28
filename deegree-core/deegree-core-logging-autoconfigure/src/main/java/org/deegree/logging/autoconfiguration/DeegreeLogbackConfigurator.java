/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2025 by:
 - Department of Geography, University of Bonn -
 and
 - grit GmbH -
 and others

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.logging.autoconfiguration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileSize;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static org.deegree.logging.autoconfiguration.StreamUtils.filterKey;
import static org.deegree.logging.autoconfiguration.StreamUtils.filterKeyValue;
import static org.deegree.logging.autoconfiguration.StreamUtils.mapKey;
import static org.deegree.logging.autoconfiguration.StreamUtils.mapKeyValue;
import static org.deegree.logging.autoconfiguration.StreamUtils.mapValue;

/**
 * Configure Logback when no other {@link Configurator} was present
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
@ConfiguratorRank(ConfiguratorRank.FALLBACK)
public class DeegreeLogbackConfigurator extends ContextAwareBase implements Configurator {

	private static final String LOGGING_DEFAULTS_PROPERTIES_NAME = "/deegree-logging-defaults.properties";

	private static final String LOGGING_PREFIX = "logging.";

	private static final String LOGGING_GROUP_PREFIX = "logging.group.";

	private static final String LOGGING_LEVEL_PREFIX = "logging.level.";

	private static final String LOGGING_LEVEL_AUTOCONFIGURATION = "logging.level.org.deegree.logging.autoconfiguration";

	private static final Map<String, String> DEFAULT_GROUPS = Map.of( //
			"deegree-recommendations-error",
			"org.apache.catalina.startup.DigesterFactory,org.apache.catalina.util.LifecycleBase,org.eclipse.jetty.util.component.AbstractLifeCycle", //
			"deegree-recommendations-warn",
			"org.apache.coyote.http11.Http11NioProtocol,org.apache.tomcat.util.net.NioSelectorPool,org.hibernate.validator.internal.util.Version" //
	);

	private static final Map<String, Level> DEFAULT_LEVELS = Map.of( //
			"deegree-recommendations-error", Level.ERROR, //
			"deegree-recommendations-warn", Level.WARN, //
			"org.deegree", Level.INFO);

	private static final Map<String, String> DEFAULT_OPTIONS = Map.of(//
			"CONSOLE_LOG_PATTERN",
			"%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd'T'HH:mm:ss.SSSXXX}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:-} --- [%t] ${LOG_CORRELATION_PATTERN:-}%-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-}", //
			"CONSOLE_LOG_CHARSET", "${file.encoding:-UTF-8}", //
			"FILE_LOG_PATTERN",
			"%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd'T'HH:mm:ss.SSSXXX}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:-} --- [%t] ${LOG_CORRELATION_PATTERN:-}%-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-}", //
			"FILE_LOG_CHARSET", "${file.encoding:-UTF-8}", //
			"LOGBACK_ROLLINGPOLICY_CLEAN-HISTORY-ON-START", "false", //
			"LOGBACK_ROLLINGPOLICY_FILE-NAME-PATTERN", "${LOG_FILE:-deegree}.%d{yyyy-MM-dd}.%i.gz", //
			"LOGBACK_ROLLINGPOLICY_MAX-FILE-SIZE", "10MB", //
			"LOGBACK_ROLLINGPOLICY_MAX-HISTORY", "7", //
			"LOGBACK_ROLLINGPOLICY_TOTAL-SIZE-CAP", "0" //
	);

	private final Map<String, String> loggingGroups = new HashMap<>();

	private VariableSubstitutionsHelper config;

	private Level debug = Level.OFF;

	@Override
	public ExecutionStatus configure(LoggerContext loggerContext) {
		addInfo("Setting up " + getClass().getName() + " configuration.");
		config = new VariableSubstitutionsHelper(loggerContext, DEFAULT_OPTIONS);

		Map<String, Level> levels = parseSettingsAndCollectLevels();

		ConsoleAppender<ILoggingEvent> ca = createConsoleAppender();
		Appender<ILoggingEvent> fa = createFileAppender();

		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(ca);
		if (fa != null) {
			rootLogger.addAppender(fa);
		}
		rootLogger.setLevel(Level.WARN);

		// configure levels
		levels.forEach((name, level) -> loggerContext.getLogger(name).setLevel(level));
		if (isDebug()) {
			levels.forEach((name, level) -> addInfo(" --> " + name + " = " + level));
		}

		return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
	}

	private Map<String, Level> parseSettingsAndCollectLevels() {
		// from LOGGING_DEFAULTS_PROPERTIES_NAME
		Stream<Map.Entry<String, String>> defaultsStream = classpathConfiguration();

		// from environment variables
		Stream<Map.Entry<String, String>> envStream = System.getenv()
			.entrySet()
			.stream()
			.filter(filterKeyValue(Objects::nonNull, Objects::nonNull))
			.map(mapKey(key -> key.toLowerCase().replace('_', '.')));

		// from system properties
		Stream<Map.Entry<String, String>> propStream = System.getProperties()
			.entrySet()
			.stream()
			.filter(filterKeyValue(String.class::isInstance, String.class::isInstance))
			.map(mapKeyValue(String.class::cast, String.class::cast));

		Map<String, Level> levels = new HashMap<>();
		Stream.of(defaultsStream, envStream, propStream)
			.flatMap(s -> s)
			.filter(filterKey(key -> key.startsWith(LOGGING_PREFIX)))
			.peek(e -> configureLogging(e.getKey(), e.getValue()))
			.filter(filterKey(key -> key.startsWith(LOGGING_LEVEL_PREFIX)))
			.map(mapValue(Level::valueOf))
			.forEach(e -> levels.put(e.getKey().substring(LOGGING_LEVEL_PREFIX.length()), e.getValue()));

		// provide defaults if empty
		if (levels.isEmpty()) {
			this.addInfo("Configure default levels if empty");
			levels.putAll(DEFAULT_LEVELS);
		}

		expandGroups(levels);

		// determine file for rolling appender
		String logPath = config.subst("${FILE_PATH:-}");
		if (logPath == null || logPath.isBlank()) {
			logPath = "";
		}
		else if (!logPath.endsWith(File.separator)) {
			logPath += File.separator;
		}
		String logFile = config.subst("${FILE_NAME:-}");
		debug("if configured file logging will go to path: " + logPath + " file: " + logFile);
		if (logFile != null && !logFile.isBlank()) {
			config.addSubstitutionProperty("LOG_FILE", logPath + logFile);
		}

		return levels;
	}

	private void expandGroups(Map<String, Level> levels) {
		if (loggingGroups.isEmpty()) {
			loggingGroups.putAll(DEFAULT_GROUPS);
		}

		loggingGroups.forEach((groupName, classList) -> {
			Level groupLevel = levels.remove(groupName);
			if (groupLevel == null) {
				return;
			}
			Arrays.stream(classList.split(",+"))
				.filter(Objects::nonNull)
				.filter(s -> !s.isBlank())
				.forEach(className -> levels.putIfAbsent(className, groupLevel));
		});
	}

	private Stream<Map.Entry<String, String>> classpathConfiguration() {
		try (final InputStream stream = getClass().getResourceAsStream(LOGGING_DEFAULTS_PROPERTIES_NAME)) {
			if (stream == null) {
				return Stream.empty();
			}

			final Properties properties = new Properties();
			properties.load(stream);
			addInfo("Adding " + properties.size() + " levels from " + LOGGING_DEFAULTS_PROPERTIES_NAME);

			return properties.entrySet()
				.stream()
				.filter(filterKeyValue(String.class::isInstance, String.class::isInstance))
				.map(mapKeyValue(String.class::cast, String.class::cast));
		}
		catch (Exception ex) {
			addInfo("Failed to load " + LOGGING_DEFAULTS_PROPERTIES_NAME, ex);
			return Stream.empty();
		}
	}

	private void configureLogging(String key, String value) {
		if (key.startsWith(LOGGING_LEVEL_AUTOCONFIGURATION)) {
			debug = Level.toLevel(value, Level.OFF);
			if (isDebug()) {
				addWarn("Debugging for autoconfiguration requested.");
			}
			return;
		}
		else if (key.startsWith(LOGGING_LEVEL_PREFIX)) {
			// ignore logging.level.*
			return;
		}
		if (key.startsWith(LOGGING_GROUP_PREFIX)) {
			loggingGroups.put(key.substring(LOGGING_GROUP_PREFIX.length()), value);
		}
		else {
			String keyInEvnFormat = key.substring(LOGGING_PREFIX.length()).replace('.', '_').toUpperCase();
			config.addSubstitutionProperty(keyInEvnFormat, value);
		}
	}

	private Charset charsetOrDefault(String key, String value) {
		try {
			return Charset.forName(value);
		}
		catch (Exception ex) {
			addWarn("Charset for " + key + " with value '" + value + "' could not be parsed: " + ex.getMessage());
			return StandardCharsets.UTF_8;
		}
	}

	private ConsoleAppender<ILoggingEvent> createConsoleAppender() {
		Level threshold = Level.toLevel(config.subst("${CONSOLE_LOG_THRESHOLD:-}"), Level.TRACE);
		String pattern = config.subst("${CONSOLE_LOG_PATTERN:-}");
		Charset charset = charsetOrDefault("console", config.subst("${CONSOLE_LOG_CHARSET:-}"));
		debug("Creating console appender with pattern " + pattern + " and threshold " + threshold);

		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
		appender.addFilter(createThresholdFilter(threshold));

		Encoder<ILoggingEvent> encoder = createEncoder(pattern, charset);
		encoder.start();

		appender.setEncoder(encoder);
		appender.setContext(context);
		appender.start();
		return appender;
	}

	private Appender<ILoggingEvent> createFileAppender() {
		String logFile = config.subst("${LOG_FILE:-}");
		if (logFile == null || logFile.isBlank()) {
			return null;
		}

		Level threshold = Level.toLevel(config.subst("${FILE_LOG_THRESHOLD:-}"), Level.TRACE);
		String pattern = config.subst("${FILE_LOG_PATTERN:-${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz}");
		Charset charset = charsetOrDefault("file", config.subst("${FILE_LOG_CHARSET:-}"));
		debug("Creating file appender with pattern " + pattern + " and threshold " + threshold);

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(context);
		appender.addFilter(createThresholdFilter(threshold));
		appender.setFile(logFile);

		RollingPolicy rollingPolicy = createRollingPolicy();
		appender.setRollingPolicy(rollingPolicy);
		rollingPolicy.setParent(appender);
		rollingPolicy.start();

		Encoder<ILoggingEvent> encoder = createEncoder(pattern, charset);
		encoder.start();

		appender.setEncoder(encoder);
		appender.start();
		return appender;
	}

	private Encoder<ILoggingEvent> createEncoder(String pattern, Charset charset) {
		if (pattern != null && !pattern.isBlank()) {
			PatternLayoutEncoder encoder = new PatternLayoutEncoder();
			encoder.setCharset(charset);
			encoder.setPattern(pattern);

			encoder.setContext(context);
			return encoder;
		}
		else {
			LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
			encoder.setContext(context);

			TTLLLayout layout = new TTLLLayout();
			layout.setContext(context);
			layout.start();

			encoder.setLayout(layout);
			encoder.setContext(context);

			return encoder;
		}
	}

	private RollingPolicy createRollingPolicy() {
		boolean cleanOnStart = Boolean.parseBoolean(config.subst("${LOGBACK_ROLLINGPOLICY_CLEAN-HISTORY-ON-START}"));
		String pattern = config.subst("${LOGBACK_ROLLINGPOLICY_FILE-NAME-PATTERN}");
		FileSize maxFileSize = FileSize.valueOf(config.subst("${LOGBACK_ROLLINGPOLICY_MAX-FILE-SIZE}"));
		FileSize totalSizeCap = FileSize.valueOf(config.subst("${LOGBACK_ROLLINGPOLICY_TOTAL-SIZE-CAP}"));
		int maxHistory = Integer.parseInt(config.subst("${LOGBACK_ROLLINGPOLICY_MAX-HISTORY}"));

		SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
		rollingPolicy.setContext(context);

		rollingPolicy.setFileNamePattern(pattern);
		rollingPolicy.setCleanHistoryOnStart(cleanOnStart);
		rollingPolicy.setMaxFileSize(maxFileSize);
		rollingPolicy.setTotalSizeCap(totalSizeCap);
		rollingPolicy.setMaxHistory(maxHistory);

		return rollingPolicy;
	}

	private ThresholdFilter createThresholdFilter(Level lvl) {
		ThresholdFilter filter = new ThresholdFilter();
		filter.setLevel(lvl.toString());
		filter.start();
		return filter;
	}

	private boolean isDebug() {
		return !debug.isGreaterOrEqual(Level.INFO);
	}

	private void debug(String msg) {
		if (isDebug()) {
			addInfo(msg);
		}
	}

}
