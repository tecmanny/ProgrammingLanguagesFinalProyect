/*
*  Request <Server> source code: object class to receive object through a socket.
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;


public class Request implements Serializable{
  public static final long serialVersionUID = 1190299301293857102L;
  public String source;

  public byte[] byteArray;

  //Constructor

  public Request(String source) throws Exception{
    this.source = source;
//Get file bytes
    this.byteArray = Files.readAllBytes(Paths.get(source));

  }
//Filename
  public String getFileName(){
    File f = new File(source);
    String name =  f.getName();

    return name;
  }
  //Obtain file byte array

  public byte[] getByteArray(){
    return byteArray;
  }
}
