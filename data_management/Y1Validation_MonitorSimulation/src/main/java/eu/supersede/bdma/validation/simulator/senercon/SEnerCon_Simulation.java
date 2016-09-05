package eu.supersede.bdma.validation.simulator.senercon;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import eu.supersede.bdma.validation.simulator.Main;
import eu.supersede.bdma.validation.simulator.Simulator;
import eu.supersede.bdma.validation.simulator.Wp2KafkaProducer;

public class SEnerCon_Simulation extends Simulator {

	private final static Logger logger = LogManager.getLogger(SEnerCon_Simulation.class);
	private static ArrayList<File> allEmails;
	private static int offset;
	
	public SEnerCon_Simulation() {
		File folder = new File(Main.properties.getProperty("emails_folder"));
		allEmails = Lists.newArrayList(folder.listFiles());
		offset = 0;
	}

	/*@Override
	public String getSequentialTuple() throws Exception {
		if (offset >= allEmails.size()) offset = 0;
		
		File email = allEmails.get(offset);
		FileInputStream fis = new FileInputStream(email);
		byte[] data = new byte[(int) email.length()];
		fis.read(data);
		fis.close();
		return new String(data, "UTF-8");
	}

	@Override
	public String getRandomTuple() throws Exception {
		File email = allEmails.get(new Random().nextInt(allEmails.size()));
		FileInputStream fis = new FileInputStream(email);
		byte[] data = new byte[(int) email.length()];
		fis.read(data);
		fis.close();
		return new String(data, "UTF-8");
	}*/

	@Override
	public String getKafkaTopicName() {
		return "senercon";
	}

	@Override
    public void run() {
	/*	Wp2KafkaProducer kafkaProducer = new Wp2KafkaProducer();
		while (true) {
			try {
				kafkaProducer.writeToKafka(getKafkaTopicName(), getSequentialTuple());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(Integer.parseInt(Main.properties.getProperty("waitingTime")));
			} catch (NumberFormatException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		*/
	}

	@Override
	public String getNextTuple() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNextTupleAndReset() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getProcessingTime(String now, String next) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
