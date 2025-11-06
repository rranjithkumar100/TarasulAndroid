package com.tcc.tarasulandroid.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme

@Composable
fun PreviewGallery(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("This is a sample Text component")
        Spacer(modifier = Modifier.height(16.dp))
        AppButton(onClick = {}) {
            Text("Primary Button")
        }
        Spacer(modifier = Modifier.height(16.dp))
        AppSecondaryButton(onClick = {}) {
            Text("Secondary Button")
        }
        Spacer(modifier = Modifier.height(16.dp))
        AppTextField(value = "Filled", onValueChange = {})
        Spacer(modifier = Modifier.height(16.dp))
        AppOutlinedTextField(value = "Outlined", onValueChange = {})
        Spacer(modifier = Modifier.height(16.dp))
        AppTopAppBar(title = { Text("Tarasul") })
        Spacer(modifier = Modifier.height(16.dp))
        LoadingIndicator()
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
fun PreviewGalleryLight() {
    TarasulTheme(darkTheme = false) {
        PreviewGallery(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewGalleryDark() {
    TarasulTheme(darkTheme = true) {
        PreviewGallery(modifier = Modifier.padding(16.dp))
    }
}
