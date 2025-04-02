<meta name="google-site-verification" content="DJ4lgfquekByEbfgY8KWGLT0DDpysxrCQtQROrZZBCc" />


# FlowIntent.Core - Android Navigation Library

**FlowIntent.Core** is a versatile navigation library for Android, designed to simplify and enhance navigation in your applications. Built with Kotlin, it offers robust support for **legacy Activity-based projects**, seamless integration with **Kotlin Coroutines** for dynamic data flows, and the newly added **Advanced Deeplink** feature for deep linking flexibility. Whether you're maintaining an older app or building a modern one, FlowIntent.Core has you covered.

---

## Features

### Legacy Navigation Support
FlowIntent.Core provides full compatibility with traditional Android navigation using Activities and Intents. Easily manage navigation flows in legacy projects with a clean and intuitive API.

- **Activity-Based Navigation:** Start Activities with customizable backstack behavior.
- **Intent Management:** Simplified Intent creation and execution.

### Kotlin Coroutines Integration
Leverage the power of Kotlin Coroutines to handle dynamic data flows within your navigation logic.

- **Asynchronous Data Emission:** Emit and collect data seamlessly during navigation.
- **Lifecycle-Aware:** Integrate with `lifecycleScope` for safe coroutine execution.

### Advanced Deeplink Support (New in v1.1.0)
Take control of deep linking with advanced features, allowing complex routing and parameter validation.

- **Custom Deeplink Handling:** Parse and validate deep link parameters effortlessly.
- **Flexible Routing:** Direct users to specific screens based on deeplink data.

---

## Installation

FlowIntent.Core is available on **Maven Central**, making it easy to integrate without any authentication tokens.

Add the dependency to your `build.gradle`:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.flowintent:flowintent-core:1.1.0'
}
```

# Usage Examples
Basic Navigation (Legacy)
Start an Activity with a simple navigation flow:
```kotlin
val flowIntent = FlowIntent(context, TargetActivity::class.java, lifecycleScope)
flowIntent.scheduleJob { intent ->
    intent.emitData("key", "Welcome to FlowIntent.Core")
}
flowIntent.startWithBackStack(clearTop = false)
```

Dynamic Data with Coroutines
Emit and handle dynamic data during navigation:
```kotlin
val flowIntent = FlowIntent(context, TargetActivity::class.java, lifecycleScope)
flowIntent.scheduleJob { intent ->
    var count = 0
    while (true) {
        intent.emitData("counter", "Count: $count")
        count++
        delay(1000)
    }
}
flowIntent.startWithBackStack()
```

Advanced Deeplink
Handle deep links with custom validation:
```kotlin
val flowIntent = FlowIntent(context, TargetActivity::class.java, lifecycleScope)
flowIntent.navigateToDeeplink("myapp://details?id=123&action=view") { params ->
    val id = params.getString("id")
    val action = params.getString("action")
    id?.isNotEmpty() == true && action == "view"
}
```

## Why FlowIntent.Core?
 * Versatile: Supports legacy Android projects with a modern Kotlin-first approach.

 * Powerful Deeplinking: Advanced deeplink features for complex navigation needs.

 * Lightweight: Minimal dependencies, optimized for performance.

 * Open Source: Free to use and contribute under the Apache 2.0 License.

## Getting Started
 * Add the Dependency: Include FlowIntent.Core in your build.gradle as shown above.

 * Explore the API: Start with basic navigation or dive into advanced deeplink features.

 * Check the Docs: See below for more resources.


## Roadmap
FlowIntent.Core is actively evolving! Upcoming features include:
 * Gradle DSL Support: Customize library features with a flowIntent {} block.

 * Jetpack Compose Integration: Full navigation support for Compose-based apps.

 * Stay tuned by watching the repository or checking the releases.

## Contributing
FlowIntent.Core is an open-source project, and we welcome contributions from the community!
 * Repository: github.com/GokhanDurmaz/FlowIntent.Core

 * Issues: Report bugs or suggest features here.

 * Pull Requests: Submit your improvements here.

## Resources
 * Latest Release: v1.1.0

 * Changelog: CHANGELOG.md

 * License: Apache 2.0

 * Contact: Gokhan Durmaz [gdurmaz1234@gmail.com](mailto:gdurmaz1234@gmail.com)


Last Updated: April 2025



