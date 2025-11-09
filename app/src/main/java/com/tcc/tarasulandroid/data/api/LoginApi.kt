package com.tcc.tarasulandroid.data.api

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val user: User? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

/**
 * Mock Login API for testing
 * Simulates a real API with network delay
 */
@Singleton
class LoginApi @Inject constructor() {

    companion object {
        // Mock credentials for testing
        private val MOCK_USERS = mapOf(
            "test@example.com" to "password123",
            "john@example.com" to "john123",
            "admin@tarasul.com" to "admin123"
        )
    }

    /**
     * Mock login function
     * Accepts test@example.com / password123
     * Or any email with password: password123
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        // Simulate network delay
        delay(1500L)

        // Validate email format
        if (!isValidEmail(request.email)) {
            return LoginResponse(
                success = false,
                message = "Invalid email format"
            )
        }

        // Check if password is empty
        if (request.password.isEmpty()) {
            return LoginResponse(
                success = false,
                message = "Password cannot be empty"
            )
        }

        // Check mock credentials
        val storedPassword = MOCK_USERS[request.email]
        if (storedPassword != null && storedPassword == request.password) {
            // Successful login
            val userName = request.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            return LoginResponse(
                success = true,
                token = generateMockToken(),
                message = "Login successful",
                user = User(
                    id = generateUserId(request.email),
                    name = userName,
                    email = request.email
                )
            )
        }

        // For demo purposes: accept any email with password "password123"
        if (request.password == "password123") {
            val userName = request.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            return LoginResponse(
                success = true,
                token = generateMockToken(),
                message = "Login successful",
                user = User(
                    id = generateUserId(request.email),
                    name = userName,
                    email = request.email
                )
            )
        }

        // Invalid credentials
        return LoginResponse(
            success = false,
            message = "Invalid email or password"
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun generateMockToken(): String {
        return "mock_token_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun generateUserId(email: String): String {
        return "user_${email.hashCode().toString().replace("-", "")}"
    }
}
