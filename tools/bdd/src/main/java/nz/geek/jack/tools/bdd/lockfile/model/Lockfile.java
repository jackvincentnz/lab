package nz.geek.jack.tools.bdd.lockfile.model;

import java.util.Map;

public record Lockfile(Map<String, Artifact> artifacts) {}
