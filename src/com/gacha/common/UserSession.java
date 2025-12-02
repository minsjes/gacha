package com.gacha.common;

import com.gacha.user.UserDTO;

public class UserSession {
	
	private static final ThreadLocal<UserDTO> userInfo = new ThreadLocal<>();
	
	public static void set(UserDTO user) {
		userInfo.set(user);
	}
	
	public static UserDTO get() {
		return userInfo.get();
	}
	
	public static void remove() {
		userInfo.remove();
	}
}

