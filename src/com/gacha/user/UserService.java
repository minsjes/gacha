package com.gacha.user;

import java.util.List;

import com.gacha.util.EncryptUtil;

public class UserService {
	
	UserDAO userDAO = new UserDAO();
	
	//잔액조회
	public int selectBalance(int user_id) {
		return userDAO.selectBalance(user_id);
	}

	//회원가입
	public int registerService(String user_login_id, String user_pw) {
		UserDTO user = new UserDTO();
		user.setUser_login_id(user_login_id);
		
		String securePw = EncryptUtil.encrypt(user_pw);
		user.setUser_pw(securePw);
		
		return userDAO.register(user_login_id, securePw);
	}

	//회원가입 중복체크
	public boolean checkDuplicateService(String user_login_id) {
		return userDAO.checkDuplicate(user_login_id);
	}
	
	//로그인
	public UserDTO loginService(String user_login_id, String user_pw) {
		String securePw = EncryptUtil.encrypt(user_pw);
		return userDAO.login(user_login_id, securePw);
	}

	public List<UserDTO> selectRankService() {
		return userDAO.selectRankAll();
	}

}
