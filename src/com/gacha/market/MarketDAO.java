package com.gacha.market;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gacha.util.DBUtil;

public class MarketDAO {

	public int insertMarket(int price, int user_id, int item_id, int inv_id) {
		Connection conn = null;
		int result=0;
		int result1=0;
		int result2=0;
		PreparedStatement pstmt1 = null; //인벤토리 -> 마켓
		PreparedStatement pstmt2 = null; //인벤토리 -> 제거
		
		//인벤토리 -> 마켓
		String sql1 = """
				INSERT INTO TBL_MARKET (
					MK_ID,      
					MK_PRICE,  
					MK_DATE,   
					MK_STATUS,
					MK_SELLER_ID,   
					MK_ITEM_ID
				) VALUES (
					SEQ_MARKET.NEXTVAL, 
					?, 
					SYSDATE, 
					'SELL', 
					?, 
					?
				)
				""";
		
		//인벤토리 -> 제거
		String sql2 = """
				DELETE FROM TBL_INVEN
				WHERE 1=1
				AND INV_USER_ID = ?
				AND INV_ID = ?
				""";
		
		try {
			conn = DBUtil.dbConnect();
			conn.setAutoCommit(false);
			
			//인벤토리 -> 마켓
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setInt(1, price);
			pstmt1.setInt(2, user_id);
			pstmt1.setInt(3, item_id);
			result1 = pstmt1.executeUpdate();
			if(result1 == 0) {
				conn.rollback();
				return -1;
			}
			
			//인벤토리 -> 제거
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, user_id);
			pstmt2.setInt(2, inv_id);
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
			DBUtil.dbDisConnect(null, pstmt1, null);
			DBUtil.dbDisConnect(conn, pstmt2, null);
		}
		return result;
	}

	//마켓 전체 조회
	public List<MarketDTO> selectAll() {
		List<MarketDTO> marketList = new ArrayList<MarketDTO>();
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = """
				SELECT 
				    market.MK_ID,
				    item.ITEM_NAME,
				    rari.RARITY_NAME,
				    market.MK_PRICE,
				    market.MK_DATE,
				    market.MK_SELLER_ID,
				    seller.USER_LOGIN_ID
				FROM TBL_MARKET market, TBL_ITEM item, TBL_USER seller, TBL_RARITY rari
				WHERE 1=1
				AND market.MK_STATUS = 'SELL'
				AND market.MK_ITEM_ID = item.ITEM_ID
				AND market.MK_SELLER_ID = seller.USER_ID
				AND item.ITEM_RARITY_ID = rari.RARITY_ID
				ORDER BY MK_ID DESC
				""";
//		StringBuilder preSql = new StringBuilder();
//		preSql.append("SELECT ");
//		preSql.append("    market.MK_ID, item.ITEM_NAME, rari.RARITY_NAME, ");
//		preSql.append("    market.MK_PRICE, market.MK_DATE, market.MK_SELLER_ID, seller.USER_LOGIN_ID ");
//		preSql.append("FROM TBL_MARKET market, TBL_ITEM item, TBL_USER seller, TBL_RARITY rari ");
//		preSql.append("WHERE market.MK_STATUS = 'SELL' "); // 무조건 필요한 기본 조건
//		preSql.append("AND market.MK_ITEM_ID = item.ITEM_ID ");
//		preSql.append("AND market.MK_SELLER_ID = seller.USER_ID ");
//		preSql.append("AND item.ITEM_RARITY_ID = rari.RARITY_ID ");
//	    
//	    String sql = preSql.toString();
	    
		try {
			conn = DBUtil.dbConnect();
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while(rs.next()) {
				MarketDTO mk = new MarketDTO();
				mk.setMk_id(rs.getInt("mk_id"));
				mk.setMk_date(rs.getDate("mk_date"));
//				mk.setMk_item_id(rs.getInt("mk_item_id"));
				mk.setMk_price(rs.getInt("mk_price"));
				mk.setMk_seller_id(rs.getInt("mk_seller_id"));
//				mk.setMk_status(rs.getString("mk_status"));
				mk.setItem_name(rs.getString("item_name"));
				mk.setUser_login_id(rs.getString("user_login_id"));
				mk.setRarity_name(rs.getString("rarity_name"));
				marketList.add(mk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, st, rs);
		}
		return marketList;
	}

	public int buyByMkId(int user_id, int mk_id, int price, int seller_id) {
		Connection conn = null;
		int result = 0;
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		int result4 = 0;
		PreparedStatement pstmt0 = null; // FOR UPDATE용
		PreparedStatement pstmt1 = null; //거래소 구매 -> 유저 인벤
		PreparedStatement pstmt2 = null; //거래소 구매 -> 잔액 차감
		PreparedStatement pstmt3 = null; //거래소 구매 -> 목록 제거
		PreparedStatement pstmt4 = null; //거래소 구매 -> 판매자 잔액 증가
		ResultSet rs0 = null;
		
		String sql0 = """
		        SELECT MK_ID, MK_STATUS, MK_ITEM_ID
		        FROM TBL_MARKET
		        WHERE MK_ID = ?
		        FOR UPDATE
		    """;

		String sql1 = """
				INSERT INTO TBL_INVEN(
				    INV_ID,
				    INV_DATE,
				    INV_USER_ID,
				    INV_ITEM_ID
				    )
				VALUES (
				    SEQ_INVEN.nextval,
				    SYSDATE,
				    ?,
				    (SELECT MK_ITEM_ID
				    FROM TBL_MARKET
				    WHERE MK_ID = ?))
				""";
		
		String sql2 = """
				UPDATE TBL_USER
				SET USER_BALANCE = USER_BALANCE - ?
				WHERE 1=1
				AND USER_ID = ?
				AND USER_BALANCE >= ?
				""";
		
		String sql3 = """
				UPDATE TBL_MARKET
				SET MK_STATUS = 'SOLD'
				WHERE 1=1
				AND MK_ID = ?
				""";
		
		String sql4 = """
				UPDATE TBL_USER
				SET USER_BALANCE = USER_BALANCE + ?
				WHERE 1=1
				AND USER_ID = ?
				""";
		
		try {
			conn = DBUtil.dbConnect();
			conn.setAutoCommit(false);
			
			//FOR UPDATE 어서 다른 트랜잭션 접근 막기
	        pstmt0 = conn.prepareStatement(sql0);
	        pstmt0.setInt(1, mk_id);
	        rs0 = pstmt0.executeQuery();
	        
	        if (!rs0.next()||"SOLD".equals(rs0.getString("MK_STATUS"))
	        		||"CANCLE".equals(rs0.getString("MK_STATUS"))) {
	            conn.rollback();
	            return -10; // 아이템 없음
	        }
			
			//거래소 구매 -> 유저 인벤
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setInt(1, user_id);
			pstmt1.setInt(2, mk_id);
			result1 = pstmt1.executeUpdate();
			if(result1 == 0) {
				conn.rollback();
				return -1;
			}
			
			//거래소 구매 -> 잔액 차감
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, price);
			pstmt2.setInt(2, user_id);
			pstmt2.setInt(3, price);
			result2 = pstmt2.executeUpdate();
			if(result2 == 0) {
				conn.rollback();
				return -2;
			}
			
			//거래소 구매 -> 목록 제거
			pstmt3 = conn.prepareStatement(sql3);
			pstmt3.setInt(1, mk_id);
			result3 = pstmt3.executeUpdate();
			if(result3 == 0) {
				conn.rollback();
				return -3;
			}
			
			//거래소 구매 -> 판매자 잔액 증가
			pstmt4 = conn.prepareStatement(sql4);
			pstmt4.setInt(1, price);
			pstmt4.setInt(2, seller_id);
			result4 = pstmt4.executeUpdate();
			if(result4 == 0) {
				conn.rollback();
				return -4;
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
			DBUtil.dbDisConnect(null, pstmt0, rs0);
			DBUtil.dbDisConnect(null, pstmt1, null);
			DBUtil.dbDisConnect(null, pstmt2, null);
			DBUtil.dbDisConnect(null, pstmt3, null);
			DBUtil.dbDisConnect(conn, pstmt4, null);
		}
		return result;
	}
	
	//마켓 등록 리스트(유저별)
	public List<MarketDTO> selectMyMkService(int user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<MarketDTO> mkList = new ArrayList<MarketDTO>();
		String sql = """
				SELECT 
				    item.ITEM_NAME,
				    rari.RARITY_NAME,
				    mk.MK_PRICE,
				    mk.MK_DATE,
				    mk.MK_ID,
				    mk.MK_ITEM_ID
				FROM TBL_USER us, TBL_MARKET mk, TBL_ITEM item, TBL_RARITY rari
				WHERE 1=1
				AND us.USER_ID = mk.MK_SELLER_ID
				AND mk.MK_ITEM_ID = item.ITEM_ID
				AND item.ITEM_RARITY_ID = rari.RARITY_ID
				AND us.USER_ID = ?
				AND mk.MK_STATUS = 'SELL'
				ORDER BY mk.MK_DATE DESC 
				""";
		
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, user_id);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				MarketDTO mk = new MarketDTO();
				mk.setItem_name(rs.getString("item_name"));
				mk.setRarity_name(rs.getString("rarity_name"));
				mk.setMk_price(rs.getInt("mk_price"));
				mk.setMk_date(rs.getDate("mk_date"));
				mk.setMk_id(rs.getInt("mk_id"));
				mk.setMk_item_id(rs.getInt("mk_item_id"));
				mkList.add(mk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return mkList;
	}

	public int updateMyMk(int price, int mk_id) {
		int result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = """
				UPDATE TBL_MARKET
				SET MK_PRICE = ?
				WHERE 1=1
				AND MK_ID = ?
				""";
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, price);
			pstmt.setInt(2, mk_id);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, null);
		}
		return result;
	}

	//거래소 등록 취소, 내 인벤에 되돌리기
	public int deleteMyMk(int mk_id, int user_id, int item_id) {
		Connection conn = null;
		PreparedStatement pstmt1 = null; //거래소 목록 삭제
		PreparedStatement pstmt2 = null; //거래소 -> 인벤
		int result = 0;
		int result1 = 0;
		int result2 = 0;
		String sql1 = """
				UPDATE TBL_MARKET 
				SET MK_STATUS = 'CANCEL'
				WHERE 1=1
				AND MK_ID = ?
				""";

		String sql2 = """
				INSERT INTO TBL_INVEN(
				    INV_ID,
				    INV_DATE,
				    INV_USER_ID,
				    INV_ITEM_ID,
				    INV_ACTIVE
				    )
				VALUES (
				    SEQ_INVEN.nextval,
				    SYSDATE,
				    ?,
				    ?,
				    'Y'
				    )
				""";
		
		try {
			conn = DBUtil.dbConnect();
			conn.setAutoCommit(false);
			
			//거래소 목록 삭제
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setInt(1, mk_id);
			result1 = pstmt1.executeUpdate();
			if(result1 == 0 ) {
				conn.rollback();
				return -1;
			}
			
			//거래소 -> 인벤
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, user_id);
			pstmt2.setInt(2, item_id);
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
			DBUtil.dbDisConnect(null, pstmt1, null);
			DBUtil.dbDisConnect(conn, pstmt2, null);
		}
		return result;
	}

	//거래소 등급별 조회
	public List<MarketDTO> selectByRari(int rarity) {
		List<MarketDTO> marketList = new ArrayList<MarketDTO>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = """
				SELECT 
				    market.MK_ID,
				    item.ITEM_NAME,
				    rari.RARITY_NAME,
				    market.MK_PRICE,
				    market.MK_DATE,
				    market.MK_SELLER_ID,
				    seller.USER_LOGIN_ID
				FROM TBL_MARKET market, TBL_ITEM item, TBL_USER seller, TBL_RARITY rari
				WHERE 1=1
				AND market.MK_STATUS = 'SELL'
				AND market.MK_ITEM_ID = item.ITEM_ID
				AND market.MK_SELLER_ID = seller.USER_ID
				AND item.ITEM_RARITY_ID = rari.RARITY_ID
				AND item.ITEM_RARITY_ID = ?
				ORDER BY MK_ID DESC
				""";
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, rarity);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				MarketDTO mk = new MarketDTO();
				mk.setMk_id(rs.getInt("mk_id"));
				mk.setMk_date(rs.getDate("mk_date"));
//					mk.setMk_item_id(rs.getInt("mk_item_id"));
				mk.setMk_price(rs.getInt("mk_price"));
				mk.setMk_seller_id(rs.getInt("mk_seller_id"));
//					mk.setMk_status(rs.getString("mk_status"));
				mk.setItem_name(rs.getString("item_name"));
				mk.setUser_login_id(rs.getString("user_login_id"));
				mk.setRarity_name(rs.getString("rarity_name"));
				marketList.add(mk);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return marketList;
	}
}
