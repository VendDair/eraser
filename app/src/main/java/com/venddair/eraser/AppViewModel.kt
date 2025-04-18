package com.venddair.eraser

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.flow.MutableSharedFlow

lateinit var appViewModel: AppViewModel

class AppViewModel : ViewModel() {
    val imagePath = MutableStateFlow("")
    val originalBitmap = MutableStateFlow(createBitmap(1, 1, Bitmap.Config.ALPHA_8))
    val imageBitmap = MutableStateFlow(createBitmap(1, 1, Bitmap.Config.ALPHA_8))
}