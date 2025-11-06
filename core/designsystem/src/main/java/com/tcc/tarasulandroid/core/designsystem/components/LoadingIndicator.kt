package com.tcc.tarasulandroid.core.designsystem.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Preview(name = "Loading Indicator")
@Composable
fun LoadingIndicatorPreview() {
    TarasulTheme {
        LoadingIndicator()
    }
}
