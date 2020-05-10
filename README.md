# KeLPie: The Human Linear Program Library

Usage in 5 minutes:

```kotlin
// Example problem from Professor James Jones at Richland University
// URL: https://people.richland.edu/james/ictcm/2006/

val x1 = ContinuousVar("x1")
val x2 = ContinuousVar("x2")
val p = 40.0*x1 + 30.0*x2
val solver = LpSolver(
        p,
        arrayListOf(
                LteConstraint(x1 + 2.0*x2, 16.0),
                LteConstraint(x1 + x2, 9.0),
                LteConstraint(3.0*x1 + 2.0*x2, 24.0),
                GteConstraint(x1.toLinearExpr(), 0.0),
                GteConstraint(x2.toLinearExpr(), 0.0)
        )
)
val solution: ProblemSolution = solver.simplexSolve()
if (solution is FeasibleSolution) {
    println(solution.getValue(x1))
    // Prints "6.0"
}
```