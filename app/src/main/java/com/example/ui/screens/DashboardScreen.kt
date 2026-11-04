package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.TransactionType
import com.example.ui.ReceiptViewModel
import com.example.ui.ScanUiState
import com.example.ui.TransactionFilter
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.MonthlyBudgetCard
import com.example.ui.components.ReceiptReviewDialog
import com.example.ui.components.SetMonthlyBudgetDialog
import com.example.ui.components.ThemeCustomizationDialog
import com.example.ui.components.TransactionItemCard
import java.io.File
import java.io.InputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }

    val userFeedback by viewModel.userFeedback.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val themeConfig by viewModel.themeConfig.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showManualAddDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
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
            val bitmap = decodeSampledAndRotatedBitmap(tempCameraFile!!.absolutePath)
            if (bitmap != null) {
                viewModel.analyzeReceipt(bitmap, tempCameraUri)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Impossibile caricare la foto scattata dalla fotocamera.")
                }
            }
        }
    }

    fun launchCameraCapture() {
        try {
            val file = File.createTempFile("receipt_capture_", ".jpg", context.cacheDir)
            tempCameraFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Impossibile aprire la fotocamera: ${e.localizedMessage ?: "Errore"}")
            }
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraCapture()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permesso fotocamera necessario per scattare foto. Puoi concederlo o usare la Galleria.")
            }
        }
    }

    fun checkAndLaunchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                                imageVector = when (currentTab) {
                                    1 -> Icons.Default.AccountBalanceWallet
                                    2 -> Icons.Default.Repeat
                                    3 -> Icons.Default.BarChart
                                    else -> Icons.Default.ReceiptLong
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (currentTab) {
                                    1 -> "I Tuoi Conti"
                                    2 -> "Transazioni Fisse & Ricorrenti"
                                    3 -> "Statistiche & Grafici"
                                    else -> "Finanze & Spese"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentTab) {
                                    1 -> "Gestione saldi e patrimonio"
                                    2 -> "Affitto, stipendio, bollette e abbonamenti"
                                    3 -> "Entrate, uscite e analisi mensile"
                                    else -> "Scanner scontrini AI & Transazioni"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("theme_customization_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Personalizza Colori e Tema",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transazioni") },
                    label = { Text("Transazioni") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Conti") },
                    label = { Text("Conti") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Ricorrenti") },
                    label = { Text("Ricorrenti") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistiche") },
                    label = { Text("Statistiche") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    // TRANSACTIONS SCREEN
                    TransactionsTabContent(
                        viewModel = viewModel,
                        onScanCameraClick = {
                            checkAndLaunchCamera()
                        },
                        onScanGalleryClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onAddManualClick = { showManualAddDialog = true }
                    )
                }
                1 -> AccountsScreen(viewModel = viewModel)
                2 -> RecurringTransactionsScreen(viewModel = viewModel)
                3 -> StatisticsScreen(viewModel = viewModel)
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
            accounts = accounts,
            onDismiss = { viewModel.dismissScanState() },
            onConfirm = { merchant, amount, currency, category, accountId, date, notes, imageUri ->
                viewModel.addTransaction(
                    type = TransactionType.EXPENSE,
                    title = merchant,
                    amount = amount,
                    currency = currency,
                    category = category,
                    accountId = accountId,
                    date = date,
                    notes = notes,
                    imageUri = imageUri
                )
            }
        )
    }

    // Error Alert Dialog with fallback
    if (scanState is ScanUiState.ErrorWithFallback) {
        val errorState = scanState as ScanUiState.ErrorWithFallback
        AlertDialog(
            onDismissRequest = { viewModel.dismissScanState() },
            title = { Text("Scansione Scontrino") },
            text = {
                Text("${errorState.message}\n\nVuoi aprire la schermata di revisione per inserire i dati mantenendo la foto scattata?")
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

    // Add Manual Transaction Dialog (Spesa o Entrata)
    if (showManualAddDialog) {
        AddTransactionDialog(
            accounts = accounts,
            onDismiss = { showManualAddDialog = false },
            onSave = { type, title, amount, category, accountId, date, notes ->
                viewModel.addTransaction(
                    type = type,
                    title = title,
                    amount = amount,
                    category = category,
                    accountId = accountId,
                    date = date,
                    notes = notes
                )
                showManualAddDialog = false
            }
        )
    }

    // Theme & Color Customization Dialog
    if (showThemeDialog) {
        ThemeCustomizationDialog(
            currentConfig = themeConfig,
            onColorThemeSelected = { colorTheme -> viewModel.setColorTheme(colorTheme) },
            onBackgroundThemeSelected = { bgTheme -> viewModel.setBackgroundTheme(bgTheme) },
            onThemeModeSelected = { mode -> viewModel.setThemeMode(mode) },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun TransactionsTabContent(
    viewModel: ReceiptViewModel,
    onScanCameraClick: () -> Unit,
    onScanGalleryClick: () -> Unit,
    onAddManualClick: () -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val budgetProgress by viewModel.budgetProgress.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }

    val totalIncome = remember(allTransactions) {
        allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(allTransactions) {
        allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = remember(totalIncome, totalExpense) {
        totalIncome - totalExpense
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Net Balance Card
        item {
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
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Bilancio Netto (Entrate - Spese)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (netBalance >= 0) "+" else ""}€${String.format(Locale.ITALIAN, "%.2f", netBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netBalance >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFF059669).copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Entrate", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("+€${String.format(Locale.ITALIAN, "%.2f", totalIncome)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFDC2626).copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Spese", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("-€${String.format(Locale.ITALIAN, "%.2f", totalExpense)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monthly Spending Limit (Budget) Tracking Card
        item {
            MonthlyBudgetCard(
                budgetProgress = budgetProgress,
                onOpenBudgetSettings = { showBudgetDialog = true }
            )
        }

        // Quick Action Bar: Camera AI, Gallery, Manual (+)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onScanCameraClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("camera_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scatta Foto", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onScanGalleryClick,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("gallery_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galleria", fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onAddManualClick,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("manual_add_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aggiungi Manuale",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Search & Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Cerca per titolo, negozio o note...") },
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

                // Type Filter Chips: Tutte / Spese / Entrate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = typeFilter == filter,
                            onClick = { viewModel.onTypeFilterChanged(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    // Category filter reset if active
                    if (selectedCategory != null) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onCategoryFilterChanged(null) },
                            label = { Text("Categoria: ${selectedCategory?.labelItalian} ✕") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Movimenti Recenti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredTransactions.size} registrati",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Nessuna transazione trovata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Scatta una foto a uno scontrino con l'IA oppure aggiungi manualmente una spesa o un'entrata.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val sampleBitmap = createSampleReceiptBitmap()
                                viewModel.analyzeReceipt(sampleBitmap, null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Prova con Scontrino Esempio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { item ->
                TransactionItemCard(
                    transaction = item,
                    accounts = accounts,
                    onDeleteClick = { viewModel.deleteTransaction(item) }
                )
            }
        }
    }

    if (showBudgetDialog) {
        SetMonthlyBudgetDialog(
            currentLimit = monthlyBudget?.monthlyLimit,
            onDismiss = { showBudgetDialog = false },
            onSave = { limit -> viewModel.setMonthlyBudget(limit) },
            onRemove = { viewModel.clearMonthlyBudget() }
        )
    }
}

private fun decodeSampledAndRotatedBitmap(filePath: String): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, boundsOptions)

        val maxDimension = 2048
        var inSampleSize = 1
        if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
            val halfHeight: Int = boundsOptions.outHeight / 2
            val halfWidth: Int = boundsOptions.outWidth / 2
            while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val decoded = BitmapFactory.decodeFile(filePath, decodeOptions) ?: return null

        val exif = android.media.ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            android.media.ExifInterface.TAG_ORIENTATION,
            android.media.ExifInterface.ORIENTATION_NORMAL
        )
        val rotationDegrees = when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        } else {
            decoded
        }
    } catch (_: Exception) {
        try {
            BitmapFactory.decodeFile(filePath)
        } catch (_: Exception) {
            null
        }
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
