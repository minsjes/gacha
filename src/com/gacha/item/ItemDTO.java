package com.gacha.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class ItemDTO {
	private Integer item_id;
	private String item_name;
	private Integer item_price;
	private Integer item_weight;
	private String item_active;
	private Integer item_rarity_id;
	
	private Integer rarity_id;
    private String rarity_name;
    private Double rarity_prob;
    private Integer rarity_price;
    
    private double absolute_prob;
}
