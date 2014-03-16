package com.yang.adb;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

public class DeviceConnection {
	private String hostName = "192.168.2.134";
	private int port = 5555;
	
	private String lpath = "c:\\es.apk";
	private String rpath = "/data/local/tmp/es.apk";
	
	private int maxPackageLength = 0x1000;
	private int maxDataLength = 0x10000;
	
	
	AdbStream shellStream;
	AdbConnection connection;
	
//	public DeviceConnection(String hostName, int port){
//		this.hostName = hostName;
//		this.port = port;
//	}
	
	public void startConnect(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Socket socket = new Socket();
				InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
				AdbCrypto adbCrypto = new AdbCrypto();
				
				try {
					socket.connect(socketAddress);
					connection = AdbConnection.create(socket, adbCrypto);
					connection.connect();
					String cmd = "sync:";//"shell:am start -n com.moretv.android/.StartActivity";
					shellStream = connection.open(cmd);
					
					ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
					buffer.put("SEND".getBytes("UTF-8"));
					int len = rpath.length()+6;
					buffer.putInt(len);
					shellStream.write(buffer.array());
					
					if(sendFile(rpath) > -1){
						String install = "shell:pm install /data/local/tmp/es.apk";
						shellStream = connection.open(install);
						
						String startApp = "shell:pm install /data/local/tmp/es.apk";
						shellStream = connection.open(startApp);
					}
					
					
				} catch (IOException e) {
					e.printStackTrace();
					AdbUtils.safeClose(shellStream);
					AdbUtils.safeClose(connection);
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
		}).start();
		
	}
	
	private int sendFile(String rpath) throws IOException, InterruptedException{
		String pathMode = rpath + ",33206";
		byte[] fileBuffer = new byte[maxDataLength]; 
		
		int readLen = 0;
		@SuppressWarnings("resource")
		FileInputStream fs = new FileInputStream(lpath);
		int readRealLen = 0;
		int bufferSize = maxPackageLength;
		boolean isFirtPackage = true;
		
		while((readRealLen = fs.read(fileBuffer, readLen, maxDataLength)) != -1){
			int dataLen = 0;
			if(readRealLen < maxDataLength){
				dataLen = readRealLen;
			}
			else{
				dataLen = 0x10000;
			}
			int bufferOffset = 0;
			boolean isFirstThisSection = true;
			
			while(dataLen - bufferOffset > 0){
				ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
				int lenUsd = 0;
				int realDatalen = 0;
				
				if(isFirtPackage){
					isFirtPackage = false;
					buffer.put(pathMode.getBytes("UTF-8"));
					lenUsd = pathMode.length();
				}
				
				if(isFirstThisSection){
					isFirstThisSection = false;
					buffer.put("DATA".getBytes("UTF-8"));
					buffer.putInt(dataLen);
					lenUsd += 8;
				}
				
				int leftLength = maxPackageLength - lenUsd;
				
				int realUsedLen = leftLength > readRealLen - bufferOffset ? readRealLen - bufferOffset :leftLength;
				buffer.put(fileBuffer, bufferOffset, realUsedLen);
				bufferOffset += realUsedLen;
				
				byte[] desBuffer = new byte[lenUsd + realUsedLen];
				System.arraycopy(buffer.array(), 0, desBuffer, 0, lenUsd + realUsedLen);
				shellStream.write(desBuffer);
			}
		}
		
		ByteBuffer doneBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
		doneBuffer.put("DONE".getBytes("UTF-8"));
		doneBuffer.putInt((int)System.currentTimeMillis());
		shellStream.write(doneBuffer.array());
		
		byte[] data = shellStream.read();
		if(new String(data ,"UTF-8").startsWith("OKAY")){
			shellStream.close();
			return 0;
		}
		
		return -1;
	}
	
	
	public static void main(String[] args)
	{
		new DeviceConnection().startConnect();
	}
}
