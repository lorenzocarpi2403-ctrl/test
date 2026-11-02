package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.ExpenseCategory
import com.example.ui.ReceiptViewModel
import com.example.ui.ScanUiState
import com.example.ui.components.CategorySpendingChart
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.ManualExpenseDialog
import com.example.ui.components.ReceiptReviewDialog
import com.example.ui.components.SheetsSettingsDialog
import java.io.File
import java.io.InputStream
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allExpenses by viewModel.allExpenses.collectAsState()
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val userFeedback by viewModel.userFeedback.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(userFeedback) {
        userFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val bitmap = BitmapFactory.decodeFile(tempCameraFile!!.absolutePath)
            if (bitmap != null) {
                viewModel.analyzeReceipt(bitmap, tempCameraUri)
            }
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = decodeBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.analyzeReceipt(bitmap, uri)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ReceiptSheet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Expense & Sheets Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Export to CSV / Google Sheets action
                    IconButton(
                        onClick = {
                            viewModel.exportToCsv { intent ->
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Esporta CSV Google Sheets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Settings dialog (Sheet ID / Apps Script Webhook)
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Impostazioni Google Sheets",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Modern Bottom Floating Action Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Button
                    Button(
                        onClick = {
                            try {
                                val file = File.createTempFile("receipt_capture_", ".jpg", context.cacheDir)
                                tempCameraFile = file
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("camera_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scatta Foto", fontWeight = FontWeight.Bold)
                    }

                    // Gallery Photo Picker
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("gallery_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galleria", fontWeight = FontWeight.Bold)
                    }

                    // Manual Entry Button
                    IconButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("manual_add_button")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aggiungi Manualmente",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Overview Card
                item {
                    val totalMonth = allExpenses.sumOf { it.amount }
                    val unsyncedCount = allExpenses.count { !it.isSyncedToSheets }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Spese Totali Mese",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )

                                    if (unsyncedCount > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable { viewModel.syncAllUnsynced() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$unsyncedCount da inviare",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Sincronizzato",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "€${String.format(Locale.ITALIAN, "%.2f", totalMonth)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val avg = if (allExpenses.isNotEmpty()) totalMonth / allExpenses.size else 0.0
                                Text(
                                    text = "${allExpenses.size} scontrini registrati • Media €${String.format(Locale.ITALIAN, "%.2f", avg)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Category Spending Chart (if expenses exist)
                if (allExpenses.isNotEmpty()) {
                    item {
                        CategorySpendingChart(expenses = allExpenses)
                    }
                }

                // Search & Filter Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Cerca per esercente o note...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancella")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_field"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            singleLine = true
                        )

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel.onCategoryFilterChanged(null) },
                                label = { Text("Tutte (${allExpenses.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            ExpenseCategory.entries.forEach { cat ->
                                val count = allExpenses.count { it.category == cat.name }
                                if (count > 0) {
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = {
                                            viewModel.onCategoryFilterChanged(if (selectedCategory == cat) null else cat)
                                        },
                                        label = { Text("${cat.labelItalian} ($count)") },
                                        leadingIcon = {
                                            Icon(cat.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Expenses List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cronologia Spese",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (allExpenses.isNotEmpty()) {
                            Text(
                                text = "${filteredExpenses.size} mostrate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Empty State
                if (filteredExpenses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (searchQuery.isNotBlank() || selectedCategory != null)
                                        "Nessuna spesa trovata con questi filtri"
                                    else
                                        "Nessuno scontrino scansionato",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Scatta una foto a uno scontrino o selezionalo dalla galleria. Gemini AI estrarrà i dati ed esporterà tutto su Google Sheets!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Demo Receipt Button for easy instant testing
                                Button(
                                    onClick = {
                                        val sampleBitmap = createSampleReceiptBitmap()
                                        viewModel.analyzeReceipt(sampleBitmap, null)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Prova con uno Scontrino Esempio",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Expenses Items
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onSyncClick = { viewModel.syncExpense(expense) },
                            onDeleteClick = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }

            // Scanning AI Overlay
            if (scanState is ScanUiState.Scanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Analisi Gemini AI in corso...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sto leggendo esercente, importo totale, data e categoria dallo scontrino.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Review Dialog when AI extracts receipt data
    if (scanState is ScanUiState.ReviewReady) {
        val ready = scanState as ScanUiState.ReviewReady
        ReceiptReviewDialog(
            extractedReceipt = ready.extracted,
            bitmap = ready.capturedBitmap,
            imageUri = ready.imageUri,
            onDismiss = { viewModel.dismissScanState() },
            onConfirm = { merchant, amount, currency, category, date, notes, imageUri, syncToSheets ->
                viewModel.saveExpense(
                    merchant = merchant,
                    amount = amount,
                    currency = currency,
                    category = category,
                    date = date,
                    notes = notes,
                    imageUri = imageUri,
                    syncToSheets = syncToSheets
                )
            }
        )
    }

    // Error Alert Dialog if scanning failed or timed out, allowing user to continue with photo
    if (scanState is ScanUiState.ErrorWithFallback) {
        val errorState = scanState as ScanUiState.ErrorWithFallback
        AlertDialog(
            onDismissRequest = { viewModel.dismissScanState() },
            title = { Text("Scansione Scontrino") },
            text = {
                Text("${errorState.message}\n\nVuoi aprire la schermata di revisione per inserire l'importo mantenendo la foto scattata?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.proceedWithManualFallback(errorState.capturedBitmap, errorState.imageUri)
                    }
                ) {
                    Text("Continua con Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissScanState() }) {
                    Text("Chiudi")
                }
            }
        )
    }

    // Google Sheets Settings Dialog
    if (showSettingsDialog) {
        SheetsSettingsDialog(
            initialSheetId = viewModel.sheetsSyncService.sheetId,
            initialWebhookUrl = viewModel.sheetsSyncService.webhookUrl,
            initialSheetName = viewModel.sheetsSyncService.sheetName,
            onDismiss = { showSettingsDialog = false },
            onSave = { sheetId, webhookUrl, sheetName ->
                viewModel.saveSheetSettings(sheetId, webhookUrl, sheetName)
                showSettingsDialog = false
            }
        )
    }

    // Manual Expense Dialog
    if (showManualDialog) {
        ManualExpenseDialog(
            onDismiss = { showManualDialog = false },
            onSave = { merchant, amount, currency, category, date, notes, syncToSheets ->
                viewModel.saveExpense(
                    merchant = merchant,
                    amount = amount,
                    currency = currency,
                    category = category,
                    date = date,
                    notes = notes,
                    imageUri = null,
                    syncToSheets = syncToSheets
                )
                showManualDialog = false
            }
        )
    }
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (_: Exception) {
        null
    }
}

/**
 * Creates a synthetic demo receipt bitmap with authentic receipt text for testing without a real physical receipt.
 */
private fun createSampleReceiptBitmap(): Bitmap {
    val width = 600
    val height = 900
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 28f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
    }

    val headerPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 34f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    canvas.drawText("SUPERMERCATO CONAD", width / 2f, 80f, headerPaint)
    canvas.drawText("VIA ROMA 124 - MILANO", width / 2f, 130f, paint.apply { textAlign = Paint.Align.CENTER; textSize = 22f })
    canvas.drawText("DOCUMENTO COMMERCIALE", width / 2f, 170f, paint)
    canvas.drawText("---------------------------------", width / 2f, 210f, paint)

    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 24f

    var y = 260f
    canvas.drawText("PANE INTEGRALE 500G        € 2.80", 50f, y, paint); y += 45f
    canvas.drawText("LATTE FRESCO 1L            € 1.65", 50f, y, paint); y += 45f
    canvas.drawText("PASTA BARILLA 1KG          € 1.89", 50f, y, paint); y += 45f
    canvas.drawText("CAFFE LAVAZZA 250G         € 3.90", 50f, y, paint); y += 45f
    canvas.drawText("MELE GOLDEN 1.5KG          € 3.20", 50f, y, paint); y += 45f
    canvas.drawText("---------------------------------", 50f, y, paint); y += 50f

    val totalPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    canvas.drawText("TOTALE COMPLESSIVO: € 13.44", 50f, y, totalPaint); y += 60f

    canvas.drawText("PAGAMENTO: BANCOMAT € 13.44", 50f, y, paint); y += 45f
    canvas.drawText("DATA: 02/11/2026 ORA: 14:15", 50f, y, paint); y += 45f
    canvas.drawText("GRAZIE E ARRIVEDERCI!", width / 2f, y + 40f, paint.apply { textAlign = Paint.Align.CENTER })

    return bitmap
}
