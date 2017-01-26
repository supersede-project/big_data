package eu.supersede.bdma;

/**
 * Created by snadal on 22/01/17.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(
                new Thread("app-shutdown-hook") {
                    @Override
                    public void run() {
                        System.out.println("bye");
                    }
                });
        Simulator sim = new JSON_Simulator();
        sim.run();
    }
}
