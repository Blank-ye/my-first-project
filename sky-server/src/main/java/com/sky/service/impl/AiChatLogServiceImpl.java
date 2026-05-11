package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.dto.AiChatLogPageQueryDTO;
import com.sky.entity.AiChatLog;
import com.sky.mapper.AiChatLogMapper;
import com.sky.result.PageResult;
import com.sky.service.AiChatLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatLogServiceImpl implements AiChatLogService {

    @Autowired
    private AiChatLogMapper aiChatLogMapper;

    @Override
    public PageResult pageQuery(AiChatLogPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        List<AiChatLog> list = aiChatLogMapper.pageQuery(dto);
        PageInfo<AiChatLog> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }
}
