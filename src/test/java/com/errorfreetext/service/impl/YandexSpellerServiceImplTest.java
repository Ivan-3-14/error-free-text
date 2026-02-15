package com.errorfreetext.service.impl;

import com.errorfreetext.config.YandexSpellerConfig;
import com.errorfreetext.dto.YandexSpellerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("YandexSpellerServiceImpl Unit Tests")
class YandexSpellerServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private YandexSpellerConfig spellerConfig;

    @InjectMocks
    private YandexSpellerServiceImpl yandexSpellerService;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestEntityCaptor;

    private final String API_URL = "https://speller.yandex.net/services/spellservice.json/checkTexts";
    private final String TEST_TEXT = "Helo world";
    private final String LANGUAGE = "en";

    @BeforeEach
    void setUp() {
        lenient().when(spellerConfig.getApiUrl()).thenReturn(API_URL);
    }

    @Nested
    @DisplayName("correctText() method tests")
    class CorrectTextTests {

        @Test
        @DisplayName("Should return corrected text when API returns corrections")
        void shouldReturnCorrectedText_WhenApiReturnsCorrections() {
            List<String> options = List.of();
            YandexSpellerResponse correction1 = createCorrection("Helo", List.of("Hello"), 0, 4);
            YandexSpellerResponse correction2 = createCorrection("yuo", List.of("you"), 20, 3);

            List<List<YandexSpellerResponse>> mockResponse = List.of(List.of(correction1, correction2));

            when(restTemplate.exchange(
                    eq(API_URL),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(mockResponse));

            String result = yandexSpellerService.correctText(TEST_TEXT, LANGUAGE, options);

            assertThat(result).isEqualTo("Hello world");
            verify(restTemplate).exchange(
                    eq(API_URL),
                    eq(HttpMethod.POST),
                    requestEntityCaptor.capture(),
                    any(ParameterizedTypeReference.class)
            );

            HttpEntity<MultiValueMap<String, String>> capturedRequest = requestEntityCaptor.getValue();
            assertThat(capturedRequest.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = capturedRequest.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getFirst("text")).isEqualTo(TEST_TEXT);
            assertThat(body.getFirst("lang")).isEqualTo(LANGUAGE);
            assertThat(body.getFirst("options")).isEqualTo("0");
        }

        @Test
        @DisplayName("Should return original text when API returns empty response")
        void shouldReturnOriginalTextWhenApiReturnsEmptyResponse() {

            List<String> options = List.of();
            List<List<YandexSpellerResponse>> mockResponse = List.of(List.of());

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(mockResponse));

            String result = yandexSpellerService.correctText(TEST_TEXT, LANGUAGE, options);

            assertThat(result).isEqualTo(TEST_TEXT);
        }

        @Test
        @DisplayName("Should return original text when API returns null response")
        void shouldReturnOriginalTextWhenApiReturnsNullResponse() {
            List<String> options = List.of();

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(null));

            String result = yandexSpellerService.correctText(TEST_TEXT, LANGUAGE, options);

            assertThat(result).isEqualTo(TEST_TEXT);
        }

        @Test
        @DisplayName("Should throw RuntimeException when API call fails")
        void shouldThrowRuntimeExceptionWhenApiCallFails() {
            List<String> options = List.of();
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Connection timeout"));

            assertThatThrownBy(() -> yandexSpellerService.correctText(TEST_TEXT, LANGUAGE, options))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to correct text")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should pass options as bitmask correctly")
        void shouldPassOptionsAsBitmaskCorrectly() {
            List<String> options = List.of("IGNORE_DIGITS", "IGNORE_URLS");
            List<List<YandexSpellerResponse>> mockResponse = List.of(List.of());

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(mockResponse));

            yandexSpellerService.correctText(TEST_TEXT, LANGUAGE, options);

            verify(restTemplate).exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    requestEntityCaptor.capture(),
                    any(ParameterizedTypeReference.class)
            );

            MultiValueMap<String, String> body = requestEntityCaptor.getValue().getBody();
            assertThat(body).isNotNull();
            assertThat(body.getFirst("options")).isEqualTo("6"); // 2 + 4 = 6
        }

        @ParameterizedTest(name = "Language {0} should be converted to {1}")
        @MethodSource("provideLanguagesForConversion")
        @DisplayName("Should convert language to lowercase correctly")
        void shouldConvertLanguageToLowercase(String inputLang, String expectedLang) {
            List<String> options = List.of();
            List<List<YandexSpellerResponse>> mockResponse = List.of(List.of());

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(ResponseEntity.ok(mockResponse));

            yandexSpellerService.correctText(TEST_TEXT, inputLang, options);

            verify(restTemplate).exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    requestEntityCaptor.capture(),
                    any(ParameterizedTypeReference.class)
            );

            MultiValueMap<String, String> body = requestEntityCaptor.getValue().getBody();
            assertThat(body).isNotNull();
            assertThat(body.getFirst("lang")).isEqualTo(expectedLang);
        }

        private static Stream<Arguments> provideLanguagesForConversion() {
            return Stream.of(
                    Arguments.of("EN", "en"),
                    Arguments.of("en", "en"),
                    Arguments.of("Ru", "ru"),
                    Arguments.of("RU", "ru"),
                    Arguments.of("Fr", "fr")
            );
        }
    }

    private YandexSpellerResponse createCorrection(String word, List<String> suggestions, int pos, int len) {
        YandexSpellerResponse response = new YandexSpellerResponse();
        response.setWord(word);
        response.setS(suggestions);
        response.setPos(pos);
        response.setLen(len);
        return response;
    }
}