package eu.supersede.bdma.validation.simulator;

public abstract class Simulator extends Thread {

	//public abstract String getSequentialTuple() throws Exception;
	public abstract String getNextTuple() throws Exception;
	public abstract String getNextTupleAndReset() throws Exception;
	public abstract long getProcessingTime(String now, String next) throws Exception;
	
	//public abstract String getRandomTuple() throws Exception;
	public abstract String getKafkaTopicName();
}
