package io.osv;

<<<<<<< HEAD
import java.io.*;
import java.net.*;
import java.util.Date;

public class Main
{
 	public static void main(String[] args) 
	{
		ServerSocket soc = null;
		try
		{
			soc = new ServerSocket(9876);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		while(true)
		{
			System.out.println("waiting for request!");
			try
			{
				Socket connectionSocket = soc.accept();
				Date date = new Date();
				DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());
				outStream.writeBytes(date.toString() + "\n");
				connectionSocket.close();
				System.out.println("Sent current date!");
			}
			catch (Exception e)
			{
				e.printStackTrace();	
			}
		}
=======
public class Main {
  public static void main(String[] args) {
    System.out.println("hello, world!");
>>>>>>> 2fa9d557e5ccf63bc6245c1cd7aa46f6933edf4f
  }
}