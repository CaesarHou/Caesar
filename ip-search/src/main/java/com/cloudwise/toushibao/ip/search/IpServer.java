package com.cloudwise.toushibao.ip.search;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.cloudwise.toushibao.ip.search.IpIndex;
import com.cloudwise.toushibao.ip.search.PrefixIndex;

/**
 * 查询tsb-ip.dat文件中的ip信息
 * 
 * @author www.toushibao.com
 *
 */
public class IpServer {

	private static IpServer instance = null;

	private byte[] data;

	private HashMap<Integer, PrefixIndex> prefixMap;

	private long firstStartIpOffset;// 索引区第一条流位置
	// private int lastStartIpOffset;//索引区最后一条流位置
	private long prefixStartOffset;// 前缀区第一条的流位置
	private long prefixEndOffset;// 前缀区最后一条的流位置
	// private int ipCount; //ip段数量
	private long prefixCount; // 前缀数量

	public IpServer(String datFile) {
		
		Path path = Paths.get(datFile);

		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		firstStartIpOffset = BytesToLong(data[0], data[1], data[2], data[3]);
		prefixStartOffset = BytesToLong(data[8], data[9], data[10], data[11]);
		prefixEndOffset = BytesToLong(data[12], data[13], data[14], data[15]);

		// //索引区块每组12字节
		prefixCount = (prefixEndOffset - prefixStartOffset) / 9 + 1; // 前缀区块每组9字节

		// 初始化前缀对应索引区区间
		byte[] indexBuffer = Arrays.copyOfRange(data, (int) prefixStartOffset, (int) prefixEndOffset + 9);
		prefixMap = new HashMap<Integer, PrefixIndex>();
		for (int k = 0; k < prefixCount; k++) {
			int i = k * 9;
			int prefix = (int) (indexBuffer[i] & 0xFFL);

			PrefixIndex pf = new PrefixIndex();
			pf.start_index = BytesToLong(indexBuffer[i + 1], indexBuffer[i + 2], indexBuffer[i + 3],
					indexBuffer[i + 4]);
			pf.end_index = BytesToLong(indexBuffer[i + 5], indexBuffer[i + 6], indexBuffer[i + 7], indexBuffer[i + 8]);
			prefixMap.put(prefix, pf);

		}

	}

	public synchronized static IpServer getInstance(String datFile) {
		if (null == instance)
			instance = new IpServer(datFile);
		return instance;
	}

	public String Get(String ip) {
		String[] ips = ip.split("\\.");
		int prefix = Integer.valueOf(ips[0]);
		long intIP = ipToLong(ip);

		long high = 0;
		long low = 0;

		if (prefixMap.containsKey(prefix)) {
			low = prefixMap.get(prefix).start_index;
			high = prefixMap.get(prefix).end_index;

		} else {
			return "";
		}

		long my_index = low == high ? low : BinarySearch(low, high, intIP);

		IpIndex ipindex = new IpIndex();
		GetIndex((int) my_index, ipindex);

		if ((ipindex.startip <= intIP) && (ipindex.endip >= intIP)) {
			return GetLocal(ipindex.local_offset, ipindex.local_length);
		} else {
			return "";
		}

	}

	// / <summary>
	// / 二分逼近算法
	// / </summary>
	public long BinarySearch(long low, long high, long k) {
		long M = 0;
		while (low <= high) {
			long mid = (low + high) / 2;

			long endipNum = GetEndIp(mid);
			if (endipNum >= k) {
				M = mid;
				if (mid == 0) {
					break; // 防止溢出
				}
				high = mid - 1;
			} else
				low = mid + 1;
		}
		return M;
	}

	// / <summary>
	// / 在索引区解析
	// / </summary>
	// / <param name="left">ip第left个索引</param>
	private void GetIndex(int left, IpIndex ipindex) {
		int left_offset = (int) firstStartIpOffset + (left * 12);
		ipindex.startip = BytesToLong(data[left_offset], data[1 + left_offset], data[2 + left_offset],
				data[3 + left_offset]);
		ipindex.endip = BytesToLong(data[4 + left_offset], data[5 + left_offset], data[6 + left_offset],
				data[7 + left_offset]);
		ipindex.local_offset = (int) BytesToLong3(data[8 + left_offset], data[9 + left_offset], data[10 + left_offset]);
		ipindex.local_length = (int) data[11 + left_offset];
	}

