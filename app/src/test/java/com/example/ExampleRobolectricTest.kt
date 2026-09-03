package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("AI Photo Studio", appName)
  }

  @Test
  fun `verify filter matrix factory produces matrices`() {
    val matrix = com.example.editor.FilterMatrixFactory.getFilterMatrix(
      com.example.editor.FilterType.CINEMATIC,
      1f
    )
    org.junit.Assert.assertNotNull(matrix)
  }
}
