package com.gacha.user;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class UserDTO {
	private Integer user_id;              
	private String user_login_id;
	private String user_pw;  
	private Integer user_balance;    
	private Date user_regdate; 
	private String user_admin; 
	private Integer user_inv_slot; 
}
