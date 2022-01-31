package helpers;

import java.util.Objects;

public class Expression {
    public enum Kind {
        LET, APPLICATION, LAMBDA, VAR
    }

    public Expression(Expression left, Expression right) {
        this.kind = Kind.APPLICATION;
        this.firstExpr = left;
        this.secondExpr = right;
    }

    public Expression(String variable, Expression firstExpr, Expression secondExpr) {
        this.kind = Kind.LET;
        this.variable = variable;
        this.firstExpr = firstExpr;
        this.secondExpr = secondExpr;
    }

    public Expression(String variable, Expression firstExpr) {
        this.kind = Kind.LAMBDA;
        this.variable = variable;
        this.firstExpr = firstExpr;
    }

    public Expression(String variable) {
        this.kind = Kind.VAR;
        this.variable = variable;
    }

    public Kind kind;
    public String variable;
    public Expression firstExpr;
    public Expression secondExpr;

    public Expression leftInApplication() {
        return firstExpr;
    }

    public Expression rightInApplication() {
        return secondExpr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        return kind == that.kind && Objects.equals(variable, that.variable) && Objects.equals(firstExpr, that.firstExpr) && Objects.equals(secondExpr, that.secondExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, variable, firstExpr);
    }
}
