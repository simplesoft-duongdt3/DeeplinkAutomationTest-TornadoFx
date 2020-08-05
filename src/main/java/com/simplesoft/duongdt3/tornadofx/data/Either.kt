package com.simplesoft.duongdt3.tornadofx.data


/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 * FP Convention dictates that [Left] is used for "failure"
 * and [Right] is used for "success".
 *
 * @see Left
 * @see Right
 */
sealed class Either<out Fail, out SuccessResult> {
    /** * Represents the left side of [Either] class which by convention is a "Failure". */
    data class Fail<out L>(val a: L) : Either<L, Nothing>()

    /** * Represents the right side of [Either] class which by convention is a "Success". */
    data class Success<out R>(val b: R) : Either<Nothing, R>()

    val isSuccess get() = this is Success<SuccessResult>
    val isFailure get() = this is Either.Fail<Fail>

    fun <Fail> fail(a: Fail) = Either.Fail(a)
    fun <SuccessResult> success(b: SuccessResult) = Either.Success(b)

    fun either(failAction: (Fail) -> Unit, successAction: (SuccessResult) -> Unit) {
        when (this) {
            is Either.Fail -> failAction(a)
            is Success -> successAction(b)
        }
    }

    companion object {
        suspend fun <SuccessResult> runSuspendWithCatchError(
                action: suspend () -> (Either<Failure, SuccessResult>)
        ): Either<Failure, SuccessResult> {
            return try {
                action()
            } catch (e: Exception) {
                Fail(Failure.UnCatchError(e))
            }
        }
    }
}