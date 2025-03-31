package com.flowintent.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.chain.flowIntentChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlowIntentDslTargetActivity : AppCompatActivity() {
    private lateinit var dslFlow: Flow<ActivityResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_dsl_target)

        val messageText = findViewById<TextView>(R.id.dslText)

        val dslStartButton1 = findViewById<Button>(R.id.dslStartButton1)
        dslStartButton1.setOnClickListener {
            dslFlow = flowIntentChain(this) {
                startActivityWithParent(MainActivity::class.java) { _ ->
                    Intent(Intent.ACTION_PICK).apply { type = "*/*" }
                }
                onResult { activityResult ->
                    val uri = activityResult.data?.data ?: Uri.EMPTY
                    if (uri != null && uri != Uri.EMPTY) {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity { intent }
                    } else {
                        Log.d("FlowIntentDsl", "Invalid URI: $uri")
                    }
                }
            }
            lifecycleScope.launch {
                dslFlow.collect { result ->
                    Log.d("FlowIntentDsl", "DSL result: ${result.resultCode}")
                    messageText.text = result.data?.dataString
                }
            }
        }
    }
}