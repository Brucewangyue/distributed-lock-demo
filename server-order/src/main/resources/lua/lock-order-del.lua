-- lua脚本的执行是原子的
-- lua中下标从1开始
if redis.call("del",KEYS[1]) then
  return true
end

return false