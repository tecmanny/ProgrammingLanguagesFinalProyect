/*
*  Client source code: implements the cmd interface for sending a Request to the Server side.
*  Copyright (C) 2020  Jose Manuel Garcia Lugo
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
	public static void main(String[] args) {
String source, destination;
		// Introduction
		System.out.printf("Welcome:\n");
		System.out.printf("Team Data Analist Copyright(C) 2020 Jose Manuel Garcia Lugo\n");
		System.out.printf("This program comes with ABSOLUTELY NO WARRANTY;\n");
		System.out.printf("This is free software, and you are welcome to redistribute it under certain conditions;\n");
		System.out.printf("\nUsage: [excel_source_path] [excel_destination_path]\n");
		System.out.printf("To Terminate execution, type: \"exit\" \n");

//address and port for connections
		String host = "localhost";
		int port = 8888;

		//Connection using sockets to server
		try(Socket socket = new Socket(host,port)){

		    OutputStream os = socket.getOutputStream();
		    InputStream is = socket.getInputStream();

			Scanner scanner = new Scanner(System.in);
			String str = null;

//Client Running until exit condition
			while(true) {
				System.out.println("> ");
				str = scanner.nextLine();

				//If user types exit, close connection and end execution
				if(str.equals("exit")) {
					System.out.printf("Closing the connection...\n");
			          socket.close();
			          System.out.printf("Connection closed\n");
			          break;
				}

				//Object IO Streams preparation
				ObjectOutputStream oos = new ObjectOutputStream(os);
		    ObjectInputStream ois = new ObjectInputStream(is);

						//User params handling
						String[] params = str.split(" +");
		        int argc = params.length;

				 if(argc == 2) {
					 source = params[0];
					 destination = params[1];
					try {
						//Send object to server
						Request request = new Request(source);
						oos.writeObject(request);
			      oos.flush();
			      System.out.println("SENT EXCEL FILE FOR ANALYSIS");
  					System.out.println("...");

						//Obtain response from server
						Response response = (Response)ois.readObject();
						byte[] byteArray = response.getByteArray();


						File file = new File(destination+"stats__"+request.getFileName());

						//Write file to given destination
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(byteArray);
						System.out.println("FILE RECEIVED");

					}catch(Exception e){
			            e.printStackTrace();
					}
				}else {
					System.out.println("Usage: [excel_source_path] [excel_destination_path]\n");
					System.out.println("Connection closed for security reasons, try again");
					socket.close();
					break;

				}
			}
			scanner.close();

		}catch(IOException ioe) {
			System.out.println("Connection Failed");
		}
	}
}
