package com.android.jake.legoapp

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.FileNotFoundException

/**
 * Created by Jake on 31.05.2018.
 */

class ItemListAdapter(private val context: Context,
                         private val dataSource: ArrayList<InventoryPart>): BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


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
            view = inflater.inflate(R.layout.row2, parent, false)

            // 3
            holder = ViewHolder()
            holder.itemImage = view.findViewById(R.id.itemImage)
            holder.textItemID = view.findViewById(R.id.textItemID)
            holder.textItemName = view.findViewById(R.id.textItemName)
            holder.textQuantity = view.findViewById(R.id.textQuantity)
            holder.buttonUp = view.findViewById(R.id.buttonUp)
            holder.buttonDown = view.findViewById(R.id.buttonDown)

            // 4
            view.tag = holder
        } else {
            // 5
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        // 6
        var itemImage = holder.itemImage
        var textItemID = holder.textItemID
        var textQuantity = holder.textQuantity
        var textItemName = holder.textItemName
        val buttonUp = holder.buttonUp
        val buttonDown = holder.buttonDown

        val item = getItem(position) as InventoryPart
        buttonUp.setTag(item.id)
        buttonDown.setTag(item.id)

        buttonDown.setOnClickListener{
            val new = textQuantity.text.toString().toInt()-1
            if(new>=0){
                textQuantity.text = new.toString()
                dataSource[position].quantityInStore-=1
            }
            if(new!=item.quantityInSet){
                var lLayout = view.findViewById<RelativeLayout>(R.id.row2)
                lLayout.setBackgroundColor(Color.WHITE)
            }
        }
        buttonUp.setOnClickListener{
            val new = textQuantity.text.toString().toInt()+1
            if(new<=item.quantityInSet){
                textQuantity.text = new.toString()
                dataSource[position].quantityInStore+=1
            }
            if(new == item.quantityInSet){
                var lLayout = view.findViewById<RelativeLayout>(R.id.row2)
                lLayout.setBackgroundColor(Color.GREEN)

            }
        }
        textItemID.text = item.itemID
        textQuantity.text = item.quantityInStore.toString()
        textItemName.text = item.partName
        try{
            itemImage.setImageBitmap(BitmapFactory.decodeFile(item.image))
        }
        catch(e:FileNotFoundException){}
        if(item.quantityInSet == item.quantityInStore){
            var lLayout = view.findViewById<RelativeLayout>(R.id.row2)
            lLayout.setBackgroundColor(Color.GREEN)
        }


        return view
    }

    private class ViewHolder {
        lateinit var itemImage: ImageView
        lateinit var textItemID: TextView
        lateinit var buttonDown: Button
        lateinit var textQuantity: TextView
        lateinit var buttonUp: Button
        lateinit var textItemName: TextView
    }

    interface MyActionCallback {
        fun onActionPerformed(type: String, position: Int)
    }
}
