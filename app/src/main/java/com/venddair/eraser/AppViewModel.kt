package com.venddair.eraser

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

lateinit var appViewModel: AppViewModel

class AppViewModel : ViewModel() {
    //val imagePath = MutableStateFlow("")

    val list = MutableStateFlow<List<Bitmap>>(listOf())

    val redoList = MutableStateFlow<List<Bitmap>>(listOf())


    fun autoRemoveBg() {
        CoroutineScope(Dispatchers.Main).launch {
            RemoveBg(Utils.context.get()!!).clearBackground(list.value.last()).collect {
                list.value += it!!
                Log.d("INFO", "Auto delete called!")
            }
        }
    }


    fun undo() {
        if (list.value.isEmpty() || list.value.size == 1) return

        redoList.value += list.value.last()

        list.value = list.value.dropLast(1)

        Log.d("INFO", list.value.toString())
    }

    fun redo() {
        if (redoList.value.isEmpty()) return

        list.value += redoList.value.last()

        redoList.value = redoList.value.dropLast(1)
    }

    fun reset() {
        list.value = listOf()
        redoList.value = listOf()
    }
}