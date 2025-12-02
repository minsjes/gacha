package com.gacha.common;

import java.io.IOException;

public interface IOInterface {
	
	void print(String msg) throws IOException;
    String read() throws IOException, ClassNotFoundException;
}
