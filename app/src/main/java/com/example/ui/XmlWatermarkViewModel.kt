package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.RectF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HistoryEntity
import com.example.data.HistoryRepository
import com.example.engine.SampleXmlPresets
import com.example.engine.XmlWatermarkEngine
import com.example.model.CleanOptions
import com.example.model.CleanResult
import com.example.model.WatermarkItem
import com.example.photo.PhotoEraserEngine
import com.example.photo.SamplePosterGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab {
    XML_CLEANER,
    PHOTO_ERASER,
    HISTORY
}

enum class AppLanguage {
    HINDI,
    ENGLISH
}

enum class DiffMode {
    CLEANED,
    ORIGINAL,
    DIFF
}

class XmlWatermarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HistoryRepository(db.historyDao())
    }

    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // General App State
    private val _currentTab = MutableStateFlow(AppTab.XML_CLEANER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.HINDI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // XML Cleaner State
    private val _xmlInput = MutableStateFlow(SampleXmlPresets.ALIGHT_MOTION_PRESET)
    val xmlInput: StateFlow<String> = _xmlInput.asStateFlow()

    private val _activePresetName = MutableStateFlow("Alight Motion Beat Shake")
    val activePresetName: StateFlow<String> = _activePresetName.asStateFlow()

    private val _detectedItems = MutableStateFlow<List<WatermarkItem>>(emptyList())
    val detectedItems: StateFlow<List<WatermarkItem>> = _detectedItems.asStateFlow()

    private val _cleanOptions = MutableStateFlow(CleanOptions())
    val cleanOptions: StateFlow<CleanOptions> = _cleanOptions.asStateFlow()

    private val _cleanResult = MutableStateFlow<CleanResult?>(null)
    val cleanResult: StateFlow<CleanResult?> = _cleanResult.asStateFlow()

    private val _diffMode = MutableStateFlow(DiffMode.DIFF)
    val diffMode: StateFlow<DiffMode> = _diffMode.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Photo Eraser State
    private val _photoSourceBitmap = MutableStateFlow<Bitmap?>(null)
    val photoSourceBitmap: StateFlow<Bitmap?> = _photoSourceBitmap.asStateFlow()

    private val _photoCurrentBitmap = MutableStateFlow<Bitmap?>(null)
    val photoCurrentBitmap: StateFlow<Bitmap?> = _photoCurrentBitmap.asStateFlow()

    private val _brushSize = MutableStateFlow(36f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    private val _maskPaths = MutableStateFlow<List<Pair<Path, Float>>>(emptyList())
    val maskPaths: StateFlow<List<Pair<Path, Float>>> = _maskPaths.asStateFlow()

    private val _rectSelections = MutableStateFlow<List<RectF>>(emptyList())
    val rectSelections: StateFlow<List<RectF>> = _rectSelections.asStateFlow()

    private val _isErasingPhoto = MutableStateFlow(false)
    val isErasingPhoto: StateFlow<Boolean> = _isErasingPhoto.asStateFlow()

    private val _compareOriginal = MutableStateFlow(false)
    val compareOriginal: StateFlow<Boolean> = _compareOriginal.asStateFlow()

    private val undoHistory = mutableListOf<Bitmap>()

    init {
        // Initial scan of default preset
        scanXmlContent(SampleXmlPresets.ALIGHT_MOTION_PRESET)
        // Preload sample photo
        loadSamplePoster()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.HINDI) AppLanguage.ENGLISH else AppLanguage.HINDI
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    // --- XML Cleaner Actions ---

    fun onXmlInputChanged(newXml: String) {
        _xmlInput.value = newXml
        _cleanResult.value = null
        scanXmlContent(newXml)
    }

    fun loadPreset(name: String, xml: String) {
        _activePresetName.value = name
        _xmlInput.value = xml
        _cleanResult.value = null
        scanXmlContent(xml)
        showToast(if (_language.value == AppLanguage.HINDI) "$name लोड किया गया" else "Loaded $name")
    }

    fun scanXmlContent(xml: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val scanned = XmlWatermarkEngine.scanXml(xml, _cleanOptions.value.customKeywords)
            withContext(Dispatchers.Main) {
                _detectedItems.value = scanned
            }
        }
    }

    fun toggleItemSelection(id: String) {
        _detectedItems.value = _detectedItems.value.map { item ->
            if (item.id == id) item.copy(isSelected = !item.isSelected) else item
        }
    }

    fun selectAllItems(selected: Boolean) {
        _detectedItems.value = _detectedItems.value.map { it.copy(isSelected = selected) }
    }

    fun updateCleanOptions(options: CleanOptions) {
        _cleanOptions.value = options
    }

    fun setDiffMode(mode: DiffMode) {
        _diffMode.value = mode
    }

    fun cleanXml() {
        val input = _xmlInput.value
        val items = _detectedItems.value
        val options = _cleanOptions.value

        if (input.isBlank()) {
            showToast(if (_language.value == AppLanguage.HINDI) "कृपया XML टेक्स्ट दर्ज करें" else "Please enter XML text")
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            val result = withContext(Dispatchers.Default) {
                XmlWatermarkEngine.cleanXml(input, items, options)
            }
            _cleanResult.value = result
            _isProcessing.value = false

            // Save to Room DB history
            repository.saveCleanedPreset(
                title = _activePresetName.value,
                watermarkCount = result.removedCount,
                originalSize = result.originalBytes,
                cleanedSize = result.cleanedBytes,
                cleanedXml = result.cleanedXml
            )

            val msg = if (_language.value == AppLanguage.HINDI) {
                "${result.removedCount} वाटरमार्क सफलतापूर्वक हटाए गए!"
            } else {
                "${result.removedCount} watermarks cleaned successfully!"
            }
            showToast(msg)
        }
    }

    // --- History Actions ---

    fun loadHistoryItem(item: HistoryEntity) {
        _xmlInput.value = item.cleanedXml
        _activePresetName.value = item.title
        _cleanResult.value = null
        scanXmlContent(item.cleanedXml)
        _currentTab.value = AppTab.XML_CLEANER
        showToast(if (_language.value == AppLanguage.HINDI) "इतिहास से लोड किया गया" else "Loaded from history")
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            showToast(if (_language.value == AppLanguage.HINDI) "इतिहास साफ कर दिया गया" else "History cleared")
        }
    }

    // --- Photo Eraser Actions ---

    fun loadSamplePoster() {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = SamplePosterGenerator.createSamplePoster()
            withContext(Dispatchers.Main) {
                _photoSourceBitmap.value = bitmap
                _photoCurrentBitmap.value = bitmap
                _maskPaths.value = emptyList()
                _rectSelections.value = emptyList()
                undoHistory.clear()
            }
        }
    }

    fun setPhotoFromBitmap(bitmap: Bitmap) {
        _photoSourceBitmap.value = bitmap
        _photoCurrentBitmap.value = bitmap
        _maskPaths.value = emptyList()
        _rectSelections.value = emptyList()
        undoHistory.clear()
        showToast(if (_language.value == AppLanguage.HINDI) "फोटो लोड की गई" else "Photo loaded")
    }

    fun setBrushSize(size: Float) {
        _brushSize.value = size
    }

    fun addMaskStroke(path: Path, strokeWidth: Float) {
        _maskPaths.value = _maskPaths.value + Pair(path, strokeWidth)
    }

    fun addRectSelection(rect: RectF) {
        _rectSelections.value = _rectSelections.value + rect
    }

    fun clearMask() {
        _maskPaths.value = emptyList()
        _rectSelections.value = emptyList()
    }

    fun undoPhoto() {
        if (undoHistory.isNotEmpty()) {
            val last = undoHistory.removeAt(undoHistory.size - 1)
            _photoCurrentBitmap.value = last
            _maskPaths.value = emptyList()
            _rectSelections.value = emptyList()
            showToast(if (_language.value == AppLanguage.HINDI) "पूर्ववत (Undo)" else "Undo")
        }
    }

    fun setCompareOriginal(show: Boolean) {
        _compareOriginal.value = show
    }

    fun erasePhotoWatermark() {
        val current = _photoCurrentBitmap.value ?: return
        val paths = _maskPaths.value
        val rects = _rectSelections.value

        if (paths.isEmpty() && rects.isEmpty()) {
            showToast(if (_language.value == AppLanguage.HINDI) "वाटरमार्क पर ब्रश या बॉक्स चलाएं" else "Brush over the watermark first")
            return
        }

        viewModelScope.launch {
            _isErasingPhoto.value = true
            // Save state for undo
            undoHistory.add(current.copy(current.config ?: Bitmap.Config.ARGB_8888, true))

            val result = PhotoEraserEngine.inpaintErase(current, paths, rects)
            _photoCurrentBitmap.value = result
            _maskPaths.value = emptyList()
            _rectSelections.value = emptyList()
            _isErasingPhoto.value = false

            val msg = if (_language.value == AppLanguage.HINDI) {
                "वाटरमार्क सफलतापूर्वक मिटा दिया गया!"
            } else {
                "Watermark erased successfully!"
            }
            showToast(msg)
        }
    }

    /**
     * Quick preset erases specific watermark areas on the sample poster
     */
    fun eraseSampleWatermarkByZone(zone: String) {
        val current = _photoCurrentBitmap.value ?: return
        val w = current.width.toFloat()
        val h = current.height.toFloat()

        val targetRect = when (zone) {
            "top_soul" -> RectF(w * 0.22f, h * 0.05f, w * 0.55f, h * 0.14f)
            "shiva_breath" -> RectF(w * 0.70f, h * 0.05f, w * 0.98f, h * 0.20f)
            "mahadev_rock" -> RectF(w * 0.70f, h * 0.79f, w * 0.98f, h * 0.94f)
            else -> null
        } ?: return

        _rectSelections.value = listOf(targetRect)
        erasePhotoWatermark()
    }
}
