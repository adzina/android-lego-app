package com.android.jake.legoapp

/**
 * Created by Jake on 30.04.2018.
 */
class Inventory {

    var id: Int = 0
    var name: String? = null
    var active: Int = 0
    var lastAccessed: Int = 0

    constructor(id: Int, name: String, active:Int, lastAccessed: Int){
        this.id = id
        this.name = name
        this.active = active
        this.lastAccessed = lastAccessed
    }

}