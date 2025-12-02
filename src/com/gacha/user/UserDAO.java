package com.gacha.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gacha.util.DBUtil;

public class UserDAO {
	
	//랭킹 조회
	public List<UserDTO> selectRankAll() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		List<UserDTO> userList = new ArrayList<UserDTO>();
		String sql = """
				SELECT 
				    USER_ID, 
				    USER_LOGIN_ID,
				    USER_BALANCE
				FROM TBL_USER
				ORDER BY USER_BALANCE DESC
				""";
		try {
			conn = DBUtil.dbConnect();
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while(rs.next()) {
				UserDTO user = new UserDTO();
				user.setUser_id(rs.getInt("USER_ID"));
                user.setUser_login_id(rs.getString("USER_LOGIN_ID"));
                user.setUser_balance(rs.getInt("USER_BALANCE"));
				userList.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, st, rs);
		}
		return userList;
	}
	
	//잔액조회
	public int selectBalance(int user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int balance = 0;
		
		String sql = """
				SELECT USER_BALANCE 
				FROM TBL_USER
				WHERE USER_ID = ?
				""";
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				balance = rs.getInt("USER_BALANCE");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return balance;
	}
	
	//중복체크
	public boolean checkDuplicate(String user_login_id) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    boolean isDuplicated = false;
	    
	    String sql = """
	    		SELECT COUNT(*) CNT
	    		FROM TBL_USER 
	    		WHERE USER_LOGIN_ID = ?
	    		""";
	    try {
	        conn = DBUtil.dbConnect(); // DB 연결
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, user_login_id);
	        rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            if (rs.getInt(1) > 0) {
	                isDuplicated = true;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        DBUtil.dbDisConnect(conn, pstmt, rs);
	    }
	    return isDuplicated;
	}

	//회원가입
	public int register(String user_login_id, String user_pw) {
		int result = 0;
		Connection conn = null;
		PreparedStatement st = null;
		String sql = """
				INSERT INTO TBL_USER(
					USER_ID, 
					USER_LOGIN_ID, 
					USER_PW) 
				VALUES(
					SEQ_USER.NEXTVAL, 
					?, 
					?)
				""";
		try {
			conn = DBUtil.dbConnect();
			st = conn.prepareStatement(sql);
			st.setString(1, user_login_id);
			st.setString(2, user_pw);
			result = st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, st, null);
		}
		return result;
	}
	
	//로그인
	public UserDTO login(String user_login_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO user = null;
		String sql = """
				SELECT 
					USER_ID, 
					USER_LOGIN_ID, 
					USER_PW, 
					USER_BALANCE, 
					USER_ADMIN 
				FROM TBL_USER 
				WHERE USER_LOGIN_ID = ?
				AND USER_PW = ?
				""";
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_login_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				user = new UserDTO();
				user.setUser_id(rs.getInt("user_id"));
				user.setUser_login_id(rs.getString("user_login_id"));
				user.setUser_pw(rs.getString("user_pw"));
				user.setUser_balance(rs.getInt("user_balance"));
				user.setUser_admin(rs.getString("user_admin"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return user;
	}

}
