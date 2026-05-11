package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String userMessage;

    private String aiResponse;

    private String toolName;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    private LocalDateTime createTime;
}
