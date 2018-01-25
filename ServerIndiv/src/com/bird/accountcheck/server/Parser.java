package com.bird.accountcheck.server;

import java.io.File;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bird.util.Tool;

class Parser {

	/**
	 * ����html�ļ���Ҫ��html�ļ�����������"���ճ�����˾@20170101023322.html"��ʽ����Ϊ��Ҫ���ļ�������ȡ��ѯ�ؼ��ֺ�ʱ����Ϣ��
	 * ��������ֵ�ԣ���ͷ��"��ѯ�ؼ���|���ճ�����˾|���ؽ��|normal|"
	 * 
	 * @param htmlpath
	 *            html·��
	 * @return
	 */
	public String parse(String htmlpath) {
		String result = "";
		String name = htmlpath.substring(htmlpath.lastIndexOf('/') + 1);
		String keyword = name.substring(0, name.indexOf('@'));
		String date = name.substring(name.indexOf('@') + 1, name.indexOf('@') + 9);
		result = "��ѯ�ؼ���|" + keyword + "|";

		File html = new File(htmlpath);
		if (!html.exists())
			return result + "���ؽ��|null|";// �ļ�����������Ϊ�ؼ��ֲ���Ҫ�󣬷���"null"
		Document doc;
		try {
			doc = Jsoup.parse(html, "UTF-8");
		} catch (Exception e) {
			Tool.printErr(e, "������ҳʧ��");
			return result + "���ؽ��|error|";
		}
		// �ж���ҳ����
		Elements es = doc.getElementsMatchingOwnText("Ӫҵִ����Ϣ");
		if (es == null || es.size() == 0) {// ��������ҳ
			es = doc.getElementsMatchingOwnText("ҵ����ѯ�绰");
			if (es != null && es.size() > 0)
				return result + "���ؽ��|null|";
			else
				return result + "���ؽ��|error|";
		} else {
			result += "���ؽ��|normal|";
		}

		// �ж��Ƿ�Ӫ�쳣
		es = doc.getElementsByAttributeValue("class", "yichangMsg");
		if (es.size() != 0)
			result += "�Ƿ��쳣|��|";
		else
			result += "�Ƿ��쳣|��|";
		// ���Ӫҵִ�ո�������ֵ��
		es = doc.getElementsMatchingOwnText("Ӫҵִ����Ϣ");
		Element e = es.first().nextElementSibling();
		es = e.getElementsByTag("dl");
		for (int i = 0; i < es.size(); i++) {
			e = es.get(i);
			result += e.getElementsByTag("dt").text().replace("��", "").trim() + "|";
			result += e.getElementsByTag("dd").text().trim() + "|";
		}

		result += "������Դ|��ʾϵͳ|";
		result += "��������|" + date + "|";
		// ���ؽ��
		return result;
	}
}
