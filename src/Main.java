import parser.*;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.util.*;
import helpers.*;

public class Main {

    public static class MyErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            // System.out.println("line: " + line + charPositionInLine + " at " + offendingSymbol + ": " + msg);
            throw new NullPointerException();
        }
    }

    public static void main(String[] args) {
        try {
            CharStream input = CharStreams.fromStream(System.in);
            MyGrammarLexer lexer = new MyGrammarLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MyGrammarParser parser = new MyGrammarParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MyErrorListener());
            MyGrammarParser.ProofsContext ctx = parser.proofs();
            if (ctx.flag) {
                System.out.println("Incorrect");
                return;
            }
            List<Proof> proofList = ctx.listOfProofs;
            Deque<Proof> deque = new LinkedList<>();
            boolean start = true;
            for (Proof proof : proofList) {
                if (deque.isEmpty()) {
                    if (start) {
                        deque.addLast(proof);
                        start = false;
                    } else {
                        System.out.println("Incorrect");
                        return;
                    }
                } else {
                    while (true) {
                        Proof last = deque.getLast();
                        if (last.level < proof.level) {
                            if (last.level + 1 != proof.level) {
                                System.out.println("Incorrect");
                                return;
                            }
                            if (
                                    last.number == 1
                                    || (last.number == 3 || last.number >= 5) && last.subproofs.size() == 1
                                    || (last.number == 2 || last.number == 4) && last.subproofs.size() == 2
                            ) {
                                System.out.println("Incorrect");
                                return;
                            }
                            last.subproofs.add(proof);
                            deque.addLast(proof);
                            break;
                        } else {
                            deque.removeLast();
                        }
                    }
                }
            }
            System.out.println((proofList.get(0).check() ? "Correct" : "Incorrect"));
        } catch (Exception ignored) {
            System.out.print("Incorrect");
        } finally {
            System.out.flush();
        }
    }
}
