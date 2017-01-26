package eu.supersede.bdma;

/**
 * Created by snadal on 22/01/17.
 */
public abstract class Simulator extends Thread {

    public abstract String getNextTuple(String topic) throws Exception;

}

