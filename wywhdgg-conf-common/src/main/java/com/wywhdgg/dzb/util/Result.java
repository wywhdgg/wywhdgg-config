package com.wywhdgg.dzb.util;

/***
 *@author dzb
 *@date 2019/7/21 17:20
 *@Description:
 *@version 1.0
 */
public class Result<T> {

	public static final int SUCCESS_CODE = 200;
	public static final int FAIL_CODE = 500;

	public static final Result<String> SUCCESS = new Result<String>(null);
	public static final Result<String> FAIL = new Result<String>(FAIL_CODE, null);

	private int code;
	private String msg;
	private T data;

	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public Result(T data) {
		this.code = SUCCESS_CODE;
		this.data = data;
	}


	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
