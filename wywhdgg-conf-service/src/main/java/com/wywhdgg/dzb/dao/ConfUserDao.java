package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfUser;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description: 配置用户
 *@version 1.0
 */
public interface ConfUserDao {
    public List<ConfUser> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("username") String username, @Param("permission") int permission);

    public int pageListCount(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("username") String username, @Param("permission") int permission);

    public int add(ConfUser xxlConfUser);

    public int update(ConfUser xxlConfUser);

    public int delete(@Param("username") String username);

    public ConfUser load(@Param("username") String username);
}
