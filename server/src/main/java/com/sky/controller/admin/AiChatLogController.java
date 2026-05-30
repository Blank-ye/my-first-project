package com.sky.controller.admin;

import com.sky.dto.AiChatLogPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AiChatLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminAiChatLogController")
@RequestMapping("/admin/ai-chat-log")
@Slf4j
public class AiChatLogController {

    @Autowired
    private AiChatLogService aiChatLogService;

    @GetMapping("/page")
    public Result<PageResult> page(AiChatLogPageQueryDTO dto) {
        log.info("AI对话日志分页查询，{}", dto);
        PageResult pageResult = aiChatLogService.pageQuery(dto);
        return Result.success(pageResult);
    }
}
