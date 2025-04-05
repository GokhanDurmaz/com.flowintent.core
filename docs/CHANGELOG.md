# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- Compose support

## [1.1.0] - 2025-04-02
### Added
- Advanced Deeplink Support: Added support for sophisticated deep linking with parameter validation and custom routing. Example:
  ```kotlin
  val flowIntent = FlowIntent(context, TargetActivity::class.java, lifecycleScope)
  flowIntent.navigateToDeeplink("myapp://details?id=123") { params ->
      params.getString("id")?.isNotEmpty() == true
  }
  ```

- Maven Central Integration: Library is now available on Maven Central. Add it with:
  ```gradle
  implementation 'com.flowintent:flowintent-core:1.1.0'
  ```

Changed
- Performance Improvements: Optimized navigation speed and memory usage for better efficiency.

- Updated documentation with new examples for advanced deeplink usage.

Fixed
- [#10]: Fixed a null pointer exception during deeplink validation.

- [#14]: Optimized coroutine scope handling for dynamic data emission.


Contributors
- [@GokhanDurmaz](https://github.com/GokhanDurmaz) - Advanced Deeplink implementation and optimizations.

Notes
- Removed references to Gradle DSL and Compose support, as they are not yet implemented.

- Full documentation available on GitHub Pages.

- Report issues at GitHub Issues.
