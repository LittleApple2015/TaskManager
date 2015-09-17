package com.beijingleader.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class User {
	private String mName;
	private String mPwd;
	//private static final String masterPassword = "FORYOU"; // AES加密算法的种子
	private static final String JSON_NAME = "user_name";
	private static final String JSON_PWD = "user_pwd";
	private static final String TAG = "LoginActivity";

	public User(String name, String pwd) {
		this.mName = name;
		this.mPwd = pwd;
	}

	public User(JSONObject json) throws Exception {
		if (json.has(JSON_NAME)) {
			String name = json.getString(JSON_NAME);
			String pwd = json.getString(JSON_PWD);
			// 解密后存放
			mName = AESUtils.decode(name);
			mPwd = AESUtils.decode(pwd);
			Log.i(TAG, "解密后:" + mName + "  " + mPwd);
		}
	}

	public JSONObject toJSON() throws Exception {
		// 使用AES加密算法加密后保存
		String name = AESUtils.encode(mName);
		String pwd = AESUtils.encode(mPwd);
		Log.i(TAG, "加密后:" + name + "  " + pwd);
		JSONObject json = new JSONObject();
		try {
			json.put(JSON_NAME, name);
			json.put(JSON_PWD, pwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public String getName() {
		return mName;
	}

	public String getPwd() {
		return mPwd;
	}
}
