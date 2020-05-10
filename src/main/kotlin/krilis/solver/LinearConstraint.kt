package krilis.solver

interface LinearConstraint {
    val lhs: LinearExpr
    fun negate(): LinearConstraint
}

class EqConstraint : LinearConstraint {
    override val lhs: LinearExpr

    constructor(lhs: LinearExpr) {
        this.lhs = lhs
    }

    constructor(lhs: ContinuousVar, rhs: ContinuousVar) {
        this.lhs = lhs - rhs
    }

    constructor(lhs: LinearExpr, rhs: LinearExpr) {
        this.lhs = lhs - rhs
    }

    constructor(lhs: LinearExpr, rhs: Double) {
        this.lhs = lhs - rhs
    }

    constructor(lhs: LinearExpr, rhs: ContinuousVar) {
        this.lhs = lhs - rhs
    }

    override fun negate(): EqConstraint {
        return EqConstraint(-this.lhs)
    }
}

class GteConstraint : LinearConstraint {
    override val lhs: LinearExpr

    constructor(lhs: LinearExpr) {
        this.lhs = lhs
    }

    constructor(lhs: LinearExpr, rhs: LinearExpr) {
        this.lhs = lhs - rhs
    }

    constructor(lhs: LinearExpr, rhs: Double) {
        this.lhs = lhs - rhs
    }

    override fun negate(): LteConstraint {
        return LteConstraint(-this.lhs)
    }
}

class LteConstraint : LinearConstraint {
    override val lhs: LinearExpr

    constructor(lhs: LinearExpr) {
        this.lhs = lhs
    }

    constructor(lhs: LinearExpr, rhs: LinearExpr) {
        this.lhs = lhs - rhs
    }

    constructor(lhs: LinearExpr, rhs: Double) {
        this.lhs = lhs - rhs
    }

    override fun negate(): GteConstraint {
        return GteConstraint(-this.lhs)
    }
}
