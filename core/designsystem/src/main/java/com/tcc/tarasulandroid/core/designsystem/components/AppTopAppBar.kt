package com.tcc.tarasulandroid.core.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = title,
        modifier = modifier
    )
}

@Preview(name = "Top App Bar")
@Composable
fun AppTopAppBarPreview() {
    TarasulTheme {
        AppTopAppBar(title = { Text("Tarasul") })
    }
}
