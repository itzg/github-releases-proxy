package me.itzg.githubreleasesproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
public class ProxyController {

    private final AppProperties appProperties;
    private final UriComponentsBuilder redirectUriBuilder;

    public ProxyController(AppProperties appProperties) {
        this.appProperties = appProperties;

        redirectUriBuilder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host("github.com")
            .path("/{user}/{app}/releases/download/{version}/{file}");
    }

    @GetMapping("/{user}/{app}/releases/download/{version}/{file}")
    public ResponseEntity<StreamingResponseBody> getFile(@PathVariable String user, @PathVariable String app,
        @PathVariable String version, @PathVariable String file) {

        final String basePath = resolveAppBasePath(app);
        if (basePath == null) {
            log.debug("No app mapping for {}", app);
            return createRedirect(user, app, version, file);
        }

        final Path basePathDir = Paths.get(basePath);
        if (!Files.isDirectory(basePathDir)) {
            log.debug("Mapped base dir {} for app {} does not exist", basePath, app);
            return createRedirect(user, app, version, file);
        }

        final Path fileOnDisk = pickFileFromDisk(
            basePathDir,
            app,
            file
        );

        if (fileOnDisk == null) {
            log.debug("Could not find file {} within base dir {} for app {} does not exist", file, basePath, app);
            return createRedirect(user, app, version, file);
        }

        log.info("Providing {}", fileOnDisk);

        return ResponseEntity.ok(outputStream -> Files.copy(fileOnDisk, outputStream));
    }

    private String resolveAppBasePath(String app) {
        final String configured = appProperties.appPaths() != null ? appProperties.appPaths().get(app) : null;
        if (configured != null) {
            return configured;
        }

        if (app.contains("/") || app.contains("\\")) {
            return null;
        }

        final Path resolved = Paths.get(".").resolve(app);
        if (Files.isDirectory(resolved)) {
            return resolved.toString();
        }

        return null;
    }

    private Path pickFileFromDisk(Path basePathDir, String app, String file) {
        // gradle application...
        final Path fileInDistributions = basePathDir.resolve("build").resolve("distributions").resolve(file);
        if (Files.exists(fileInDistributions)) {
            return fileInDistributions;
        }

        // goreleaser...
        final Path distDir = basePathDir.resolve("dist");
        // ...archive
        final Path archiveInDist = distDir.resolve(file);
        if (Files.exists(archiveInDist)) {
            return archiveInDist;
        }
        // ..binary
        for (String dir : List.of(file, file.replace("amd64", "amd64_v1"))) {
            final Path binaryInOsArchDir = distDir.resolve(dir).resolve(app);
            if (Files.exists(binaryInOsArchDir)) {
                return binaryInOsArchDir;
            }
        }

        return null;
    }

    private ResponseEntity<StreamingResponseBody> createRedirect(String user, String app, String version, String file) {
        final URI location = redirectUriBuilder.build(user, app, version, file);
        log.debug("Redirecting to {}", location);
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, location.toString())
            .build();
    }
}
