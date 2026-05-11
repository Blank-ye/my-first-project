package com.sky.service;

import com.sky.dto.AiChatLogPageQueryDTO;
import com.sky.result.PageResult;

public interface AiChatLogService {

    PageResult pageQuery(AiChatLogPageQueryDTO dto);
}
