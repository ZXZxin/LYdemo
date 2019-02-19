package com.zxin.springboot.mapper;

import com.zxin.springboot.pojo.User;
import tk.mybatis.mapper.common.Mapper; // 导入的是通用Mapper的

public interface UserMapper extends Mapper<User> {
}
