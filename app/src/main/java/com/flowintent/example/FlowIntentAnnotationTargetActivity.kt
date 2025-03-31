package com.flowintent.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.annotations.InitialStep
import com.flowintent.core.annotations.OnResult
import com.flowintent.core.annotations.StartActivity
import com.flowintent.core.chain.flowIntentChainFromAnnotations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlowIntentAnnotationTargetActivity : AppCompatActivity() {
    private lateinit var annotationFlow: Flow<ActivityResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_annotation_target)

        val messageText = findViewById<TextView>(R.id.annotationText)

        annotationFlow = flowIntentChainFromAnnotations()
        lifecycleScope.launch {
            annotationFlow.collect { result ->
                Log.d("FlowIntentAnnotation", "Annotation result: ${result.resultCode}")
                messageText.text = result.data?.dataString
            }
        }
    }

    @InitialStep
    @StartActivity(stepName = "pick_image", action = Intent.ACTION_PICK, parent = "com.flowintent.example.MainActivity")
    fun pickImage(result: ActivityResult?): Intent = Intent(Intent.ACTION_PICK)

    @OnResult(stepName = "pick_image", nextStep = "view_image")
    fun handlePickResult(result: ActivityResult) {
        Log.d("MainActivity", "Result: ${result.data}")
    }

    @StartActivity(stepName = "view_image", action = Intent.ACTION_VIEW, parent = "com.flowintent.example.MainActivity")
    @OnResult(stepName = "view_image")
    fun viewImage(result: ActivityResult?): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(result?.data?.getStringExtra("key")))
}