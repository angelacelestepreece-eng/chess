package ui;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public abstract class StandardClient {
    public void runLoop(String welcomeMessage) {
        System.out.println(welcomeMessage);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                if (!result.equals("quit")) {
                    System.out.print(BLUE + result);
                }
            } catch (Throwable e) {
                System.out.print(e.toString());
            }
        }
    }

    protected void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    protected abstract String eval(String input);
}
