package com.murqin.nocroptrample;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesValidationTest {

    private static final List<String> LANGUAGES = List.of(
            "en_us", "tr_tr", "de_de", "es_es", "fr_fr",
            "pt_br", "ru_ru", "zh_cn", "ja_jp", "ko_kr");

    private static final List<String> REQUIRED_GAMERULE_KEYS = List.of(
            "gamerule.nocroptrample.prevent_empty_trampling",
            "gamerule.nocroptrample.prevent_empty_trampling.description",
            "gamerule.nocroptrample.prevent_player_trampling",
            "gamerule.nocroptrample.prevent_player_trampling.description",
            "gamerule.nocroptrample.prevent_mob_trampling",
            "gamerule.nocroptrample.prevent_mob_trampling.description",
            "gamerule.nocroptrample.min_feather_falling_level",
            "gamerule.nocroptrample.min_feather_falling_level.description",
            "gamerule.nocroptrample.max_safe_fall_distance",
            "gamerule.nocroptrample.max_safe_fall_distance.description");

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%s");

    @Test
    void allLanguageFilesShareExactKeysAndPlaceholderCounts() throws IOException {
        Map<String, Object> english = loadJsonObject("assets/nocroptrample/lang/en_us.json");
        Set<String> expectedKeys = english.keySet();
        assertFalse(expectedKeys.isEmpty(), "Expected at least one translation key");
        for (String requiredKey : REQUIRED_GAMERULE_KEYS) {
            assertTrue(expectedKeys.contains(requiredKey), "Missing game rule key in en_us.json: " + requiredKey);
        }

        for (String lang : LANGUAGES) {
            Map<String, Object> localized = loadJsonObject("assets/nocroptrample/lang/" + lang + ".json");
            assertEquals(expectedKeys, localized.keySet(), "Key mismatch in language file: " + lang);

            for (String key : expectedKeys) {
                String enValue = assertInstanceOf(String.class, english.get(key));
                String locValue = assertInstanceOf(String.class, localized.get(key),
                        "Non-string translation for key '" + key + "' in " + lang);
                assertFalse(locValue.isBlank(), "Blank translation for key '" + key + "' in " + lang);
                assertEquals(
                        countPlaceholders(enValue),
                        countPlaceholders(locValue),
                        "Placeholder count mismatch for key '" + key + "' in " + lang);
            }
        }
    }

    @Test
    void datapackTagFilesAreValidJsonWithExpectedDefaults() throws IOException {
        Map<String, Object> protectedCrops = loadJsonObject("data/nocroptrample/tags/block/protected_crops.json");
        List<?> cropValues = assertInstanceOf(List.class, protectedCrops.get("values"));
        assertTrue(cropValues.contains("minecraft:torchflower"),
                "protected_crops tag should include minecraft:torchflower by default");

        Map<String, Object> exempt = loadJsonObject("data/nocroptrample/tags/entity_type/trample_exempt_entities.json");
        assertInstanceOf(List.class, exempt.get("values"));

        Map<String, Object> alwaysTrample = loadJsonObject("data/nocroptrample/tags/entity_type/always_trample_entities.json");
        assertInstanceOf(List.class, alwaysTrample.get("values"));
    }

    private static Map<String, Object> loadJsonObject(String path) throws IOException {
        try (InputStream stream = ResourcesValidationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource on classpath: " + path);
            String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Object parsed = new SimpleJsonParser(raw).parseDocument();
            @SuppressWarnings("unchecked")
            Map<String, Object> object = assertInstanceOf(Map.class, parsed);
            return object;
        }
    }

    private static int countPlaceholders(String value) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Minimal dependency-free JSON parser so tests compile on both Fabric and NeoForge
     * without requiring Gson on the test compile classpath.
     */
    private static final class SimpleJsonParser {
        private final String input;
        private int pos;

        private SimpleJsonParser(String input) {
            this.input = input;
        }

        private Object parseDocument() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (pos != input.length()) {
                throw new IllegalStateException("Unexpected trailing JSON content at index " + pos);
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= input.length()) {
                throw new IllegalStateException("Unexpected end of JSON");
            }
            char c = input.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> expectLiteral("true", Boolean.TRUE);
                case 'f' -> expectLiteral("false", Boolean.FALSE);
                case 'n' -> expectLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield parseNumber();
                    }
                    throw new IllegalStateException("Unexpected char '" + c + "' at index " + pos);
                }
            };
        }

        private Map<String, Object> parseObject() {
            expectChar('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expectChar(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek(',')) {
                    pos++;
                } else {
                    expectChar('}');
                    return map;
                }
            }
        }

        private List<Object> parseArray() {
            expectChar('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                pos++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek(',')) {
                    pos++;
                } else {
                    expectChar(']');
                    return list;
                }
            }
        }

        private String parseString() {
            expectChar('"');
            StringBuilder sb = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= input.length()) {
                        throw new IllegalStateException("Unterminated escape sequence");
                    }
                    char esc = input.charAt(pos++);
                    switch (esc) {
                        case '"', '\\', '/' -> sb.append(esc);
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            String hex = input.substring(pos, pos + 4);
                            pos += 4;
                            sb.append((char) Integer.parseInt(hex, 16));
                        }
                        default -> throw new IllegalStateException("Invalid escape \\" + esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalStateException("Unterminated string literal");
        }

        private Object expectLiteral(String literal, Object value) {
            if (!input.startsWith(literal, pos)) {
                throw new IllegalStateException("Expected '" + literal + "' at index " + pos);
            }
            pos += literal.length();
            return value;
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (Character.isDigit(c) || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    pos++;
                } else {
                    break;
                }
            }
            return Double.parseDouble(input.substring(start, pos));
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private boolean peek(char expected) {
            return pos < input.length() && input.charAt(pos) == expected;
        }

        private void expectChar(char expected) {
            if (pos >= input.length() || input.charAt(pos) != expected) {
                throw new IllegalStateException("Expected '" + expected + "' at index " + pos);
            }
            pos++;
        }
    }
}
