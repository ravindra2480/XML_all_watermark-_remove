package com.example.model

enum class WatermarkType(val labelEn: String, val labelHi: String) {
    TEXT_WATERMARK("Text Watermark", "टेक्स्ट वाटरमार्क"),
    AUTHOR_CREDIT("Author Credit", "क्रेडिट / नाम"),
    SOCIAL_LINK("Social Link", "सोशल मीडिया लिंक"),
    WATERMARK_LAYER("Watermark Layer", "वाटरमार्क लेयर"),
    COMMENT_METADATA("Metadata Comment", "कमेंट मेटाडेटा"),
    CUSTOM_TAG("Custom Keyword", "कस्टम कीवर्ड")
}

data class WatermarkItem(
    val id: String,
    val type: WatermarkType,
    val matchedText: String,
    val lineIndex: Int,
    val lineNumber: Int,
    val rawSnippet: String,
    val isSelected: Boolean = true,
    val isFullElement: Boolean = false,
    val elementStartTag: String? = null
)

data class CleanOptions(
    val removeTextWatermarks: Boolean = true,
    val removeWatermarkShapes: Boolean = true,
    val removeAuthorLinks: Boolean = true,
    val removeComments: Boolean = true,
    val blankOutInsteadOfDelete: Boolean = false,
    val customReplacementText: String = "",
    val customKeywords: List<String> = emptyList()
)

data class CleanResult(
    val originalXml: String,
    val cleanedXml: String,
    val removedCount: Int,
    val originalBytes: Int,
    val cleanedBytes: Int,
    val removedItems: List<WatermarkItem>,
    val timestamp: Long = System.currentTimeMillis()
)
