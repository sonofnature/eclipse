package main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MyZip {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("本程序将压缩file1/file2/file3/file4/ 下所有文件（不含文件夹），压缩文件与file2同名");
		System.out.println("请输入包含每天下发数据的文件夹路径（日期文件夹的父文件）--file1：");
		File f1 = new File(in.readLine());
		System.out.println("请输入最终需要压缩的文件的父文件夹名 --file4：");
		String foldname = in.readLine();
		File[] fs1 = f1.listFiles();
		if (fs1 == null)
			return;
		for (int i = 0; i < fs1.length; i++) {
			File f2 = fs1[i];// 日期文件夹
			String name = f2.getName();
			Pattern pattern = Pattern.compile("[0-9]*");
			Matcher isNum = pattern.matcher(name);
			if (name.length() != 8 || !isNum.matches())
				continue;
			File[] fs2 = f2.listFiles();
			if (fs2 == null)
				continue;
			byte[] buf = new byte[1024];
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(name + ".zip"));
			for (int j = 0; j < fs2.length; j++) {
				File f3 = fs2[j];
				File[] fs3 = f3.listFiles();
				if (fs3 == null)
					continue;
				for (int k = 0; k < fs3.length; k++) {
					File f4 = fs3[k];
					if (!f4.getName().equals(foldname))
						continue;
					File[] fs4 = f4.listFiles();
					if (fs4 == null)
						continue;
					for (int l = 0; l < fs4.length; l++) {
						File f5 = fs4[l];
						if (f5.isDirectory())
							continue;
						zout.putNextEntry(new ZipEntry(f5.getName()));
						BufferedInputStream fi = new BufferedInputStream(new FileInputStream(f5));
						while (true) {
							int n = fi.read(buf, 0, buf.length);
							if (n == -1)
								break;
							zout.write(buf, 0, n);
						}
						fi.close();
					}
				}
			}
			zout.close();
			System.out.println("压缩完成：" + name);
		}
	}

}
