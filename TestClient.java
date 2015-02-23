import java.io.*;
import java.net.*;

class TestClient
{
	public static void main(String argv[]) throws Exception
	{
		Socket clientSoc = new Socket("localhost", 9876);
		BufferedReader inBuf = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
		System.out.println("VM says date is: " + inBuf.readLine());
  		clientSoc.close();
 	}
}
