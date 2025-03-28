package com.flowintent.example

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.FlowIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { (application as MyApplication).viewModel }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            val flowIntent = FlowIntent(this, TargetActivity::class.java, viewModel)

            lifecycleScope.launch {
                var count = 0
                while (true) {
                    flowIntent.emitData("message", "Mesaj #$count")
                    count++
                    delay(1000) // A message at a time
                }
            }

            flowIntent.start()
        }
    }
}