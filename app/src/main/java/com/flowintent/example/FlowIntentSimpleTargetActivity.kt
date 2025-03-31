package com.flowintent.example

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowIntent
import com.flowintent.core.SimpleFlowIntent
import kotlinx.coroutines.launch

class FlowIntentSimpleTargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_simple_target)
        val messageTV = findViewById<TextView>(R.id.simpleText)
        val messageIntTV = findViewById<TextView>(R.id.simpleIntText)

        val initialData = intent.getStringExtra("string_key")
        val flowId = FlowIntent.getFlowId(intent)
        Log.d("FlowIntentSimple", "Statik Veri: $initialData, Flow ID: $flowId")

        SimpleFlowIntent.current(this@FlowIntentSimpleTargetActivity).getDynamicData().let { flow ->
            lifecycleScope.launch {
                flow?.collect { bundleData ->
                    when(bundleData.key) {
                        "string_key" -> messageTV.text = bundleData.value.toString()
                        "int_key" -> messageIntTV.text = bundleData.value.toString()
                    }
                }
            }
        }
    }
}