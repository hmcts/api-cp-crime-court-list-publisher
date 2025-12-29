package uk.gov.hmcts.cp.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenAPIConfigurationLoader {

    // Match your Gradle include("*.openapi.yml")
    private static final String SPEC_PATH = "openapi/court-list-publisher.openapi.yml";


    public static OpenAPI loadOpenApiFromClasspath(final String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Provided path is null or blank");
        }

        try (InputStream inputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {

            if (inputStream == null) {
                log.error("OpenAPI specification file not found on classpath: {}", path);
                throw new IllegalArgumentException("Missing resource: " + path);
            }

            final String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            if (yaml.isBlank()) {
                throw new IllegalArgumentException("OpenAPI specification is empty: " + path);
            }

            final SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
            if (result == null || result.getOpenAPI() == null) {
                final String messages = (result != null && result.getMessages() != null)
                        ? String.join("; ", result.getMessages())
                        : "Unknown parser error";
                log.error("Failed to parse OpenAPI spec at {}: {}", path, messages);
                throw new IllegalStateException("Failed to parse OpenAPI spec at " + path + ": " + messages);
            }

            return result.getOpenAPI();

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load OpenAPI spec from classpath: " + path, e);
        }
    }

    /**
     * Convenience accessor using the default SPEC_PATH.
     */
    public OpenAPI openAPI() {
        return loadOpenApiFromClasspath(SPEC_PATH);
    }
}
