package com.gacha.inven;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gacha.item.ItemDTO;
import com.gacha.util.DBUtil;

public class InvenDAO {

	//인벤토리 저장
	public int insert(ItemDTO item, int user_id) {
		int result = 0;
		int resultInven = 0;
		int resultUser = 0;
		Connection conn = null;
		PreparedStatement pstmtInven = null;
		PreparedStatement pstmtUser = null;
		String sqlInven = """
				INSERT INTO tbl_inven 
				VALUES(
				    SEQ_INVEN.NEXTVAL,
				    SYSDATE,
				    ?,
				    ?,
				    'Y')
				""";
		String sqlUser = """
				UPDATE tbl_user
				SET USER_BALANCE = USER_BALANCE - 10000
				WHERE 1=1
				AND USER_ID = ?
				AND USER_BALANCE >= 10000
				""";
			
		try {
			conn = DBUtil.dbConnect();
			conn.setAutoCommit(false);
			
			//금액 차감
			pstmtUser = conn.prepareStatement(sqlUser);
			pstmtUser.setInt(1, user_id);
			resultUser = pstmtUser.executeUpdate();
			if(resultUser == 0) {
                conn.rollback();
                return -1;
            }
			
			//뽑기
			pstmtInven = conn.prepareStatement(sqlInven);
			pstmtInven.setInt(1, user_id);
			pstmtInven.setInt(2, item.getItem_id());
			resultInven = pstmtInven.executeUpdate();
			if(resultInven == 0) {
                conn.rollback();
                return -2;
            }
			
			conn.commit();
			result = 1;
			
		} catch (SQLException e) {
			try {
				if(conn!=null) conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				if(conn!=null) conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DBUtil.dbDisConnect(conn, pstmtUser, null);
			DBUtil.dbDisConnect(conn, pstmtInven, null);
		}
		return result;
	}

	//개인 인벤토리 조회
	public List<InvenDTO> selectById(int user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<InvenDTO> invenlist = new ArrayList<InvenDTO>();
		String sql = """
				SELECT
				    ROWNUM,
				    a.*
				FROM(
				    SELECT 
				        inven.INV_ID,
				        inven.INV_DATE,
				        inven.INV_USER_ID,
				        inven.INV_ITEM_ID,
				        item.ITEM_NAME,
				        rari.RARITY_NAME,
				        rari.RARITY_PRICE
				    FROM 
				        TBL_INVEN inven, TBL_ITEM item, TBL_RARITY rari 
				    WHERE 1=1
				    AND inven.INV_ITEM_ID = item.ITEM_ID
				    AND item.ITEM_RARITY_ID = rari.RARITY_ID
				    AND inven.INV_USER_ID = ?
				    AND inven.INV_ACTIVE = 'Y'
				    ORDER BY INV_DATE DESC
				    ) a
				""";
		
		try {
			conn = DBUtil.dbConnect();
			
			if (conn == null) {
	            System.out.println("⚠️ 데이터베이스 연결 실패! 작업을 수행할 수 없습니다.");
	            return null;
	        }
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, user_id);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				InvenDTO inven = new InvenDTO();
				inven.setInv_id(rs.getInt("inv_id"));
				inven.setInv_user_id(rs.getInt("inv_user_id"));
				inven.setInv_item_id(rs.getInt("inv_item_id"));
				inven.setInv_date(rs.getDate("inv_date"));
				inven.setItem_id(rs.getInt("inv_item_id"));
				inven.setItem_name(rs.getString("item_name"));
				inven.setRarity_name(rs.getString("rarity_name"));
				inven.setRarity_price(rs.getInt("rarity_price"));
				invenlist.add(inven);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return invenlist;
	}

	public int throwById(int inv_id, int rarity_price, int user_id) {
		Connection conn = null;
		int result = 0;
		int result1 = 0;
		int result2 = 0;
		PreparedStatement pstmt1 = null; //인벤토리 -> 제거
		PreparedStatement pstmt2 = null; //금액 증감
		String sql1 = """
				UPDATE TBL_INVEN
				SET 
					INV_ACTIVE = 'N'
				WHERE 1=1
				AND INV_ID = ?
				""";
		
		String sql2 = """
				UPDATE TBL_USER 
				SET 
				    USER_BALANCE = USER_BALANCE + ?
				WHERE 1=1
				AND USER_ID = ?
				""";
		try {
			conn = DBUtil.dbConnect();
			
			//인벤토리 -> 제거
			pstmt1= conn.prepareStatement(sql1);
			pstmt1.setInt(1, inv_id);
			result1 = pstmt1.executeUpdate();
			if(result1 == 0) {
				conn.rollback();
				return -1;
			}
			
			//금액 차감
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, rarity_price);
			pstmt2.setInt(2, user_id);
			result2 = pstmt2.executeUpdate();
			if(result2 == 0) {
				conn.rollback();
				return -2;
			}
			conn.commit();
			result = 1;
		} catch (SQLException e) {
			try {
				if(conn!=null) conn.rollback();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				if(conn!=null) conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DBUtil.dbDisConnect(conn, pstmt1, null);
			DBUtil.dbDisConnect(conn, pstmt2, null);
		}
		return result;
	}

}
