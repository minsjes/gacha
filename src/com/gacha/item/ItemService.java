package com.gacha.item;

import java.util.List;

public class ItemService {
	
	ItemDAO itemDAO = new ItemDAO();

	//뽑기 1회
	public ItemDTO selectOneService() {
		return itemDAO.selectOne();
	}

	public int throwOnly(int rarity_price, int user_id) {
		return itemDAO.throwOnly(rarity_price, user_id);
	}

	public List<ItemDTO> selectRariService() {
		return itemDAO.selectRari();
	}

	public List<ItemDTO> selectProbService() {
		return itemDAO.selectProb();
	}
}
