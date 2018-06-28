package com.android.jake.legoapp

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_url.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class URLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url)
        readFromFile()
    }
    fun saveURL(view: View){
        val url = URLText.text
        try {
            val file = OutputStreamWriter(openFileOutput("url.txt", Activity.MODE_PRIVATE))

            file.write (url.toString())
            file.flush ()
            file.close ()
            Toast.makeText(this, "URL modified", Toast.LENGTH_LONG).show()
        } catch (e : IOException) {
            Toast.makeText(this, "Unable to modify URL", Toast.LENGTH_LONG).show()
        }
        super.finish()

    }
    fun readFromFile(){
        var url = ""
        if(fileList().contains("url.txt")){
            try {
                val file = InputStreamReader(openFileInput("url.txt"))
                val br = BufferedReader(file)
                var line = br.readLine()
                    url = line
                    line = br.readLine()
                br.close()
                file.close()
                URLText.setText(url)
            }
            catch (e:IOException) {
            }
        }
    }
}
