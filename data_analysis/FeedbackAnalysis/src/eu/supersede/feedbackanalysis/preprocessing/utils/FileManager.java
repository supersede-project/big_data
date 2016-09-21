package eu.supersede.feedbackanalysis.preprocessing.utils;



import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class FileManager{

	public FileManager(){

	}
	/**
	 * Method to save files
	 * @param fileName name of the file including the extension
	 * @param lines of the text to save in a file
	 * @param folderPath place to save the file
	 */
	//	public void writeFile(String folderPath, String fileName, String lines){
	//		try {
	//			FileWriter outFile = new FileWriter(folderPath  + fileName);
	//			PrintWriter out = new PrintWriter(outFile);
	//			// Write text to file
	//			out.println(lines);
	//			out.close();
	//		} catch (IOException e){
	//			e.printStackTrace();
	//		}
	//
	//	}

	public void writeFile(String folderPath, String fileName, String lines){
		if(createFolder(folderPath.substring(0, folderPath.length()-1))==true)
		{
			try {
//				byte[] out = UnicodeUtil.convert(lines.getBytes("UTF-16"), "UTF-8"); //Shanghai in Chinese  
				FileOutputStream fos = new FileOutputStream(folderPath  + fileName);  
				fos.write(lines.getBytes());  
				fos.close();  
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void writeToXML(org.w3c.dom.Document createdDoc, String folderPath, String fileName){
		if(createFolder(folderPath.substring(0, folderPath.length()-1))==true)
		{
			try
			{
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(createdDoc);
				StreamResult result = new StreamResult(new File(folderPath  + fileName +".xml"));

				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);

				transformer.transform(source, result);

				System.out.println("File saved!");

			} catch (TransformerException tfe) {
				tfe.printStackTrace();
			}
		}
	}

	public void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		FileChannel source = null;
		FileChannel destination = null;
		try {
			fIn = new FileInputStream(sourceFile);
			source = fIn.getChannel();
			fOut = new FileOutputStream(destFile);
			destination = fOut.getChannel();
			long transfered = 0;
			long bytes = source.size();
			while (transfered < bytes) {
				transfered += destination.transferFrom(source, 0, source.size());
				destination.position(transfered);
			}
		} finally {
			if (source != null) {
				source.close();
			} else if (fIn != null) {
				fIn.close();
			}
			if (destination != null) {
				destination.close();
			} else if (fOut != null) {
				fOut.close();
			}
		}
	}

	//Addition made for CrowdIntent2, May 25 2015
	/** This method takes a txt file representing a discussion thread.
	 * The input needs to be in the following format
	 * 3 first lines that will be skipped
	 * Then, each comment of the thread must be separated by the line  "---------------------------------------"
	 * @param folderPath
	 * @param aFileName
	 * @throws IOException
	 */
	public void splitDiscussionFilesCrowdIntent(String folderPath, String aFileName, String destPath, String folderName)throws IOException {
		FileInputStream fstream = new FileInputStream(folderPath);
		// or using Scanner
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String lines="";
		//Changing the file name and assigning message ID
		System.out.println("File name: " + aFileName);
		aFileName=aFileName.substring(aFileName.indexOf(")_id-")+5, aFileName.indexOf("_xwiki"));
		System.out.println("File name: " + aFileName);
		//Changing the file name and assigning message ID
		String newPath=destPath + "Discussions/" + folderName + aFileName;
		createFolder(destPath + "Discussions");
		int numEmail=0;
		//Read File Line By Line
		strLine = br.readLine();
		strLine = br.readLine();
		strLine = br.readLine();
		while ((strLine = br.readLine()) != null)   {
			// split string and call your function
			int length=strLine.split(" ").length;
			if (length>0){
				if(strLine.compareTo("---------------------------------------")==0)
				{
					numEmail++;

					writeFile(destPath + "Discussions/", "message_" + aFileName + "_email_" + numEmail + ".txt",lines);
					lines="";

				}
				else
				{
					lines+=strLine;
					lines+= "\n";
				}
			}
		}
		br.close();
	}
	//Addition made for CrowdIntent2, May 25 2015





	//Addition made for RE 2014, Feb 17
	/** This method takes a txt file representing a discussion thread.
	 * The input needs to be in the following format
	 * 3 first lines that will be skipped
	 * Then, each comment of the thread must be separated by the line  "---------------------------------------"
	 * @param folderPath
	 * @param aFileName
	 * @throws IOException
	 */
	public void splitDiscussionFiles(String folderPath, String aFileName, String destPath, String folderName)throws IOException {
		FileInputStream fstream = new FileInputStream(folderPath);
		// or using Scanner
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String lines="";
		String newPath=destPath + "Discussions/" + folderName + aFileName.replace(".txt", "");
		createFolder(destPath + "Discussions");
		int numEmail=0;
		//Read File Line By Line
		try{
			strLine = br.readLine();
			strLine = br.readLine();
			strLine = br.readLine();
			while ((strLine = br.readLine()) != null)   {
				// split string and call your function
				int length=strLine.split(" ").length;
				if (length>0){
					if(strLine.compareTo("---------------------------------------")==0)
					{
						numEmail++;

						writeFile(newPath + "/", "email" + numEmail + ".txt",lines);
						lines="";

					}
					else
					{
						lines+=strLine;
						lines+= "\n";
					}
				}
			}		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//Addition made for RE 2014, Feb 17


	//Addition made for joint work-journal July 2014
	/** This method takes a txt file representing a discussion thread.
	 * The input needs to be in the following format
	 * 3 first lines that will be skipped
	 * Then, each comment of the thread must be separated by the line  "---------------------------------------"
	 * @param folderPath
	 * @param aFileName
	 * @throws IOException
	 */
	public void splitDiscussionFiles2(String folderPath, String aFileName, String destPath)throws IOException {
		FileInputStream fstream = new FileInputStream(folderPath);
		// or using Scaner
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String lines="";
		String newPath=destPath + aFileName.replace(".txt", "");
		//createFolder(destPath);
		int numEmail=0;

		try{
			strLine = br.readLine();

			while ((strLine = br.readLine()) != null)   {
				// split string and call your function
				int length=strLine.split(" ").length;
				if (length>0){
					String aux=strLine.split(" ")[0];
					if(aux.compareTo("From")==0)
					{
						numEmail++;
						//System.out.println("true");
						writeFile(destPath, aFileName.replace(".txt", "") + "_email" + numEmail + ".txt",lines);
						lines="";

					}
					else
					{
						lines+=strLine;
						lines+= "\n";
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//Addition made for joint work-journal July 2014


	public boolean createFolder(String path){
		File folder = new File(path);
		boolean result=false;
		if (!folder.exists()) {
			System.out.println("creating folder: " + folder);
			result = folder.mkdir();  

			if(result) {    
				System.out.println("DIR created"); 
			}
		}
		if(folder.exists())
			result=true;
		return result;
	}

	public String convertFileText(String folderPath)throws IOException {
		FileInputStream fstream = new FileInputStream(folderPath);
		// or using Scaner
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine="";
		String lines="";
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
			// split string and call your function
			strLine = strLine.trim(); // remove leading and trailing whitespace
			if (!strLine.equals("")) // don't write out blank lines
			{
				lines+=strLine + "\n";
			}
		}
		br.close();
		return lines;
	}


	public File[] getFilesToBeIndexed(String dataDirectory){
		File dataDir  = new File(dataDirectory);
		if(!dataDir.exists()){
			throw new RuntimeException(dataDirectory+" does not exist");
		}
		File[] files = dataDir.listFiles();
		return files;
	}

	public static void main(String arg[]) throws Exception{
		FileManager mfiles=new FileManager();
		String sourceDir="/Users/itzelmorales/Desktop/Splitting/source/";
		String destPath="/Users/itzelmorales/Desktop/Splitting/target/";
		//String folderName="noname";
		File[] files = mfiles.getFilesToBeIndexed(sourceDir);
		//Addition made for RE 2014, Feb 17	
		try
		{
			for(File file: files)
			{
				System.out.println(sourceDir);
				System.out.println(file.getName());
				//mfiles.splitDiscussionFiles(file.getPath(), file.getName(),destPath,folderName);
				mfiles.splitDiscussionFiles2(file.getPath(), file.getName(),destPath);
			}
		}
		catch(IOException ex)
		{
			
			ex.printStackTrace();
			return;
		}
		//Addition made for RE 2014, Feb 17

	}


}
