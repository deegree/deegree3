import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class IterateSourceTree {

	private static final Logger LOG = getLogger(IterateSourceTree.class);

	public static boolean contains(Path path, Path needle) {
		Path p = path;
		while (p != null) {
			if (p.endsWith(needle)) {
				return true;
			}
			p = p.getParent();
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		Path base = Paths.get("");
		List<String> ignored = List.of("target", "uncoupled");
		Path dirSrcMain = Path.of("src", "main", "java");
		LOG.warn("DIR {}", base.toAbsolutePath());

		Files.walkFileTree(base, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				String dirName = dir.getFileName().toString();
				if (dirName.startsWith(".") || ignored.contains(dirName)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				if (contains(dir, dirSrcMain)) {
					long cnt = -1;
					try (Stream<Path> entries = Files.walk(dir, 1)) {
						cnt = entries.filter(pf -> Files.isRegularFile(pf, LinkOption.NOFOLLOW_LINKS)) //
							// .peek( pf -> LOG.info("- (file): {}", pf) ) //
							.count();
					}

					LOG.info("- {} = {}", dir, cnt);
					// if ( cnt > 0) {
					// return FileVisitResult.TERMINATE;
					// }
				}
				return FileVisitResult.CONTINUE;

			}
		});
		// .filter( Files::isDirectory )
		// .forEach( p -> LOG.info("- {}", p) );
	}

}
