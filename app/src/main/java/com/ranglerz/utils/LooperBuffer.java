package com.ranglerz.utils;

public interface LooperBuffer {
	void add(byte[] buffer);

	byte[] getFullPacket();
}