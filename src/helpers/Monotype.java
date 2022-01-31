package helpers;

import java.util.*;

public class Monotype {
    enum Kind {
        VAR, MNTtoMNT
    }

    public Monotype(String variable) {
        this.kind = Kind.VAR;
        this.variable = variable;
        values.add(variable);
    }

    public Monotype(Monotype firstMNT, Monotype secondMNT) {
        this.kind = Kind.MNTtoMNT;
        this.firstMNT = firstMNT;
        this.secondMNT = secondMNT;
        values.addAll(firstMNT.values);
        values.retainAll(secondMNT.values);
    }

    public Monotype(String variable, Monotype secondMNT) {
        this.kind = Kind.MNTtoMNT;
        this.firstMNT = new Monotype(variable);
        this.secondMNT = secondMNT;
        values.add(variable);
        values.retainAll(secondMNT.values);
    }

    public Kind kind;
    public String variable;
    public Monotype firstMNT;
    public Monotype secondMNT;
    public Set<String> values = new HashSet<>();

    public Monotype leftPartOfArrow() {
        return firstMNT;
    }

    public Monotype rightPartOfArrow() {
        return secondMNT;
    }

    public Set<String> freeValues(Set<String> forall) {
        HashSet<String> res = new HashSet<>(values);
        res.removeAll(forall);
        return res;
    }

    public boolean checkFreeInType(String typeVar) {
        return values.contains(typeVar);
    }

    public boolean findSubstitution(Set<String> forall, Map<String, Monotype> subst, Monotype biggerMonotype) {
        Monotype left, right;
        Monotype inMap;
        switch (kind) {
            case VAR:
                if (!forall.contains(variable)) {
                    return biggerMonotype.kind == Kind.VAR && biggerMonotype.variable.equals(variable);
                }
                inMap = subst.putIfAbsent(variable, biggerMonotype);
                if (inMap != null) {
                    return inMap.equals(biggerMonotype);
                } else {
                    return true;
                }
            case MNTtoMNT:
                left = biggerMonotype.leftPartOfArrow();
                right = biggerMonotype.rightPartOfArrow();
                if (right == null || left == null) {
                    return false;
                }
                if (Collections.disjoint(forall, firstMNT.values)) {
                    if (firstMNT.equals(left)) {
                        if (Collections.disjoint(forall, secondMNT.values)) {
                            return secondMNT.equals(right);
                        } else {
                            return secondMNT.findSubstitution(forall, subst, right);
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (firstMNT.findSubstitution(forall, subst, left)) {
                        if (Collections.disjoint(forall, secondMNT.values)) {
                            return secondMNT.equals(right);
                        } else {
                            return secondMNT.findSubstitution(forall, subst, right);
                        }
                    } else {
                        return false;
                    }
                }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monotype monotype = (Monotype) o;
        return kind == monotype.kind && Objects.equals(variable, monotype.variable) && Objects.equals(firstMNT, monotype.firstMNT) && Objects.equals(secondMNT, monotype.secondMNT) && values.equals(monotype.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, variable, firstMNT, secondMNT, values);
    }
}
