package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppLanguage
import com.example.ui.AppTab
import com.example.ui.XmlWatermarkViewModel
import com.example.ui.tabs.HistoryTab
import com.example.ui.tabs.PhotoEraserTab
import com.example.ui.tabs.XmlCleanerTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight

class MainActivity : ComponentActivity() {

    private val viewModel: XmlWatermarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: XmlWatermarkViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val isHindi = language == AppLanguage.HINDI
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showHelpDialog) {
        HelpGuideDialog(
            isHindi = isHindi,
            onDismiss = { showHelpDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "XML वाटरमार्क रिमूवर" else "XML Watermark Remover",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "Alight Motion, CapCut & Photo Cleaner" else "Alight Motion, CapCut & Photo Cleaner",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher Button
                    Surface(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("btn_language_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryBlueLight
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "ENG" else "हिंदी",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Help button
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.testTag("btn_help_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = if (isHindi) "मदद" else "Help",
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
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.XML_CLEANER,
                    onClick = { viewModel.setTab(AppTab.XML_CLEANER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = if (isHindi) "XML क्लीनर" else "XML Cleaner"
                        )
                    },
                    label = { Text(if (isHindi) "XML रिमूवर" else "XML Cleaner", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryBlue.copy(alpha = 0.2f),
                        selectedIconColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlueLight
                    ),
                    modifier = Modifier.testTag("nav_tab_xml")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PHOTO_ERASER,
                    onClick = { viewModel.setTab(AppTab.PHOTO_ERASER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = if (isHindi) "फोटो वाटरमार्क" else "Photo Eraser"
                        )
                    },
                    label = { Text(if (isHindi) "फोटो इरेज़र" else "Photo Eraser", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryBlue.copy(alpha = 0.2f),
                        selectedIconColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlueLight
                    ),
                    modifier = Modifier.testTag("nav_tab_photo")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = if (isHindi) "इतिहास" else "History"
                        )
                    },
                    label = { Text(if (isHindi) "इतिहास" else "History", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryBlue.copy(alpha = 0.2f),
                        selectedIconColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlueLight
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    AppTab.XML_CLEANER -> XmlCleanerTab(viewModel = viewModel)
                    AppTab.PHOTO_ERASER -> PhotoEraserTab(viewModel = viewModel)
                    AppTab.HISTORY -> HistoryTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HelpGuideDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHindi) "वाटरमार्क कैसे हटाएं? (Guide)" else "How to Remove Watermarks",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = if (isHindi)
                        "1. XML रिमूवर:\n• Alight Motion, CapCut या Premiere का XML कोड पेस्ट करें या 'फाइल खोलें' दबाएं।\n• ऐप अपने आप @username, वाटरमार्क लेयर्स, सोशल लिंक्स और क्रेडिट्स ढूंढ लेगा।\n• 'XML साफ करें' दबाएं और क्लीन XML कॉपी या सेव करें।\n\n2. फोटो वाटरमार्क:\n• 'फोटो इरेज़र' टैब में जाएं।\n• वाटरमार्क टेक्स्ट (जैसे SAME SOUL, हर हर महादेव) पर ब्रश चलाएं या 1-टैप चिप दबाएं।\n• 'वाटरमार्क मिटाएं' दबाएं, फोटो अपने आप इनपेंट होकर साफ हो जाएगी।"
                    else
                        "1. XML Cleaner:\n• Paste any XML preset (Alight Motion, CapCut, Premiere) or import a .xml file.\n• The engine auto-detects creator handles, watermark shapes, and credit layers.\n• Tap 'Clean XML' to strip watermarks and export or copy clean code.\n\n2. Photo Eraser:\n• Switch to Photo Eraser tab.\n• Brush over any watermark or logo, or use 1-tap quick clean.\n• Tap 'Erase Watermark' to inpaint and remove the watermark cleanly.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "समझ गया" else "Got It")
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

