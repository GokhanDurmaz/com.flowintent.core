package com.flowintent.core

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TargetActivity : AppCompatActivity() {
    private val viewModel: FlowIntentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_target)

        val messageText = findViewById<TextView>(R.id.messageText)

        // Get the flowId from FlowIntent
        val flowId = FlowIntent.getFlowId(intent) ?: return
        val flow = viewModel.getFlow(flowId) ?: return

        // Listen the received data and show in the screen
        lifecycleScope.launch {
            flow.collect { bundleData ->
                messageText.text = bundleData.value.toString()
            }
        }
    }
}