package com.gacha.market;

import java.io.IOException;
import java.util.List;

import com.gacha.common.ControllerInterface;
import com.gacha.common.IOInterface;
import com.gacha.common.UserSession;
import com.gacha.inven.InvenDTO;
import com.gacha.inven.InvenService;
import com.gacha.user.UserService;
import com.gacha.util.IOUtil;

public class MarketController implements ControllerInterface{

	private IOInterface io;
	private MarketService marketService = new MarketService();
	private UserService userService = new UserService();
	private InvenService invenService = new InvenService();
	
	public MarketController(IOInterface io) {
		this.io = io;
	}

	@Override
	public void execute() throws IOException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		boolean isStop = false;
		while(!isStop) {
			io.print("----------------------------------------------------------------------\n");
			List<MarketDTO> marketList = marketService.selectAllService();
			String header = "%-4s %-20s %-8s %-8s %-7s %-5s\n";
			if (marketList == null || marketList.isEmpty()) {
				io.print("거래소 목록이 비어있습니다.\n");
			} else {
				io.print(String.format(header,"번호", "아이템명", "가격", "등급", "판매자", "등록일"));
				int i=marketList.size();
				for(MarketDTO inven:marketList) {
					io.print(String.format(header,
							i--,
							inven.getItem_name(),
							IOUtil.currency(inven.getMk_price()),
							inven.getRarity_name(),
							inven.getUser_login_id(),
							inven.getMk_date()));
				}
			}
			//잔액 조회
			int currentBalance = userService.selectBalance(user_id);
			io.print("======================================================================\n"
					+ "[메인화면 > 거래소]\n"
					+ "현재 잔액은 "+IOUtil.currency(currentBalance)+"원 입니다.\n"
		            + "1.구매하기 | 2.판매하기 | 0.뒤로가기\n"
//		            + "1.구매하기  2.판매하기  3.등급별 조회  4.판매자별 조회  5.가격별 조회  0.뒤로가기\n"
		            + "----------------------------------------------------------------------\n"
		            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {f_buy(marketList);}
			case 2 -> {f_sellService();}
//			case 3 -> {f_selectByRari();}
//			case 4 -> {f_selectBySeller();}
//			case 5 -> {f_sellService();}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	/*private void f_selectBySeller() {
		
	}

	private void f_selectByRari() throws IOException, ClassNotFoundException {
		int rarity = 0;
		while(rarity<1||rarity>5) {
			io.print("1.노멀  2.레어  3.에픽  4.유니크  5.레전더리 ");
			rarity = IOUtil.readInt(io, "작업선택>> ");
			if(rarity<1||rarity>5) io.print("다시 선택해주세요.\n");
			else break;
		}
		
		List<MarketDTO> marketList = marketService.selectByRariService(rarity);
		String header = "%-4s %-20s %-8s %-8s %-7s %-5s\n";
		if (marketList == null || marketList.isEmpty()) {
			io.print("----------------------------------------------------------------------\n"
					+ Font.RED+Font.BOLD+"거래소 목록이 비어있습니다.\n"+Font.RESET);
		} else {
			io.print(String.format(header,"번호", "아이템명", "가격", "등급", "판매자", "등록일"));
			int i=marketList.size();
			for(MarketDTO inven:marketList) {
				io.print(String.format(header,
						i--,
						inven.getItem_name(),
						inven.getMk_price(),
						inven.getRarity_name(),
						inven.getUser_login_id(),
						inven.getMk_date()));
			}
		}
	}*/

	private void f_sellService() throws IOException, NumberFormatException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		//내 인벤 목록 조회
		boolean isStop = false;
		while(!isStop) {
			io.print("----------------------------------------------------------------------\n");
			List<InvenDTO> invenList = invenService.selecByIdService(user_id);
			String header = "%-4s %-20s %-8s %-8s %-12s\n";
			
			if (invenList == null || invenList.isEmpty()) {
	            io.print("❌ 등록할 아이템이 없습니다.\n");
			} else {
				io.print(String.format(header,"번호", "아이템명", "등급", "가격", "등록일"));
				int i=invenList.size();
				for(InvenDTO inven:invenList) {
					io.print(String.format(header,
							i--,
							inven.getItem_name(),
							inven.getRarity_name(),
							IOUtil.currency(inven.getRarity_price()),
							inven.getInv_date()));
				}
			}
			io.print("----------------------------------------------------------------------\n");
			int num = IOUtil.readInt(io, "등록할 아이템 번호를 입력해주세요.>> ");
			int index = invenList.size() - num;
			if(index<0 || index>=invenList.size()) {
				io.print("----------------------------------------------------------------------\n"
						+ "❌ 잘못된 번호입니다.\n" );
				return;
			}
			
			int price = IOUtil.readInt(io, "판매할 가격을 입력해주세요.>> ");
			
			InvenDTO selected = invenList.get(index);
			int item_id = selected.getInv_item_id();
			int inv_id = selected.getInv_id();
			
			int result = marketService.insertMarketService(price, user_id, item_id, inv_id);
			if(result>0) {
				io.print("거래소에 아이템이 등록되었습니다.\n");
				isStop = true;
			} else if(result==-1){
				io.print("등록에 실패했습니다");
			} else if(result==-2) {
				io.print("등록에 실패했습니다2");
			}
		}
	}

