package com.gacha.item;

import java.io.IOException;

import com.gacha.common.ControllerInterface;
import com.gacha.common.IOInterface;
import com.gacha.common.UserSession;
import com.gacha.inven.InvenService;
import com.gacha.user.UserService;
import com.gacha.util.IOUtil;

public class ItemController implements ControllerInterface{
	
	private IOInterface io;
	private ItemService itemService = new ItemService();
	private InvenService invenService = new InvenService();
	private UserService userService = new UserService();
	
	public ItemController(IOInterface io) {
		this.io = io;
	}

	@Override
	public void execute() throws IOException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		boolean isStop = false;
		while(!isStop) {
			int currentBalance = userService.selectBalance(user_id);
			io.print("======================================================================\n"
					+ "[메인화면 > 뽑기]\n"
					+ "현재 잔액은 "
					+ IOUtil.currency(currentBalance)
					+ "원 입니다.\n"
		            + "뽑기 1회당 10,000원이 차감됩니다.\n"
		            + "정말 뽑으시겠습니까?\n"
		            + "1.뽑기 | 0.취소\n"
		            + "----------------------------------------------------------------------\n"
		            );
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {
				if(currentBalance<10000) {
					io.print("----------------------------------------------------------------------\n"
							+ "❌ 잔액이 부족합니다. 뽑기를 진행할 수 없습니다.\n");
				} else f_gacha();}
			case 0 -> {isStop = true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	private void f_gacha() throws IOException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		ItemDTO item = itemService.selectOneService();
		boolean isStop = false;
		while(!isStop) {
			io.print("🎉 "
					+ "["+item.getRarity_name()+"] "
					+ item.getItem_name()+" 이/가 나왔습니다! 🎉\n"
					+ "저장하시겠습니까?\n"
					+ "1.저장하기  2.버리기\n"
					+ "----------------------------------------------------------------------\n");
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {
				int result = invenService.insertService(item, user_id);
				if((result)>0) {
					io.print("----------------------------------------------------------------------\n"
							+ "✅ 아이템이 인벤토리에 저장되고 10,000원이 차감되었습니다!\n");
					isStop = true;
				} else if (result == -1) {
		            io.print("\n❌ 오류: 잔액이 부족하여 아이템을 저장할 수 없습니다.\n");
		            isStop = true;
		        } else if (result == -2) {
		            io.print("\n❌ 오류: 인벤토리 저장 중 문제가 발생했습니다. (트랜잭션 롤백됨)\n");
		            isStop = true;
		        } else {
		             io.print("\n❌ 심각한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n");
		             isStop = true;
		        }
				isStop=true; 
			}
			case 2 -> {if(f_throw(item)>0) isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
	}

	private int f_throw(ItemDTO item) throws IOException, NumberFormatException, ClassNotFoundException {
		int user_id = UserSession.get().getUser_id();
		String item_name = item.getItem_name();
		int rarity_price = item.getRarity_price();
		int result = 0;
		io.print("----------------------------------------------------------------------\n"
				+ "["+item.getRarity_name()+"] "+item_name
				+ " 을 버리면 "+rarity_price+"원을 획득합니다.\n"
				+ "정말 버리시겠습니까?\n"
				+ "1.버리기 | 0.취소\n"
				+ "----------------------------------------------------------------------\n");
		boolean isStop = false;
		while(!isStop) {
			int job = IOUtil.readInt(io, "작업선택>> ");
			switch(job) {
			case 1 -> {
				result = itemService.throwOnly(rarity_price, user_id);
				if(result>0) {
					io.print("10,000원이 차감되었습니다.\n"
							+ item_name+"을 버리고 "+rarity_price+"원을 획득하였습니다.\n");
					isStop = true;
				} else {
					io.print("\n❌ 심각한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n");
		            isStop = true;
				}
			}
			case 0 -> {result=-1; isStop=true;}
			default -> {io.print("❌ 다시 선택해주세요.\n");}
			}
		}
		return result;
	}
}
