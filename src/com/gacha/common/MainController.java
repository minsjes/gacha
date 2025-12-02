package com.gacha.common;

import java.io.IOException;
import java.util.List;

import com.gacha.item.ItemController;
import com.gacha.item.ItemDTO;
import com.gacha.item.ItemService;
import com.gacha.market.MarketController;
import com.gacha.user.UserController;
import com.gacha.user.UserDTO;
import com.gacha.user.UserService;
import com.gacha.util.IOUtil;

public class MainController implements ControllerInterface{
	
	private IOInterface io;
	private UserService userService = new UserService();
	private ItemService itemService = new ItemService();

	public MainController(IOInterface io) {
		this.io = io;
	}

	@Override
	public void execute() throws IOException, ClassNotFoundException {
		boolean isStop = false;
		while(!isStop) {
			io.print("======================================================================\n"
				+ "[로그인/회원가입]\n"
	            + "1.로그인 | 2.회원가입 | 0.종료\n"
	            + "----------------------------------------------------------------------\n"
	            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
				case 1 -> {
					UserDTO user = f_login();
					if(user!=null) f_start();	
				}
				case 2 -> {f_register();}
				case 0 -> {io.print("TERMINATE"); isStop=true;}
				default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}
	
	//회원가입
	private void f_register() throws IOException, ClassNotFoundException {
		String user_login_id = "";
		String user_pw = "";
			user_login_id = IOUtil.readString(io, "아이디 입력>> ");
			boolean isDuplicate = userService.checkDuplicateService(user_login_id);
			if(isDuplicate) {
				io.print("❌ 이미 사용 중인 ID입니다.\n");
			} else {
				user_pw = IOUtil.readString(io, "비밀번호 입력>> ");
				int result = userService.registerService(user_login_id, user_pw);
				if(result>0) {
					io.print("----------------------------------------------------------------------\n"
							+"🎉 회원가입에 성공했습니다.\n");
				} else {
					io.print("❌ 회원가입에 실패했습니다.\n");
				}
			}
		
	}
	
	//로그인
	public UserDTO f_login() throws IOException, ClassNotFoundException {
		String user_login_id = IOUtil.readString(io, "아이디 입력>> ");
		String user_pw = IOUtil.readString(io, "비밀번호 입력>> ");
		
		UserDTO user = userService.loginService(user_login_id, user_pw);
		
		if (user != null) {
			UserSession.set(user);
			io.print("----------------------------------------------------------------------\n"
					+ "로그인 되었습니다.\n"
					+ "\" "+user.getUser_login_id()+" \""
					+" 님 환영합니다!\n");
		} else {
			io.print("❌ 아이디 또는 비밀번호가 일치하지 않습니다.\n");
		}
		return user;
	}
	
	//로그인 후 시작화면
	public void f_start() throws IOException, ClassNotFoundException {
		
		ControllerInterface controller = null;
		boolean isStop = false;
		while(!isStop) {
			io.print("======================================================================\n"
					+ "[메인화면]\n"
					+ "1.마이페이지 | 2.뽑기 | 3.거래소 | 4.랭킹 | 5.확률 정보 | 0.로그아웃\n"
					+ "----------------------------------------------------------------------\n"
					);
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {controller = new UserController(io);}
			case 2 -> {controller = new ItemController(io);}
			case 3 -> {controller = new MarketController(io);}
			case 4 -> {f_rank(); controller=null;}
			case 5 -> {f_prob(); controller=null;}
			case 0 -> {
				UserSession.set(null);
				controller = null; 
				isStop = true;
				io.print("----------------------------------------------------------------------\n"
						+"로그아웃 되었습니다.\n");
			}
			default -> {io.print("❌ 다시 선택해주세요.\n"); controller=null;}
			}
			if(controller!=null) {
				controller.execute();
			}
		}
	}

	//확률 정보 조회
	private void f_prob() throws IOException {
		//등급 확률 정보
		io.print("======================================================================\n"
				+ "※ 등급 확률 ※\n");		
		List<ItemDTO> rarityList = itemService.selectRariService();
		io.print(String.format("%-5s %-10s %-10s %-15s\n",
				"번호","등급","확률(%)","기본가격"));
		int i=1;
		for(ItemDTO rari:rarityList) {
			io.print(String.format("%-5d %-10s %-10s %-15s\n",
				i++,
				rari.getRarity_name(),
				rari.getRarity_prob(),
				IOUtil.currency(rari.getRarity_price())
				));
		}
		
		io.print("----------------------------------------------------------------------\n"
				+ "※ 아이템별 확률 ※\n");		
		
		//아이템별 확률 정보
		List<ItemDTO> itemProbList = itemService.selectProbService();
		io.print(String.format("%-7s %-23s %-10s %-20s %-10s\n",
				"번호","아이템명","등급","기본가격","확률(%)"));
		int j=1;
		for(ItemDTO item:itemProbList) {
			io.print(String.format("%-7d %-23s %-10s %-20s %-10s\n",
				j++,
				item.getItem_name(),
				item.getRarity_name(),
				IOUtil.currency(item.getRarity_price()),
				item.getAbsolute_prob()
				));
		}
	}

	//랭킹 조회
	private void f_rank() throws IOException {
		List<UserDTO> userList = userService.selectRankService();
		io.print("======================================================================\n"
				+ "※ 유저 랭킹 ※\n");	
		
		if (userList == null || userList.isEmpty()) {
            io.print("랭킹 정보가 없습니다.\n");
            return;
		}  
		
		io.print(String.format("%-5s %-15s %-15s\n","랭킹","아이디","잔액"));
		
		int i=1;
		for(UserDTO user:userList) {
			io.print(String.format("%-5d %-15s %-15s\n",
				i++,
				user.getUser_login_id(),
				IOUtil.currency(user.getUser_balance())
			));
		}
	}
}
