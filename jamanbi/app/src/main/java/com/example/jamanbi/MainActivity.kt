package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.jamanbi.ui.theme.JamanbiTheme
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JamanbiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onGoToPostWrite = {
                            // 🔥 PostWriteActivity로 이동
                            val intent = Intent(this, PostWriteActivity::class.java)
                            startActivity(intent)
                        }

                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, onGoToPostWrite: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "자만비 홈 화면", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoToPostWrite) {
            Text(text = "게시글 작성하기")
        }
        Button(onClick = {
            context.startActivity(Intent(context, PostListActivity::class.java))
        }) {
            Text(text = "게시글 목록 보기")
        }


    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JamanbiTheme {
        MainScreen(onGoToPostWrite = {})
    }
}
