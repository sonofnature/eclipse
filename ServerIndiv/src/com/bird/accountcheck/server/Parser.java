package com.bird.accountcheck.server;

import java.io.File;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bird.util.Tool;

class Parser {

	/**
	 * 解析html文件，要求html文件名规则是如"江苏长江公司@20170101023322.html"格式，因为需要从文件名中提取查询关键字和时间信息。
	 * 返回属性值对，开头如"查询关键字|江苏长江公司|返回结果|normal|"
	 * 
	 * @param htmlpath
	 *            html路径
	 * @return
	 */
	public String parse(String htmlpath) {
		String result = "";
		String name = htmlpath.substring(htmlpath.lastIndexOf('/') + 1);
		String keyword = name.substring(0, name.indexOf('@'));
		String date = name.substring(name.indexOf('@') + 1, name.indexOf('@') + 9);
		result = "查询关键字|" + keyword + "|";

		File html = new File(htmlpath);
		if (!html.exists())
			return result + "返回结果|null|";// 文件不存在是因为关键字不合要求，返回"null"
		Document doc;
		try {
			doc = Jsoup.parse(html, "UTF-8");
		} catch (Exception e) {
			Tool.printErr(e, "解析网页失败");
			return result + "返回结果|error|";
		}
		// 判断网页类型
		Elements es = doc.getElementsMatchingOwnText("营业执照信息");
		if (es == null || es.size() == 0) {// 非正常网页
			es = doc.getElementsMatchingOwnText("业务咨询电话");
			if (es != null && es.size() > 0)
				return result + "返回结果|null|";
			else
				return result + "返回结果|error|";
		} else {
			result += "返回结果|normal|";
		}

		// 判读是否经营异常
		es = doc.getElementsByAttributeValue("class", "yichangMsg");
		if (es.size() != 0)
			result += "是否异常|是|";
		else
			result += "是否异常|否|";
		// 添加营业执照各项属性值组
		es = doc.getElementsMatchingOwnText("营业执照信息");
		Element e = es.first().nextElementSibling();
		es = e.getElementsByTag("dl");
		for (int i = 0; i < es.size(); i++) {
			e = es.get(i);
			result += e.getElementsByTag("dt").text().replace("：", "").trim() + "|";
			result += e.getElementsByTag("dd").text().trim() + "|";
		}

		result += "数据来源|公示系统|";
		result += "数据日期|" + date + "|";
		// 返回结果
		return result;
	}
}
