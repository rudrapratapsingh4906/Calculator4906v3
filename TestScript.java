import java.util.regex.*;

public class TestScript {
    public static void main(String[] args) {
        String regex = "(?i)^\\s*(find(\\s+[a-zA-Z]+\\s+for|\\s+the\\s+value\\s+of(\\s+[a-zA-Z]+)?(\\s+for)?|\\s+derivative\\s+of|\\s+integral\\s+of)?|solve(\\s+for\\s+[a-zA-Z]+)?|calculate|simplify|differentiate|integrate|evaluate|compute|what\\s+is(\\s+the)?|equation|expression)\\s*[:=]*\\s*";
        String[] tests = {
            "find x for x^2 - 4 = 0",
            "find the value of x for x^2 - 4 = 0",
            "find derivative of sin(x)",
            "solve for y: y = 2"
        };
        for (String t : tests) {
            System.out.println(t.replaceAll(regex, ""));
        }
    }
}
