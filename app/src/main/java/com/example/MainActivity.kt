package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.ReceiptViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.ReceiptSheetTheme

class MainActivity : ComponentActivity() {

    private val receiptViewModel: ReceiptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration before ViewModels or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            ReceiptSheetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(viewModel = receiptViewModel)
                }
            }
        }
    }
}
