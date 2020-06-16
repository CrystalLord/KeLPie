# KeLPie: Basic Usage

## Table of Contents

1. [Review on Linear Programs](#1-review-on-linear-programs)
2. [Variables](#2-variables)
3. [Linear Expressions](#3-linear-expressions)
4. [Constraints](#4-constraints)
5. [Linear Programs](#5-linear-programs)
6. [Linear Program Solutions](#6-linear-program-solutions)

## 1. Review on Linear Programs

A [Linear Program](https://en.wikipedia.org/wiki/Linear_programming) is a special case of a mathematical model which
has requirements specified by linear relationships. Linear Programs are problems which must be able to be
expressed in the following canonical form:

![Wikipedia Linear Program](https://wikimedia.org/api/rest_v1/media/math/render/svg/639c4281a57140db9a4416ca58f9d9af14243bb0)

Not all Linear Programs are easily written in this form, and so KeLPie allows you to define and solve Linear Programs
in any non-canonical form.

## 2. Variables

The heart of the Linear Program in KeLPie is the `LpSolver` class. The `LpSolver` takes in a linear expression
$\mathbf{c}^T\mathbf{x}$ which it will attempt to maximise, and a set of linear constraints which make each row of the
resulting matrix equation $\mathbf{Ax} \leq \mathbf{b}$.

However, to make an LpSolver instance, we have to define some variables first for the $\mathbf{x}$ vector.
We can do so with `ContinuousVar`:

```kotlin
import krilis.solver.*

fun main() {
    val x1 = ContinuousVar("x1")
    val x2 = ContinuousVar("x2")
}
```
Every `ContinuousVar` needs to have a name. If you create two `ContinuousVar`s with the same name, they will refer
to the same Linear Program variable despite being separate instances of ContinuousVar.

## 3. Linear Expressions

We can do linear operations between these variables to create a linear Expression:

```kotlin
val x1 = ContinuousVar("x1")
val x2 = ContinuousVar("x2")

val x1Plusx2: LinearExpr = x1 + x2
val x1Minusx2: LinearExpr = x1 - x2
```

You can add coefficients and constants.

```kotlin
val constantOffset: LinearExpr = x1 - 5.0
val coefficients: LinearExpr = -x2 * 3.0
```

You can combine `LinearExpr` objects with other `LinearExpr`s and other `ContinousVar`s.

```kotlin
val le1: LinearExpr = x1 * 5.0
val le2: LinearExpr = x2 * -10.0 + 7.0

// Represents 5*(x1) - 10*(x2) + 7
val combined: LinearExpr = le1 + le2
```

You can also convert individual variables to the `LinearExpr` type with a cast:

```kotlin
val x1 = ContinuousVar("x1")
val x1LPExpr = x1.toLinearExpr()
```

## 4. Constraints

KeLPie can handle both equalities and inequalities as constraints:

```kotlin
val x1 = ContinuousVar("x1")
val x2 = ContinuousVar("x2")

// Let x1 = x2
val equalityConstraint = EqConstraint(x1, x2)

// Let x1 >= x2
val gteConstraint = GteConstraint(x1, x2)

// Let x1 <= x2
val lteConstraint = LteConstraint(x1, x2)
```

These work just the same with `LinearExpr`s and `Double`s as arguments.

```kotlin
// Let x1 + x2 = 0
val equalityConstraint = EqConstraint(x1 + x2, 0.0)
```

## 5. Linear Programs

Now that we can construct linear expressions and linear constraints, we can finally construct our linear program:

```kotlin
val x1 = ContinuousVar("x1")
val x2 = ContinuousVar("x2")
val p = x1 + x2
val lp = LpSolver(
    maximize = p,
    subjectTo = arrayListOf(
        EqConstraint(x2, -x1 + 1.0),
        GteConstraint(x1, 0.0),
        GteConstraint(x2, 0.0)
    )
)
```

And solve it using the simplex algorithm:

```kotlin
val solution: ProblemSolution = lp.simplexSolve()
```

## 6. Linear Program Solutions

There are three different kinds of solutions:

1. A single answer; represents the solution which maximises the objective function.
2. No feasible solution; the Linear Program is infeasible.
3. Unbounded solution; the objective function can increase infinitely and is unbounded.

These are respectively represented with the following `ProgramSolution` child types:

1. `OptimalSolution`
2. `InfeasibleSolution`
3. `UnboundedSolution`

Only `OptimalSolution` has useful information inside it besides the type. You can then use the resulting
`OptimalSolution` object to ask questions about the solution.

You can retrieve the maximum objective function value using with the `objectiveValue` property.

```kotlin
val optimizationValue: Double = solution.objectiveValue
```

You can retrieve variable values if you have a reference to the `ContinuousVar`.

```kotlin
val x1 = ContinuousVar("x1")

// Get a solution here ...

val x1Result: Double = solution.getValue(x1)
```

You can also retrieve variable values if you know the name of the variable:

```kotlin
val x1Result: Double = solution.getValue("x1")
```

Some solution handling code may look like:

```kotlin
val solution: ProblemSolution = lp.simplexSolve()
when (solution) {
    is OptimalSolution -> {
        println("Optimal solution found!")
        prinln("Objective function had value: ${solution.objectiveValue}")
        println("And variable x1 had value: ${solution.valueOf("x1")}")
    }
    is InfeasibleSolution -> println("No solution found, problem is infeasible.")
    is UnboundedSolution -> println("No optimal solution found, objective is unbounded.")
}
```