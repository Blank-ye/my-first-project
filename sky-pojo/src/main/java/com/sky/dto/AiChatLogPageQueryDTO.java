package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiChatLogPageQueryDTO implements Serializable {

    private Long userId;

    private int page;

    private int pageSize;
}
