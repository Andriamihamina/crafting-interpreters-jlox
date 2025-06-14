package com.sebastienyannis.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


enum TokenType {
// Single-character tokens.
LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
// One or two character tokens.
BANG, BANG_EQUAL,
EQUAL, EQUAL_EQUAL,
GREATER, GREATER_EQUAL,
LESS, LESS_EQUAL,
// Literals.
IDENTIFIER, STRING, NUMBER,
// Keywords.
AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
EOF
}


public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;
        interpreter.interpret(statements);

    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where,String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
        "\n[line" + error.token.line + "]");
        hadRuntimeError = true;
    }
}
