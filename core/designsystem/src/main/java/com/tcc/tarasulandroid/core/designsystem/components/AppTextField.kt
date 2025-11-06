package com.tcc.tarasulandroid.core.designsystem.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
    )
}

@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
    )
}

@Preview(name = "Text Field")
@Composable
fun AppTextFieldPreview() {
    TarasulTheme {
        AppTextField(value = "Filled", onValueChange = {})
    }
}

@Preview(name = "Outlined Text Field")
@Composable
fun AppOutlinedTextFieldPreview() {
    TarasulTheme {
        AppOutlinedTextField(value = "Outlined", onValueChange = {})
    }
}
