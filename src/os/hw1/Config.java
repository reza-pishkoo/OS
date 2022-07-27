package os.hw1;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    public static final int WAIT_P1 = 1000;
    public static final int WAIT_P2 = 2000;
    public static final int WAIT_P3 = 1500;
    public static final long SAFA_MARGIN = 900;
    public static final String JAVA_PATH = getJavaPath();
    public static final String CLASS_PATH = getClassPath();

    private static String getJavaPath() {
        if (Files.isDirectory(Paths.get("./target"))) {
            return "java";
        }
        return "/home/sam/.jdks/corretto-1.8.0_322/bin/java";
    }
    private static String getClassPath() {
        if (Files.isDirectory(Paths.get("./target"))) {
            return "target/test-classes/:target/classes/";
        }
        return "out/test/OS-HW1/:out/production/OS-HW1/";
    }
}
