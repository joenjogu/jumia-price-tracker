package com.example.jpt_demo

import com.google.firebase.database.Exclude

data class Product(

    @get: Exclude
    var id : String? = null,
    var productname : String? = null,
    var seller : String? = null,
    var currentprice : Int? = null,
    var previousprice : Int? = null,
    var targetprice : Int? = null,
    var producturl : String? = null,
    var imageurl : String? = null,
    @get: Exclude
    var isDeleted : Boolean = false
){
    override fun equals(other: Any?): Boolean {
        return if (other is Product){
            other.id == id
        }else false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (productname?.hashCode() ?: 0)
        result = 31 * result + (seller?.hashCode() ?: 0)
        result = 31 * result + (currentprice ?: 0)
        result = 31 * result + (previousprice ?: 0)
        result = 31 * result + (targetprice ?: 0)
        result = 31 * result + (producturl?.hashCode() ?: 0)
        return result
    }
}