CREATE TABLE IF NOT EXISTS `ai_chat_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '用户ID',
    `user_message` TEXT COMMENT '用户问题',
    `ai_response` TEXT COMMENT 'AI回答',
    `tool_name` VARCHAR(200) COMMENT '命中的工具名称',
    `input_tokens` INT COMMENT '输入Token数',
    `output_tokens` INT COMMENT '输出Token数',
    `total_tokens` INT COMMENT '总Token数',
    `create_time` DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话日志表';
