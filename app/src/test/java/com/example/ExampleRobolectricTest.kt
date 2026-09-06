package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.SampleXmlPresets
import com.example.engine.XmlWatermarkEngine
import com.example.model.CleanOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("XML Watermark Remover", appName)
  }

  @Test
  fun `xml watermark scanner detects watermark layers and author handles`() {
    val xml = SampleXmlPresets.ALIGHT_MOTION_PRESET
    val detected = XmlWatermarkEngine.scanXml(xml)
    assertTrue("Should detect watermarks in sample preset", detected.isNotEmpty())
    assertTrue("Should detect author handle @rahul_vfx_official", detected.any { it.matchedText.contains("@rahul_vfx_official") })
  }

  @Test
  fun `xml watermark cleaner removes watermarks`() {
    val xml = SampleXmlPresets.ALIGHT_MOTION_PRESET
    val detected = XmlWatermarkEngine.scanXml(xml)
    val result = XmlWatermarkEngine.cleanXml(xml, detected, CleanOptions())
    assertTrue("Should remove watermarks", result.removedCount > 0)
    assertTrue("Cleaned XML should be smaller than original", result.cleanedBytes < result.originalBytes)
    assertTrue("Cleaned XML should not contain watermark comment", !result.cleanedXml.contains("Watermark: Edited by"))
  }
}
