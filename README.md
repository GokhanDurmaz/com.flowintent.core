# com.flowintent.core
<details open>
<summary>FlowIntent: Empower Android Intents with Reactive Flows</summary>

### 1. Limitations of Static Intent Data  
Problem: Traditional Intent data transfer is one-time and static. Updating the target component with changing data requires additional mechanisms (e.g., BroadcastReceiver, LiveData).  

Solution: FlowIntent delivers dynamic data flows, allowing the target component to subscribe to real-time updates.
------------- ------------- ------------- ------------- ------------- ------------- 
### 2. Complexity of Asynchronous Data Transfer  
Problem: Sending asynchronous data between Activities or components often requires complex setups (e.g., EventBus, RxJava).  

Solution: FlowIntent simplifies this with Kotlin Flow, seamlessly integrating asynchronous streams.
------------- ------------- ------------- ------------- ------------- ------------- 
### 3. Lifecycle and State Management Challenges  
Problem: When transferring data via Intent, lifecycle mismatches can lead to data loss or synchronization issues.  

Solution: Integrated with ViewModel, FlowIntent provides a lifecycle-aware structure, reducing such risks.
------------- ------------- ------------- ------------- ------------- ------------- 
### 4. Code Repetition and Boilerplate  
Problem: Developers often need to write custom communication layers for dynamic data transfer.  

Solution: FlowIntent standardizes this process, reducing boilerplate code.
</details>
------------- ------------- ------------- ------------- ------------- ------------- 

### Use Case Scenarios
1. Real-Time Data Updates
Scenario: You want to send real-time location updates from one Activity to another (e.g., in a mapping app).  

How It’s Used:
```kotlin
val flowIntent = FlowIntent(context, MapActivity::class.java, viewModel)
lifecycleScope.launch {
    while (true) {
        val location = getCurrentLocation() // Fetch location
        flowIntent.emitData("location", location)
        delay(1000)
    }
}
flowIntent.start()
```

Target MapActivity:
```kotlin
lifecycleScope.launch {
    viewModel.getFlow(flowId).collect { bundleData ->
        updateMapLocation(bundleData.value as Location)
    }
}
```
Benefit: Continuously changing data like location is streamed to the target Activity without requiring additional infrastructure.

2. Dynamic Data Transfer Based on User Input
Scenario: You want to send user input from a form to another screen in real-time as they fill it out (e.g., a survey app).  

How It’s Used:
```kotlin
val flowIntent = FlowIntent(context, SummaryActivity::class.java, viewModel)
editText.addTextChangedListener { text ->
    lifecycleScope.launch {
        flowIntent.emitData("answer", text.toString())
    }
}
flowIntent.start()
```
Benefit: User inputs are instantly streamed to another component, enabling real-time summary updates.

3. Streaming Task Progress and Results
Scenario: You start a task in one Activity and want to monitor its progress or results in another (e.g., a file upload).  

How It’s Used:
```kotlin
val flowIntent = FlowIntent(context, UploadActivity::class.java, viewModel)
flowIntent.start()
lifecycleScope.launch {
    viewModel.getFlow(flowId).collect { bundleData ->
        when (bundleData.key) {
            "progress" -> updateProgressBar(bundleData.value as Int)
            "result" -> showResult(bundleData.value as String)
        }
    }
}
```
UploadActivity:
```kotlin
lifecycleScope.launch {
    uploadFile().collect { progress ->
        flowIntent.emitData("progress", progress)
    }
    flowIntent.emitData("result", "Upload complete!")
}
```
Benefit: Replaces static startActivityForResult with a stream of progress and results.

4. Shared Data Flow Across Multiple Components
Scenario: Multiple Activities or Fragments need to subscribe to the same data stream (e.g., incoming messages in a chat app).  

How It’s Used:
```kotlin
val flowIntent = FlowIntent(context, ChatActivity::class.java, viewModel)
lifecycleScope.launch {
    messageStream.collect { message ->
        flowIntent.emitData("message", message)
    }
}
flowIntent.start()
```
Another component:
```kotlin
lifecycleScope.launch {
    viewModel.getFlow(flowId).collect { bundleData ->
        displayMessage(bundleData.value as String)
    }
}
```
Benefit: A centralized stream can be easily shared across components.

5. Monitoring Long-Running Tasks
Scenario: You want to monitor a background service task (e.g., a download) from an Activity.  

How It’s Used:
```kotlin
val flowIntent = FlowIntent(context, DownloadActivity::class.java, viewModel)
downloadService.onProgress = { progress ->
    lifecycleScope.launch {
        flowIntent.emitData("downloadProgress", progress)
    }
}
flowIntent.start()
```
Benefit: Communication between a Service and UI becomes simple and reactive.
------------- ------------- ------------- ------------- ------------- ------------- 
### General Benefits and Solution Summary
Flexibility: Transitions from static data transfer to dynamic flows.  

Modernity: Fully aligns with Kotlin Flow and Jetpack ecosystem.  

Simplicity: Reduces boilerplate compared to traditional methods.  

Reactivity: Natural solution for real-time data updates.
------------- ------------- ------------- ------------- ------------- ------------- 
### Potential Application Areas
Real-Time Apps: Maps, chat, monitoring tools.  

Form and Data Collection: Surveys, user input tracking.  

Background Tasks: Downloads, uploads, task tracking.  

Shared State Management: Data synchronization across components.




