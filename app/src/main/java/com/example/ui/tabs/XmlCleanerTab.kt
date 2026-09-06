package com.example.ui.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.SampleXmlPresets
import com.example.model.CleanOptions
import com.example.ui.AppLanguage
import com.example.ui.DiffMode
import com.example.ui.XmlWatermarkViewModel
import com.example.ui.components.CodeDiffViewer
import com.example.ui.components.WatermarkItemCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun XmlCleanerTab(
    viewModel: XmlWatermarkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val language by viewModel.language.collectAsStateWithLifecycle()
    val isHindi = language == AppLanguage.HINDI

    val xmlInput by viewModel.xmlInput.collectAsStateWithLifecycle()
    val activePresetName by viewModel.activePresetName.collectAsStateWithLifecycle()
    val detectedItems by viewModel.detectedItems.collectAsStateWithLifecycle()
    val cleanOptions by viewModel.cleanOptions.collectAsStateWithLifecycle()
    val cleanResult by viewModel.cleanResult.collectAsStateWithLifecycle()
    val diffMode by viewModel.diffMode.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    var showPresetMenu by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var customKeywordInput by remember { mutableStateOf("") }
    var customReplaceInput by remember { mutableStateOf(cleanOptions.customReplacementText) }

    // File Import launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().readText()
                    viewModel.onXmlInputChanged(content)
                    viewModel.showToast(if (isHindi) "XML फाइल लोड हो गई" else "XML file imported")
                }
            } catch (e: Exception) {
                viewModel.showToast(if (isHindi) "फाइल पढ़ने में त्रुटि" else "Failed to read file: ${e.message}")
            }
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri: Uri? ->
        if (uri != null && cleanResult != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(cleanResult!!.cleanedXml.toByteArray(Charsets.UTF_8))
                    viewModel.showToast(if (isHindi) "साफ XML फाइल सेव हो गई!" else "Clean XML saved successfully!")
                }
            } catch (e: Exception) {
                viewModel.showToast(if (isHindi) "सेव करने में त्रुटि" else "Export failed: ${e.message}")
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Action Card: Preset Selector, Import, Paste
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "प्रोजेक्ट XML फाइल" else "Project XML Preset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Preset Dropdown Button
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { showPresetMenu = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activePresetName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showPresetMenu,
                                    onDismissRequest = { showPresetMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Alight Motion Beat Shake (XML)") },
                                        onClick = {
                                            viewModel.loadPreset("Alight Motion Beat Shake", SampleXmlPresets.ALIGHT_MOTION_PRESET)
                                            showPresetMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("CapCut / Premiere XML Sequence") },
                                        onClick = {
                                            viewModel.loadPreset("Premiere XML Preset", SampleXmlPresets.CAPCUT_PREMIERE_PRESET)
                                            showPresetMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Vector SVG Artwork Preset") },
                                        onClick = {
                                            viewModel.loadPreset("Vector SVG Preset", SampleXmlPresets.SVG_WATERMARK_PRESET)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Import & Paste Quick Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Paste from Clipboard
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val text = clip.getItemAt(0).text?.toString() ?: ""
                                        if (text.isNotBlank()) {
                                            viewModel.onXmlInputChanged(text)
                                            viewModel.showToast(if (isHindi) "XML पेस्ट हो गया!" else "XML pasted!")
                                        }
                                    } else {
                                        viewModel.showToast(if (isHindi) "क्लिपबोर्ड खाली है" else "Clipboard is empty")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_paste_xml")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "पेस्ट" else "Paste", fontSize = 12.sp)
                            }

                            // Import .xml
                            Button(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("text/xml", "application/xml", "text/*", "*/*"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_import_file")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "फाइल खोलें" else "Open", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editable XML input preview / expandable text
                    OutlinedTextField(
                        value = xmlInput,
                        onValueChange = { viewModel.onXmlInputChanged(it) },
                        label = { Text(if (isHindi) "XML कोड / प्रीसेट" else "XML Code / Preset Text") },
                        maxLines = 6,
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xml_input_field")
                    )
                }
            }
        }

        // 2. Watermark Removal Options Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOptionsSheet = !showOptionsSheet },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PrimaryBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "वाटरमार्क हटाने के नियम (Options)" else "Cleaning Rules & Options",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (showOptionsSheet) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Always visible quick filter chips
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = cleanOptions.removeTextWatermarks,
                            onClick = {
                                viewModel.updateCleanOptions(cleanOptions.copy(removeTextWatermarks = !cleanOptions.removeTextWatermarks))
                            },
                            label = { Text(if (isHindi) "टेक्स्ट वाटरमार्क" else "Text Watermarks", fontSize = 12.sp) },
                            leadingIcon = if (cleanOptions.removeTextWatermarks) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = cleanOptions.removeWatermarkShapes,
                            onClick = {
                                viewModel.updateCleanOptions(cleanOptions.copy(removeWatermarkShapes = !cleanOptions.removeWatermarkShapes))
                            },
                            label = { Text(if (isHindi) "वाटरमार्क लेयर" else "Layer Shapes", fontSize = 12.sp) },
                            leadingIcon = if (cleanOptions.removeWatermarkShapes) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = cleanOptions.removeAuthorLinks,
                            onClick = {
                                viewModel.updateCleanOptions(cleanOptions.copy(removeAuthorLinks = !cleanOptions.removeAuthorLinks))
                            },
                            label = { Text(if (isHindi) "सोशल लिंक्स" else "Social Links", fontSize = 12.sp) },
                            leadingIcon = if (cleanOptions.removeAuthorLinks) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = cleanOptions.removeComments,
                            onClick = {
                                viewModel.updateCleanOptions(cleanOptions.copy(removeComments = !cleanOptions.removeComments))
                            },
                            label = { Text(if (isHindi) "कमेंट्स" else "XML Comments", fontSize = 12.sp) },
                            leadingIcon = if (cleanOptions.removeComments) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }

                    AnimatedVisibility(visible = showOptionsSheet) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Preserve Layer Timing toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isHindi) "लेयर टाइमिंग सुरक्षित रखें (Blank Out)" else "Preserve Layer Timing (Blank Out)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isHindi) "लेयर डिलीट करने की जगह टेक्स्ट खाली करेगा ताकि टाइमलाइन न बिगड़े"
                                        else "Wipes text without deleting layers to preserve video sync",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = cleanOptions.blankOutInsteadOfDelete,
                                    onCheckedChange = {
                                        viewModel.updateCleanOptions(cleanOptions.copy(blankOutInsteadOfDelete = it))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom Watermark Replace Field
                            OutlinedTextField(
                                value = customReplaceInput,
                                onValueChange = {
                                    customReplaceInput = it
                                    viewModel.updateCleanOptions(cleanOptions.copy(customReplacementText = it))
                                },
                                label = { Text(if (isHindi) "अपना वाटरमार्क लगाएं (Replace With)" else "Replace With Your Handle / Name") },
                                placeholder = { Text("@my_handle") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 3. Detected Watermarks Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isHindi) "मिले हुए वाटरमार्क" else "Detected Watermarks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (detectedItems.isNotEmpty()) DangerRed else SuccessGreen)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = detectedItems.size.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (detectedItems.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isHindi) "सभी चुनें" else "Select All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlueLight,
                            modifier = Modifier
                                .clickable { viewModel.selectAllItems(true) }
                                .padding(4.dp)
                        )
                        Text(
                            text = if (isHindi) "हटाएं" else "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.selectAllItems(false) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // Detected Items list
        if (detectedItems.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "इस XML में कोई वाटरमार्क नहीं मिला!" else "No watermarks detected in this XML!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "फाइल पहले से ही साफ है या कस्टम कीवर्ड जोड़ें" else "The XML is already clean or add custom keywords",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(detectedItems) { item ->
                WatermarkItemCard(
                    item = item,
                    language = language,
                    onToggle = { viewModel.toggleItemSelection(item.id) }
                )
            }
        }

        // 4. BIG Primary Button: Clean Watermarks
        item {
            val selectedCount = detectedItems.count { it.isSelected }
            Button(
                onClick = { viewModel.cleanXml() },
                enabled = !isProcessing && xmlInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_clean_watermarks"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (isHindi) "साफ किया जा रहा है..." else "Cleaning XML...", fontSize = 16.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) {
                            if (selectedCount > 0) "$selectedCount वाटरमार्क हटाएं (Clean XML)" else "XML साफ करें (Clean XML)"
                        } else {
                            if (selectedCount > 0) "Remove $selectedCount Watermarks" else "Clean XML"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 5. Cleaned Result Section (Appears after cleaning)
        if (cleanResult != null) {
            val res = cleanResult!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Success Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "XML साफ हो गया!" else "XML Cleaned Successfully!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isHindi) "${res.removedCount} वाटरमार्क हटाए गए" else "${res.removedCount} items removed",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                label = if (isHindi) "हटाए गए" else "Removed",
                                value = res.removedCount.toString(),
                                color = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = if (isHindi) "आकार कम" else "Size Saved",
                                value = "${res.originalBytes - res.cleanedBytes} B",
                                color = AccentCyan,
                                modifier = Modifier.weight(1f)
                            )
                            val percentSaved = if (res.originalBytes > 0) {
                                ((res.originalBytes - res.cleanedBytes).toFloat() / res.originalBytes * 100).toInt()
                            } else 0
                            StatBox(
                                label = if (isHindi) "बचत" else "Reduced",
                                value = "$percentSaved%",
                                color = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Diff Mode Tabs: Cleaned vs Diff vs Original
                        TabRow(
                            selectedTabIndex = diffMode.ordinal,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[diffMode.ordinal]),
                                    color = PrimaryBlue
                                )
                            },
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            Tab(
                                selected = diffMode == DiffMode.CLEANED,
                                onClick = { viewModel.setDiffMode(DiffMode.CLEANED) },
                                text = { Text(if (isHindi) "साफ XML" else "Cleaned", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = diffMode == DiffMode.DIFF,
                                onClick = { viewModel.setDiffMode(DiffMode.DIFF) },
                                text = { Text(if (isHindi) "अंतर (Diff)" else "Diff View", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = diffMode == DiffMode.ORIGINAL,
                                onClick = { viewModel.setDiffMode(DiffMode.ORIGINAL) },
                                text = { Text(if (isHindi) "मूल (Original)" else "Original", fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Code Viewer Component
                        val displayXml = when (diffMode) {
                            DiffMode.CLEANED -> res.cleanedXml
                            DiffMode.ORIGINAL -> res.originalXml
                            DiffMode.DIFF -> res.originalXml
                        }

                        CodeDiffViewer(
                            originalXml = res.originalXml,
                            cleanedXml = res.cleanedXml,
                            removedItems = res.removedItems,
                            isDiffMode = diffMode == DiffMode.DIFF,
                            modifier = Modifier.height(280.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Copy, Export, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copy Button
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Cleaned XML", res.cleanedXml)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showToast(if (isHindi) "क्लिपबोर्ड पर कॉपी हो गया!" else "Cleaned XML copied to clipboard!")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_copy_cleaned_xml")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "कॉपी करें" else "Copy", fontSize = 12.sp)
                            }

                            // Export Button
                            Button(
                                onClick = {
                                    exportLauncher.launch("cleaned_${activePresetName.lowercase().replace(" ", "_")}.xml")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_cleaned_xml")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "सेव करें" else "Save .xml", fontSize = 12.sp)
                            }

                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, res.cleanedXml)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, if (isHindi) "XML शेयर करें" else "Share Cleaned XML")
                                    context.startActivity(shareIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_share_cleaned_xml")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
