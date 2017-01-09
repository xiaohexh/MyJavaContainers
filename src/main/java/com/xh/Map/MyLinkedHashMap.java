package com.xh.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xh.List.MyArrayList;
import com.xh.Set.MyHashSet;
import com.xh.Set.MyLinkedHashSet;

public class MyLinkedHashMap extends Thread {
	
	/* singleton */
	private static MyLinkedHashMap instance = null;
	
	/* */
	private Map<String, MyHashMap> logFileMap = new ConcurrentHashMap<String, MyHashMap>();
	
//	private static LogFileItem logFileItem = null;
	
	public final static long WRITE_LOG_INV_TIME = MyHashSet.getConfigByLong("WRITE_LOG_INV_TIME", 1000);
	public final static long SINGLE_LOG_FILE_SIZE = MyHashSet.getConfigByLong("SINGLE_LOG_FILE_SIZE", 10 * 1024 * 1024);
	public final static long SINGLE_LOG_CACHE_SIZE = MyHashSet.getConfigByLong("SINGLE_LOG_CACHE_SIZE", 10 * 1024);
	
	private boolean bIsRun = true;
	
	private MyLinkedHashMap() {
		
	}
	
	public static MyLinkedHashMap getInstance() {
		if (instance == null) {
			instance = new MyLinkedHashMap();
			instance.setName("FLogger");
			instance.start();
		}
		
		return instance;
	}
	
	/**
	 * 添加日志
	 * @param logFileName  日志文件名称
	 * @param logMsg      日志内容
	 */
	public void addLog(String logFileName, StringBuffer logMsg) {
		MyHashMap lfi = logFileMap.get(logFileName);
		if (lfi == null) {
			synchronized (this) {
				lfi = logFileMap.get(logFileName);
				if (lfi == null) {
					lfi = new MyHashMap();
					lfi.nextWriteTime = System.currentTimeMillis() + WRITE_LOG_INV_TIME;
					lfi.logFileName = logFileName;
					logFileMap.put(logFileName, lfi);
				}
			}
		}
		
		synchronized (lfi) {
			if (lfi.currLogBuff == 'A') {
				lfi.alLogBufA.add(logMsg);
			} else {
				lfi.alLogBufB.add(logMsg);
			}
			
			lfi.currCacheSize += MyHashSet.StringToBytes(logMsg.toString()).length;
		}
	}
	
	public void run() {
		int i = 0;
		while (bIsRun) {
			try {
				flush(false);
				// reload log level
				if (i++ % 100 == 0) {
					MyArrayList.CFG_LOG_LEVEL = MyHashSet.getConfigByString("LOG_LEVEL", "2");
					i = 1;
				}
			} catch (Exception e) {
				System.out.println("start log service failed ...");
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		bIsRun = false;
		try {
			flush(true);
		} catch (Exception e) {
			System.out.println("close log service failed ...");
			e.printStackTrace();
		}
	}
	
	private void flush(boolean bIsForce) throws IOException {
		long currTime = System.currentTimeMillis();
		Iterator<String> iter = logFileMap.keySet().iterator();
		while (iter.hasNext()) {
			MyHashMap lfi = logFileMap.get(iter.next());
			if (currTime >= lfi.nextWriteTime
			|| SINGLE_LOG_CACHE_SIZE <= lfi.currCacheSize
			|| bIsForce) {
				ArrayList<StringBuffer> alWriteLog = null;
				synchronized (lfi) {
					if (lfi.currLogBuff == 'A') {
						alWriteLog = lfi.alLogBufA;
						lfi.currLogBuff = 'B';
					} else {
						alWriteLog = lfi.alLogBufB;
						lfi.currLogBuff = 'A';
					}
					lfi.currCacheSize = 0;
				}
				
				createLogFile(lfi);
				
				int iWriteSize = writeToFile(lfi.fullLogFileName, alWriteLog);
				lfi.currLogSize += iWriteSize;
			}
		}
	}
	
	private void createLogFile(MyHashMap lfi) {
		// current system date
		String currPCDate = MyLinkedHashSet.getPCDate('-');
		
		// rotate
		if (lfi.fullLogFileName != null
		&& lfi.fullLogFileName.length() > 0
		&& lfi.currLogSize >= MyLinkedHashMap.SINGLE_LOG_FILE_SIZE) {
			File oldFile = new File(lfi.fullLogFileName);
			if (oldFile.exists()) {
				String newFileName = MyArrayList.CFG_LOG_PATH + "/"
						+ lfi.lastPCDate + "/"
						+ lfi.logFileName + "_"
						+ MyLinkedHashSet.getPCDate() + "_"
						+ MyLinkedHashSet.getCurrTime() + ".log";
				File newFile = new File(newFileName);
				oldFile.renameTo(newFile);
				lfi.fullLogFileName = "";
				lfi.currLogSize = 0;
			}
		}
		
		if (lfi.fullLogFileName == null
		|| lfi.fullLogFileName.length() <= 0
		|| lfi.lastPCDate.equals(currPCDate) == false) {
			String sDir = MyArrayList.CFG_LOG_PATH + "/" + currPCDate;
			File file = new File(sDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			lfi.fullLogFileName = sDir + "/" + lfi.logFileName + ".log";
			lfi.lastPCDate = currPCDate;
			
			file = new File(lfi.fullLogFileName);
			if (file.exists()) {
				lfi.currLogSize = file.length();
			} else {
				lfi.currLogSize = 0;
			}
		}
	}
	
	private int writeToFile(String sFullFileName, ArrayList<StringBuffer> sbLogMsg) throws IOException {
		OutputStream fos = null;
		int size = 0;
		
		try {
			fos = new FileOutputStream(sFullFileName, true);
			for (StringBuffer sb : sbLogMsg) {
				byte [] tmpBytes = MyHashSet.StringToBytes(sb.toString());
				fos.write(tmpBytes);
				size += tmpBytes.length;
			}
			fos.flush();
			sbLogMsg.clear();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		
		return size;
	}
}
