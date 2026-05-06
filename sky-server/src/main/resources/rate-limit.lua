-- 滑动窗口限流
-- KEYS[1]: 限流 key
-- ARGV[1]: 最大请求数
-- ARGV[2]: 窗口大小（秒）

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(redis.call('TIME')[1])

redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

local current = redis.call('ZCARD', key)

if current < limit then
    redis.call('ZADD', key, now, now .. ':' .. math.random(100000))
    redis.call('EXPIRE', key, window)
    return 1
else
    return 0
end
