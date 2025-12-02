package com.gacha.item;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.gacha.util.DBUtil;

public class ItemDAO {

	public ItemDTO selectOne() {
		Connection conn = null;
		CallableStatement cstmt = null;
		ItemDTO itemDTO = null;
		String sql = "{ call GACHA_PKG.PULL_ONE_ITEM_PROC(?, ?, ?, ?) }";
		
		try {
			conn = DBUtil.dbConnect();
			cstmt = conn.prepareCall(sql);
			cstmt.registerOutParameter(1, Types.INTEGER);
			cstmt.registerOutParameter(2, Types.VARCHAR);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.registerOutParameter(4, Types.INTEGER);
			
			cstmt.execute();
			
			itemDTO = new ItemDTO();
			itemDTO.setItem_id(cstmt.getInt(1));
			itemDTO.setItem_name(cstmt.getString(2));
			itemDTO.setRarity_name(cstmt.getString(3));
			itemDTO.setRarity_price(cstmt.getInt(4));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, cstmt, null);
		}
		return itemDTO;
	}

	public int throwOnly(int rarity_price, int user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = """
				UPDATE TBL_USER
				SET USER_BALANCE = USER_BALANCE -10000 + ?
				WHERE 1=1
				AND USER_ID = ?
				""";
		
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, rarity_price);
			pstmt.setInt(2, user_id);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, null);
		}
		return result;
	}

	public List<ItemDTO> selectRari() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		List<ItemDTO> rariList = new ArrayList<ItemDTO>();
		String sql = """
				SELECT 
				    RARITY_NAME,
				    RARITY_PROB,
				    RARITY_PRICE
				FROM TBL_RARITY
				""";
		try {
			conn = DBUtil.dbConnect();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery(sql);
			while(rs.next()) {
				ItemDTO rari = new ItemDTO();
				rari.setRarity_name(rs.getString("RARITY_NAME"));
				rari.setRarity_prob(rs.getDouble("RARITY_PROB"));
				rari.setRarity_price(rs.getInt("RARITY_PRICE"));
				rariList.add(rari);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, st, rs);
		}
		return rariList;
	}

	public List<ItemDTO> selectProb() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<ItemDTO> probList = new ArrayList<ItemDTO>();
		String sql = """
				WITH
				    TotalRarityProb AS (
				        SELECT SUM(RARITY_PROB) AS TOTAL_RARITY_PROB
				        FROM TBL_RARITY
				    ),
				    RarityAbsProb AS (
				        SELECT
				            r.RARITY_ID,
				            r.RARITY_NAME,
				            r.RARITY_PRICE,
				            r.RARITY_PROB / tw.TOTAL_RARITY_PROB AS ABS_RARITY_PROB
				        FROM TBL_RARITY r
				        CROSS JOIN TotalRarityProb tw
				    ),
				    ItemGroupTotalWeight AS (
				        SELECT 
				            ITEM_RARITY_ID,
				            SUM(ITEM_WEIGHT) AS ITEM_GROUP_TOTAL_WEIGHT
				        FROM TBL_ITEM
				        GROUP BY ITEM_RARITY_ID
				    )
				SELECT
				    t.ITEM_ID,
				    t.ITEM_NAME,
				    rap.RARITY_NAME,
				    rap.RARITY_PRICE,
				    ROUND(
				        rap.ABS_RARITY_PROB * (t.ITEM_WEIGHT / igtw.ITEM_GROUP_TOTAL_WEIGHT) * 100, 
				        4
				    ) AS ABSOLUTE_PROBABILITY_PERCENT
				FROM TBL_ITEM t
				JOIN RarityAbsProb rap
				    ON t.ITEM_RARITY_ID = rap.RARITY_ID
				JOIN ItemGroupTotalWeight igtw
				    ON t.ITEM_RARITY_ID = igtw.ITEM_RARITY_ID
				ORDER BY ABSOLUTE_PROBABILITY_PERCENT DESC, rap.RARITY_ID
				""";
		try {
			conn = DBUtil.dbConnect();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				ItemDTO item = new ItemDTO();
				item.setItem_id(rs.getInt("ITEM_ID"));
				item.setItem_name(rs.getString("ITEM_NAME"));
				item.setRarity_name(rs.getString("RARITY_NAME"));
				item.setRarity_price(rs.getInt("RARITY_PRICE"));
				item.setAbsolute_prob(rs.getDouble("ABSOLUTE_PROBABILITY_PERCENT"));
				probList.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.dbDisConnect(conn, pstmt, rs);
		}
		return probList;
	}
}
