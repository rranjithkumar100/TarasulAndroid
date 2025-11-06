package com.tcc.tarasulandroid.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}

@Composable
fun AppSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}

@Preview(name = "Primary Button")
@Composable
fun AppButtonPreview() {
    TarasulTheme {
        AppButton(onClick = {}) {
            Text("Primary Button")
        }
    }
}

@Preview(name = "Secondary Button")
@Composable
fun AppSecondaryButtonPreview() {
    TarasulTheme {
        AppSecondaryButton(onClick = {}) {
            Text("Secondary Button")
        }
    }
}
