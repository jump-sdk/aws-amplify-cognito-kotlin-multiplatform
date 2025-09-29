package com.jump.sdk.amplifyframework

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

suspend fun HttpResponse.toCognitoException(): CognitoException? {
    return if (!isSuccess()) {
        val responseBody = bodyAsText()
        val retryAfterHeader = headers["Retry-After"]

        val exception = CognitoErrorParser.parseErrorResponse(responseBody, status.value)

        // Add retry-after information to TooManyRequestsException
        if (exception is CognitoException.TooManyRequestsException) {
            val retryAfterSeconds = CognitoErrorParser.parseRetryAfterHeader(retryAfterHeader)
            CognitoException.TooManyRequestsException(exception.originalMessage, retryAfterSeconds)
        } else {
            exception
        }
    } else {
        null
    }
}

fun HttpResponse.isSuccess(): Boolean {
    return status.value in 200..299
}

suspend fun HttpResponse.getCognitoErrorCode(): String? {
    return if (!isSuccess()) {
        val responseBody = bodyAsText()
        val exception = CognitoErrorParser.parseErrorResponse(responseBody, status.value)
        exception.errorCode
    } else {
        null
    }
}

suspend fun HttpResponse.getCognitoErrorMessage(): String? {
    return if (!isSuccess()) {
        val responseBody = bodyAsText()
        val exception = CognitoErrorParser.parseErrorResponse(responseBody, status.value)
        exception.message
    } else {
        null
    }
}

suspend fun HttpResponse.getRetryAfterSeconds(): Long? {
    return headers["Retry-After"]?.let { retryAfter ->
        CognitoErrorParser.parseRetryAfterHeader(retryAfter)
    }
}
