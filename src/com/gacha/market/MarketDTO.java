package com.gacha.market;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class MarketDTO {
	private Integer mk_id;
	private Integer mk_price;
	private Date mk_date;
	private String mk_status;
	private Integer mk_seller_id;
	private Integer mk_item_id;
	
	private String item_name;
	
	private String user_login_id;
	
	private String rarity_name;
}
