package com.tandiantong.framework.common.boundary;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FrameworkBoundaryTest {

    private static final List<String> BUSINESS_PACKAGE_PREFIXES = List.of(
            "com.tandiantong.adminapi",
            "com.tandiantong.analytics",
            "com.tandiantong.bootstrap",
            "com.tandiantong.catalog",
            "com.tandiantong.integration",
            "com.tandiantong.miniapi",
            "com.tandiantong.order",
            "com.tandiantong.reservation",
            "com.tandiantong.security",
            "com.tandiantong.verification"
    );

    @Test
    void frameworkShouldNotDependOnBusinessPackages() throws IOException {
        Path frameworkRoot = Path.of("..").toAbsolutePath().normalize();
        try (Stream<Path> paths = Files.walk(frameworkRoot)) {
            List<Path> violatingFiles = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("src\\main\\java")
                            || path.toString().contains("src/main/java"))
                    .filter(this::containsBusinessPackage)
                    .toList();

            assertThat(violatingFiles).isEmpty();
        }
    }

    private boolean containsBusinessPackage(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return BUSINESS_PACKAGE_PREFIXES.stream().anyMatch(content::contains);
        } catch (IOException ex) {
            throw new IllegalStateException("读取框架源码失败：" + path, ex);
        }
    }
}
