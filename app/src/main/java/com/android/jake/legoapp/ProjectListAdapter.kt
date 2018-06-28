package com.android.jake.legoapp


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.android.jake.legoapp.R.id.listView

class ProjectListAdapter(private val context: Context,
                           private val dataSource: ArrayList<Inventory>, private val actionCallBack: MyActionCallback) : BaseAdapter() {

    private var cb: MyActionCallback = actionCallBack
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    val myArchiveClickListener = View.OnClickListener {view ->

        cb.onActionPerformed("archive",view.getTag().toString().toInt())


    }
    val myViewClickListener = View.OnClickListener {view ->

        cb.onActionPerformed("view",view.getTag().toString().toInt())

    }
    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        // 1
        if (convertView == null) {

            // 2
            view = inflater.inflate(R.layout.row, parent, false)

            // 3
            holder = ViewHolder()
            holder.nameTextView = view.findViewById(R.id.textView)
            holder.archiveButton = view.findViewById(R.id.buttonArchive)
            holder.viewButton = view.findViewById(R.id.buttonView)

            // 4
            view.tag = holder
        } else {
            // 5
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        // 6
        val nameTextView  = holder.nameTextView
        val archiveButton = holder.archiveButton
        val viewButton = holder.viewButton

        val project = getItem(position) as Inventory
        archiveButton.setTag(project.id)
        viewButton.setTag(project.id)
        archiveButton.setOnClickListener(myArchiveClickListener)
        viewButton.setOnClickListener(myViewClickListener)
        nameTextView.text = project.name



        return view
    }

    private class ViewHolder {
        lateinit var nameTextView: TextView
        lateinit var archiveButton: Button
        lateinit var viewButton: Button
    }

    interface MyActionCallback {
        fun onActionPerformed(type: String, position: Int)
    }
}
