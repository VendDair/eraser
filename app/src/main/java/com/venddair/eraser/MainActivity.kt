package com.venddair.eraser

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.venddair.eraser.ui.theme.EraserTheme
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File
import java.lang.ref.WeakReference

enum class Screens(val route: String) {
    SelectImage("selectImage"),
    EditImage("editImage"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Utils.init(this)

        setContent {
            val viewModel: AppViewModel by viewModels()
            appViewModel = viewModel
            Utils.navController = WeakReference(rememberNavController())
            EraserTheme {
                NavHost(
                    navController = Utils.navController.get()!!,
                    startDestination = Screens.SelectImage.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable(Screens.SelectImage.route) { SelectImageMenu() }
                    composable(Screens.EditImage.route) { EditMenu() }
                }
            }
        }
    }
}

@Composable
fun SelectImageMenu() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(10.dp)
                .padding(innerPadding)
        ) {
            Text("Version: ${Utils.getVersion()}")
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ImagePicker { path ->
                appViewModel.imagePath.update { path }
                appViewModel.originalBitmap.update { BitmapFactory.decodeFile(path) }
                appViewModel.imageBitmap.update { appViewModel.originalBitmap.value }
                Utils.navController.get()!!.navigate(Screens.EditImage.route)
            }
        }
    }
}

@Composable
fun EditMenu() {
    val imageBitmap by appViewModel.imageBitmap.collectAsState()


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = "idk",
                modifier = Modifier
                    .zoomable(rememberZoomState())
                    .fillMaxWidth()
            )
        }

        ToolBar(modifier = Modifier
            .padding(innerPadding)
            .padding(start = 10.dp, end = 10.dp, bottom = 20.dp))
    }
}

@Composable
fun ToolBar(modifier: Modifier = Modifier) {
    val originalBitmap by appViewModel.originalBitmap.collectAsState()
    val imageBitmap by appViewModel.imageBitmap.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolbarIcon(
                text = "Auto delete",
                icon = Icons.Rounded.AccountBox,
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        RemoveBg(Utils.context.get()!!)
                            .clearBackground(imageBitmap).collect {
                                appViewModel.imageBitmap.value = it!!
                            }
                    }
                }
            )
            ToolbarIcon(
                text = "Magic Eraser",
                icon = ImageVector.vectorResource(R.drawable.star_24px)
            )
            ToolbarIcon(
                text = "Eraser",
                icon = ImageVector.vectorResource(R.drawable.ink_eraser_24px)
            )
        }
    }
}

@Composable
fun ToolbarIcon(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .clickable { onClick() }
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(30.dp)
                .fillMaxHeight(),
            imageVector = icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )

        Text(
            text = text
        )
    }
}

@Composable
fun ImagePicker(
    onImageSelected: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        Utils.context.get()!!.contentResolver.openInputStream(it)?.let { input ->
            File(Utils.appFiles + "/image").outputStream().use { output ->
                input.copyTo(output)
                onImageSelected(Utils.appFiles + "/image")
            }
        }

    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("Select Image")
    }
}