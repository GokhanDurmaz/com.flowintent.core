package com.flowintent.example

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flowintent.core.SimpleFlowIntent
import org.json.JSONObject

class FlowIntentDeepLinkTargetActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_intent_deep_link_target)

        val messageText1 = findViewById<TextView>(R.id.deeplinkText1)
        val messageText2 = findViewById<TextView>(R.id.deeplinkText2)

        val flowIntent = SimpleFlowIntent.current(this)
        val params = flowIntent.getDeepLinkParams()
        val jsonData = params.get<JSONObject>("data")
        val id = jsonData?.getString("id")
        val type = jsonData?.getString("type")
        messageText1.text = "ID: $id"
        messageText2.text = "Type: $type"
        Log.d("FlowIntentDeepLink", "ID: $id, Type: $type")
    }
}