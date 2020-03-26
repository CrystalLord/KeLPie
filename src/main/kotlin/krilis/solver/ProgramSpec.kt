package krilis.solver

class ProgramSpec {
    private val equalityConstraints: ArrayList<LinearExpr>

    fun eqConstraint(lhs: LinearExpr, rhs: LinearExpr) {
        val simplifiedExpr: LinearExpr = lhs - rhs
        this.equalityConstraints.append(simplifiedExpr)
    }

    // TODO
    fun gteConstraint(lhs: LinearExpr, rhs: LinearExpr)

    // TODO
    fun lteConstraint(lhs: LinearExpr, rhs: LinearExpr)
}

