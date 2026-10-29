package com.kamal.creditliability

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.compose.setContent
import com.kamal.creditliability.presentation.navigation.CreditLiabilityNavigation
import com.kamal.creditliability.ui.theme.CreditLiabilityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreditLiabilityTheme {
                CreditLiabilityNavigation()
            }
        }
    }
}
