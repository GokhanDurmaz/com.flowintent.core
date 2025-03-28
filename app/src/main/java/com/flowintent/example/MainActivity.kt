package com.flowintent.example

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowCleanupPolicy
import com.flowintent.core.FlowIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { (application as MyApplication).viewModel }

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
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
    }
}