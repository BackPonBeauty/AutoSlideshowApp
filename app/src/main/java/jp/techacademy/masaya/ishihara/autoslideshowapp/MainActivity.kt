package jp.techacademy.masaya.ishihara.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var n = 0
    private var nn = 0
    private var maxnn = 0
    private var mHandler = Handler()
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        nn = 0

        start_button.setOnClickListener {
            start_button.text = "stop"
            reset_button.isEnabled = false
            pause_button.isEnabled = false


            if (mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {

                        mTimerSec += 0.1
                        mHandler.post {

                            nn++
                            if(nn > maxnn) {
                                nn = 0
                            }
                            timer.text = nn.toString()
                            val id = preference.getLong(nn.toString(),0)
                            //getLong(n.toString())
                            val imguri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            imageView.setImageURI(imguri)
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
            }else{
                mTimer!!.cancel()
                mTimer = null
                start_button.text = "start"
                reset_button.isEnabled = true
                pause_button.isEnabled = true
            }
        }

        pause_button.setOnClickListener {
         /*   if (mTimer != null){
                mTimer!!.cancel()
                mTimer = null
            }
          */
            val preference = PreferenceManager.getDefaultSharedPreferences(this)
            nn++
            if(nn > maxnn) {
                nn = 0
            }
            timer.text = nn.toString()
            val id = preference.getLong(nn.toString(),0)
            //getLong(n.toString())
            val imguri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imguri)
        }

        reset_button.setOnClickListener {
            val preference = PreferenceManager.getDefaultSharedPreferences(this)
            nn--
            if(nn <= 0) {
                nn = maxnn
            }
            timer.text = nn.toString()
            val id = preference.getLong(nn.toString(),0)
            //getLong(n.toString())
            val imguri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imguri)
        }

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            val preference = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = preference.edit()

            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                editor.putLong(n.toString(), id)
                editor.commit()
                Log.d("ANDROID", "URI : " + id + ":" + imageUri.toString())
                n++
        } while (cursor.moveToNext())
            maxnn = n-1
            nn = 0
            val id = preference.getLong(nn.toString(),0)
            //getLong(n.toString())
            val imguri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imguri)

        }
        cursor.close()
    }
}