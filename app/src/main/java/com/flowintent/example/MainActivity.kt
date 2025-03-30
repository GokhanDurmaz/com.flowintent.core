package com.flowintent.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowIntent
import com.flowintent.core.annotations.InitialStep
import com.flowintent.core.annotations.OnResult
import com.flowintent.core.annotations.StartActivity
import com.flowintent.core.chain.flowIntentChain
import com.flowintent.core.chain.flowIntentChainFromAnnotations
import com.flowintent.core.model.FlowCleanupPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { (application as MyApplication).viewModel }

    private lateinit var dslFlow: Flow<ActivityResult>

    private lateinit var annotationFlow: Flow<ActivityResult>

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton1 = findViewById<Button>(R.id.startButton1)
        startButton1.setOnClickListener {
            val flowIntent = FlowIntent(
                context = this,
                target = TargetActivity::class.java,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
                scope = lifecycleScope
            )
            flowIntent.scheduleJob { intent ->
                while (true) {
                    intent.emitData("message", "Mesaj #$count")
                    count++
                    delay(1000) // A message at a time
                }
            }

            flowIntent.start()
        }

        dslFlow = flowIntentChain(this) {
            startActivity { _ -> Intent(Intent.ACTION_PICK).apply { type = "*/*" } }
            onResult { activityResult ->
                val uri = activityResult.data?.data ?: Uri.EMPTY
                if (uri != null && uri != Uri.EMPTY) {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity { intent }
                } else {
                    Log.d("MainActivity", "Invalid URI: $uri")
                }
            }
        }

        annotationFlow = flowIntentChainFromAnnotations()

        val startButton2 = findViewById<Button>(R.id.startButton2)
        startButton2.setOnClickListener {
            lifecycleScope.launch {
                dslFlow.collect { result ->
                    Log.d("MainActivity", "DSL result: ${result.resultCode}")
                }
            }
        }

        val startButton3 = findViewById<Button>(R.id.startButton3)
        startButton3.setOnClickListener {
            lifecycleScope.launch {
                annotationFlow.collect { result ->
                    Log.d("MainActivity", "Annotation result: ${result.resultCode}")
                }
            }
        }
    }

    @InitialStep
    @StartActivity(stepName = "pick_image", action = Intent.ACTION_PICK)
    fun pickImage(result: ActivityResult?): Intent = Intent(Intent.ACTION_PICK)

    @OnResult(stepName = "pick_image", nextStep = "view_image")
    fun handlePickResult(result: ActivityResult) {
        Log.d("MainActivity", "Result: ${result.data}")
    }

    @StartActivity(stepName = "view_image", action = Intent.ACTION_VIEW)
    @OnResult(stepName = "view_image")
    fun viewImage(result: ActivityResult?): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(result?.data?.getStringExtra("key")))
}