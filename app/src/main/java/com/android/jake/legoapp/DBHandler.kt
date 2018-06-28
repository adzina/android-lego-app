package com.android.jake.legoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteException
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime




/**
 * Created by Jake on 30.04.2018.
 */
class DBHandler(context: Context, name: String?, factory:SQLiteDatabase.CursorFactory?, version: Int):
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION)
{
    private val DB_PATH = "/data/data/com.android.jake.legoapp/databases/"
    private val DB_NAME = "BrickList.db"
    private var myDataBase: SQLiteDatabase ?= null
    private val myContext: Context = context

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "BrickList.db"
        val TABLE_INVENTORIES = "Inventories"
        val TABLE_INVENTORIESPARTS = "InventoriesParts"
        val TABLE_CODES = "Codes"
        val TABLE_PARTS = "Parts"
        val COLUMN_ID = "_id"
        val COLUMN_NAME = "Name"
        val COLUMN_ACTIVE = "Active"
        val COLUMN_LAST_ACCESSED = "LastAccessed"
        val COLUMN_INVENTORY_ID = "InventoryID"
        val COLUMN_TYPE_ID = "TypeID"
        val COLUMN_ITEM_ID = "ItemID"
        val COLUMN_QUANTITY_IN_SET = "QuantityInSet"
        val COLUMN_QUANTITY_IN_STORE = "QuantityInStore"
        val COLUMN_COLOR_ID = "ColorID"
        val COLUMN_EXTRA = "Extra"
        val COLUMN_IMAGE = "Image"
        val COLUMN_CODE = "Code"
    }

    @Throws(SQLException::class)
    fun openDataBase() {

        //Open the database
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)

    }
    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) {
            //do nothing - database already exist
        } else {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase

            try {
                copyDataBase()
            } catch (e: IOException) {
                Log.i("INFO", "error copying database")
                throw Error("Error copying database")

            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private fun checkDataBase(): Boolean {

        var checkDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteCantOpenDatabaseException) {
            Log.i("INFO", "database doesn't exist yet")
            //database does't exist yet.
        }

        if (checkDB != null) {
            Log.i("INFO", "database exists")
            checkDB.close()
        }
        return if (checkDB != null) true else false
    }


    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    @Throws(IOException::class)
    private fun copyDataBase() {

        //Open your local db as the input stream
        val myInput = myContext.getAssets().open(DB_NAME)

        // Path to the just created empty db
        val outFileName = DB_PATH + DB_NAME

        //Open the empty db as the output stream
        val myOutput = FileOutputStream(outFileName)

        //transfer bytes from the inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        length = myInput.read(buffer)
        while (length  > 0) {
            myOutput.write(buffer, 0, length)
            length = myInput.read(buffer)

        }

        //Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()

    }

    @Synchronized override fun close() {

        if (myDataBase != null)
            myDataBase?.close()

        super.close()

    }
    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun addInventory(name: String): Int{
        openDataBase()
        var id : Int
        val current = LocalDateTime.now()
        var lastAccessed = current.year
        val values = ContentValues()
        values.put(COLUMN_ACTIVE,1)
        values.put(COLUMN_LAST_ACCESSED,lastAccessed)
        values.put(COLUMN_NAME, name)


        myDataBase?.insert(TABLE_INVENTORIES, null, values)
        var cursor: Cursor?
        cursor = myDataBase?.rawQuery("select * from " + TABLE_INVENTORIES+" where "+ COLUMN_NAME+" ="+name, null)
        if (cursor!!.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            }
        else{
            return -1
        }

        close()
        return id
    }

    fun desactivateInventory(id: Int){
        Log.i("desactivating",id.toString())
        openDataBase()
        val values = ContentValues()
        values.put(COLUMN_ACTIVE,0)
        var condition = arrayOf(id.toString())
        myDataBase?.update(TABLE_INVENTORIES,values, COLUMN_ID+"=?", condition)
        close()
    }
    fun getAllInventories(): ArrayList<Inventory>{
        openDataBase()
        val inventories = ArrayList<Inventory>()
        var cursor: Cursor?
        try {
            Log.i("INFO",myDataBase.toString())
            cursor = myDataBase?.rawQuery("select * from " + TABLE_INVENTORIES+" where "+ COLUMN_ACTIVE+" =1 ORDER BY "+ COLUMN_LAST_ACCESSED+" DESC", null)
        } catch (e: SQLiteException) {
            Log.i("INFO", e.toString())
            return inventories
        }

        var id: Int
        var name: String
        var active: Int
        var lastAccessed: Int
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                lastAccessed = cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_ACCESSED))
                active = cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVE))
                inventories.add(Inventory(id, name, active, lastAccessed))
                cursor.moveToNext()
            }
        }
        close()
        return inventories
    }
    fun getInventoryName(id: Int): String{
        openDataBase()
        var name =""
        var cursor: Cursor?
        try {
            cursor = myDataBase?.rawQuery("select * from " + TABLE_INVENTORIES+" where "+ COLUMN_ID+" ="+id, null)
        } catch (e: SQLiteException) {
            Log.i("INFO", e.toString())
            return name
        }

        if (cursor!!.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))

        }
        close()
        return name
    }
    fun getInventoryID(name: String?): Int{
        openDataBase()
        var id = 0
        var cursor: Cursor?
        try {
            cursor = myDataBase?.rawQuery("select * from " + TABLE_INVENTORIES+" where "+ COLUMN_NAME+" ="+name+" AND "+ COLUMN_ACTIVE+"=1", null)
        } catch (e: SQLiteException) {
            Log.i("INFO", e.toString())
            return id
        }

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))

            }
        }
        close()
        return id
    }
    fun saveInventoryParts(id: Int, parts: ArrayList<InventoryPart>){
        openDataBase()
        for (i in parts.indices) {
            val values = ContentValues()
            values.put(COLUMN_INVENTORY_ID,id)
            values.put(COLUMN_TYPE_ID,parts[i].type)
            values.put(COLUMN_ITEM_ID, parts[i].itemID)
            values.put(COLUMN_QUANTITY_IN_SET, parts[i].quantityInSet)
            values.put(COLUMN_QUANTITY_IN_STORE, 0)
            values.put(COLUMN_COLOR_ID, parts[i].colorCode)
            values.put(COLUMN_EXTRA, parts[i].extra)

            myDataBase?.insert(TABLE_INVENTORIESPARTS, null, values)
        }
        Log.i("INFO", "Elementy poprawnie załadowane z XML do bazy danych")

        close()
    }

    fun updateInventoriesPart(inventoryID: Int, ItemID: String, quantityInStore: Int){
        openDataBase()
        val values = ContentValues()
        var condition = arrayOf(inventoryID.toString(), ItemID.toString())
        values.put(COLUMN_QUANTITY_IN_STORE,quantityInStore)
        myDataBase?.update(TABLE_INVENTORIESPARTS,values,"InventoryID=? and ItemID=?", condition)
        close()
    }

    fun updateInventoryActive(inventoryID: Int,ts:Int){
        openDataBase()

        val values = ContentValues()
        values.put(COLUMN_LAST_ACCESSED,ts)
        var condition = arrayOf(inventoryID.toString())
        myDataBase?.update(TABLE_INVENTORIES,values, COLUMN_ID+"=?", condition)

    }
    fun getPartName(code: String):String{
        openDataBase()
        var name = ""
        var cursor: Cursor?
        cursor = myDataBase?.rawQuery("select * from "+ TABLE_PARTS +" where "+ COLUMN_CODE+
                                        " ='"+code+"'", null)
        if (cursor!!.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))

        }
        close()
        return name

    }
    fun getInventoryParts(inventoryID: Int): ArrayList<InventoryPart>{

        var parts: ArrayList<InventoryPart> = ArrayList()
        openDataBase()

        var cursor: Cursor?
        try {
            cursor = myDataBase?.rawQuery("SELECT * FROM "+ TABLE_INVENTORIESPARTS+" WHERE "+
                    COLUMN_INVENTORY_ID+" ='"+inventoryID+
                    "' ORDER BY CASE ("+ COLUMN_QUANTITY_IN_SET+ "-"+COLUMN_QUANTITY_IN_STORE+")" +
                    " WHEN 0 THEN 0 " +
                    "ELSE 1 " +
                    "END " +
                    "DESC, "+ COLUMN_COLOR_ID,null)


        } catch (e: SQLiteException) {
            Log.i("INFO", e.toString())
            return parts
        }

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                val item = InventoryPart()
                item.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                item.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_ID))
                item.itemID = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_ID))
                item.quantityInSet = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY_IN_SET))
                item.quantityInStore = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY_IN_STORE))
                item.colorCode = cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR_ID))
                item.extra = cursor.getString(cursor.getColumnIndex(COLUMN_EXTRA))

                parts.add(item)
                cursor.moveToNext()
            }
        }
        close()
        return parts
    }

    fun getImage(itemID: String, color: Int):String {
        openDataBase()
        val cursor = myDataBase?.rawQuery("select * from " + TABLE_PARTS + " where Code='" + itemID+"'" , null)

        if (cursor!!.moveToFirst()) {
            var id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))

            val cursor2 = myDataBase?.rawQuery("select * from " + TABLE_CODES + " where ItemID='" + id+"' AND ColorID="+color , null)
            if(cursor2!!.moveToFirst()){
                close()
                try{
                    return cursor2.getString(cursor2.getColumnIndex(COLUMN_IMAGE))
                }
                catch(e:IllegalStateException){}
            }
        }
        close()
        return ""
    }
    fun getImageCode(itemID: String, color: Int):String {
        openDataBase()
        val cursor = myDataBase?.rawQuery("select * from " + TABLE_PARTS + " where Code='" + itemID+"'" , null)

        if (cursor!!.moveToFirst()) {
            var id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))

            val cursor2 = myDataBase?.rawQuery("select * from " + TABLE_CODES + " where ItemID='" + id+"' AND ColorID="+color , null)
            if(cursor2!!.moveToFirst()){
                close()
                return cursor2.getString(cursor2.getColumnIndex(COLUMN_CODE))
            }
        }
        close()
        return ""
    }

    fun getCode(itemID: String, color: Int):Int {
        openDataBase()
        val cursor = myDataBase?.rawQuery("select * from " + TABLE_CODES + " where ItemID='" + itemID +
                "' AND ColorID='" + color + "'", null)

        if (cursor!!.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(COLUMN_CODE))
        }
        return 0
    }
    fun insertImage(img: String, color: Int, typeID: String): Boolean {
        openDataBase()
        val cursor = myDataBase?.rawQuery("select * from " + TABLE_PARTS + " where Code='" + typeID+"'" , null)

        if (cursor!!.moveToFirst()) {
            var id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))

            val cv = ContentValues()
            cv.put(COLUMN_IMAGE, img)
            cv.put(COLUMN_COLOR_ID, color)
            cv.put(COLUMN_ITEM_ID, id)
            val result = myDataBase?.insert(TABLE_CODES, null, cv)

            return if (result!!.equals( -1))
                false
            else
                true
        }
    return false
    }

}