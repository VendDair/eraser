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
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.venddair.eraser.ui.theme.EraserTheme
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
                appViewModel.reset()
                appViewModel.list.value += BitmapFactory.decodeFile(path)
                Utils.navController.get()!!.navigate(Screens.EditImage.route)
            }
        }
    }
}

@Composable
fun EditMenu() {
    //val imageBitmap by appViewModel.imageBitmap.collectAsState()
    val list by appViewModel.list.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = list.last().asImageBitmap(),
                contentDescription = "idk",
                modifier = Modifier
                    .zoomable(rememberZoomState())
                    .fillMaxWidth()
            )
        }

        ToolBar(modifier = Modifier
            .padding(innerPadding)
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp))
    }
}

@Composable
fun ToolBar(modifier: Modifier = Modifier) {
    val list by appViewModel.list.collectAsState()
    val redoList by appViewModel.redoList.collectAsState()
    //val originalBitmap by appViewModel.originalBitmap.collectAsState()
    //val imageBitmap by appViewModel.imageBitmap.collectAsState()


    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (redoList.isNotEmpty())
                ToolbarIcon(
                    text = "Redo",
                    icon = ImageVector.vectorResource(R.drawable.redo_24px),
                    onClick = {
                        appViewModel.redo()
                    }
                )
            if (list.size > 1)
                ToolbarIcon(
                    text = "Undo",
                    icon = ImageVector.vectorResource(R.drawable.undo_24px),
                    onClick =  {
                        appViewModel.undo()
                    }
                )
        }
    }

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
                    appViewModel.autoRemoveBg()
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
            ToolbarIcon(
                text = "Repair",
                icon = ImageVector.vectorResource(R.drawable.ink_eraser_off_24px)
            )
        }
    }
}

@Composable
fun ToolbarIcon(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .clickable { onClick() }
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(27.dp)
                .fillMaxHeight(),
            imageVector = icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )

        Text(
            text = text,
            fontSize = 12.sp
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