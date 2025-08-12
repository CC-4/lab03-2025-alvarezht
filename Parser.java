/*
    Laboratorio No. 3 - Recursive Descent Parsing
    CC4 - Compiladores

    Clase que representa el parser

    Actualizado: agosto de 2021, Luis Cu
*/

import java.util.LinkedList;
import java.util.Stack;

public class Parser {

    // Puntero next que apunta al siguiente token
    private int next;
    // Stacks para evaluar en el momento
    private Stack<Double> operandos;
    private Stack<Token> operadores;
    // LinkedList de tokens
    private LinkedList<Token> tokens;

    // Funcion que manda a llamar main para parsear la expresion
    public boolean parse(LinkedList<Token> tokens) {
        this.tokens = tokens;
        this.next = 0;
        this.operandos = new Stack<Double>();
        this.operadores = new Stack<Token>();

        // Recursive Descent Parser
        // Imprime si el input fue aceptado
        System.out.println("Aceptada? " + S());

        // Shunting Yard Algorithm
        // Imprime el resultado de operar el input
        // System.out.println("Resultado: " + this.operandos.peek());

        // Verifica si terminamos de consumir el input
        if(this.next != this.tokens.size()) {
            return false;
        }
        return true;
    }

    private boolean term(int id) {
        if(this.next < this.tokens.size() && this.tokens.get(this.next).equals(id)) {
            this.next++;
            return true;
        }
        return false;
    }

    private int pre(Token op) {
        switch(op.getId()) {
            case Token.PLUS:
            case Token.MINUS:
                return 1;
            case Token.MULT:
            case Token.DIV:
            case Token.MOD:
                return 2;
            case Token.EXP:
                return 3;
            case Token.LPAREN:
            case Token.RPAREN:
                return 4;
            case Token.UNARY:
                return 5;
            default:
                return -1;
        }
    }

    private void popOp() {
        Token op = this.operadores.pop();
        double a, b;
        switch (op.getId()) {
            case Token.PLUS:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(b + a);
                break;
            case Token.MINUS:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(b - a);
                break;
            case Token.MULT:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(b * a);
                break;
            case Token.DIV:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(b / a);
                break;
            case Token.MOD:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(b % a);
                break;
            case Token.EXP:
                a = this.operandos.pop();
                b = this.operandos.pop();
                this.operandos.push(Math.pow(b, a));
                break;
            case Token.UNARY:
                a = this.operandos.pop();
                this.operandos.push(-a);
                break;
        }
    }

    private void pushOp(Token op) {
        if (this.operadores.empty() || op.getId() == Token.LPAREN) {
            this.operadores.push(op);
            return;
        }

        if (op.getId() == Token.RPAREN) {
            while (!this.operadores.empty() && this.operadores.peek().getId() != Token.LPAREN) {
                popOp();
            }
            if (!this.operadores.empty() && this.operadores.peek().getId() == Token.LPAREN) {
                this.operadores.pop();
            }
            return;
        }

        while (!this.operadores.empty() && pre(this.operadores.peek()) >= pre(op) && this.operadores.peek().getId() != Token.LPAREN) {
            popOp();
        }
        this.operadores.push(op);
    }

    private boolean S() {
        boolean ok = E() && term(Token.SEMI);

        if (ok && !this.operandos.empty()) {
            System.out.println(this.operandos.peek());
        }
        return ok;
    }

    private boolean E() {

        return expr();
    }

    private boolean expr() {
        if (!termExpr()) return false;
        while (true) {
            Token t = peek();
            if (t != null && (t.getId() == Token.PLUS || t.getId() == Token.MINUS)) {
                this.next++;
                pushOp(t);
                if (!termExpr()) return false;
            } else {
                break;
            }
        }
        return true;
    }

    private boolean termExpr() {
        if (!factorExpr()) return false;
        while (true) {
            Token t = peek();
            if (t != null && (t.getId() == Token.MULT || t.getId() == Token.DIV || t.getId() == Token.MOD)) {
                this.next++;
                pushOp(t);
                if (!factorExpr()) return false;
            } else {
                break;
            }
        }
        return true;
    }

    private boolean factorExpr() {
        if (!unaryExpr()) return false;
        while (true) {
            Token t = peek();
            if (t != null && t.getId() == Token.EXP) {
                this.next++;
                pushOp(t);
                if (!unaryExpr()) return false;
            } else {
                break;
            }
        }
        return true;
    }

    private boolean unaryExpr() {
        Token t = peek();
        if (t != null && t.getId() == Token.MINUS) {
            this.next++;
            pushOp(new Token(Token.UNARY));
            if (!unaryExpr()) return false;
            return true;
        } else if (t != null && t.getId() == Token.LPAREN) {
            this.next++;
            pushOp(t);
            if (!expr()) return false;
            t = peek();
            if (t != null && t.getId() == Token.RPAREN) {
                this.next++;
                pushOp(t);
                return true;
            } else {
                return false;
            }
        } else if (t != null && t.getId() == Token.NUMBER) {
            this.next++;
            operandos.push(t.getVal());
            return true;
        }
        return false;
    }

    private Token peek() {
        if (this.next < this.tokens.size()) return this.tokens.get(this.next);
        return null;
    }
 
}
