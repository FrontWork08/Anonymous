package com.example.data

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // 16-byte key for AES-128
    private val keyBytes = "RevelaSecureChat".toByteArray(Charsets.UTF_8)
    // 16-byte fixed IV (Initialization Vector)
    private val ivBytes = "RevelaInitVector".toByteArray(Charsets.UTF_8)

    /**
     * Criptografa o texto usando AES/CBC/PKCS5Padding.
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        return try {
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Decriptografa o texto cifrado em Base64 usando AES/CBC/PKCS5Padding.
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return encryptedText
        return try {
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText // Retorna o texto original caso ocorra um erro de decodificação
        }
    }
}
