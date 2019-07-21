package com.wywhdgg.dzb.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.ToString;

/**
 * @author xuxueli 2015-9-4 15:26:01
 */
@Data
@ToString
public class ConfNodeMsg implements Serializable {
	private static final long serialVersionUID = -1267063301941712540L;
	private int id;
	private Date addtime;
	private String env;
	private String key;
	private String value;



}
