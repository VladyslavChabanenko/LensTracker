package com.chb.lenstracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chb.lenstracker.ui.screen.HomeScreen
import com.chb.lenstracker.ui.theme.LensTrackerTheme

@Composable
@Preview
fun App() {
    LensTrackerTheme {
        HomeScreen()
    }
}