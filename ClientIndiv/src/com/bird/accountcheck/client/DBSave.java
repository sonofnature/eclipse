package com.bird.accountcheck.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.bird.util.Tool;

/**
 * �����������
 * 
 * @author bird
 *
 */
class DBSave {
	private boolean isconn;
	private Client owner;
	private Connection conn;
	private PreparedStatement ps;
	private String[] value;// ��¼���ݿ�ÿ���ֶε�ֵ

	public DBSave(Client owner) {
		this.owner = owner;
		isconn = false;
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
			String url = "jdbc:db2://32.234.32.139:50000/EDP";
			String user = "dw";
			String pswd = "dw";
			conn = DriverManager.getConnection(url, user, pswd);
			isconn = true;
			String sql = "MERGE INTO WQK_GSXX T1"
					+ " USING (SELECT ? A,? B,? C,? D,? E,? F,? G,? H,? I,? J,? K,? L,? M,? N,? O,? P FROM DUAL)T2"
					+ " ON T1.ID=T2.B WHEN MATCHED THEN"
					+ " UPDATE SET (NAME,ID,ISEXCP,STATE,TYPE,MAINPERSON,REGMONEY,REGDT,STARTDT,ENDDT,REGORG,CHECKDT,ADDRESS,RANGE,SOURCE,UPDATEDT)"
					+ "          =(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)"
					+ " WHEN NOT MATCHED THEN INSERT VALUES(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)";
			ps = conn.prepareStatement(sql);
		} catch (Exception e) {
			isconn = false;
		}
	}

	/**
	 * ���ӷ��������ص�һ����¼�������
	 * 
	 * @param record
	 *            ���������صĲ�ѯ���
	 */
	public void save(String record) {
		if (!isconn)
			return;
		// ���������ݿ��ֶ�ֵ
		value = parser(record);
		// �������ݿ�
		try {
			ps.clearParameters();
			for (int i = 0; i < value.length; i++) {
				if (i == 6) {
					if (value[i] == null) {
						ps.setNull(i + 1, java.sql.Types.DECIMAL);
					} else {
						ps.setDouble(i + 1, Double.valueOf(value[i]));
					}
				} else {
					if (value[i] == null) {
						ps.setNull(i + 1, java.sql.Types.VARCHAR);
					} else {
						ps.setString(i + 1, value[i]);
					}
				}
			}
			ps.execute();
			owner.log("�������ɹ�");
		} catch (SQLException | NumberFormatException e) {
			Tool.printErr(e, "���ʧ�ܣ�" + record);
		}
	}

	public void exit() {
		Tool.runNoException(() -> ps.close());
		Tool.runNoException(() -> conn.close());
	}

	/**
	 * ���ݷ��������ص�����-ֵԪ������Ӧ�����ݿ�����ֶΣ�������Ӧ��ֵ
	 * 
	 * @param record
	 *            ���������صĽ��
	 * @return ��Ӧ�����ݿ��ֵ��ֵ����
	 */
	public String[] parser(String record) {
		String[] value = new String[16];
		for (int i = 0; i < value.length; i++) {
			value[i] = null;
		}

		StringTokenizer st = new StringTokenizer(record, "|", true);
		while (st.hasMoreTokens()) {
			String col = st.nextToken();// �ֶ���
			st.nextToken();
			String v = st.nextToken();
			if (v.equals("|"))// �ֶ�ֵΪ��
				v = "";
			else
				st.nextToken();

			if (col.contains("��")) {// ��ҵ����
				value[0] = v;
			} else if (col.contains("��") || col.contains("��")) {// ͳһ���ô���
				value[1] = v;
			} else if (col.contains("�쳣")) {// �Ƿ��쳣
				value[2] = v;
			} else if (col.contains("״̬")) {// ״̬
				value[3] = v;
			} else if (col.contains("����")) {// ��ҵ����
				value[4] = v;
			} else if (col.contains("��") || col.contains("��")) {// ���˴���
				value[5] = v;
			} else if (col.contains("�ʱ�") || col.contains("��") || col.contains("��")) {// ע���ʱ�
				value[6] = formatNum(v);
			} else if (col.contains("��������") || col.contains("ע������")) {// ��������
				value[7] = formatDate(v);
			} else if (col.contains("��")) {// ��Ӫ������
				value[8] = formatDate(v);
			} else if (col.contains("��")) {// ��Ӫ������
				value[9] = formatDate(v);
			} else if (col.contains("����")) {// �Ǽǻ���
				value[10] = v;
			} else if (col.contains("��׼����")) {// ��׼����
				value[11] = formatDate(v);
			} else if (col.contains("ס��") || col.contains("����")) {// ��Ӫ����
				value[12] = v;
			} else if (col.contains("��Χ")) {// Ӫҵ��Χ
				value[13] = v;
			} else if (col.contains("��Դ")) {// ������Դ
				if (v.equals("��ʾϵͳ"))
					value[14] = "G";
			} else if (col.contains("��������")) {// ��������
				value[15] = v;
			}
		}
		return value;
	}

	/**
	 * ����ʽΪ'2016��6��12��'��ʽ��Ϊ'20160612'��ʽ
	 * 
	 * @param date
	 * @return ��ʽ����ģ��������δ�շ���99991231����������ʽ���Է���00000000
	 */
	private String formatDate(String date) {
		if (date.isEmpty())
			return "99991231";
		try {
			String year = date.substring(0, date.indexOf('��')).trim();
			String month = date.substring(date.indexOf('��') + 1, date.indexOf('��')).trim();
			String day = date.substring(date.indexOf('��') + 1, date.indexOf('��')).trim();
			if (year.length() == 2)
				year = "20" + year;
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1)
				day = "0" + day;
			return year + month + day;
		} catch (Exception e) {
			return "00000000";
		}
	}

	/**
	 * ��"50.000�������"��ʽ��Ϊ"500000.00"
	 * 
	 * @param num
	 * @return ��ʽ����ģ���������ʽ���󷵻�0.00
	 */
	private String formatNum(String num) {
		try {
			String s = num.substring(0, num.indexOf('��')).trim();
			double n = Double.valueOf(s);
			n = n * 10000.00;
			return String.format("%.2f", n);
		} catch (Exception e) {
			return "0.00";
		}
	}
}
