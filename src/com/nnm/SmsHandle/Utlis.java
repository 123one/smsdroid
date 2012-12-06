package com.nnm.SmsHandle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utlis {
	public static final Character[] flipchar = { 'd', 'd' };
	public static final String flipString = "015�?ᔭ29Ɫ86˙';:¡¿@#$%&)(][‾+-*/=><�?qɔp�?ɟƃɥıɾʞlɯuodbɹsʇnʌ�?xʎzáàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêế�?ểễệíìỉĩịóò�?õ�?ôốồổỗộơớ�?ởỡợúùủũụưứừửữựýỳỷỹỵ∀qϽpƎℲƃHIɾʞLWNOԀbᴚS⊥∩ΛMXʎZ�?ÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬ�?ÉÈẺẼẸÊẾỀỂỄỆ�?ÌỈĨỊÓÒỎÕỌÔ�?ỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰ�?ỲỶỸỴ~^}{`’‘�?“, ";
	public static final String stMap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.,?!";
	public static final String stMapFlip = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.,?!";
	public static final String stFlip = "∀qɔp3ɟ6HIſʞlWNOdQɹS┴∩ʌMXʎZɐqɔpeɟ6ɥıɾʞlɯuodbɹsʇnʌʍxʎz˙'¿¡,,";
	public static final String stWide = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＵＶＷＸＹＺ�?ｂｃｄｅｆｇｈｉｊｋｌ�?ｎ�?�?ｑｒｓｔｕｖｗｘｙｚ.,?!";
	public static final String stRusify = "ABCD�?FGHЇJКLMИОPФЯ$ЦVЩЖУZaЬcdёfgнїjкlмйоpqґ$тцvшжуz.,?!";
	public static final String url = "http://fsymbols.com/generators/rusify/";
	public static final String enCool = "Åℬℭ�?∃ℱḠ♓�?ⒿḰℒℳℕϴℙℚℛϟṲⅤШẌẎ☡αß¢∂εƒℊ♄ḯ�?кʟмηø℘ⓠґ﹩⊥ü♥ẘ✖ƴℨ▪,�?�‼";
	public static final String stWave = "ABCDEFGHIJKLMNOPQRSUVWXYZᗩᕊᑕ�?ᙓℱ�?ᖺᓮᒎḰᒪᙢﬡ�?ᖰᖳᖇᔕ♈⋒�?�ᗯჯᎩᔓ";

	public static String getCurrentDataTime() {
		long now = System.currentTimeMillis();
		DateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		Log.i("time", "time" + formatter.format(calendar.getTime()));

		return formatter.format(calendar.getTime());
	}

	static String E = "Ǝ";
	static String l = "˥";
	static String e = "ǝ";
	static String a = "∀";
	static String t = "⊥";
	static String s = "(>‿♥)   ✿◕ ‿ ◕✿   �?�◕ ‿ ◕�?�   �??◕ ‿ ◕�??   ✾◕ ‿ ◕✾   (◡‿◡✿)   (✿◠‿◠)   ≧�?�‿�?�≦   -=#:-)   :-Ø   =ϕ   :-ϕ   :-Φ   (�?�◕‿◕)�?� ♥ ";
	static String g = "ƃ";

	public static String flipString(final String string) {
		String result = "";
		for (int i = 0; i < string.length(); i++) {
			Character c = string.charAt(i);
			if (stMapFlip.indexOf(c.toString()) != -1) {
				Character rep = stFlip.charAt(stMapFlip.indexOf(c.toString()));
				result = result + rep.toString();
				Log.i("TAG", c.toString() + " có");
			} else {
				result = result + c.toString();
				Log.i("TAG", c.toString() + " không");
			}
		}
		// result = result.replace("T", t);
		// result = result.replace("e", e);
		result = HandleSms.daoNguocTungTuSms(result);

		return result;
	}

	public static String RusifyString(final String string) {
		String result = "";
		for (int i = 0; i < string.length(); i++) {
			Character c = string.charAt(i);
			if (stMap.indexOf(c.toString()) != -1) {
				Character rep = stRusify.charAt(stMap.indexOf(c.toString()));
				result = result + rep.toString();
				Log.i("TAG", c.toString() + " có");
			} else {
				result = result + c.toString();
				Log.i("TAG", c.toString() + " không");
			}
		}
		return result;
	}

	public static boolean checkNetworkStatus(final Context context) {
		final ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}

	public static int getAPILevel() {
		return Integer.valueOf(android.os.Build.VERSION.SDK);
	}

	private static final String KEY_STRING = "yourkey";

	public static byte[] Encrypt(final byte[] encrypt) {
		if (encrypt == null) {
			return null;
		}
		try {
			// Generate the secret key specs.
			Key key = new SecretKeySpec(KEY_STRING.getBytes(), "DES");
			// Generate the secret key specs.
			Cipher cipher = Cipher.getInstance("DES");

			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] decrypted = cipher.doFinal(encrypt);
			return decrypted;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] Decrypt(final byte[] encrypt) {
		if (encrypt == null) {
			return null;
		}
		try {
			// Generate the secret key specs.
			Key key = new SecretKeySpec(KEY_STRING.getBytes(), "DES");
			// Generate the secret key specs.
			Cipher cipher = Cipher.getInstance("DES");

			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decrypted = cipher.doFinal(encrypt);
			return decrypted;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void downLoadFile(final String filePath, final String url) {
		try {
			InputStream input = new URL(url).openStream();
			File f = new File(filePath);
			// if (f.exists()){
			// return;
			// }
			OutputStream out = new FileOutputStream(f);
			byte buf[] = new byte[1024];
			int len;
			while ((len = input.read(buf)) > 0) {
				// Log.i("TAG","download " + len);
				out.write(buf, 0, len);
			}
			out.close();
			input.close();

		} catch (Exception e) {
			// Toast.makeText(this,
			// "Có lỗi xảy ra trong quá trình cập nhật, \n Tắt và mở lại chương trình để thực hiện lại!",
			// Toast.LENGTH_LONG).show();
		}
	}

	public static void showDialogSpen(final Context context, final String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						System.exit(0);
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static String convertStreamToString(final InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		return sb.toString();
	}
}
