package com.cloudwise.toushibao.test;

import java.io.File;
import java.io.RandomAccessFile;

public class ByteFileTest {

	private static RandomAccessFile raf;

	public static void main(String[] args) throws Exception {
		File filename = new File("xxxx.dat");
		if(filename.exists()){
			filename.delete();
		}
		RandomAccessFile mm = null;
		try {
			mm = new RandomAccessFile(filename, "rw");
			System.out.println("文件头开始索引：" + mm.getFilePointer());
			int a = 0x00000000;
			mm.writeInt(a);
			mm.writeInt(a);
			mm.writeInt(a);
			mm.writeInt(a);
			long fileHeadLeng = mm.length();
			System.out.println("文件头长度：" + fileHeadLeng);
			System.out.println("文件头结束处索引：" + (mm.getFilePointer()-1));
			System.out.println("内容区开始处索引：" + mm.getFilePointer());
			String local = "中国|北京|北京市|中国移动｜中国|河南|安阳|中国联通";
			byte[] localByte = local.getBytes();
			long localLength = localByte.length;
			System.out.println("内容区长度：" + localLength);
			mm.write(localByte);
			System.out.println("内容区结束处索引：" + (mm.getFilePointer()-1));
			System.out.println("索引区开始处索引：" + (mm.getFilePointer()));
			mm.writeInt(a);
			mm.writeInt(a);
			mm.writeInt(a);
			System.out.println("索引区结束处索引：" + (mm.getFilePointer()-1));
			System.out.println("前缀区开始处索引：" + (mm.getFilePointer()));
			mm.writeByte(0);
			mm.writeInt(a);
			mm.writeInt(a);
			System.out.println("前缀区结束处索引：" + (mm.getFilePointer()-1));
			
			//文件区 赋值
			mm.seek(0);
			mm.writeInt(88);
			mm.writeInt(88);
			mm.writeInt(100);
			mm.writeInt(100);
			
			//1852730990
			//索引区 赋值
			mm.seek(88);
			mm.writeInt(1852730990);
			mm.writeInt(1852730990);
			int tmp = 16;
			tmp <<= 8;
			mm.writeInt(tmp);
			mm.seek(mm.getFilePointer()-1);
			mm.writeByte(36);
			
			System.out.println("===>" + ("中国|北京|北京市|中国移动".getBytes().length));
			//前缀区赋值
			mm.seek(100);
			mm.writeByte(110);
			mm.writeInt(0);
			mm.writeInt(0);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (mm != null) {
				try {
					mm.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

	}
}