	private void f_buy(List<MarketDTO> marketList) throws IOException, NumberFormatException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		int currentBalance = userService.selectBalance(user_id);
		if (marketList == null || marketList.isEmpty()) {
            io.print("구매할 아이템이 없습니다.\n");
            return;
		}
		
		io.print("----------------------------------------------------------------------\n");
		int num = IOUtil.readInt(io, "구매할 아이템 번호를 입력해주세요>> ");
		int index = marketList.size() - num;
		
		if(index<0 || index>=marketList.size()) {
			io.print("----------------------------------------------------------------------\n"
					+ "❌ 잘못된 번호입니다.\n");
			return;
		}
		
		io.print("정말 구매하시겠습니까? \n"
				+ "1.구매하기  0.취소\n"
				+ "----------------------------------------------------------------------\n"
				);
		int job = IOUtil.readInt(io, "작업선택>> ");
		boolean isStop = false;
		while(!isStop) {
			int price = marketList.get(index).getMk_price();
			int mk_id = marketList.get(index).getMk_id();
			int mk_seller_id = marketList.get(index).getMk_seller_id();
			switch(job) {
			case 1 -> {
				if(currentBalance<price) {
					io.print("----------------------------------------------------------------------\n"
							+ "❌ 잔액이 부족합니다. 아이템을 구매할 수 없습니다.\n");
					isStop=true;
				} else {
					int result = marketService.buyService(user_id, mk_id, price, mk_seller_id);
					if(result>0) {
						io.print("아이템을 구매하였습니다.\n");
						isStop = true;
					} else if(result==-1) {
						io.print("\n❌ 실패: 인벤토리에 아이템을 저장할 수 없습니다.\n");
			            isStop = true;
					} else if(result==-2) {
						io.print("\n❌ 실패: 잔액이 부족하여 아이템을 구매할 수 없습니다.\n");
			            isStop = true;
					} else if(result==-3) {
						io.print("\n❌ 실패: 거래소에서 아이템을 제거할 수 없습니다.\n");
			            isStop = true;
					} else if(result==-4) {
						io.print("\n❌ 실패: 판매자에게 금액이 전달되지 않았습니다.\n");
						isStop = true;
					} else if(result==-10) {
						io.print("\n❌ 실패: 아이템이 존재하지 않습니다.\n");
						isStop = true;
					} else {
						io.print("\n❌ 심각한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n");
			            isStop = true;
					}
					
				}
			}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}
}
