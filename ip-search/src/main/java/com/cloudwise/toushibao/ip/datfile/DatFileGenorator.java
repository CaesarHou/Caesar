package com.cloudwise.toushibao.ip.datfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * ip元数据转dat数据文件
 * @author www.toushibao.com
 *
 */
public class DatFileGenorator {
//	private static final String ipRawFile = "taiwan.txt";
	private static final String ipRawFile = "ip_location_check2.txt";
	private static final String ipDatFile = "tw-tsb-ip.dat";
	public static void main(String[] args) throws Exception {
		IpInfo ipinfo = new IpInfo();
		parseLocalFile(ipRawFile, ipinfo);

		File filename = new File(ipDatFile);
		if (filename.exists()) {
			filename.delete();
		}
		RandomAccessFile mm = null;
		try {
			mm = new RandomAccessFile(filename, "rw");
			//文件头处理
			System.out.println("文件头开始索引：" + mm.getFilePointer());
			//文件头 占位，最后会赋值
			int a = 0x00000000;
			mm.writeInt(a);
			mm.writeInt(a);
			mm.writeInt(a);
			mm.writeInt(a);
			long fileHeadLeng = mm.length();
			System.out.println("文件头长度：" + fileHeadLeng);
			System.out.println("文件头结束处索引：" + (mm.getFilePointer() - 1));

			//内容区处理
			Map<String, IpLocalOffsetInfo> localOffset = new HashMap<String, IpLocalOffsetInfo>();
			long contentBeginIndex = mm.getFilePointer();
			System.out.println("内容区开始处索引：" + contentBeginIndex);
			Set<String> ipLocals = ipinfo.getLocals();
			System.out.println("ip地域数：" + ipLocals.size());
			for(String ipLocal : ipLocals){
				byte[] iplocalBytes = ipLocal.getBytes(Charset.forName("UTF-8"));
				int byteleng = iplocalBytes.length;
				long offset = mm.getFilePointer();
				IpLocalOffsetInfo iloi = new IpLocalOffsetInfo();
				iloi.setLength(byteleng);
				iloi.setOffset(offset);
				localOffset.put(ipLocal, iloi);
				mm.write(iplocalBytes);
			}
			System.out.println("内容区结束处索引：" + (mm.getFilePointer() - 1));

			//索引区处理
			Map<String, PreOffset> preOffsetMap = new TreeMap<String, PreOffset>();
			long indexBeginIndex = mm.getFilePointer();
			long indexEndIndex = mm.getFilePointer();
			System.out.println("索引区开始处索引：" + indexBeginIndex);
			Set<Entry<String, String>> entries = ipinfo.getIpLocalMap().entrySet();
			System.out.println("ip条数：" + entries.size());
			int i = 0;
			for(Entry<String, String> entry : entries){
				String key = entry.getKey();
				String value = entry.getValue();
				String[] ips = key.split("\\-");
				long ipStart = ipToLong(ips[0]);
				long ipEnd = ipToLong(ips[1]);
				
				String pre = ips[0].split("\\.")[0];
				PreOffset preOffset = preOffsetMap.get(pre);
				if(preOffset == null){
					preOffset = new PreOffset();
					preOffsetMap.put(pre, preOffset);
					preOffset.setStart(i);
				}
				preOffset.setEnd(i);
				indexEndIndex = mm.getFilePointer();
				
				long offset = localOffset.get(value).getOffset();
				int length = localOffset.get(value).getLength();
				mm.writeInt((int)ipStart);
				mm.writeInt((int)ipEnd);
				
				int tmp = (int)offset;
				tmp <<= 8;
				mm.writeInt(tmp);
				mm.seek(mm.getFilePointer()-1);
				mm.writeByte(length);
				
				i++;
			}
			System.out.println("索引区结束处索引：" + (mm.getFilePointer() - 1));
			
			//前缀区处理
			long preBeginIndex = mm.getFilePointer();
			long preEndIndex = mm.getFilePointer();
			System.out.println("前缀区开始处索引：" + preBeginIndex);
			Set<Entry<String, PreOffset>> preOffsetEntry = preOffsetMap.entrySet();
			for(Entry<String, PreOffset> entry : preOffsetEntry){
				String key = entry.getKey();
				PreOffset value = entry.getValue();
				preEndIndex = mm.getFilePointer();
				
				mm.writeByte(Integer.parseInt(key));
				mm.writeInt((int)value.getStart());
				mm.writeInt((int)value.getEnd());
			}
			System.out.println("前缀区结束处索引：" + (mm.getFilePointer() - 1));


			//文件头 赋值
			mm.seek(0);
			mm.writeInt((int)indexBeginIndex);
			mm.writeInt((int)indexEndIndex);
			mm.writeInt((int)preBeginIndex);
			mm.writeInt((int)preEndIndex);
			
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

	public static void parseLocalFile(String filePath, IpInfo ipinfo) {
		try {
			String encoding = "UTF-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int i = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					i++;
					String[] items = lineTxt.split("\\,", -2);

					String ipStart = items[1];
					String ipEnd = items[3];
					String country = items[5];
					String province = items[6];
					String city = items[7];
					String oper = items[9];
					
					String localInfo = country + "|" + province + "|" + city + "|" + oper + "|";
					ipinfo.getLocals().add(localInfo);
//					if(ipinfo.getIpLocalMap().containsKey(ipStart + "-" + ipEnd)){
//						if(!ipinfo.getIpLocalMap().get(ipStart + "-" + ipEnd).equals(localInfo)){
//							System.out.println(ipStart + "-" + ipEnd + "|" + ipinfo.getIpLocalMap().get(ipStart + "-" + ipEnd) + "==>" + localInfo);
//						}
//					}
					ipinfo.getIpLocalMap().put(ipStart + "-" + ipEnd, localInfo);
				}
				System.out.println("line==>" + i);
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
	}
	
	public static long ipToLong(String ip) {
		String[] quads = ip.split("\\.");
		long result = 0;
		result += Integer.parseInt(quads[3]);
		result += Long.parseLong(quads[2]) << 8L;
		result += Long.parseLong(quads[1]) << 16L;
		result += Long.parseLong(quads[0]) << 24L;
		return result;
	}
}
