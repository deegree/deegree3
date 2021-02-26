package org.deegree.uncoupled.jaxbutil;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Option;

public abstract class AbstractScanner {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractScanner.class);

	protected static final Pattern PAT_VERSION = Pattern.compile("^(VERSION|V|)[0-9]([._-][0-9]+)*(|-SNAPSHOT)$",
			Pattern.CASE_INSENSITIVE);

	@Option(names = { "-b", "--base" }, description = "Base path (where to start scanning)")
	protected String path = "../../";

	@Option(names = { "--schemas" }, //
			description = "Location where to find schemas, default to http://schemas.deegree.org")
	protected String schemasUrl = "http://schemas.deegree.org";

	public static List<Path> getFilesMatching(Path basePath, String glob) throws IOException {
		if (!Files.isDirectory(basePath)) {
			return emptyList();
		}

		final PathMatcher mext = FileSystems.getDefault().getPathMatcher(glob);
		final List<Path> res = new LinkedList<Path>();

		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if (!attrs.isDirectory() && mext.matches(file.getFileName())) {
					LOG.trace("Lookup Soruce file {} for test", file);
					res.add(file);
				}

				return FileVisitResult.CONTINUE;
			}
		});

		return res;
	}

	public static List<Path> getFilesMatchingCompletePath(Path basePath, String glob, String... excludes)
			throws IOException {
		if (!Files.isDirectory(basePath)) {
			return emptyList();
		}
		FileSystem fs = FileSystems.getDefault();
		final PathMatcher mext = fs.getPathMatcher(glob);
		List<PathMatcher> mexcludes = Arrays.stream(excludes) //
				.map(fs::getPathMatcher) //
				.collect(Collectors.toList());
		final List<Path> res = new LinkedList<Path>();

		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				boolean exclude = false;
				for (PathMatcher mexclude : mexcludes) {
					if (mexclude.matches(file)) {
						exclude = true;
						break;
					}
				}

				if (!attrs.isDirectory() && !exclude && mext.matches(file)) {
					LOG.trace("Lookup Soruce file {} for test", file);
					res.add(file);
				}

				return FileVisitResult.CONTINUE;
			}
		});

		return res;
	}

	protected Path removeVersion(Path p) {
		Path res = null;
		for (int i = 0, len = p.getNameCount(); i < len; i++) {
			String seg = p.getName(i).toString();
			if (PAT_VERSION.matcher(seg).matches()) {
				continue;
			}
			if (res == null) {
				res = p.getName(i);
			} else {
				res = res.resolve(p.getName(i));
			}
		}
		return res;
	}
}
