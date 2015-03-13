package archer.util;

public class StringUtil {

	// ��̬�滮��
	public static int EditDistanceDP(String source, String target) {
		char[] s = source.toCharArray();
		char[] t = target.toCharArray();
		int slen = source.length();
		int tlen = target.length();
		int d[][] = new int[slen + 1][tlen + 1];
		for (int i = 0; i <= slen; i++) {
			d[i][0] = i;
		}
		for (int i = 0; i <= tlen; i++) {
			d[0][i] = i;
		}
		for (int i = 1; i <= slen; i++) {
			for (int j = 1; j <= tlen; j++) {
				if (s[i - 1] == t[j - 1]) {
					d[i][j] = d[i - 1][j - 1];
				} else {
					int insert = d[i][j - 1] + 1;
					int del = d[i - 1][j] + 1;
					int update = d[i - 1][j - 1] + 1;
					d[i][j] = Math.min(insert, del) > Math.min(del, update) ? Math
							.min(del, update) : Math.min(insert, del);
				}
			}
		}
		return d[slen][tlen];
	}

	// �ݹ�ʵ�� --- ��ٷ���ö�ٷ���
	public static int EditDistanceENUM(String source, String target) {
		if (target.length() != 0 && source.length() == 0) {
			return EditDistanceENUM(source, target.substring(1)) + 1;
		} else if (target.length() == 0 && source.length() != 0) {
			return EditDistanceENUM(source.substring(1), target) + 1;
		} else if (target.length() != 0 && source.length() != 0) {
			// ��Դ�ַ��һ��ֵ��Ŀ���ַ��һ��ֵ��ͬʱ
			if (source.charAt(0) == target.charAt(0)) {
				return EditDistanceENUM(source.substring(1),
						target.substring(1));
			} else {
				int insert = EditDistanceENUM(source.substring(1), target) + 1;
				int del = EditDistanceENUM(source, target.substring(1)) + 1;
				int update = EditDistanceENUM(source.substring(1),
						target.substring(1)) + 1;
				return Math.min(insert, del) > Math.min(del, update) ? Math
						.min(del, update) : Math.min(insert, del);
			}
		} else {
			return 0;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
