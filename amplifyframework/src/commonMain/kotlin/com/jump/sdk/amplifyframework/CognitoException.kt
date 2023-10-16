package com.jump.sdk.amplifyframework

sealed class CognitoException(
    override val message: String,
) : Exception(message) {
    data object BadSrpB : CognitoException("Bad server public value 'B'")
    data object HashOfAAndSrpBCannotBeZero : CognitoException("Hash of A and B cannot be zero")
    data object UserPoolNameNotSet : CognitoException("Must call setUserPoolParams() before this")
    data object UserIdNotSet : CognitoException("Must call setUserPoolParams() before this")
}
