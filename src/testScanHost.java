import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class testScanHost
{
	public static void main(String[] args)
	{

		// 定义IP 区段
		String ip1 = ""; 	// 范围 1

		try
		{
			InetAddress localAddress = InetAddress.getLocalHost();
			String hostAddress = localAddress.getHostAddress();		// 获取本机IP地址
			int dotPos = hostAddress.lastIndexOf(".");
			ip1 = hostAddress.substring(0, dotPos + 1);				// 设置IP区头前段

		}
		catch (UnknownHostException e1)
		{
			e1.printStackTrace();
		}

		ExecutorService service = Executors.newFixedThreadPool(255);
		
		try
		{
			for (int i = 1; i < 255; i++)
			{
				final String ipaddress = ip1 + i;
				service.execute(new Thread1(ipaddress));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


	}
}


class Thread1 implements Runnable
{
	private String hostaddress;


	public Thread1(String hostaddress)
	{
		this.hostaddress = hostaddress;
	}


	@Override
	public void run()
	{
		try
		{
			InetAddress address = InetAddress.getByName(hostaddress);

//			if (address.isReachable(3000))	// 测试3秒内是否能够到达此地址
			{
//				System.out.println(address.getHostAddress());
				new Thread(new testOpenPortThread(hostaddress)).start();	// 扫描开放端口
			}
//			else
//			{
////				System.out.println(address.getHostAddress() + ":connection time out");
//			}
		}
		catch (UnknownHostException e)
		{
		}
		catch (IOException e)
		{
		}
	}
}


class testOpenPortThread implements Runnable
{
	private String hostaddress;


	testOpenPortThread(String hostaddress)
	{
		this.hostaddress = hostaddress;
	}


	// 测试开放端口
	public void run()
	{
		for (int i = 5555; i <= 5555; i++)
		{
			Socket socket;
			try
			{
				socket = new Socket(hostaddress, i);
				if (socket.isConnected())
				{
					
					System.out.println(hostaddress + ":" + i + "端口 :open");
					break;
				}
			}
			catch (Exception e)
			{
//			System.out.println(hostaddress + ":" + i + "端口 :close");	
			}



		}
	}


}