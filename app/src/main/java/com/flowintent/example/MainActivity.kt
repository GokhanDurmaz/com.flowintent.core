package com.flowintent.example

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowIntent
import com.flowintent.core.SimpleFlowIntent
import com.flowintent.core.model.FlowCleanupPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { (application as MyApplication).viewModel }

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainStartButton1 = findViewById<Button>(R.id.mainStartButton1)
        mainStartButton1.setOnClickListener {
            val flowIntent = FlowIntent(
                context = this,
                target = FlowIntentScheduleTargetActivity::class.java,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
                scope = lifecycleScope
            )
            flowIntent.scheduleJob { intent ->
                while (true) {
                    intent.emitData("dynamicKey", "Mesaj #$count")
                    count++
                    delay(1000) // A message at a time
                }
            }

            flowIntent.start()
        }

        val mainStartButton2 = findViewById<Button>(R.id.mainStartButton2)
        mainStartButton2.setOnClickListener {
            SimpleFlowIntent.from(this, FlowIntentSimpleTargetActivity::class.java)
                .withData("initialData", "deneme1")
                .withDynamicData("stringKey", "denemeDynamic1")
                .start()
        }
    }
}