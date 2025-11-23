package com.tcc.tarasulandroid.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.feature.login.viewmodels.OtpViewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.delay
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    navController: NavController,
    viewModel: OtpViewModel = hiltViewModel(),
    otpLength: Int = 4
) {
    var otp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resendCounter by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    var otpDigits by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = List(otpLength) { remember { FocusRequester() } }
    // val focusManager = LocalFocusManager.current // Unused, so commented out

    // Resend timer
    LaunchedEffect(canResend) {
        if (!canResend) {
            resendCounter = 30
            repeat(30) {
                delay(1000)
                resendCounter--
            }
            canResend = true
        }
    }

    // Handle OTP verification result
    LaunchedEffect(Unit) {
        viewModel.otpResult.collect { success ->
            if (success) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                isLoading = false
                errorMessage = "Invalid OTP. Please try again."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.verify_otp)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(R.string.enter_verification_code),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(R.string.otp_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // OTP Input Field using custom Row of TextFields
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                                val updated = otpDigits.toMutableList()
                                updated[index] = newValue
                                otpDigits = updated
                                otp = otpDigits.joinToString("")
                                errorMessage = null
                                if (newValue.isNotEmpty() && index < otpLength - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                } else if (newValue.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .width(56.dp)
                            .focusRequester(focusRequesters[index])
                            .padding(horizontal = 4.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (index == otpLength - 1) ImeAction.Done else ImeAction.Next
                        ),
                        visualTransformation = VisualTransformation.None,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Verify Button
            Button(
                onClick = {
                    if (otp.length == otpLength) {
                        isLoading = true
                        viewModel.verifyOtp(otp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = otp.length == otpLength && !isLoading,
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
                        text = stringResource(R.string.verify),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Code Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (canResend) {
                    TextButton(
                        onClick = {
                            canResend = false
                            otp = ""
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.resend_code),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.resend_code_in, (30 - resendCounter).toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note for testing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.otp_test_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
