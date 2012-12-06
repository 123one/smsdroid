package com.nnm.SmsHandle;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

public class HandleSms {

	public static final String[] nguyenam = { "b", "c", "d", "đ", "g", "h", "k", "l", "m", "n",
			"p", "q", "r", "s", "t", "u", "v", "x" };
	public static final String[] chughep2chu = { "ch", "gh", "gi", "kh", "ng", "nh", "ph", "th",
			"tr" };

	public static String reserverSmS(String sms) {
		sms = sms.toLowerCase();
		ArrayList<ContentSms> contentsms = new ArrayList<ContentSms>();
		int index = 0;
		for (int i = 0; i < sms.length(); i++) {
			Character c = sms.charAt(i);
			if (c == '?' || c == '.' || c == '!' || c == '\n') {
				ContentSms cts = new ContentSms();
				String tempCut = sms.substring(index, i);
				// if (tempCut.startsWith(" ")){
				// tempCut = tempCut.substring(1, tempCut.length());
				// }
				// if (tempCut.endsWith(" ")){
				// tempCut = tempCut.substring(0, tempCut.length() - 1);
				// }
				tempCut = tempCut.trim();
				Log.i("TAG", "cat ra tung cau: " + tempCut + c.toString());
				tempCut = laiTungCau(tempCut);
				cts.special = c.toString();
				cts.body = tempCut;
				contentsms.add(cts);
				index = i + 1;
			} else if (i == sms.length() - 1) {
				ContentSms cts = new ContentSms();
				String tempCut = sms.substring(index, i + 1);
				Log.i("TAG", "cat ra tung cau: " + tempCut + ".");
				tempCut = laiTungCau(tempCut);
				cts.special = "";
				cts.body = tempCut;
				contentsms.add(cts);
				index = i;
			}
		}

		sms = "";
		for (ContentSms c : contentsms) {
			c.body = c.body.trim();

			Log.i("TAG", "tung cau: " + c.body + ".");
		}
		for (int i = 0; i < contentsms.size(); i++) {

			contentsms.get(i).body = contentsms.get(i).body.trim();
			sms = sms + contentsms.get(i).body + contentsms.get(i).special;
			if (i != contentsms.size() - 1 && !contentsms.get(i).special.equals("\n")) {
				sms = sms + " ";
			}
		}

		// ArrayList<String> splipString = new ArrayList<String>();
		//
		// if (!sms.contains(" ")){
		// return sms;
		// }
		// String[] splip = sms.split(" ");
		//
		// for (int i = 0; i < splip.length; i++) {
		// splipString.add(splip[i]);
		// }
		//
		// sms = "";
		//
		// int index = 0;
		// while (!splipString.isEmpty()){
		// if (splipString.size() == 3){
		// String[] temp1 = cat1chu(splipString.get(index));
		// String[] temp2 = cat1chu(splipString.get(index + 2));
		//
		// String temp = temp1[0] + temp2[1] + " " + splipString.get(index + 1)
		// + " " + temp2[0] + temp1[1];
		// sms = sms + temp;
		// splipString.clear();
		// }else{
		// String temp = noilai(splipString.get(index), splipString.get(index +
		// 1));
		// sms = sms + temp;
		// sms = sms + " ";
		// splipString.remove(index);
		// splipString.remove(index);
		// }
		// }

		return sms;
	}

	public static String laiTungCau(final String cau) {
		String sms = cau;
		sms = sms.trim();
		ArrayList<String> splipString = new ArrayList<String>();

		if (!sms.contains(" ")) {
			return sms;
		}
		String[] splip = sms.split(" ");

		for (int i = 0; i < splip.length; i++) {
			splipString.add(splip[i]);
		}

		sms = "";

		int index = 0;
		while (!splipString.isEmpty()) {
			if (splipString.size() == 3) {
				String[] temp1 = cat1chu(splipString.get(index));
				String[] temp2 = cat1chu(splipString.get(index + 2));

				String temp = temp1[0] + temp2[1] + " " + splipString.get(index + 1) + " "
						+ temp2[0] + temp1[1];
				sms = sms + temp;
				splipString.clear();
			} else {
				String temp = noilai(splipString.get(index), splipString.get(index + 1));
				sms = sms + temp;
				if (splipString.size() != 1) {
					sms = sms + " ";
				}
				splipString.remove(index);
				splipString.remove(index);
			}
		}

		return sms;
	}

