package com.bird.accountcheck.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.bird.util.Tool;

/**
 * 负责将数据入库
 * 
 * @author bird
 *
 */
class DBSave {
	private boolean isconn;
	private Client owner;
	private Connection conn;
	private PreparedStatement ps;
	private String[] value;// 记录数据库每个字段的值

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
	 * 将从服务器返回的一条记录数据入库
	 * 
	 * @param record
	 *            服务器返回的查询结果
	 */
	public void save(String record) {
		if (!isconn)
			return;
		// 解析出数据库字段值
		value = parser(record);
		// 更新数据库
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
			owner.log("数据入库成功");
		} catch (SQLException | NumberFormatException e) {
			Tool.printErr(e, "入库失败：" + record);
		}
	}

	public void exit() {
		Tool.runNoException(() -> ps.close());
		Tool.runNoException(() -> conn.close());
	}

	/**
	 * 根据服务器返回的属性-值元组结果对应到数据库各个字段，赋上相应的值
	 * 
	 * @param record
	 *            服务器返回的结果
	 * @return 对应于数据库字典的值数组
	 */
	public String[] parser(String record) {
		String[] value = new String[16];
		for (int i = 0; i < value.length; i++) {
			value[i] = null;
		}

		StringTokenizer st = new StringTokenizer(record, "|", true);
		while (st.hasMoreTokens()) {
			String col = st.nextToken();// 字段名
			st.nextToken();
			String v = st.nextToken();
			if (v.equals("|"))// 字段值为空
				v = "";
			else
				st.nextToken();

			if (col.contains("名")) {// 企业名称
				value[0] = v;
			} else if (col.contains("码") || col.contains("号")) {// 统一信用代码
				value[1] = v;
			} else if (col.contains("异常")) {// 是否异常
				value[2] = v;
			} else if (col.contains("状态")) {// 状态
				value[3] = v;
			} else if (col.contains("类型")) {// 企业类型
				value[4] = v;
			} else if (col.contains("人") || col.contains("者")) {// 法人代表
				value[5] = v;
			} else if (col.contains("资本") || col.contains("金") || col.contains("额")) {// 注册资本
				value[6] = formatNum(v);
			} else if (col.contains("成立日期") || col.contains("注册日期")) {// 成立日期
				value[7] = formatDate(v);
			} else if (col.contains("自")) {// 经营期限自
				value[8] = formatDate(v);
			} else if (col.contains("至")) {// 经营期限至
				value[9] = formatDate(v);
			} else if (col.contains("机关")) {// 登记机关
				value[10] = v;
			} else if (col.contains("核准日期")) {// 核准日期
				value[11] = formatDate(v);
			} else if (col.contains("住所") || col.contains("场所")) {// 经营场所
				value[12] = v;
			} else if (col.contains("范围")) {// 营业范围
				value[13] = v;
			} else if (col.contains("来源")) {// 数据来源
				if (v.equals("公示系统"))
					value[14] = "G";
			} else if (col.contains("数据日期")) {// 数据日期
				value[15] = v;
			}
		}
		return value;
	}

	/**
	 * 将格式为'2016年6月12日'格式化为'20160612'格式
	 * 
	 * @param date
	 * @return 格式化后的，如果输入未空返回99991231，如果输入格式不对返回00000000
	 */
	private String formatDate(String date) {
		if (date.isEmpty())
			return "99991231";
		try {
			String year = date.substring(0, date.indexOf('年')).trim();
			String month = date.substring(date.indexOf('年') + 1, date.indexOf('月')).trim();
			String day = date.substring(date.indexOf('月') + 1, date.indexOf('日')).trim();
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
	 * 将"50.000万人民币"格式化为"500000.00"
	 * 
	 * @param num
	 * @return 格式化后的，如果输入格式有误返回0.00
	 */
	private String formatNum(String num) {
		try {
			String s = num.substring(0, num.indexOf('万')).trim();
			double n = Double.valueOf(s);
			n = n * 10000.00;
			return String.format("%.2f", n);
		} catch (Exception e) {
			return "0.00";
		}
	}
}
