package com.android.jake.legoapp

import org.simpleframework.xml.Element
import java.sql.Blob

/**
 * Created by Jake on 05.05.2018.
 */
class InventoryPart {
    var id: Int = 0
    var type: String = ""
    var itemID: String = ""
    var quantityInSet: Int = 0
    var quantityInStore: Int = 0
    var colorCode: Int = 0
    var extra: String = ""
    var alternate: String = ""
    var partName: String = ""
    var image: String = ""

}