package com.tcc.tarasulandroid.feature.login

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.LanguageManager
import com.tcc.tarasulandroid.di.LanguageManagerEntryPoint
import com.tcc.tarasulandroid.feature.login.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Get LanguageManager from the application context, not as ViewModel
    val languageManager = remember {
        (context.applicationContext as com.tcc.tarasulandroid.TarasulApplication).let {
            // Get from Hilt's entry point
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                it,
                LanguageManagerEntryPoint::class.java
            ).languageManager()
        }
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    // val context = LocalContext.current  <-- This is the duplicate declaration
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Show error snackbar
    if (uiState.errorMessage != null) {
        LaunchedEffect(uiState.errorMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Switcher Button
            OutlinedButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_translate),
                    contentDescription = stringResource(R.string.language)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (languageManager.getCurrentLanguage() == LanguageManager.LANGUAGE_ENGLISH)
                        stringResource(R.string.english)
                    else
                        stringResource(R.string.arabic)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Logo/Title
            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.sign_in_to_continue),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email)) },
                placeholder = { Text(stringResource(R.string.enter_email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(R.string.email)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !uiState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password)) },
                placeholder = { Text(stringResource(R.string.enter_password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "👁️" else "👁️‍🗨️",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        }
                    }
                ),
                singleLine = true,
                enabled = !uiState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Forgot Password
            TextButton(
                onClick = { /* TODO: Forgot password */ },
                modifier = Modifier.align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_in),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sign Up
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { /* TODO: Sign up */ },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.sign_up),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error Snackbar
        if (uiState.errorMessage != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            ) { Text(uiState.errorMessage ?: "") }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    languageManager.getAvailableLanguages().forEach { lang ->
                        TextButton(
                            onClick = {
                                languageManager.setLanguage(lang.code, activity)
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.flag, style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(lang.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenContent(
            email = "test@example.com",
            password = "password",
            passwordVisible = false,
            isLoading = false,
            errorMessage = null,
            currentLanguage = "en",
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onSignUpClick = {},
            onLanguageClick = {},
            onErrorDismiss = {}
        )
    }
}

@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    currentLanguage: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Switcher Button
            OutlinedButton(
                onClick = onLanguageClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_translate),
                    contentDescription = stringResource(R.string.language)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (currentLanguage == "en")
                        stringResource(R.string.english)
                    else
                        stringResource(R.string.arabic)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Logo/Title
            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.sign_in_to_continue),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email)) },
                placeholder = { Text(stringResource(R.string.enter_email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(R.string.email)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password)) },
                placeholder = { Text(stringResource(R.string.enter_password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Text(
                            text = if (passwordVisible) "👁️" else "👁️‍🗨️",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLoginClick()
                        }
                    }
                ),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Forgot Password
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_in),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sign Up
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onSignUpClick,
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(R.string.sign_up),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error Snackbar
        if (errorMessage != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onErrorDismiss) {
                        Text(stringResource(R.string.ok))
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}
