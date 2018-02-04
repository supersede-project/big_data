package eu.supersede.bdma.sa.eca_rules.conditions;

/**
 * Created by snadal on 25/01/17.
 */
public class TextCondition {

    public TextCondition(String x) {
        this.x = x;
    }

    String x;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public static int compareStrings(String A, String B) {
        return A.equals(B) ? 1 : 0;
    }

}
