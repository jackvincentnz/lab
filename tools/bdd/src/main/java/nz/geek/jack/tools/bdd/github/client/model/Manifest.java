package nz.geek.jack.tools.bdd.github.client.model;

import java.util.Map;

public record Manifest(String name, File file, Map<String, ResolvedPackage> resolved) {}
