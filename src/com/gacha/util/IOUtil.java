package com.gacha.util;

import java.io.IOException;
import java.text.DecimalFormat;

import com.gacha.common.IOInterface;

public class IOUtil {

    //숫자 입력
    public static int readInt(IOInterface io, String message) throws IOException, ClassNotFoundException {
        while (true) {
            io.print(message);
            String input = io.read().trim();
            if(input.isEmpty()) {
            	io.print("❌ 값을 입력해주세요.\n");
                continue;
            }
            try {
            	return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                io.print("❌ 숫자만 입력 가능합니다.\n");
            }
        }
    }

    //문자 입력
    public static String readString(IOInterface io, String message) throws IOException, ClassNotFoundException {
        while (true) {
            io.print(message);
            String input = io.read();
            if (input == null || input.strip().isEmpty()) { 
                io.print("❌ 값이 비어있습니다. 다시 입력해주세요.\n");
            } else {
                return input.trim();
            }
        }
    }
    
    //화폐 단위
    public static String currency(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}