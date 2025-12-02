package com.gacha.inven;

import java.util.List;

import com.gacha.item.ItemDTO;

public class InvenService {
	
	InvenDAO invenDAO = new InvenDAO();

	public int insertService(ItemDTO item, int user_id) {
		return invenDAO.insert(item, user_id);
	}

	public List<InvenDTO> selecByIdService(int user_id) {
		return invenDAO.selectById(user_id);
	}

	public int throwService(int inv_id, int rarity_price, int user_id) {
		return invenDAO.throwById(inv_id, rarity_price, user_id);
	}

}
