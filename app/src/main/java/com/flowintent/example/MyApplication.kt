package com.flowintent.example

import android.app.Application
import com.flowintent.core.vm.FlowIntentViewModel

class MyApplication : Application() {
    val viewModel by lazy { FlowIntentViewModel() }
}