import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;

public class CreatePackageInfoClasses {

	private static final Logger LOG = getLogger(CreatePackageInfoClasses.class);

	public static void main(String[] args) throws Exception {
		URL tplURL = CreatePackageInfoClasses.class.getResource("/PackageInfo.template");
		URL tplList = CreatePackageInfoClasses.class.getResource("/PackageInfo.list");
		String template = Files.readString(Paths.get(tplURL.toURI()));
		for (String line : Files.readAllLines(Paths.get(tplList.toURI()))) {
			if (line.trim().isEmpty() || line.trim().startsWith("#") || line.trim().endsWith("-")) {
				continue;
			}
			String[] parts = line.trim().split("[ =\t]+", 2);
			if (parts.length != 2 || parts[0] == null || parts[1] == null) {
				LOG.warn("Ignored Line: {}", line);
				continue;
			}
			String baseDir = parts[0].trim().substring(parts[0].indexOf("src\\main\\java") + 14);
			String pkg = baseDir.replace("\\", ".");
			String className = parts[1].trim();
			String srcMainDir = parts[0].trim().substring(0,parts[0].indexOf("src\\main\\") + 9);

			String newFile = template.replace("$PACKAGE$", pkg).replace("$CLASS$", className);

			Path np = Paths.get(parts[0], className + ".java");
			Path mi = Paths.get(srcMainDir, "resources/META-INF/services/org.deegree.moduleinfo.ModuleInfoProvider");

			LOG.info("A -> {}", np);
			LOG.info("A -> {}", mi);

			Files.createDirectories(np.getParent());
			Files.writeString(np, newFile, CREATE, TRUNCATE_EXISTING);

			Files.createDirectories(mi.getParent());
			Files.writeString(mi, pkg + "." + className, CREATE, TRUNCATE_EXISTING);

			//LOG.info("-> Class {} Package: {} - {}", parts[1], pkg, parts[0]);
			LOG.info("Done");
		}
	}

}
