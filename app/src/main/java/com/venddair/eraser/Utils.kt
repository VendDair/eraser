package com.venddair.eraser

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import java.lang.ref.WeakReference

object Utils {
    lateinit var context: WeakReference<ComponentActivity>
    lateinit var navController: WeakReference<NavHostController>

    val appFiles get() = context.get()!!.filesDir.absolutePath

    fun init(appContext: ComponentActivity) {
        context = WeakReference(appContext)
    }


    fun getVersion(): String {
        return context.get()!!.packageManager.getPackageInfo(context.get()!!.packageName, 0).versionName!!
    }

    fun executeCommand(command: String): String {
        val process = Runtime.getRuntime().exec(command)
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()

        return output
    }


    fun checkRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        }
    }
}
