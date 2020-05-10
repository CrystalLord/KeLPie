package krilis.solver.utils

import kotlin.math.abs
import kotlin.math.pow

// This is 2.0.pow(-50.0), which is 2 bits away from the smallest increment near 1.0 (52 bit fraction in IEEE 754)
const val DEFAULT_ABS_TOLERANCE: Double = 4.440892098500626e-16

fun withinAbsTolerance(lhs: Double, rhs: Double, tol: Double = DEFAULT_ABS_TOLERANCE): Boolean {
    return abs(lhs - rhs) <= tol
}
