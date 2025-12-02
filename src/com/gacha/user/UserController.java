package com.gacha.user;

import java.io.IOException;
import java.util.List;

import com.gacha.common.ControllerInterface;
import com.gacha.common.IOInterface;
import com.gacha.common.UserSession;
import com.gacha.inven.InvenDTO;
import com.gacha.inven.InvenService;
import com.gacha.market.MarketDTO;
import com.gacha.market.MarketService;
import com.gacha.util.IOUtil;

public class UserController implements ControllerInterface{

	private IOInterface io;
	private InvenService invenService = new InvenService();
	private MarketService marketService = new MarketService();
	private UserService userService = new UserService();
	
	public UserController(IOInterface io) {
		this.io = io;
	}

	@Override
	public void execute() throws IOException, ClassNotFoundException {
		
		boolean isStop = false;
		
		while(!isStop) {
			io.print("======================================================================\n"
					+ "[메인화면 > 마이페이지]\n"
		            + "1.인벤토리 | 2.등록한 아이템 | 0.뒤로가기\n"
//		            + "1.인벤토리  2.등록한 아이템  3.거래내역  0.뒤로가기\n"
		            + "----------------------------------------------------------------------\n"
		            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {f_myInven();}
			case 2 -> {f_myMarket();}
//			case 3 -> {f_myTrade();}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
		
	}

	private void f_myMarket() throws IOException, NumberFormatException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		boolean isStop = false;
		while(!isStop) {
			io.print("----------------------------------------------------------------------\n");
			List<MarketDTO> mkList = marketService.selectMyMkService(user_id);
			String header = "%-4s %-20s %-8s %-8s %-12s\n";
			
			if(mkList == null || mkList.isEmpty()) {
				io.print("❌ 등록한 아이템이 없습니다.\n");
			} else {
				io.print(String.format(header,"번호", "아이템명", "등급", "가격", "등록일"));
				int i=mkList.size();
				for(MarketDTO mk:mkList) {
					io.print(String.format(header,
						i--,
						mk.getItem_name(),
						mk.getRarity_name(),
						IOUtil.currency(mk.getMk_price()),
						mk.getMk_date()));
				}
			}
			int currentBalance = userService.selectBalance(user_id);
			io.print("======================================================================\n"
					+ "[메인화면 > 마이페이지 > 등록한 아이템]\n"
					+ "현재 잔액은 "+ IOUtil.currency(currentBalance) +"원 입니다.\n"
		            + "1.가격 수정 | 2.등록 취소 | 0.뒤로가기\n"
		            + "----------------------------------------------------------------------\n"
		            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {f_myMarketUdt(mkList);}
			case 2 -> {f_myMarketDel(mkList);}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
		
	}

	private void f_myMarketDel(List<MarketDTO> mkList) throws IOException, NumberFormatException, ClassNotFoundException {
		io.print("----------------------------------------------------------------------\n");
		int num = IOUtil.readInt(io, "등록을 취소할 아이템 번호를 입력해주세요>> ");
		int index = mkList.size() - num;
		if(index<0 || index>=mkList.size()) {
			io.print("----------------------------------------------------------------------\n"
					+ "❌ 잘못된 번호입니다.\n");
			return;
		}
		
		io.print("정말 등록을 취소하시겠습니까?\n"
				+ "1.취소  0.취소를 취소\n"
				+ "----------------------------------------------------------------------\n");
		
		int mk_id = mkList.get(index).getMk_id();
		int user_id = UserSession.get().getUser_id();
		int item_id = mkList.get(index).getMk_item_id();
		
		//마겟 아이디, 유저 아이디, 아이템 아이디
		boolean isStop = false;
		while(!isStop) {
			int job = IOUtil.readInt(io, "작업선택>> ");
			io.print("----------------------------------------------------------------------\n");
			switch(job) {
			case 1 -> {
				int result = marketService.deleteMyMkService(mk_id, user_id, item_id);
				if(result>0) {
					io.print("등록을 취소하고 인벤토리에 저장하였습니다.\n");
					isStop = true;
				} else if(result==-1) {
					io.print("\n❌ 오류: 거래소 등록 취소에 실패하였습니다.\n");
		            isStop = true;
				} else if(result==-2) {
					io.print("\n❌ 오류: 인벤토리에 저장하지 못하였습니다.\n");
		            isStop = true;
				} else {
					io.print("\n❌ 심각한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n");
		            isStop = true;
				}
			}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	private void f_myMarketUdt(List<MarketDTO> mkList) throws IOException, NumberFormatException, ClassNotFoundException {
		io.print("----------------------------------------------------------------------\n");
		int num = IOUtil.readInt(io, "가격을 수정할 아이템 번호를 입력해주세요>> ");
		int index = mkList.size() - num;
		
		if(index<0 || index>=mkList.size()) {
			io.print("----------------------------------------------------------------------\n"
					+ "❌ 잘못된 번호입니다.\n");
			return;
		}

		int price = IOUtil.readInt(io, "수정할 금액을 입력해주세요>> ");
		int mk_id = mkList.get(index).getMk_id();
		int result = marketService.updateMyMkService(price, mk_id);
		
		if(result > 0) {
			io.print("수정되었습니다.\n");
		}
	}

	private void f_myInven() throws IOException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		boolean isStop = false;
		while(!isStop) {
			io.print("----------------------------------------------------------------------\n");
			List<InvenDTO> invenList = invenService.selecByIdService(user_id);
			String header = "%-4s %-20s %-8s %-8s %-12s\n";
			
			if (invenList == null || invenList.isEmpty()) {
	            io.print("❌ 인벤토리가 비어있습니다.\n");
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
			int currentBalance = userService.selectBalance(user_id);
			io.print("======================================================================\n"
					+ "[메인화면 > 마이페이지 > 인벤토리]\n"
					+ "현재 잔액은 "+ IOUtil.currency(currentBalance) +"원 입니다.\n"
		            + "1.거래소 등록 | 2.아이템 버리기 | 0.뒤로가기\n"
		            + "----------------------------------------------------------------------\n"
		            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {f_insertMarket(invenList);}
			case 2 -> {f_throw(invenList);}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	private void f_throw(List<InvenDTO> invenList) throws IOException, NumberFormatException, ClassNotFoundException {
		io.print("----------------------------------------------------------------------\n");
		if (invenList == null || invenList.isEmpty()) {
            io.print("❌ 버릴 아이템이 없습니다.\n");
            return;
		}
		int num = IOUtil.readInt(io, "버릴 아이템 번호를 입력해주세요>> ");
		int index = invenList.size() - num;
		
		if(index<0 || index>=invenList.size()) {
			io.print("잘못된 번호입니다.\n");
			return;
		}
		
		InvenDTO selected = invenList.get(index);
		int user_id = selected.getInv_user_id();
		int inv_id = selected.getInv_id();
		String item_name = selected.getItem_name();
		int rarity_price = selected.getRarity_price();
		io.print(item_name+"을 버리면 "+IOUtil.currency(rarity_price)+"원을 획득합니다.\n"
				+ "정말 버리시겠습니까?\n"
				+ "1.버리기 | 0.취소\n"
				+ "----------------------------------------------------------------------\n");
		boolean isStop = false;
		while(!isStop) {
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {
				int result = invenService.throwService(inv_id, rarity_price, user_id);
				if(result>0) {
					io.print(item_name+"을 버리고 "+IOUtil.currency(rarity_price)+"원을 획득하였습니다.\n");
					isStop = true;
				} else if(result==-1) {
					io.print("\n❌ 오류: 아이템을 삭제에 실패하였습니다.\n");
		            isStop = true;
				} else if(result==-2) {
					io.print("\n❌ 오류: 돈을 받지 못하였습니다.\n");
		            isStop = true;
				} else {
					io.print("\n❌ 심각한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n");
		            isStop = true;
				}
			}
			case 0 -> {isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	private void f_insertMarket(List<InvenDTO> invenList) throws IOException, ClassNotFoundException {
		if (invenList == null || invenList.isEmpty()) {
            io.print("❌ 등록할 아이템이 없습니다.\n");
            return;
		}
		int num = IOUtil.readInt(io, "등록할 아이템 번호를 입력해주세요.>> ");
		int index = invenList.size() - num;
		if(index<0 || index>=invenList.size()) {
			io.print("----------------------------------------------------------------------\n"
					+ "❌ 잘못된 번호입니다.\n");
			return;
		}
		
		int price = IOUtil.readInt(io, "판매할 가격을 입력해주세요>> ");
		
		InvenDTO selected = invenList.get(index);
		int user_id = selected.getInv_user_id();
		int item_id = selected.getInv_item_id();
		int inv_id = selected.getInv_id();
		
		int result = marketService.insertMarketService(price, user_id, item_id, inv_id);
		if(result>0) {
			io.print("----------------------------------------------------------------------\n"
					+"거래소에 아이템이 등록되었습니다.\n");
		} else if(result==-1){
			io.print("----------------------------------------------------------------------\n"
					+ "등록에 실패했습니다");
		} else if(result==-2) {
			io.print("----------------------------------------------------------------------\n"
					+ "인벤토리에서 제거하지 못했습니다.");
		}
	}
} 

