package com.sky.ToolAi;

import java.util.regex.Pattern;

/**
 * AI 输入清洗器，防御 Prompt 注入
 */
public class InputSanitizer {

    // 常见注入关键词
    private static final Pattern INJECTION_PATTERNS = Pattern.compile(
        "(?i)" +
        "(忽略|ignore|disregard|forget|无视).{0,20}(之前|以上|上面|previous|above|all)" +
        "|(你现在是|你扮演|从现在起|now you are|you are now|act as|pretend)" +
        "|(新指令|新角色|new instructions|new role|system prompt)" +
        "|(忽略.*指令|ignore.*instructions)" +
        "|(告诉我.*密码|tell me.*password|show me.*secret)" +
        "|(输出.*system|print.*system|reveal.*prompt)" +
        "|(DAN|jailbreak|越狱模式)"
    );

    public static String sanitize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        if (input.length() > 500) {
            return "消息过长，请精简后重试";
        }
        if (INJECTION_PATTERNS.matcher(input).find()) {
            return null;
        }
        return input.trim();
    }
}
