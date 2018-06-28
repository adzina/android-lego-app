package com.android.jake.legoapp

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_project.*
import kotlinx.android.synthetic.main.activity_url.*
import okhttp3.OkHttpClient
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.SimpleXmlConverterFactory
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class NewProjectActivity : AppCompatActivity() {

    private var myDBHelper = DBHandler(this, null, null, 1)
    private lateinit var listView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_project)
    }
    fun addNewProject(v: View){

        val name = nameText.text.toString()
        val url = getUrl()
        val projectID = myDBHelper.addInventory(name)
        val xmlTask = BgTask()
        xmlTask.execute(projectID.toString(),name, url)
        Toast.makeText(this,"New project added",Toast.LENGTH_LONG).show()

        super.finish()

    }

    private fun getUrl():String{
        var base_url = ""
        if(fileList().contains("url.txt")){
            try {
                val file = InputStreamReader(openFileInput("url.txt"))
                val br = BufferedReader(file)
                base_url = br.readLine()
                br.close()
                file.close()
            }
            catch (e: IOException) {
                Log.i("INFO",e.toString())
            }
        }
        else{
            base_url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
        }
        return base_url
    }
    private inner class BgTask: AsyncTask<String, Int, String>(){
        override fun doInBackground(vararg params: String): String {
            try {
                var projectID = params[0]
                var projectNumber = params[1]
                var urlPart = params[2]
                val url = URL("$urlPart$projectNumber.xml")

                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream( "$testDirectory/$projectNumber.xml")
                val data = ByteArray(1024)
                var total: Long = 0
                var progress = 0
                var count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progressTemp = total.toInt() * 100 / lengthOfFile
                    if (progressTemp % 10 == 0 && progress != progressTemp) {
                        progress = progressTemp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
                Log.i("INFO","getting inv parts for "+projectNumber)
                getInventoryParts(projectNumber, projectID)
            } catch (e: MalformedURLException) {
                Log.i("INFO","Malformed URL")
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                Log.i("INFO","File not found")
                return "File not found"
            } catch (e: IOException) {
                Log.i("INFO","IO Exception")
                return "IO Exception"
            }
            Log.i("INFO", "plik odczytany")

            return "success"
        }
    }
    private fun getInventoryParts(projectNumber: String, projectID: String):ArrayList<InventoryPart>{
        val items = ArrayList<InventoryPart>()
        val filename = "$projectNumber.xml"
        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {

            val file = File(inDir, filename)
            if(file.exists()) {
                Log.i("INFO", "exists")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()
                val itemsXML: NodeList = xmlDoc.getElementsByTagName("ITEM")
                for (i in 0 until itemsXML.length) {
                    val itemNode: Node = itemsXML.item(i)
                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        val elem = itemNode as Element
                        val children = elem.childNodes
                        val item = InventoryPart()

                        for (j in 0 until children.length) {
                            val node=children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "ITEMTYPE" -> { item.type = node.textContent }
                                    "ITEMID" -> { item.itemID = node.textContent }
                                    "QTY" -> { item.quantityInSet = node.textContent.toInt() }
                                    "COLOR" -> { item.colorCode = node.textContent.toInt() }
                                    "EXTRA" -> { item.extra = node.textContent }
                                    "ALTERNATE" -> { item.alternate = node.textContent }
                                }
                            }
                        }

                        if (item.type != null && item.itemID != null && item.quantityInSet != null
                                && item.colorCode != null && item.extra != null && item.alternate == "N")
                            items.add(item)
                    }
                }
            }
        }
        Log.i("INFO", "części zapisane")
        myDBHelper.saveInventoryParts(projectID.toInt(),items)
        return items

    }
}

