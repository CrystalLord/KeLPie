# KeLPie: Basic Usage

## Review on Linear Programs

[Linear Programs](https://en.wikipedia.org/wiki/Linear_programming) are special case of mathematical models which have
requirements specified by linear relationships. Linear Programs are problems which can (but need not be)
expressed in the following canonical form:

![Wikipedia Linear Program](https://wikimedia.org/api/rest_v1/media/math/render/svg/639c4281a57140db9a4416ca58f9d9af14243bb0)

## Variables

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
Every ContinuousVar needs to have a name. If you create two `ContinuousVar`s with the same name, they will refer
to the same Linear Program variable despite being separate instances of ContinuousVar.

## Linear Expressions

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

## Linear Constraints

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
val expr = x1 + x2

// Let x1 + x2 = 0
val equalityConstraint = EqConstraint(expr, 0.0)
```

## Linear Programs

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

## Linear Program Solutions

There are three different kinds of solution types:

1. No feasible solution; the Linear Program is infeasible.
2. Unbounded solution; the objective function can increase infinitely and is unbounded.
3. A single answer; represents the solution which maximises the objective function.

These are respectively represented with the following `ProgramSolution` subtypes:

1. `InfeasibleSolution`
2. TODO
3. `FeasibleSolution`