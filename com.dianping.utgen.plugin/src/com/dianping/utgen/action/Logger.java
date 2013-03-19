package com.dianping.utgen.action;

public class Logger {
	public static void log(String msg) {
		System.out.println(msg);
	}
	public static void log(Throwable exception) {
		System.out.println(exception.getMessage());
	}
}
