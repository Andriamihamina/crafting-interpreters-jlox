package com.sebastienyannis.lox;

import java.util.List;

import com.sebastienyannis.lox.Expr.Assign;
import com.sebastienyannis.lox.Expr.Grouping;
import com.sebastienyannis.lox.Expr.Unary;
import com.sebastienyannis.lox.Expr.Variable;
import com.sebastienyannis.lox.Stmt.Expression;
import com.sebastienyannis.lox.Stmt.Print;
import com.sebastienyannis.lox.Stmt.Var;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG: return !isTruphy(right);
        }

        return null;

    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS: 
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH: 
                checkNumberOperands(expr.operator, left, right);
                if ((Double)right == 0) throw new RuntimeError(expr.operator, "Dividing by 0 is not allowed");
                return (double)left / (double)right;
            case STAR: 
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
            case GREATER: 
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL: 
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS: 
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL: 
                checkNumberOperands(expr.operator, left, right);                
                return (double)left <= (double)right;
            case BANG_EQUAL:return !isEqual(left,right);
            case EQUAL_EQUAL:return isEqual(left,right);
        }

        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruphy(Object object)  {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number. ");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");   
    }

    /* void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    } */

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement: statements) {
                execute(statement);
            }
        } catch (RuntimeError e){
            Lox.runtimeError(e);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
    }

}
