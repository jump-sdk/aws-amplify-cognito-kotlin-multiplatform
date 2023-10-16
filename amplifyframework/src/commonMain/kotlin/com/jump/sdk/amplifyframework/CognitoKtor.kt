package com.jump.sdk.amplifyframework

import io.ktor.http.ContentType

// Configure Ktor HttpClient with:
// install(ContentNegotiation) {
//    json(
//        Json { ... },
//        contentType = AmzJson,
//    )
// }
// And then call with:
// client.post(your cognito endpoint) {
//    contentType(AmzJson)
//    header(AmzActionHeader, CognitoAction.headerValue)
//    setBody(requestBody)
// }

object CognitoKtor {
    val AmzJson = ContentType("application", "x-amz-json-1.1")
    const val AmzTargetHeader = "X-Amz-Target"
}
