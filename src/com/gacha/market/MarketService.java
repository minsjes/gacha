package com.gacha.market;

import java.util.List;

public class MarketService {

	private MarketDAO marketDAO = new MarketDAO();
	
	//인벤토리에서 마켓 등록
	public int insertMarketService(int price, int user_id, int item_id, int inv_id) {
		return marketDAO.insertMarket(price, user_id, item_id, inv_id);
	}

	//거래소 전체 조회
	public List<MarketDTO> selectAllService() {
		return marketDAO.selectAll();
	}

	public int buyService(int user_id, int mk_id, int price, int seller_id) {
		return marketDAO.buyByMkId(user_id, mk_id, price, seller_id);
	}
	
	//거래소 등록 리스트(유저별)
	public List<MarketDTO> selectMyMkService(int user_id) {
		return marketDAO.selectMyMkService(user_id);
	}

	public int updateMyMkService(int price, int mk_id) {
		return marketDAO.updateMyMk(price, mk_id);
	}

	public int deleteMyMkService(int mk_id, int user_id, int item_id) {
		return marketDAO.deleteMyMk(mk_id, user_id, item_id);
	}

	//거래소 등급별 조회
	public List<MarketDTO> selectByRariService(int rarity) {
		return marketDAO.selectByRari(rarity);
	}

}
