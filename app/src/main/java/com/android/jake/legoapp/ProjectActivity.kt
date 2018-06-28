package com.android.jake.legoapp

import android.content.res.Resources
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class ProjectActivity : AppCompatActivity() {
    var name: String = ""
    var inventoryID: Int = 0
    private var myDBHelper = DBHandler(this, null, null, 1)
    private lateinit var listView: ListView
    private var adapter: ItemListAdapter? = null
    private var parts: ArrayList<InventoryPart> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        inventoryID = intent.getIntExtra("id",-1)
        name = myDBHelper.getInventoryName(inventoryID)
        val tsLong = System.currentTimeMillis() / 10000
        val ts = tsLong.toInt()
        myDBHelper.updateInventoryActive(inventoryID,ts)
        parts = myDBHelper.getInventoryParts(inventoryID)
        createView(parts)

    }

    fun save(v: View){
        for(i in 0 until parts.size){
            Log.i("INFO", parts[i].quantityInStore.toString())
            Log.i("INFO", i.toString())
            myDBHelper.updateInventoriesPart(inventoryID,parts[i].itemID,parts[i].quantityInStore)
        }

        Toast.makeText(this,"Stan klocków został pomyślnie zapisany",Toast.LENGTH_LONG).show()
    }
    fun generateXML(v: View){

        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val rootElement: Element = doc.createElement("INVENTORY")

        for(i in 0 until parts.size){
            var view = adapter?.getView(i, null, listView)
            var textView: TextView? = view?.findViewById(R.id.textQuantity)
            var currentQuantity = textView?.text.toString().toInt()
            myDBHelper.updateInventoriesPart(inventoryID,parts[i].itemID,currentQuantity.toString().toInt())

            var missing = parts[i].quantityInSet - currentQuantity
            if(missing!=0){
                val itemElement: Element = doc.createElement("ITEM")

                val itemTypeElement: Element = doc.createElement("ITEMTYPE")
                itemTypeElement.appendChild(doc.createTextNode(parts[i].type))
                itemElement.appendChild(itemTypeElement)

                val itemIDElement: Element = doc.createElement("ITEMID")
                itemIDElement.appendChild(doc.createTextNode(parts[i].id.toString()))
                itemElement.appendChild(itemIDElement)
                val itemColorElement: Element = doc.createElement("COLOR")
                itemColorElement.appendChild(doc.createTextNode(parts[i].colorCode.toString()))
                itemElement.appendChild(itemColorElement)
                val itemQtyFilled: Element = doc.createElement("QTYFILLED")
                itemQtyFilled.appendChild(doc.createTextNode(missing.toString()))
                itemElement.appendChild(itemQtyFilled)

                rootElement.appendChild(itemElement)
            }

        }
        doc.appendChild(rootElement)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()

        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2")

        val path = getExternalFilesDir(null)
        val outDir = File(path, "XML")
        outDir.mkdir()
        val file = File(outDir,"toBuy"+name+".xml")
        transformer.transform(DOMSource(doc),StreamResult(file))
        Toast.makeText(this,"Plik został pomyślnie zapisany na karcie SD",Toast.LENGTH_LONG).show()
    }

    fun createView(parts: ArrayList<InventoryPart>){
        for(i in parts.indices) {
            parts[i].partName = myDBHelper.getPartName(parts[i].itemID)
            var imgPath = myDBHelper.getImage(parts[i].itemID, parts[i].colorCode)
            var imgPresent = imgPath!=""
            if(!imgPresent){
                var imgCode = myDBHelper.getImageCode(parts[i].itemID, parts[i].colorCode)
                //obrazek bez kodu, więc sprawdzamy, czy pobraliśmy już dla niego obrazek
                if(imgCode==""){
                    val path = filesDir
                    val inDir = File(path, "Images")
                    if(!inDir.exists()) inDir.mkdir()

                    val filePath = parts[i].itemID+".jpg"
                    val file = File(inDir, filePath)
                    if(file.exists()){
                        parts[i].image = file.absolutePath
                    }
                    else{
                        getImageFromWeb("https://www.bricklink.com/PL/",filePath, i,parts[i].colorCode)
                    }

                }
                else{
                    getImageFromWeb(null, imgCode, i,null)
                }
            }
            else{
                parts[i].image = imgPath
            }


        }
        listView = findViewById<ListView>(R.id.listViewItems)
        adapter = ItemListAdapter(this, parts)
        listView.adapter = adapter

    }
    fun getImageFromWeb(endpoint: String?, file: String,index: Int,color: Int?):String{

            var imageTask = BgTask()
            if(endpoint.isNullOrBlank()){
                try{
                    imageTask.execute("https://www.lego.com/service/bricks/5/2/",file, index.toString())
                }
                catch(e:Resources.NotFoundException){
                    imageTask.execute("http://img.bricklink.com/P/"+color,file+".gif", index.toString())
                }
            }
            else{
                imageTask.execute(endpoint, file, index.toString())
            }


        return ""

    }

    private inner class BgTask: AsyncTask<String, Int, String>(){
        override fun doInBackground(vararg params: String): String {
            try {
                var urlPart = params[0]
                var partCode = params[1]
                var index = params[2].toInt()
                val url = URL("$urlPart$partCode")
                Log.i("INFO", url.path)
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/Images")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream( "$testDirectory/$partCode")
                val data = ByteArray(1024)
                Log.i("INFO", data.toString())
                var count = isStream.read(data)
                while (count != -1) {
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
                parts[index].image = "$testDirectory/$partCode"
                myDBHelper.insertImage("$testDirectory/$partCode",parts[index].colorCode,parts[index].type)
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
            return "success"
        }
    }

}
