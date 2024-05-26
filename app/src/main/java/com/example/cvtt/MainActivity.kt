package com.example.cvtt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cvtt.ui.components.Album
import com.example.cvtt.ui.components.DealImage
import com.example.cvtt.ui.theme.CvttTheme

class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    private external fun getHelloMessage(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = getHelloMessage()
        Log.d("Test", "onCreate: $message")
        enableEdgeToEdge()
        setContent {
            CvttTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    DealImage()
                    Album()

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CvttTheme {
        Greeting("Android")
    }
}

