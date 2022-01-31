package helpers;

import java.util.*;

public class Type {
    enum Kind {
        MONOTYPE, FORALL
    }

    public Type(LinkedHashSet<String> variable, Monotype monotype) {
        if (variable.isEmpty()) {
            this.kind = Kind.MONOTYPE;
        } else {
            this.kind = Kind.FORALL;
        }
        this.forall = variable;
        this.monotype = monotype;
    }

    public Kind kind;
    public Monotype monotype;
    public LinkedHashSet<String> forall;

    public boolean checkMonotype() {
        return kind == Kind.MONOTYPE;
    }

    public boolean checkFreeInType(String typeVar) {
        if (forall.contains(typeVar)) {
            return false;
        } else {
            return monotype.checkFreeInType(typeVar);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return kind == type.kind && monotype.equals(type.monotype) && forall.equals(type.forall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, monotype);
    }
}
