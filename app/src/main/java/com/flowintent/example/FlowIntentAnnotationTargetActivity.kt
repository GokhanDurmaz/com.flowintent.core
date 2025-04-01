package com.flowintent.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.annotations.DeepLinkParam
import com.flowintent.core.annotations.InitialStep
import com.flowintent.core.annotations.OnDeepLinkError
import com.flowintent.core.annotations.OnResult
import com.flowintent.core.annotations.StartActivity
import com.flowintent.core.chain.flowIntentChainFromAnnotations
import com.flowintent.core.deeplink.DeepLinkParams
import kotlinx.coroutines.launch

class FlowIntentAnnotationTargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_annotation_target)

        val messageText = findViewById<TextView>(R.id.annotationText)

        val deepLinkParams = DeepLinkParams().apply {
            put("id", "123")
            put("action", "fetch")
        }

        val flow = flowIntentChainFromAnnotations {
            withDeepLink(deepLinkParams) {
                param("id", isRequired = true, validator = { it?.toString()?.isNotEmpty() == true })
                param("action", isRequired = true, validator = { it == "view" || it == "edit" || it == "fetch" })
            }
            onDeepLinkError { error ->
                Toast.makeText(this@FlowIntentAnnotationTargetActivity, "Deeplink error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            flow.collect { result ->
                Log.d("FlowIntentAnnotation", "Annotation result: ${result.resultCode}")
                messageText.text = result.data?.dataString
            }
        }
    }

    @InitialStep
    @StartActivity(stepName = "start", parent = "com.flowintent.example.MainActivity")
    @DeepLinkParam(name = "id", isRequired = true, errorMessage = "ID is required")
    @DeepLinkParam(name = "action", isRequired = true, validator = FlowIntentActionValidator::class, errorMessage = "Action must be view, edit or fetch")
    fun startStep(result: ActivityResult?, params: DeepLinkParams?): Intent {
        return Intent(this, FlowIntentDeepLinkTargetActivity::class.java).apply {
            params?.get<String>("id")?.let { putExtra("id", it) }
            params?.get<String>("action")?.let { putExtra("action", it) }
        }
    }

    @OnResult(stepName = "next")
    fun onStartResult(result: ActivityResult) {
        Log.d("FlowIntentAnnotation", "Result: ${result.data}")
    }

    @StartActivity(stepName = "next")
    fun nextStep(result: ActivityResult?): Intent {
        return Intent(this, FlowIntentDslTargetActivity::class.java)
    }

    @OnDeepLinkError
    fun handleDeepLinkError(error: Throwable) {
        Log.e("FlowIntentAnnotation", "Deeplink error: ${error.message}")
    }
}