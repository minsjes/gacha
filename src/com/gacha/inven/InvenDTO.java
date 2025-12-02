package com.gacha.inven;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class InvenDTO {
	private Integer inv_id;
	private Date inv_date;
	private Integer inv_user_id;
	private Integer inv_item_id;
	private String inv_active;
	
	private Integer item_id;
	private String item_name;
	
	private String rarity_name;
	private Integer rarity_price;
}
