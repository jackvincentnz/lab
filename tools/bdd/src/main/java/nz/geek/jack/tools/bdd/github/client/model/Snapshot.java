package nz.geek.jack.tools.bdd.github.client.model;

import java.util.Map;

public record Snapshot(
    int version,
    Job job,
    String sha,
    String ref,
    Detector detector,
    Map<String, Manifest> manifests,
    String scanned) {}
