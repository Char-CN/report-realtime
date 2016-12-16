package org.blazer.scheduler.util;

public class SequenceUtil {

	private static int n = 1;

	private static int reset = 10000;

	private static int max = 99999;

	public static synchronized int get() {
		if (n > max) {
			// 此刻重置，是为系统保留reset，只有在系统启动的时候，可以用到1-reset的number，因此在重启系统之后，尽可能不会出现重复值。理论上来说，保留数越大，风险越低
			n = reset;
			return n;
		}
		return n++;
	}

	public static String getStr0() {
		int newNumber = get();
		if (newNumber < 10) {
			return "0000" + newNumber;
		} else if (newNumber < 100) {
			return "000" + newNumber;
		} else if (newNumber < 1000) {
			return "00" + newNumber;
		} else if (newNumber < 10000) {
			return "0" + newNumber;
		}
		return "" + newNumber;
	}

}