	// / <summary>
	// / 只获取结束ip的数值
	// / </summary>
	// / <param name="left">索引区第left个索引</param>
	// / <returns>返回结束ip的数值</returns>
	private long GetEndIp(long left) {
		int left_offset = (int) firstStartIpOffset + (int) (left * 12);
		return BytesToLong(data[4 + left_offset], data[5 + left_offset], data[6 + left_offset], data[7 + left_offset]);

	}

	// / <summary>
	// / 返回地址信息
	// / </summary>
	// / <param name="local_offset">地址信息的流位置</param>
	// / <param name="local_length">地址信息的流长度</param>
	// / <returns></returns>
	private String GetLocal(int local_offset, int local_length) {
		byte[] bytes = new byte[local_length];
		bytes = Arrays.copyOfRange(data, local_offset, local_offset + local_length);
		return new String(bytes, Charset.forName("UTF-8"));

	}

	// / <summary>
	// / 字节转整形 大节序
	// / </summary>
	private long BytesToLong(byte a, byte b, byte c, byte d) {
		return (d & 0xFFL) | ((c << 8) & 0xFF00L) | ((b << 16) & 0xFF0000L) | ((a << 24) & 0xFF000000L);

	}

	private long BytesToLong3(byte a, byte b, byte c) {
		return (c & 0xFFL) | ((b << 8) & 0xFF00L) | ((a << 16) & 0xFF0000L);

	}

	public long ipToLong(String ip) {
		String[] quads = ip.split("\\.");
		long result = 0;
		result += Integer.parseInt(quads[3]);
		result += Long.parseLong(quads[2]) << 8L;
		result += Long.parseLong(quads[1]) << 16L;
		result += Long.parseLong(quads[0]) << 24L;
		return result;
	}

	public static void main(String[] args) {

		String datFile1 = "tsb-ip-old.dat";
		String datFile2 = "tsb-ip-new.dat";
		Random random = new Random();
//		IpServer finder = IpServer.getInstance(args[0]);
		IpServer finder = IpServer.getInstance("tsb-ip.dat");
		
//		int i = 0;
//		int j = 0;
//		long now = System.currentTimeMillis();
//		while (System.currentTimeMillis() - now < 10000) {
//			i++;
//			String ip = random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "."
//					+ random.nextInt(256);
//			String result = finder.Get(ip);
////			if (result == null || result.trim().length() == 0) {
////				j++;
////			}
////			 System.out.println(ip);
////			 System.out.println(result);
//		}
//		System.out.println("search : " + i + ", null: " + j);

		/**
		 * 233.208.186.227 233.229.249.147 246.29.36.236 245.59.215.51
		 * 236.7.134.39 237.90.84.94 254.102.170.96 215.68.9.237 215.16.36.149
		 * 246.40.73.250 215.190.113.226 229.207.47.168 234.214.122.80
		 * 215.139.8.177 242.24.109.254 229.96.198.38 237.160.255.215
		 * 229.121.94.24 233.150.211.45 236.60.161.129 251.161.84.149
		 * 237.82.214.89 242.136.141.211 230.190.122.116 230.73.66.193
		 * 237.210.35.24 234.249.21.217 245.176.17.44 241.33.247.9
		 * 238.108.108.118 237.226.50.114 232.130.206.143 230.235.188.45
		 * 230.147.41.67 231.134.135.44 234.37.159.175 238.33.77.18
		 * 251.180.209.64 238.154.250.118 242.199.161.151 238.127.78.4
		 * 231.221.161.132 241.98.246.54 234.155.1.124 231.252.254.186
		 * 234.203.20.59 241.240.105.155 243.14.17.127 243.12.121.30
		 * 229.196.227.142 243.36.223.106 226.192.73.145 103.45.194.154
		 * 215.171.139.245 246.253.33.31 253.180.112.50 239.46.221.90
		 * 235.84.92.251 230.44.124.241 252.159.136.9
		 */
		String ip = "61.237.127.34";
		String result = finder.Get(ip);
		System.out.println(ip);
		System.out.println(result);

	}
}