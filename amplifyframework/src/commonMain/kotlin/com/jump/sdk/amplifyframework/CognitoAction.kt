package com.jump.sdk.amplifyframework

enum class CognitoAction(val headerValue: String) {
    CONFIRM_FORGOT_PASSWORD("AWSCognitoIdentityProviderService.ConfirmForgotPassword"),
    CONFIRM_SIGN_UP("AWSCognitoIdentityProviderService.ConfirmSignUp"),
    FORGOT_PASSWORD("AWSCognitoIdentityProviderService.ForgotPassword"),
    INITIATE_AUTH("AWSCognitoIdentityProviderService.InitiateAuth"),
    RESPOND_TO_AUTH_CHALLENGE("AWSCognitoIdentityProviderService.RespondToAuthChallenge"),
    RESEND_CONFIRMATION_CODE("AWSCognitoIdentityProviderService.ResendConfirmationCode"),
    SIGN_UP("AWSCognitoIdentityProviderService.SignUp"),
}
