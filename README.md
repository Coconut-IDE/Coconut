# Coconut

How to get started
------------------

### Prerequisite
1. Install 1) Android Studio (for Android development and plugin testing, **recommended version: Android Studio AI-182.5107.16.33.5314842**, see [Android Studio download archives](https://developer.android.com/studio/archive)) 2) IntelliJ IDEA (for plugin development)
2. Install the "Groovy" plugin in IntelliJ IDEA or Android Studio

### Steps to build and run the plugin
1. open the privacyhelperplugin project with IntelliJ IDEA
2. setup the IntelliJ Platform SDK. Go to File -> Project Structure -> SDKs, use the "+" button to add an IntelliJ Platform Plugin SDK and then select the right path in your Android Studio folder. See the screenshot below for details.

**Note:**
1. You need to manually add the Groovy plugin jars (in Groovy/libs) to the classpath of the SDK. See the last four jars in the screenshot below as an example.
2. Make sure you name the SDK: Android Studio AI-182.5107.16.33.5314842, otherwise you will need to modify ``PrivacyHelperPlugin.iml`` accordingly
3. Make sure you use the Jetbrains Java Runtime (JDK), which should be bundled with the Android Studio application (See [this post](https://intellij-support.jetbrains.com/hc/en-us/articles/206544879) for more information). You will need to create a new JDK with this path, then specify it for the "Internal Java Platform" in the SDK config (See the screenshot).

![How to set up Android Studio SDK](https://github.com/i7mist/Coconut/blob/loc_source/readme/androidstudiosdks.png)
3. setup run/debug configuration. Go to Run -> Edit Configurations. Add a new configuration under the Plugin category. Make sure you select the right JRE. See the screenshot below.
![How to set up run/build configuration](https://github.com/i7mist/Coconut/blob/loc_source/readme/runconfigurations.png)
4. select the configuration that you just created, and run/debug it (just as how you do that in Android Studio)

If done correctly, you should be able to see Coconut in your plugin list (Android Studio -> Preferences -> Plugins).

For a more comprehensive instruction, please refer to: https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/running_and_debugging_a_plugin.html 

Contributors
------------
* Tianshi Li (Carnegie Mellon University)
* Mike Czapik (Carnegie Mellon University)
* Tiffany Yu (Carnegie Mellon University)
* Elijah Neundorfer (Columbus State University)
