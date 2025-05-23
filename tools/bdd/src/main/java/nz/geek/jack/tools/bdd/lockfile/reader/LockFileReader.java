package nz.geek.jack.tools.bdd.lockfile.reader;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import nz.geek.jack.tools.bdd.lockfile.model.Lockfile;

public class LockFileReader {

  private static final Gson GSON = new Gson();

  private final Path lockfilePath;

  public LockFileReader(Path lockfilePath) {
    this.lockfilePath = lockfilePath;
  }

  public Lockfile read() {
    if (!Files.exists(lockfilePath)) {
      throw new RuntimeException(String.format("lockfile %s does not exist", lockfilePath));
    }

    var fileContents = readFile();

    return GSON.fromJson(fileContents, Lockfile.class);
  }

  private String readFile() {
    try {
      return Files.readString(lockfilePath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read lockfile", e);
    }
  }
}
