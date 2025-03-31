package com.flowintent.example

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowIntent
import kotlinx.coroutines.launch

class FlowIntentScheduleTargetActivity : AppCompatActivity() {
    private val viewModel by lazy { (application as MyApplication).viewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_schedule_target)

        val messageText = findViewById<TextView>(R.id.scheduleText)
        Log.d("FlowIntentSchedule", "Obtain ViewModel: $viewModel")
        // Get the flowId from FlowIntent
        val flowId = FlowIntent.getFlowId(intent) ?: return
        val flow = viewModel.getFlow(flowId) ?: return

        var mainDynamicData: String? = null
        // Listen the received data and show in the screen
        lifecycleScope.launch {
            flow.collect { bundleData ->
                if (bundleData.key == "dynamicKey") {
                    mainDynamicData = bundleData.value.toString()
                    messageText.text = mainDynamicData
                }
            }
        }
    }
}