-- lua脚本的执行是原子的
-- lua中下标从1开始

-- 获取key
local key = KEYS[1]
-- 获取val
local value = KEYS[2]
-- 获取过期时间
local expire = tonumber(ARGV[1])

if redis.call("get",key) == false then
    -- 没有值就插入
    if redis.call("set",key,value) then
        -- 插入成功就设置过期时间
        if expire > 0 then
            redis.call("expire",key,expire)
        end

        return true
    end
    return false
end
return false

