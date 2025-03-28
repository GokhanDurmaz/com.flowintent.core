package com.flowintent.example

import android.app.Application
import com.flowintent.core.FlowIntentViewModel

class MyApplication : Application() {
    val viewModel by lazy { FlowIntentViewModel() }
}