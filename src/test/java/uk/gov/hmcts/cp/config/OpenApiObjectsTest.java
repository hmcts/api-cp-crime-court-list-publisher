package uk.gov.hmcts.cp.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.cp.openapi.api.CourtListPublishApi;
import uk.gov.hmcts.cp.openapi.model.CourtListPublishRequest;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenApiObjectsTest {

    private static final String PUBLISH_POST = "application/vnd.courtlistpublishing-service.publish.post+json";
    private static final String CROWN_PUBLISH_POST = "application/vnd.courtlistpublishing-service.crown.publish.post+json";

    @Test
    void generated_error_response_should_have_expected_fields() {
        assertThat(ErrorResponse.class).hasDeclaredMethods("error", "message", "details", "traceId");
    }

    @Test
    void publish_operation_should_accept_and_return_the_magistrates_and_crown_media_types() throws Exception {
        RequestMapping mapping = CourtListPublishApi.class
                .getMethod("publishCourtList", CourtListPublishRequest.class)
                .getAnnotation(RequestMapping.class);

        assertThat(mapping.value()).containsExactly(CourtListPublishApi.PATH_PUBLISH_COURT_LIST);
        assertThat(mapping.consumes()).containsExactlyInAnyOrder(PUBLISH_POST, CROWN_PUBLISH_POST);
        assertThat(mapping.produces()).contains(PUBLISH_POST, CROWN_PUBLISH_POST);
    }

    @Test
    void crown_lists_should_have_no_separate_publish_path() {
        assertThat(Arrays.stream(CourtListPublishApi.class.getFields()).map(Field::getName))
                .noneMatch(name -> name.contains("CROWN"));
        assertThat(CourtListPublishApi.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("publishCrownCourtList"));
    }
}
