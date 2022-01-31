# Hindley-Milner-types-validation
Validation of Proofs for Inferring the Type of an Expression

This project check correctness of proof of expression's type. 

Grammar for proofs given in src/parser/MyGrammar.g4

Proof use different rules and subproof. Each expression has indent where one is "*   ". Each subproof has indent + 1 from indent of proof.

Example: 

1.

x : a |- x : (forall b. a) [rule #6]
*   x : a |- x : a [rule #1]

2.

|- (\x. x) (\y. y) : a -> a [rule #2]
*   |- \x. x : (a -> a) -> (a -> a) [rule #3]
*   *  x : a -> a |- x : a -> a [rule #1]
*   |- \y. y : a -> a [rule #3]
*   *   y : a |- y : a [rule #1]

Each expression of proof has number 1-6 that shows which dependincies we wait for correct proof.

<img src="*/Pictures/Rules.png" />