package com.flowintent.example

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flowintent.core.SimpleFlowIntent
import kotlinx.coroutines.launch

class FlowIntentSimpleTargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_simple_target)

        val messageText = findViewById<TextView>(R.id.simpleText)

        lifecycleScope.launch {
            SimpleFlowIntent.current(this@FlowIntentSimpleTargetActivity).getDynamicData()
                ?.collect { bundleData ->
                    runOnUiThread {
                        when(bundleData?.key) {
                            "stringKey" -> messageText.text = bundleData.value.toString()
                        }
                    }
                }
        }
    }
}