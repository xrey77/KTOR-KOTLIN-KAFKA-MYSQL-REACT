package com.kotlin.services

import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm

import qrcode.QRCode
import java.util.Base64

class TotpService {
    private val secretGenerator = DefaultSecretGenerator()

    private val timeProvider = SystemTimeProvider()
    private val codeGenerator = DefaultCodeGenerator(HashingAlgorithm.SHA1)
    private val codeVerifier = DefaultCodeVerifier(codeGenerator, timeProvider).apply {
        // Allows a 1-period window before/after to handle clock drift
        setAllowedTimePeriodDiscrepancy(1) 
    }


    fun generateSecret(): String {
        return secretGenerator.generate()
    }

    fun getQrCodeUrl(
        secret: String,
        accountName: String,
        issuer: String
    ): String {

        val qrCodeText =
            "otpauth://totp/$issuer:$accountName" +
            "?secret=$secret" +
            "&issuer=$issuer" +
            "&algorithm=SHA1" +
            "&digits=6" +
            "&period=30"

        val pngBytes = QRCode.ofSquares()
            .withCanvasSize(200)            
            .build(qrCodeText)
            .render()
            .getBytes()

        val base64String =
            Base64.getEncoder().encodeToString(pngBytes)

        return "data:image/png;base64,$base64String"
    }


   /**
     * Verifies the user-entered OTP against the secret.
     * Returns true if valid, false otherwise.
     */
    fun verifyOtp(secret: String, code: String): Boolean {
        // Clean input by removing spaces (common when users copy/paste)
        val cleanCode = code.replace("\\s".toRegex(), "")
        return codeVerifier.isValidCode(secret, cleanCode)
    }    
}
