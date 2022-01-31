package helpers;

import java.util.*;

public class Proof {
    public int level;
    public int number;
    public Map<String, Type> ctx;
    public Expression expr;
    public Type type;
    public ArrayList<Proof> subproofs = new ArrayList<>(2);

    public Proof(int level, Map<String, Type> ctx, Expression expr, Type type, int number) {
        this.level = level;
        this.ctx = ctx;
        this.expr = expr;
        this.type = type;
        this.number = number;
    }

    public boolean check() {
        switch (number) {
            case 1:
                return check1();
            case 2:
                return check2();
            case 3:
                return check3();
            case 4:
                return check4();
            case 5:
                return check5();
            case 6:
                return check6();
            default:
                throw new IllegalStateException();
        }
    }

//    RULE #1
//    Г |- x : G
//    - x = переменная
//    - х в контексте с данным типом
    private boolean check1() {
        if (expr.kind != Expression.Kind.VAR || subproofs.size() != 0) {
            return false;
        }
        return type.equals(ctx.get(expr.variable)); // check that we have type of variable
    }

//    RULE #2
//    Г |- е0 е1 : т1
//    * Г |- е0 : т0 -> т1
//    * Г |- е1 : т0
//
//    - т0 = монотип
//    - т1 = монотип
//    - т0 -> т1 = тип первого подвывода
//    - левая часть применения = выражение в первом подвыводе
//    - правая часть применения = выражение во втором подвыводе
//    - все контексты совпадают по содержимому
//    - проверить первый подвывод
//    - проверить второй подвывод
    private boolean check2() {
        if (expr.kind != Expression.Kind.APPLICATION || subproofs.size() != 2) {
            return false;
        }
        Proof fstSubProof = subproofs.get(0);
        Type rightType = type;
        Type arrowType = fstSubProof.type;
        if (
                !rightType.checkMonotype() // t1 is monotype
                        || !arrowType.checkMonotype()
                        || !rightType.monotype.equals(arrowType.monotype.rightPartOfArrow())
                        || !fstSubProof.ctx.equals(ctx)
                        || !fstSubProof.expr.equals(expr.leftInApplication()) // e0 is left part of application
        ) {
            return false;
        }
        Proof sndSubProof = subproofs.get(1);
        Type leftType = sndSubProof.type;

        return
                leftType.checkMonotype() // t0 is monotype
                        && leftType.monotype.equals(arrowType.monotype.leftPartOfArrow())
                        && sndSubProof.ctx.equals(ctx)
                        && sndSubProof.expr.equals(expr.rightInApplication()) // e1 is right part of application
                        && sndSubProof.check() // check proof of e1 : t0
                        && fstSubProof.check(); // check proof of e0 : t0 -> t1

    }

//    RULE #3
//    Г |- \Х. е0 : т0 -> т1
//    * Г, Х : т0 |- е0 : т1
//
//    - т0 = монотип
//    - т1 = монотип
//    - Х = переменная
//    - выражение после абстракции = выражение в подвыводе
//    - {   - т0 -> т1 = тип изначального вывода
//          - тип подвывода т1
//          - тип Х т0 (тоже что и составляет ->)
//      }
//    - контексты отличаются только Х
//    - проверить подвывод
    private boolean check3() {
        if (expr.kind != Expression.Kind.LAMBDA || subproofs.size() != 1) {
            return false;
        }
        String variable = expr.variable;
        Proof subproof = subproofs.get(0);
        Type typeOfX = subproof.ctx.remove(variable);
        // check ctx diff from subproof ctx by X
        if (typeOfX == null || !ctx.equals(subproof.ctx)) {
            return false;
        }
        subproof.ctx.put(variable, typeOfX);
        return
                typeOfX.checkMonotype() // t0 is monotype
                        && subproof.type.checkMonotype() // t1 is monotype
                        && type.checkMonotype()
                        && type.monotype.leftPartOfArrow().equals(typeOfX.monotype)
                        && type.monotype.rightPartOfArrow().equals(subproof.type.monotype)
                        && subproof.expr.equals(expr.firstExpr) // check equality of expr in lambda and in subproof
                        && subproof.check(); // check proof of e : t1
    }

//    RULE #4
//    Г |- let X = e0 in e1 : т1
//    * Г |- е0 : G
//    * Г, Х : G |- е1 : т1
//
//    - т1 (тип вывода) = монотип
//    - Х = переменная
//    - контекст вывода = контекст первого подвывода
//    - контекст вывода отличается от контекста второго подвывода на Х
//    - тип первого подвывода = типу Х в контексте второго подвывода
//    - тип второго подвывода = тип вывода
//    - Х совпадает в выводе и контексте второго подвывода
//    - выражение первого подвывода = выражение после знака равно
//    - выражение второго подвывода = выражение после in
//    - проверить первый подвывод
//    - проверить второй подвывод
    private boolean check4() {
        if (expr.kind != Expression.Kind.LET || subproofs.size() != 2) {
            return false;
        }
        if (!type.checkMonotype()) { // check that t1 is monotype
            return false;
        }
        String variable = expr.variable;
        Proof fstSubProof = subproofs.get(0);
        Proof sndSubProof = subproofs.get(1);
        if (
                !fstSubProof.ctx.equals(ctx)
                        || !fstSubProof.expr.equals(expr.firstExpr) // check that first subproof expr is expr after =
        ) { // check same context of first subproof
            return false;
        }

        Type typeOfX = sndSubProof.ctx.remove(variable); // X совпадает в выводе и контексте подвывода 2
        // check diff ctx and second subproof ctx by X
        if (typeOfX == null || !sndSubProof.ctx.equals(ctx)) {
            return false;
        }
        sndSubProof.ctx.put(variable, typeOfX);
        if (!fstSubProof.type.equals(typeOfX)) { // check type of x equals type of e0
            return false;
        }
        return sndSubProof.type.equals(type) // check type of subproof 2 equal type of proof
                && sndSubProof.expr.equals(expr.secondExpr) // check that second subproof expr is expr after in
                && sndSubProof.check() // check proof of x : t0 |- e1 : t1
                && fstSubProof.check(); // check proof of |- e0 : t0
    }

//    RULE #5
//    Г |- е0 : g0
//    * Г |- e0 : g1
//
//    - контексты совпадают
//    - выражение вывода = выражение подвывода
//    - g1 более общий тип g0
//    * g1 = forall a1..an . A
//    * g0 = forall b1..bn . B
//    * * b1..bn not in FV(A)
//    * * B = A[a1=T1, a2=T2, ..., an=Tn]
//    - проверить подвывод
    private boolean check5() {
        if (subproofs.size() != 1) return false;
        Proof subproof = subproofs.get(0);
        if (!subproof.ctx.equals(ctx) || !subproof.expr.equals(expr)) { // check equality of context and expressions
            return false;
        }
        // maybe change way ot check
        for (String tmp : type.forall) { // check that b not in free value of A
            if (subproof.type.monotype.values.contains(tmp)) {
                return false;
            }
        }
        Set<String> forallInSubProof = new HashSet<>(subproof.type.forall); // a1..an
        Map<String, Monotype> susbt = new HashMap<>();
        // find substitution and check that free values save their types
        if (!subproof.type.monotype.findSubstitution(forallInSubProof, susbt, type.monotype)) {
            return false;
        }
        return subproof.check();
    }

//    RULE #6
//    Г |- е0 : forall a . g0
//    * Г |- e0 : g0
//
//    - выражение вывода = выражение подвывода
//    - контексты совпадают
//    - а not in FV(Г)
//    - а = типовая переменная
//    - тип вывоад отличается от типа подвывода только самым внешним квантором
//    - проверить подвывод
    private boolean check6() {
        if (type.kind != Type.Kind.FORALL || subproofs.size() != 1) {
            return false;
        }
        Proof subproof = subproofs.get(0);
        if (!ctx.equals(subproof.ctx) || !subproof.expr.equals(expr)) { // check same context in subproof and same expr
            return false;
        }
        Iterator<String> it = type.forall.iterator();
        String varType = it.next();
        for (Type tmp : ctx.values()) {
            if (tmp.checkFreeInType(varType)) { // check that a isn't in FV(ctx)
                return false;
            }
        }
        for (String b : subproof.type.forall) {
            varType = it.next();
            if (!b.equals(varType)) {
                return false;
            }
        }
        if (it.hasNext()) {
            return false;
        }
        return subproof.type.monotype.equals(type.monotype) // check equality of type of e and inner type of forall
                && subproof.check(); // check proof of e : t0
    }
}
