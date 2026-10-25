package Server.Util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJsonParser {

    private final String text;
    private int pos;

    private SimpleJsonParser(String text) {
        this.text = text;
        this.pos = 0;
    }

    public static Object parse(String json) {
        SimpleJsonParser parser = new SimpleJsonParser(json);
        parser.skipWhitespace();
        Object result = parser.parseValue();
        parser.skipWhitespace();
        return result;
    }

    private Object parseValue() {
        skipWhitespace();
        char c = text.charAt(pos);

        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        return parseNumber();
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();

        if (peek() == '}') {
            pos++;
            return map;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();

            char c = text.charAt(pos);
            if (c == ',') {
                pos++;
            } else if (c == '}') {
                pos++;
                break;
            }
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();

        if (peek() == ']') {
            pos++;
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();

            char c = text.charAt(pos);
            if (c == ',') {
                pos++;
            } else if (c == ']') {
                pos++;
                break;
            }
        }
        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();

        while (true) {
            char c = text.charAt(pos++);
            if (c == '"') break;

            if (c == '\\') {
                char escaped = text.charAt(pos++);
                switch (escaped) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        String hex = text.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default: sb.append(escaped);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Double parseNumber() {
        int start = pos;
        while (pos < text.length() &&
                "-+.eE0123456789".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        return Double.parseDouble(text.substring(start, pos));
    }

    private Boolean parseBoolean() {
        if (text.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        pos += 5;
        return Boolean.FALSE;
    }

    private Object parseNull() {
        pos += 4;
        return null;
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return text.charAt(pos);
    }

    private void expect(char c) {
        skipWhitespace();
        if (text.charAt(pos) != c) {
            throw new RuntimeException(
                "Esperava '" + c + "' na posição " + pos +
                " mas encontrei '" + text.charAt(pos) + "'"
            );
        }
        pos++;
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        if (o == null) return new ArrayList<>();
        return (List<Object>) o;
    }

    public static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    public static int asInt(Object o) {
        return o == null ? 0 : ((Double) o).intValue();
    }
}