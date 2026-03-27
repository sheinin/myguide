package com.myguide.data


enum class Query {
    ITEM,
    ITEMS,
    SHOP,
    SHOPS;

    val next: Query
        get() = when (this) {
            ITEM -> SHOP
            ITEMS -> ITEM
            SHOP -> ITEM
            SHOPS -> SHOP
        }
    val title: String
        get() = when (this) {
            ITEM -> "Available at These Shops:"
            ITEMS -> "All Items:"
            SHOP -> "Available Items:"
            SHOPS -> "All Shops:"
        }
}