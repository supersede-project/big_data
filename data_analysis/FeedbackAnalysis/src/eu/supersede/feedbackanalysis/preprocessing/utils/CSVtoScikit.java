package eu.supersede.feedbackanalysis.preprocessing.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVtoScikit {

	public static String separator = File.separator;
	
	public static void main(String[] args) throws IOException {
		if (args.length < 3){
			System.err.println("Please provide path to CSV file, path to output directory, a label for the dataset, and the percentage of data for training (default: 0.3)!");
			System.exit(0);
		}
		String csvFilePath = args[0];
		String outputDir = args[1];
		
		double percTest = 0.3;
		if (args.length == 4){
			percTest = Double.parseDouble(args[3]);
		}
		
		String datasetLabel = args[2] + "_" + (int)((1 - percTest)*100) + ((int)(percTest*100));
		
		String datasetBaseDir = outputDir + separator + datasetLabel + separator;
		
		// if old dir with same name exists, delete it
		File d = new File(datasetBaseDir);
		if (d.exists() && d.isDirectory()){
			d.renameTo(new File(datasetBaseDir + "_prev"));
		}
		
		Random rnd = new Random();
		
		FileReader reader = new FileReader(csvFilePath);
		CSVReader csvReader = new CSVReader(reader);
		
		Iterator<String[]> iterator = csvReader.iterator();
		String[] csvHeader = iterator.next(); //discard the header! 
		while(iterator.hasNext()){
			String[] csvLine = iterator.next();
			
			// debug
			for (String s : csvLine){
				System.out.print(s + ",");
			}
			System.out.println();
			
			// each line has form: comment_id, thetext, thelabel
			String commentId = csvLine[0];
			String theText = csvLine[1];
			String theLabel = csvLine[2];
			String dir;
			if (rnd.nextDouble() < percTest){
				dir = datasetBaseDir + "test" + separator + theLabel;
			}else{
				dir = datasetBaseDir + "train" + separator + theLabel;
			}
			String outputFileName = dir + separator + commentId + ".txt";
			(new File(dir)).mkdirs();
			FileWriter writer = new FileWriter(outputFileName);
			writer.write(theText);
			writer.close();
		}
		csvReader.close();
		
		// write metadtaa
		String metadata = "name: " + datasetLabel + " \ndescription: open office writer issues \nformat: DocumentClassification \n";
		FileWriter writer = new FileWriter(datasetBaseDir + "metadata");
		writer.write(metadata);
		writer.close();
	}

}
