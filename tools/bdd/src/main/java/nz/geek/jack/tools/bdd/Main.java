package nz.geek.jack.tools.bdd;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURLBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import nz.geek.jack.tools.bdd.github.client.http.SnapshotClient;
import nz.geek.jack.tools.bdd.github.client.model.Detector;
import nz.geek.jack.tools.bdd.github.client.model.File;
import nz.geek.jack.tools.bdd.github.client.model.Job;
import nz.geek.jack.tools.bdd.github.client.model.Manifest;
import nz.geek.jack.tools.bdd.github.client.model.ResolvedPackage;
import nz.geek.jack.tools.bdd.github.client.model.Snapshot;
import nz.geek.jack.tools.bdd.lockfile.model.Artifact;
import nz.geek.jack.tools.bdd.lockfile.reader.LockFileReader;

public class Main {

  private static final SnapshotClient CLIENT = new SnapshotClient(PropertyResolver.ACCESS_TOKEN);

  public static void main(String[] args) throws IOException, MalformedPackageURLException {
    var fullPath =
        Paths.get(PropertyResolver.REPO_ROOT_PATH).resolve(PropertyResolver.LOCKFILE_PATH);
    var lockfile = new LockFileReader(fullPath).read();

    var resolvedPackages = mapToResolvedPackages(lockfile.artifacts());
    var snapshot = buildSnapshot(resolvedPackages);

    CLIENT.postSnapshot(snapshot);
  }

  private static Snapshot buildSnapshot(Map<String, ResolvedPackage> packages) {
    return new Snapshot(
        PropertyResolver.SNAPSHOT_VERSION,
        new Job(PropertyResolver.RUN_ID, PropertyResolver.CORRELATOR_ID),
        PropertyResolver.SHA,
        PropertyResolver.REF,
        new Detector(
            "bazel-dependency-detector",
            "0.0.1",
            "https://github.com/jackvincentnz/lab/tree/main/tools/bdd"),
        Map.of(
            PropertyResolver.LOCKFILE_PATH,
            new Manifest(
                PropertyResolver.LOCKFILE_PATH,
                new File(PropertyResolver.LOCKFILE_PATH),
                packages)),
        Instant.now().toString());
  }

  private static Map<String, ResolvedPackage> mapToResolvedPackages(
      Map<String, Artifact> artifacts) {
    return artifacts.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Main::mapToResolvedPackage));
  }

  private static ResolvedPackage mapToResolvedPackage(Map.Entry<String, Artifact> entry) {
    var nameParts = entry.getKey().split(":");

    try {
      var packageUrl =
          PackageURLBuilder.aPackageURL()
              .withType("maven")
              .withNamespace(nameParts[0])
              .withName(nameParts[1])
              .withVersion(entry.getValue().version())
              .build();
      return new ResolvedPackage(packageUrl.canonicalize());
    } catch (MalformedPackageURLException ex) {
      throw new RuntimeException("Failed to form package url", ex);
    }
  }
}
