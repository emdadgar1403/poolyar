package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.PoulyarRepository
import com.example.ui.PoulyarApp
import com.example.ui.theme.PoulyarTheme
import com.example.ui.viewmodel.PoulyarViewModel
import com.example.ui.viewmodel.PoulyarViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PoulyarRepository(database)
        val factory = PoulyarViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[PoulyarViewModel::class.java]

        setContent {
            PoulyarTheme {
                PoulyarApp(viewModel = viewModel)
            }
        }
    }
}
