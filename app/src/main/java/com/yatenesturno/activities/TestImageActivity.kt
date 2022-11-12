 package com.yatenesturno.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.play.core.common.c
import com.yatenesturno.R
import com.yatenesturno.activities.ui.theme.AdminTurnosTheme

class TestImageActivity : ComponentActivity() {
    private val pickImage = 100
    private var imageUri: Uri? = null
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    Column(modifier = Modifier.height(200.dp).width(200.dp)) {
                        Button(modifier = Modifier
                            .width(200.dp)
                            .height(200.dp),onClick = {
                            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                            startActivityForResult(gallery, pickImage)
                        }) {
                            Text(text = "Hello!")
                        }
                    }


                    imageUri?.let {
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUri.toString()))
                        bitmap?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = "elegida")

                        }

                    }

                }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
        }
    }
}

