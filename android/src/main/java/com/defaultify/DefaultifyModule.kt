package com.defaultify

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

import com.facebook.react.bridge.ReactContext
import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.util.concurrent.TimeUnit

class DefaultifyModule(reactContext: ReactApplicationContext) :   PermissionListener,
  LifecycleObserver, ReactContextBaseJavaModule(reactContext) {

  private var activity: Activity? = null
  private val mainHandler = Handler(Looper.getMainLooper())
  private var screenRecorderActivityResultLauncher: ActivityResultLauncher<Intent>? = null
  private var appContext: Application? = null
  private var token = ""

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(LoggingInterceptor())
    .build()

  init {
    // Initialize screenRecorderActivityResultLauncher in activity onCreate s
  }

  fun startRecording(appToken: String) {
    (currentActivity as? AppCompatActivity)?.let { activity ->
      screenRecorderActivityResultLauncher = activity.activityResultRegistry.register(
        "ScreenRecorder", ActivityResultContracts.StartActivityForResult()
      ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
          // Handle the result
          Defaultify.launch(activity, token,"ReactNative")
        } else {
          // Handle denied permission scenario
        }
      }
    }
    val activity = currentActivity
    if (activity != null) {
      val mediaProjectionManager =
        activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
      val permissionIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
      screenRecorderActivityResultLauncher?.launch(permissionIntent)
    } else {

    }
  }


  override fun getName(): String {
    return NAME
  }

  private val lifecycle: Lifecycle by lazy {
    ((reactApplicationContext as ReactContext).currentActivity as AppCompatActivity).lifecycle
  }

  @ReactMethod
  fun sendCrashReport(crashData: String, promise: Promise) {
    try {
      currentActivity?.let { Defaultify.logException(crashData, it) }
      // Simulate sending cleanedCrashData to the server here and assume it's successful
      promise.resolve(true) // Resolve the promise with true indicating success
    } catch (e: Exception) {
      promise.reject("SEND_CRASH_REPORT_FAILED", e) // Reject the promise with the error
    }
  }

  @SuppressLint("SuspiciousIndentation")
  @ReactMethod
  fun defaultifyLaunch(appToken: String) {
    val activity = currentActivity
    if (activity != null) {
      try {
        mainHandler.post {
          lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
              token = appToken
              try {
                val permissionsGranted = hasPermissions()
                if (permissionsGranted) {
                  Defaultify.launch(activity, token,"ReactNative")
                } else {
                  requestPermissions()
                }
              } catch (e: Exception) {
                Log.d("Launch failed", "Launch failed $e")
              }
            }
          })
        }
      } catch (e: Exception) {
        Log.d("Launch failed", "Launch failed $e")
      }
    } else {
      Log.d("No Activity", "No current activity available")

    }
  }

@ReactMethod
fun sendNetworkLog(networkData: String) {
  try {
    val activity = currentActivity
    Defaultify.logNetwork(networkData,activity!!)
  } catch (e: Exception) {
    Log.d("TAG", "sendNetworkLog: "+e.message)
    // Exception block for error handling
  }
}
  private fun hasPermissions(): Boolean {
    val permissionsGranted = (ContextCompat.checkSelfPermission(
      currentActivity!!.applicationContext!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
      currentActivity!!.applicationContext!!, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED)
    return permissionsGranted
  }

  private fun requestPermissions() {
    if (currentActivity == null) {
      return
    }
    val permissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
      arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_IMAGES
      )
    } else {
      arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
    }
    val permissionAwareActivity = currentActivity as PermissionAwareActivity
    permissionAwareActivity.requestPermissions(permissions, PERMISSION_REQUEST_CODE, this)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<out String>, grantResults: IntArray
  ): Boolean {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
      if (allPermissionsGranted) {
        currentActivity?.let {
          Defaultify.launch(it, token,"ReactNative")
        }
      } else {
        Log.d("DefaultifyKotlinSwiftModule", "Some permissions were denied")
      }
      return true
    }
    return false
  }

  private class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

      val request = chain.request()
      val requestLog = StringBuilder()
      requestLog.append("Request:")
        .append("\n${request.method} ${request.url}")
        .append("\nHeaders: ${request.headers}")

      val requestBody = request.body
      if (requestBody != null) {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        val charset = requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        requestLog.append("\nBody: ${buffer.readString(charset)}")
      }

      val response = chain.proceed(request)
      val responseBody = response.body

      val responseLog = StringBuilder()
      responseLog.append("Response:")
        .append("\n${response.code} ${response.message}")
        .append("\nHeaders: ${response.headers}")

      if (responseBody != null) {
        val contentType = responseBody.contentType()
        val charset = contentType?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        val bodyString = responseBody.string()
        responseLog.append("\nBody: $bodyString")
      }


      return response.newBuilder()
        .body(
          ResponseBody.create(
            responseBody?.contentType(),
            responseLog.toString().toByteArray()
          )
        )
        .build()
    }
  }

  companion object {
    const val NAME = "Defaultify"
    private const val PERMISSION_REQUEST_CODE = 101
    private const val SCREEN_RECORD_REQUEST_CODE = 102
  }

}


