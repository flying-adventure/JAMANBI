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
import com.example.jamanbi.ui.theme.JamanbiTheme
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // 로그인 상태 -> 게시판으로 이동
            startActivity(Intent(this, PostListActivity::class.java))
            finish()
        } else {
            // 로그인 안 되어있으면 로그인 버튼만 보여줌
            setContent {
                JamanbiTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LoginOnlyScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun LoginOnlyScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("로그인")
        }
    }
}
