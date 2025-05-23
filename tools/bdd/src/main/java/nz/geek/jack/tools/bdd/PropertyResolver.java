package nz.geek.jack.tools.bdd;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PropertyResolver {

  private static final String REPO_ROOT_PATH_ENV = "REPO_ROOT_PATH";
  private static final String LOCKFILE_PATH_ENV = "LOCKFILE_PATH";
  private static final String SNAPSHOT_VERSION_ENV = "SNAPSHOT_VERSION";
  private static final String RUN_ID_ENV = "RUN_ID";
  private static final String CORRELATOR_ID_ENV = "CORRELATOR_ID";
  private static final String SHA_ENV = "SHA";
  private static final String REF_ENV = "REF";
  private static final String ACCESS_TOKEN_ENV = "ACCESS_TOKEN";

  public static String REPO_ROOT_PATH;
  public static String LOCKFILE_PATH;
  public static int SNAPSHOT_VERSION;
  public static String RUN_ID;
  public static String CORRELATOR_ID;
  public static String SHA;
  public static String REF;
  public static String ACCESS_TOKEN;

  static {
    validateVars();

    REPO_ROOT_PATH = System.getenv(REPO_ROOT_PATH_ENV);
    LOCKFILE_PATH = System.getenv(LOCKFILE_PATH_ENV);
    SNAPSHOT_VERSION = Integer.parseInt(System.getenv(SNAPSHOT_VERSION_ENV));
    RUN_ID = System.getenv(RUN_ID_ENV);
    CORRELATOR_ID = System.getenv(CORRELATOR_ID_ENV);
    SHA = System.getenv(SHA_ENV);
    REF = System.getenv(REF_ENV);
    ACCESS_TOKEN = System.getenv(ACCESS_TOKEN_ENV);
  }

  private static void validateVars() {
    List.of(
            REPO_ROOT_PATH_ENV,
            LOCKFILE_PATH_ENV,
            SNAPSHOT_VERSION_ENV,
            RUN_ID_ENV,
            CORRELATOR_ID_ENV,
            SHA_ENV,
            REF_ENV,
            ACCESS_TOKEN_ENV)
        .forEach(PropertyResolver::validateVariable);
  }

  private static void validateVariable(String name) {
    if (StringUtils.isBlank(System.getenv(name))) {
      missingEnvironmentVariable(name);
    }
  }

  private static void missingEnvironmentVariable(String name) {
    throw new RuntimeException(String.format("Missing required environment variable: %s", name));
  }
}
