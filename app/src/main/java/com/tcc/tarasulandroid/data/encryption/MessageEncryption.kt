package com.tcc.tarasulandroid.data.encryption

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Message encryption/decryption utility for End-to-End encryption
 * Uses AES-256-GCM for authenticated encryption
 * 
 * NOTE: This is a simplified implementation. In production, you should:
 * 1. Use a proper key exchange protocol (e.g., Signal Protocol, Double Ratchet)
 * 2. Store keys securely in Android KeyStore
 * 3. Implement proper key rotation
 * 4. Use forward secrecy
 */
object MessageEncryption {
    
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    
    /**
     * Generate a new AES-256 key for encryption
     * In production, use key exchange protocol instead
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }
    
    /**
     * Convert SecretKey to Base64 string for storage
     */
    fun keyToString(key: SecretKey): String {
        return Base64.encodeToString(key.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string back to SecretKey
     */
    fun stringToKey(keyString: String): SecretKey {
        val decodedKey = Base64.decode(keyString, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
    
    /**
     * Encrypt a message using AES-256-GCM
     * Returns: Base64(IV + EncryptedData + AuthTag)
     */
    fun encrypt(plaintext: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        // Generate random IV for each message
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // Combine IV + encrypted data for storage
        val combined = iv + encryptedBytes
        
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt a message using AES-256-GCM
     * Input: Base64(IV + EncryptedData + AuthTag)
     */
    fun decrypt(ciphertext: String, key: SecretKey): String {
        val combined = Base64.decode(ciphertext, Base64.NO_WRAP)
        
        // Extract IV and encrypted data
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return try {
            Cipher.getInstance(TRANSFORMATION)
            true
        } catch (e: Exception) {
            false
        }
    }
}
