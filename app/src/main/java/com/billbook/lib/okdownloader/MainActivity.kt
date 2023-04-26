package com.billbook.lib.okdownloader

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.billbook.lib.downloader.*
import com.billbook.lib.okdownloader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NetworkMonitor.getInstance(this).startup()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun download() {
        val downloader = Downloader.Builder()
            .addInterceptor(CopyOnExistsInterceptor(this.applicationContext))
            .addInterceptor(NetworkInterceptor(this.applicationContext))
            .build()
        val request = DownloadRequest.Builder()
            .url("https://wap.pp.cn/app/dl/fs08/2023/03/21/2/110_083a6016054e988728d7b6b36f1fdb4b.apk")
            .md5("2f4374a1936b6a2a24b1a0fe3aac0edc")
            .path(filesDir.absolutePath + "/222.apk")
            .build()
        downloader.newCall(request)
            .enqueue(object : Download.Callback {
                override fun onStart(call: Download.Call) {
                    super.onStart(call)
                    Log.i("OkDownloader", "${Thread.currentThread()} onStart call = $call")
                }

                override fun onLoading(call: Download.Call, current: Long, total: Long) {
                    super.onLoading(call, current, total)
                    Log.i(
                        "OkDownloader",
                        "${Thread.currentThread()} onLoading call = $call, current = $current, total = $total"
                    )
                }

                override fun onFailure(call: Download.Call, response: Download.Response) {
                    super.onFailure(call, response)
                    Log.i(
                        "${Thread.currentThread()} OkDownloader",
                        "onFailure call = $call, response = $response"
                    )
                }

                override fun onSuccess(call: Download.Call, response: Download.Response) {
                    super.onSuccess(call, response)
                    Log.i(
                        "${Thread.currentThread()} OkDownloader",
                        "onSuccess call = $call, response = $response"
                    )
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                download()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}