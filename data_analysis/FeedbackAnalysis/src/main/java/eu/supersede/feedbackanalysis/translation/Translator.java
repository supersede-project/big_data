package eu.supersede.feedbackanalysis.translation;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by Emitza on 3/16/17.
 */
public class Translator {

    /*
    Sergi: I assume joshua server (./joshua -m 16g -server-port 5674) is running
     */
    // We assume you are running
    public String translate(String text) {

        String joshua = "/home/snadal/Desktop/apache-joshua-de-en-2016-11-18/";

        List<String> texts = prepare(text);
        List<String> translated = Lists.newArrayList();

        // Translate using joshua
        for (String feedback : texts) {
            try {
                String[] commands = {"/bin/sh", "-c", "echo \"" + feedback + "\" | " + joshua + "prepare.sh | nc localhost 5674"};
                Runtime rt = Runtime.getRuntime();
                Process pr1 = rt.exec(commands);
                BufferedReader input1 = new BufferedReader(new InputStreamReader(pr1.getInputStream()));
                String line1 = null;

                translated.add(input1.readLine());

                pr1.destroy();

                /*while ((line1 = input1.readLine()) != null) {
                    translated.add(line1);
                }



                pr1.destroy();*/
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // merge translations into a single string
        return String.join(" ", translated);
    }

    /*
    Converts feedback text to format required by the translation application (one sentence per line) in a file
     */
    private static List<String> prepare(String text){
        List<String> res = Lists.newArrayList();

        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        Matcher reMatcher = re.matcher(text);

        while (reMatcher.find()) {
            // TODO: save to file :)
            res.add(reMatcher.group());
        }
        return res;
    }


    public static void main(String [] args) throws IOException, InterruptedException {

        Translator trans = new Translator();
        String translation = trans.translate("Hallo! Guten Morgen! Wie geht's dir? Was gibt's Neues?");

        System.out.println("finished translation ");
        System.out.println(translation);
    }


}