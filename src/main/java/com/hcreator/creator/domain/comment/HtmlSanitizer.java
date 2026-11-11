package com.hcreator.creator.domain.comment;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 메모 에디터(Quill)가 만든 HTML에서 "허용한 서식"만 남기는 간단한 필터.
 * 허용: 문단/줄바꿈, 굵게, 기울임, 밑줄, 취소선, 글자 크기(작게/크게/아주 크게), 글자색, 배경색.
 * 그 밖의 태그(script, img, a ...)와 속성(onclick 등)은 모두 제거한다.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {}

    private static final Set<String> TAGS = Set.of("p", "br", "strong", "b", "em", "i", "u", "s", "strike", "span");
    private static final Set<String> CLASSES = Set.of("ql-size-small", "ql-size-large", "ql-size-huge");

    private static final Pattern TAG = Pattern.compile("^<(/?)([a-zA-Z][a-zA-Z0-9]*)([^>]*)>$", Pattern.DOTALL);
    private static final Pattern ATTR = Pattern.compile("([a-zA-Z-]+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern COLOR = Pattern.compile(
            "^(#[0-9a-fA-F]{3,8}|rgba?\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*(,\\s*(0|1|0?\\.\\d+)\\s*)?\\))$");

    public static String clean(String html) {
        if (html == null) return "";
        StringBuilder out = new StringBuilder(html.length());
        int i = 0;
        int n = html.length();
        while (i < n) {
            char c = html.charAt(i);
            if (c != '<') {
                out.append(c);
                i++;
                continue;
            }
            int end = html.indexOf('>', i);
            if (end < 0) {
                out.append("&lt;");
                i++;
                continue;
            }
            Matcher m = TAG.matcher(html.substring(i, end + 1));
            if (!m.matches()) {
                out.append("&lt;");
                i++;
                continue;
            }
            boolean closing = !m.group(1).isEmpty();
            String name = m.group(2).toLowerCase();
            if (TAGS.contains(name)) {
                if (closing) {
                    if (!name.equals("br")) out.append("</").append(name).append('>');
                } else if (!name.equals("p") && !name.equals("br")) {
                    // 굵게/기울임/밑줄/span 등: 에디터가 크기(class)와 색(style)을 같은 태그에 붙이기도 한다
                    out.append(openFormatTag(name, m.group(3)));
                } else {
                    out.append('<').append(name).append('>');
                }
            }
            i = end + 1;
        }
        return out.toString();
    }

    /** 태그를 모두 지운 순수 글자 (비어 있는지 검사용) */
    public static String plainText(String cleanedHtml) {
        return cleanedHtml.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }

    private static String openFormatTag(String name, String attrs) {
        StringBuilder cls = new StringBuilder();
        String style = "";
        Matcher a = ATTR.matcher(attrs);
        while (a.find()) {
            String key = a.group(1).toLowerCase();
            String value = a.group(2);
            if (key.equals("class")) {
                for (String token : value.trim().split("\\s+")) {
                    if (CLASSES.contains(token)) {
                        if (cls.length() > 0) cls.append(' ');
                        cls.append(token);
                    }
                }
            } else if (key.equals("style")) {
                style = safeStyle(value);
            }
        }
        StringBuilder sb = new StringBuilder("<").append(name);
        if (cls.length() > 0) sb.append(" class=\"").append(cls).append('"');
        if (!style.isEmpty()) sb.append(" style=\"").append(style).append('"');
        return sb.append('>').toString();
    }

    private static String safeStyle(String style) {
        StringBuilder sb = new StringBuilder();
        for (String decl : style.split(";")) {
            int idx = decl.indexOf(':');
            if (idx < 0) continue;
            String prop = decl.substring(0, idx).trim().toLowerCase();
            String value = decl.substring(idx + 1).trim();
            if ((prop.equals("color") || prop.equals("background-color")) && COLOR.matcher(value).matches()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(prop).append(": ").append(value).append(';');
            }
        }
        return sb.toString();
    }
}