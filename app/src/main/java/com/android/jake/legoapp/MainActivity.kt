package com.android.jake.legoapp

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity(), ProjectListAdapter.MyActionCallback {

    private var myDBHelper = DBHandler(this, null, null, 1)
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            showNewProjectActivity()
        }
        this.StoreDatabase()
        refresh()


    }

    override fun onActionPerformed(type: String,id: Int) {
        Log.i("INFO", type+" "+id)
        if(type=="archive") archiveInventory(id)
        else viewInventory(id)
    }
    private fun StoreDatabase() {

        try {
            myDBHelper.createDataBase()

        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }



    }
    fun getInventories(): ArrayList<Inventory>{
        var inventories: ArrayList<Inventory>
        inventories = myDBHelper.getAllInventories()
        return inventories
    }
    fun createView(inventories: ArrayList<Inventory>){
        listView = findViewById<ListView>(R.id.listView)

        val adapter = ProjectListAdapter(this, inventories,this)

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedProject = inventories[position]
            Log.i("INFO",selectedProject.name)
            val id = myDBHelper.getInventoryID(selectedProject.name)
            onActionPerformed("view",id)
        }

    }
    fun viewInventory(i:Int){
        showProjectActivity(i)
    }
    fun archiveInventory(i:Int){
        myDBHelper.desactivateInventory(i)
        refresh()
    }
    fun refresh(){
        createView(getInventories())
    }
    fun showNewProjectActivity(){
        val i = Intent(this,NewProjectActivity::class.java)
        startActivityForResult(i,100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        refresh()
    }
    fun showProjectActivity(id:Int){
        val i = Intent(this,ProjectActivity::class.java)
        i.putExtra("id",id)
        startActivityForResult(i,200)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = Intent(this, URLActivity::class.java)
        startActivity(i)

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
