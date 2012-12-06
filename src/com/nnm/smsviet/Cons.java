package com.nnm.smsviet;

public interface Cons {
	public static final String dbName = "smsdb.sqlite";
	public static final int BO_DAU_SMS = 0;
	public static final int SMS_SENT = 1;
	public static final int SMS_FAIL = 2;
	public static final int BO_DAU_TRANS = 3;
	public static final int ID_LAI = 0;
	public static final int ID_DAONGUOCTU = 1;
	public static final int ID_DAONGUOCCHU = 2;
	public static final int ID_BODAU = 3;
	public static final int ID_VIETNGUOC = 4;
	public static final int ID_RUSIFY = 5;
	public static final int ID_TEXTEMO = 6;
	public static final int ID_BINHTHUONG = 7;
	public static final int ID_SMS_COLLECTIONS = 8;
	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String BODY = "body";
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final String[] adapterEditListDialog = { "Lái từ", "�?ảo ngược từng chữ",
			"�?ảo ngược từng từ", "Tự động b�? dấu", "Viết ngược (ICS)", "Rusify (không dấu only)",
			"Text Emoticons", "Bình thư�?ng" };
	public static final String[] emo = { "≥^.^≤", "≤^.^≥", "ж)", ":*)", "=^.^=", "^.^)", "^.^",
			"^o^",

			"٩(�?̮̮̃•)۶", "٩(̃-̮̮̃-)۶", "(-̮̮̃•)۶", "٩(×̯×)۶", "٩(•̮̮̃-̃)۶", "<(^,^)>", "(~_^)",
			"≥,≤", "<3", "</3", "@>--;--", "@};---", "웃�?�유", "<('o'<)", "^( '-' )^", "(>‘o’)>",
			"(^3^)", "(>^3^)>", "(¬.¬) ", "┌∩�?(-.-)┌∩�?", "┌∩�?" };
}
