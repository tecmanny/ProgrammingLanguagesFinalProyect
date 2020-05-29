/*
*  Server source code: Handles clients and their request, returns processed excel file
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;




@SuppressWarnings("deprecation")
public class Server {
	public Server(){}

	public static void main(String[] args) throws IOException {
		ServerSocket  serverSocket = null;
		try {
			//Connection with clients socket data
			serverSocket = new ServerSocket(8888);

			serverSocket.setReuseAddress(true);

			System.out.println("Server stared...");
			//Client Aceptations
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("Client " + socket.getInetAddress().getHostAddress() + " connected");
//Client Handler for each connected client, (fork)
				ClientHandler clientHandler = new ClientHandler(socket);
		        new Thread(clientHandler).start();

			}
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}finally {
			if(serverSocket != null) {
				try {
				serverSocket.close();

				}catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
//Client Handler
	private static class ClientHandler implements Runnable{
		private final Socket socket;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			// Params for the excel thing
			String folder = "inputs/";
			while(true) {
				try {
//Preparation of the IO Socket Streams
					InputStream is = socket.getInputStream();
			        OutputStream os = socket.getOutputStream();
//Preparation of the IO Object Streams
			        ObjectInputStream ois = new ObjectInputStream(is);
			        ObjectOutputStream oos = new ObjectOutputStream(os);
//We obtain the request and Read it
			        Request request = (Request)ois.readObject();
			        byte[] byteArray = request.getByteArray();

//Save Excel file with unique name ID in our works folder to work with
							int leftLimit = 48; // numeral '0'
							int rightLimit = 122; // letter 'z'
							int targetStringLength = 10;
							Random random = new Random();

							String generatedString = random.ints(leftLimit, rightLimit + 1)
							.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
							.limit(targetStringLength)
							.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
							.toString();

							String path = folder.concat(request.getFileName()+generatedString+".xlsx");
			        File file = new File(path);
			        FileOutputStream fos = new FileOutputStream(file);
			        fos.write(byteArray);

							//THIS SECTIONS DOES THE ANALYSIS OF THE EXCEL
							//Open Excel file for the ANALYSIS
							try (InputStream inp = new FileInputStream(path)) {

		    Workbook wb = WorkbookFactory.create(inp);
		    Sheet sheet = wb.getSheetAt(0);
		    DataFormatter formatter = new DataFormatter();


		    //WIN PERCENTAGE
		    List<String> TotalWL = new ArrayList<String>();
		    for(Row r : sheet) {
		       Cell c = r.getCell(3);
		       if(c != null) {
		          if(c.getCellType() == CellType.STRING) {
		             TotalWL.add(c.getStringCellValue());
		          }
		       }
		    }


		    int Wins = 0;
		    int Loses = 0;
		    for(String i: TotalWL) {
		    	if(i.equals("Win")) {
		    		Wins++;
		    	}else if(i.equals("Lose")) {
		    		Loses++;
		    	}
		    }

		    double WL = Wins+Loses;

		   //WIN PERCENTAGE
		   double WP = (Wins/WL)*100;


		   //LOSE PERCENTAGE
		   double LP = (Loses/WL)*100;




		//TIME ANALYSIS
		    List<Double> values = new ArrayList<Double>();
		    for(Row r : sheet) {
		       Cell c = r.getCell(4);
		       if(c != null) {
		          if(c.getCellType() == CellType.NUMERIC) {
		             values.add(c.getNumericCellValue());
		          } else if(c.getCellType() == CellType.FORMULA && c.getCachedFormulaResultType() == CellType.NUMERIC) {
		             values.add(c.getNumericCellValue());
		          }
		       }
		    }

		    double TimeTotal = 0;
		    for(double i: values) {
		    	TimeTotal += i;
		    }
		    //AVERAGE TIME PER GAME
		    double AvgTime = TimeTotal/values.size();


		    //TOWERS ANALYSIS
			    List<Double> numT = new ArrayList<Double>();
			    for(Row r : sheet) {
			       Cell c = r.getCell(5);
			       if(c != null) {
			          if(c.getCellType() == CellType.NUMERIC) {
			             numT.add(c.getNumericCellValue());
			          } else if(c.getCellType() == CellType.FORMULA && c.getCachedFormulaResultType() == CellType.NUMERIC) {
			             numT.add(c.getNumericCellValue());
			          }
			       }
			    }

			    double TowersTotal = 0;
			    for(double i: numT) {
			    	TowersTotal += i;
			    }
			    //AVERAGE TOWERS PER GAME
			    double AvgTowers = TowersTotal/numT.size();



			    //DRAKES ANALYSIS
			    List<Double> numD = new ArrayList<Double>();
			    for(Row r : sheet) {
			       Cell c = r.getCell(6);
			       if(c != null) {
			          if(c.getCellType() == CellType.NUMERIC) {
			             numD.add(c.getNumericCellValue());
			          } else if(c.getCellType() == CellType.FORMULA && c.getCachedFormulaResultType() == CellType.NUMERIC) {
			             numD.add(c.getNumericCellValue());
			          }
			       }
			    }

			    double DragonsTotal = 0;
			    for(double i: numD) {
			    	DragonsTotal += i;
			    }
			    //AVERAGE DRAGONS PER GAME
			    double AvgDragons = DragonsTotal/numD.size();


			    //BARONS ANALYSIS

			    List<Double> numB = new ArrayList<Double>();
			    for(Row r : sheet) {
			       Cell c = r.getCell(7);
			       if(c != null) {
			          if(c.getCellType() == CellType.NUMERIC) {
			             numB.add(c.getNumericCellValue());
			          } else if(c.getCellType() == CellType.FORMULA && c.getCachedFormulaResultType() == CellType.NUMERIC) {
			             numB.add(c.getNumericCellValue());
			          }
			       }
			    }

			    double BaronsTotal = 0;
			    for(double i: numB) {
			    	BaronsTotal += i;
			    }
			    //AVERAGE BARONS PER GAME
			    double AvgBarons = BaronsTotal/numB.size();



			    //HERALDS ANALYSIS
			    List<Double> numH = new ArrayList<Double>();
			    for(Row r : sheet) {
			       Cell c = r.getCell(8);
			       if(c != null) {
			          if(c.getCellType() == CellType.NUMERIC) {
			             numH.add(c.getNumericCellValue());
			          } else if(c.getCellType() == CellType.FORMULA && c.getCachedFormulaResultType() == CellType.NUMERIC) {
			             numH.add(c.getNumericCellValue());
			          }
			       }
			    }

			    double HeraldsTotal = 0;
			    for(double i: numH) {
			    	HeraldsTotal += i;
			    }
			    //AVERAGE HERALDS PER GAME
			    double AvgHeralds = HeraldsTotal/numH.size();
								System.out.println(WP);
								System.out.println(LP);
								System.out.println(AvgTime);
								System.out.println(AvgTowers);
								System.out.println(AvgDragons);
								System.out.println(AvgBarons);
								System.out.println(AvgHeralds);



								CreationHelper createHelper = wb.getCreationHelper();
										String safeName = WorkbookUtil.createSafeSheetName("[Output*?]"); // returns " O'Brien's sales   "
										Sheet sheet1 = wb.createSheet(safeName);

										//Cell Headers
										Row row = sheet1.createRow(0);
										Cell cell = row.createCell(0);
										Cell cell1 = row.createCell(1);
										Cell cell2 = row.createCell(2);
										Cell cell3 = row.createCell(3);
										Cell cell4 = row.createCell(4);
										Cell cell5 = row.createCell(5);
										Cell cell6 = row.createCell(6);



										//Comparison LPL Header
										Row rowLPL = sheet1.createRow(4);
										Cell CellLPLHeader = rowLPL.createCell(3);
										CellLPLHeader.setCellValue("Stats Comparison With LPL");
										sheet1.addMergedRegion(new CellRangeAddress(
												4,
												5,
												3,
												5
												));
										Font fontLPL = wb.createFont();
										fontLPL.setFontHeightInPoints((short) 18);
										fontLPL.setBold(true);

										//LPL Header Style
										CellStyle cellStyleLPL = wb.createCellStyle();
										cellStyleLPL.setAlignment(HorizontalAlignment.CENTER);
										cellStyleLPL.setVerticalAlignment(VerticalAlignment.CENTER);
										cellStyleLPL.setFont(fontLPL);
										CellLPLHeader.setCellStyle(cellStyleLPL);

										//Font for cell
										Font font = wb.createFont();
										font.setBold(true);

										//Header Values
										cell.setCellValue("Win Percentage");
										cell1.setCellValue("Lose Percentage");
										cell2.setCellValue("Average Time Per Match");
										cell3.setCellValue("Average Towers Per Match");
										cell4.setCellValue("Average Drakes Per Match");
										cell5.setCellValue("Average Heralds Per Match");
										cell6.setCellValue("Average Barons Per Match");




										//Cell Headers Style
										CellStyle cellStyle = wb.createCellStyle();
										cellStyle.setAlignment(HorizontalAlignment.CENTER);
										cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
										cellStyle.setFont(font);

										//Style Assignment for each cell
										cell.setCellStyle(cellStyle);
										cell1.setCellStyle(cellStyle);
										cell2.setCellStyle(cellStyle);
										cell3.setCellStyle(cellStyle);
										cell4.setCellStyle(cellStyle);
										cell5.setCellStyle(cellStyle);
										cell6.setCellStyle(cellStyle);



										//Values of the Analysis

										Row ResRow = sheet1.createRow(1);

										Cell WinC = ResRow.createCell(0);
										Cell LoseC = ResRow.createCell(1);
										Cell AvgTPMC = ResRow.createCell(2);
										Cell AvgTWPMC = ResRow.createCell(3);
										Cell AvgDPMC = ResRow.createCell(4);
										Cell AvgHPMC = ResRow.createCell(5);
										Cell AvgBPMC = ResRow.createCell(6);


										/* VALUES TO COPY TO EXCEL
										 * WP
										 * LP
										 * AvgTime
										 * AvgTowers
										 * AvgDragons
										 * AvgBarons
										 * AvgHeralds
										 */
										 //Decimal Handler
										BigDecimal WPBD = new BigDecimal(WP).setScale(2, RoundingMode.HALF_UP);
								        double WPR = WPBD.doubleValue();

								        BigDecimal LPBD = new BigDecimal(LP).setScale(2, RoundingMode.HALF_UP);
								        double LPR = LPBD.doubleValue();

								        BigDecimal AVT = new BigDecimal(AvgTime).setScale(2, RoundingMode.HALF_UP);
								        double AVTR = AVT.doubleValue();

								        BigDecimal AVTW = new BigDecimal(AvgTowers).setScale(2, RoundingMode.HALF_UP);
								        double AVTWR = AVTW.doubleValue();

								        BigDecimal AVD = new BigDecimal(AvgDragons).setScale(2, RoundingMode.HALF_UP);
								        double AVDR = AVD.doubleValue();

								        BigDecimal AVB = new BigDecimal(AvgBarons).setScale(2, RoundingMode.HALF_UP);
								        double AVBR = AVB.doubleValue();

								        BigDecimal AVH = new BigDecimal(AvgHeralds).setScale(2, RoundingMode.HALF_UP);
								        double AVHR = AVH.doubleValue();



										WinC.setCellValue(WPR);
										LoseC.setCellValue(LPR);
										AvgTPMC.setCellValue(AVTR);
										AvgTWPMC.setCellValue(AVTWR);
										AvgDPMC.setCellValue(AVDR);
										AvgHPMC.setCellValue(AVHR);
										AvgBPMC.setCellValue(AVBR);



										//Values of the LPL Analysis
										Row LPLRow = sheet1.createRow(6);

										Cell LPLM = LPLRow.createCell(2);
										Cell LPLT = LPLRow.createCell(3);
										Cell LPLD = LPLRow.createCell(4);
										Cell LPLH = LPLRow.createCell(5);
										Cell LPLB = LPLRow.createCell(6);

										Font fontLPLText = wb.createFont();
										fontLPLText.setItalic(true);
										fontLPLText.setFontHeightInPoints((short) 13);

										CellStyle StyleTextLPL = wb.createCellStyle();
										StyleTextLPL.setAlignment(HorizontalAlignment.CENTER);
										StyleTextLPL.setVerticalAlignment(VerticalAlignment.CENTER);
										StyleTextLPL.setFont(fontLPLText);

										//ANALYSIS LPL
										double LPLTime = 32.29;
										double LPLTowers = 11;
										double LPLDrakes = 3;
										double LPLHeralds = 1;
										double LPLBarons = 2;

										/* VALUES
										 * AvgTime
										 * AvgTowers
										 * AvgDragons
										 * AvgBarons
										 * AvgHeralds
										 */

										double LPLCT = ((AvgTime*100)/LPLTime);

										if(LPLCT > 100) {
											LPLCT = LPLCT -100;
											BigDecimal LPLCTA = new BigDecimal(LPLCT).setScale(2, RoundingMode.HALF_UP);
									        double LPLCTAR = LPLCTA.doubleValue();
											LPLM.setCellValue("You end your games "+LPLCTAR+"%"+" slower");
										}else {
											LPLCT = LPLCT -100;
											LPLCT = LPLCT * -1;
											BigDecimal LPLCTA = new BigDecimal(LPLCT).setScale(2, RoundingMode.HALF_UP);
									        double LPLCTAR = LPLCTA.doubleValue();
											LPLM.setCellValue("You end your games "+LPLCTAR+"%"+" faster");
										}


										double LPLCTW =  (AvgTowers*100)/LPLTowers;
										if(LPLCTW < 100) {
											LPLCTW = LPLCTW - 100;
											LPLCTW = LPLCTW * -1;
											BigDecimal LPLCTWA = new BigDecimal(LPLCTW).setScale(2, RoundingMode.HALF_UP);
									        double LPLCTWAR = LPLCTWA.doubleValue();
											LPLT.setCellValue("You take "+LPLCTWAR+"% less towers");
										}else {
											LPLCTW = LPLCTW - 100;
											BigDecimal LPLCTWA = new BigDecimal(LPLCTW).setScale(2, RoundingMode.HALF_UP);
									        double LPLCTWAR = LPLCTWA.doubleValue();
											LPLT.setCellValue("You take "+LPLCTWAR+"% more towers");
										}

										double LPLDC = (AvgDragons*100)/LPLDrakes;
										if(LPLDC < 100) {
											LPLDC = LPLDC - 100;
											LPLDC = LPLDC * -1;
											BigDecimal LPLDCA = new BigDecimal(LPLDC).setScale(2, RoundingMode.HALF_UP);
									        double LPLDCAR = LPLDCA.doubleValue();
											LPLD.setCellValue("You take "+LPLDCAR+"% less drakes");
										}else {
											LPLDC = LPLDC - 100;
											BigDecimal LPLDCA = new BigDecimal(LPLDC).setScale(2, RoundingMode.HALF_UP);
									        double LPLDCAR = LPLDCA.doubleValue();
											LPLD.setCellValue("You take "+LPLDCAR+"% more drakes");
										}

										double LPLHC = (AvgHeralds*100)/LPLHeralds;
										if(LPLHC < 100) {
											LPLHC = LPLHC - 100;
											LPLHC = LPLHC * -1;
											BigDecimal LPLHCA = new BigDecimal(LPLHC).setScale(2, RoundingMode.HALF_UP);
									        double LPLDHCAR = LPLHCA.doubleValue();
											LPLH.setCellValue("You take "+LPLDHCAR+"% less heralds");
										}else {
											LPLHC = LPLHC - 100;
											BigDecimal LPLHCA = new BigDecimal(LPLHC).setScale(2, RoundingMode.HALF_UP);
									        double LPLDHCAR = LPLHCA.doubleValue();
											LPLH.setCellValue("You take "+LPLDHCAR+"% more heralds");
										}

										double LPLBC= (AvgBarons*100)/LPLBarons;
										if(LPLBC < 100) {
											LPLBC = LPLBC - 100;
											LPLBC = LPLBC * -1;
											BigDecimal LPLBCA = new BigDecimal(LPLBC).setScale(2, RoundingMode.HALF_UP);
									        double LPLBCAR = LPLBCA.doubleValue();
											LPLB.setCellValue("You take "+LPLBCAR+"% less barons");
										}else {
											LPLBC = LPLBC - 100;
											BigDecimal LPLBCA = new BigDecimal(LPLBC).setScale(2, RoundingMode.HALF_UP);
									        double LPLBCAR = LPLBCA.doubleValue();
											LPLB.setCellValue("You take "+LPLBCAR+"% more barons");
										}


										 //Cell Styles for LPL Statistics Analysis

										 LPLM.setCellStyle(StyleTextLPL);
										 LPLT.setCellStyle(StyleTextLPL);
										 LPLD.setCellStyle(StyleTextLPL);
										 LPLH.setCellStyle(StyleTextLPL);
										 LPLB.setCellStyle(StyleTextLPL);


										//Adjust Column Width
											sheet1.autoSizeColumn(0);
											sheet1.autoSizeColumn(1);
											sheet1.autoSizeColumn(2);
											sheet1.autoSizeColumn(3);
											sheet1.autoSizeColumn(4);
											sheet1.autoSizeColumn(5);
											sheet1.autoSizeColumn(6);
											sheet1.autoSizeColumn(7);
											sheet1.autoSizeColumn(8);



											//Write file with our modified Excel Wookbook
										System.out.println("DONE");
										try (OutputStream fileOut = new FileOutputStream(path)) {
    								wb.write(fileOut);
									}
										//Send the response Object to client
										oos.writeObject(new Response(path));
										oos.flush();
										System.out.println("FILE SENT");
				}catch(Exception ioe){
				System.out.println("Couldnt Open the file, try again");
				}
				}catch(Exception ioe) {
					//ioe.printStackTrace();
					System.out.println("Client Disconnected");
					break;
				}
			}
		}
	}
}
