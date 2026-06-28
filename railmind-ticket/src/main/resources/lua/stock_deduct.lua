-- 库存扣减Lua脚本（原子操作）
-- KEYS[1]: 库存Key (ticket:stock:{trainId}:{date}:{from}:{to}:{seatType})
-- KEYS[2]: 用户下单标记Key (ticket:user:{userId}:{trainId}:{date})
-- ARGV[1]: 用户ID
-- ARGV[2]: 扣减数量
-- ARGV[3]: 用户下单标记过期时间(秒)

-- 1. 检查库存是否充足
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil then
    return -1  -- 库存Key不存在
end

if stock < tonumber(ARGV[2]) then
    return 0  -- 库存不足
end

-- 2. 检查用户是否重复下单
local userOrder = redis.call('GET', KEYS[2])
if userOrder ~= nil then
    return -2  -- 用户已下单
end

-- 3. 扣减库存
redis.call('DECRBY', KEYS[1], ARGV[2])

-- 4. 记录用户下单标记
redis.call('SETEX', KEYS[2], tonumber(ARGV[3]), ARGV[1])

return 1  -- 扣减成功