	public static ArrayList<String> splipString(String sms) {
		ArrayList<String> splipString = new ArrayList<String>();
		for (int i = 0; i < sms.length(); i++) {
			Character c = sms.charAt(i);
			if (c == '.' || c == '!' || c == '?') {
				splipString.add(sms.substring(0, i));
				sms = sms.substring(0, i);
				if (i < sms.length() - 1) {
					splipString.add(sms.substring(0, 1));
				}
			}
		}

		boolean is = sms.contains(".") || sms.contains("!") || sms.contains("?");

		return splipString;
	}

	public static String noilai(String str1, String str2) {
		String[] temp1 = cat1chu(str1);
		String[] temp2 = cat1chu(str2);
		str1 = temp1[0] + temp2[1];
		str2 = temp2[0] + temp1[1];
		Log.i("TAG", "s1 " + temp1[0] + " s2 " + temp1[1]);
		Log.i("TAG", "s1 " + temp2[0] + " s2 " + temp2[1]);
		Log.i("TAG", "đã nối: " + str1 + " " + str2);
		return str1 + " " + str2;
	}

	public static String[] cat1chu(final String string) {
		Log.i("TAG", "đưa vào để cắt: " + string);
		String s1 = "";
		String s2 = "";
		if (string.length() == 0) {
			s1 = string;
		} else if (string.length() == 1) {
			s2 = string;
		} else if (string.length() == 2) {
			if (check1chu(string)) {
				s1 = string.substring(0, 1);
				s2 = string.substring(1, 2);
			} else {
				s2 = string;
			}
		} else if (string.length() == 3) {
			if (check2chu(string)) {
				s1 = string.substring(0, 2);
				s2 = string.substring(2, string.length());
			} else if (check1chu(string)) {
				s1 = string.substring(0, 1);
				s2 = string.substring(1, 3);
			} else {
				s2 = string;
			}
		} else if (string.length() == 4) {
			if (string.subSequence(0, 3).equals("ngh")) {
				s1 = string.substring(0, 3);
				s2 = string.substring(0, 3);
			} else if (check2chu(string)) {
				s1 = string.substring(0, 2);
				s2 = string.substring(2, 4);
			} else if (check1chu(string)) {
				s1 = string.substring(0, 1);
				s2 = string.substring(1, 4);
			} else {
				s2 = string;
			}
		} else if (string.length() > 4) {
			if (string.subSequence(0, 3).equals("ngh")) {
				s1 = string.substring(0, 3);
				s2 = string.substring(3, string.length());
			} else if (check2chu(string)) {
				s1 = string.substring(0, 2);
				s2 = string.substring(2, string.length());
			} else if (check1chu(string)) {
				s1 = string.substring(0, 1);
				s2 = string.substring(1, string.length());
			} else {
				s2 = string;
			}
		}

		String[] a = { s1, s2 };
		return a;
	}

	public static boolean check2chu(final String str) {
		String temp = str.substring(0, 2);
		for (int i = 0; i < chughep2chu.length; i++) {
			if (temp.equals(chughep2chu[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean check1chu(final String str) {
		String temp = str.substring(0, 1);
		for (int i = 0; i < nguyenam.length; i++) {
			if (temp.equals(nguyenam[i])) {
				return true;
			}
		}
		return false;
	}

	public static String daoNguocTungChuSms(String sms) {
		if (sms.contains(" ")) {
			String[] temp = sms.split(" ");
			sms = "";
			for (int i = temp.length - 1; i >= 0; i--) {
				sms = sms + temp[i];
				if (i != 0) {
					sms = sms + " ";
				}
			}
		}
		return sms;
	}

	public static String daoNguocTungTuSms(final String sms) {
		String temp = "";
		if (sms.length() > 0) {
			for (int i = sms.length() - 1; i >= 0; i--) {
				temp = temp + Character.toString(sms.charAt(i));
			}
		}
		return temp;
	}

	public static String tuDongBoDau(String sms) {
		String result = "";
		try {
			Document doc = Jsoup.connect("http://vietlabs.com/vietizer/vietizer.php")
					.data("INPUT", sms).data("radio", "5").data("submit", "Thêm dấu").post();
			// Document doc =
			// Jsoup.connect("http://vietlabs.com/vietizer.html").get();
			if (doc != null) {
				sms = doc.text();
				Log.i("TAG", sms);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sms;
	}
}

class ContentSms {
	String special;
	String body;

	public ContentSms() {
		// TODO Auto-generated constructor stub
	}
}
