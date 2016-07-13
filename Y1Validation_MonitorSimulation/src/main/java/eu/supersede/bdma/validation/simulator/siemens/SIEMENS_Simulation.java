package eu.supersede.bdma.validation.simulator.siemens;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.supersede.bdma.validation.simulator.Main;
import eu.supersede.bdma.validation.simulator.Simulator;
import eu.supersede.bdma.validation.simulator.Wp2KafkaProducer;

public class SIEMENS_Simulation extends Simulator {

	private final static Logger logger = LogManager.getLogger(SIEMENS_Simulation.class);
	private static RandomAccessFile logFile;
	
	public SIEMENS_Simulation() {
		 try {
			logFile = new RandomAccessFile(Main.properties.getProperty("ecosys_core_log"), "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getKafkaTopicName() {
		return "siemens";
	}
	
	@Override
    public void run() {
		Wp2KafkaProducer kafkaProducer = new Wp2KafkaProducer(getKafkaTopicName());
		while (true) {
			String currentTuple = null;
			String nextTuple = null;
			try {
				currentTuple = getNextTuple();
				nextTuple = getNextTupleAndReset();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				kafkaProducer.writeToKafka(getKafkaTopicName(), currentTuple);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(getProcessingTime(currentTuple, nextTuple));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	@Override
	public String getNextTuple() throws Exception {
		System.out.println("IN GET NEXT TUPLE");
		System.out.println("Current offset = "+logFile.getFilePointer());
		String line = logFile.readLine();
		System.out.println("Size of line = "+line.getBytes().length);
		System.out.println("New offset = "+logFile.getFilePointer());
		if (line == null || line.isEmpty()) {
			logFile.seek(0);
			line = logFile.readLine();
		}
		return line;
	}

	@Override
	public String getNextTupleAndReset() throws Exception {
		System.out.println("IN GET NEXT TUPLE AND RESET");
		System.out.println("Current offset = "+logFile.getFilePointer());
		String line = logFile.readLine();
		if (line == null || line.isEmpty()) return null;
		else {
			logFile.seek(logFile.getFilePointer()-line.getBytes().length-2);
			System.out.println("Size of line = "+line.getBytes().length);
			System.out.println("New offset = "+logFile.getFilePointer());
			return line;
		}
	}

	@Override
	public long getProcessingTime(String now, String next) throws Exception {
		now = now.replace("﻿", "");
		next = next.replace("﻿", "");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss,SSS");
		Date nowDate = dateFormat.parse(now.split("\\|")[0].trim());
		Date newDate = dateFormat.parse(next.split("\\|")[0].trim());
		
		if (newDate.getTime() < nowDate.getTime()) return 0;
		return (newDate.getTime() - nowDate.getTime()) >= 3000 ? 3000 : newDate.getTime() - nowDate.getTime();
	}

}
