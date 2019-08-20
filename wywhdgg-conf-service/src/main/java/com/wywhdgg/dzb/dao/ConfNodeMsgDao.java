package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfNodeMsg;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description:
 *@version 1.0
 */
public interface ConfNodeMsgDao {

	public void add(ConfNodeMsg confNode);

	public List<ConfNodeMsg> findMsg(@Param("readedMsgIds") List<Integer> readedMsgIds);

	public int cleanMessage(@Param("messageTimeout") int messageTimeout);

}
