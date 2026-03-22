package com.myguide.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.jvm.java

@Entity(tableName = "items", primaryKeys = ["id","parent"])
data class Item(
    val parent: String,
    val id: String,
    val pic: String?,
    val title: String?,
    val origin: String?,
    val description: String?,
    val drawable: Int?
) {
    @Ignore
    val level: Int = 0
}

data class ItemWithLevel(
     val parent: String,
     val id: String,
     val pic: String?,
     val title: String?,
     val origin: String?,
     val description: String?,
     val drawable: Int?,
    val level: Int
)



@Entity(tableName = "shops", primaryKeys = ["id"])
data class Shop(
    val id: String,
    val title: String,
    val description: String,
    val origin: String,
    val lat: Double,
    val lng: Double,
    val drawable: Int?
) {
    @Ignore
    val level: Int = 0
}

@Entity(tableName = "shop_items", primaryKeys = ["shop", "item"])
data class ShopItems(
    val shop: String,
    val item: String
)

interface ListInterface {
    val description: String?
    val drawable: Int?
    val id: String?
    val lat: Double?
    val lng: Double?
    val origin: String?
    val title: String?
    val level: Int
}

class Repository(private val storeDao: StoreDao) {

    fun getItemDetails(id: String, callback: (Item?) -> Unit) {
        Thread {
            callback(storeDao.itemDetails(id))
        }.start()
    }

    fun getShopDetails(id: String, callback: (Shop?) -> Unit) {
        Thread {
            callback(storeDao.shopDetails(id))
        }.start()
    }
    fun getItems(callback: (List<Item>) -> Unit) {
        Thread {
            callback(storeDao.items())
        }.start()
    }
    fun getShops(callback: (List<Shop>) -> Unit) {
        Thread {
            callback(storeDao.shops())
        }.start()
    }
    fun getShops(id: String,callback: (List<Shop>) -> Unit) {
        Thread {
            callback(storeDao.shops(id))
        }.start()
    }
    fun getTree(callback: (List<ItemWithLevel>) -> Unit) {
        Thread {
            callback(storeDao.tree())
        }.start()
    }
    fun getTree(id: String, callback: (List<ItemWithLevel>) -> Unit) {
        Thread {
            callback(storeDao.tree(id))
        }.start()
    }
    fun updateShop(drawable: Int, id: String) {
        Thread {
            storeDao.updateShop(drawable, id)
        }.start()
    }
    fun updateItem(drawable: Int, pic: String) {
        Thread {
            storeDao.updateItem(drawable, pic)
        }.start()
    }
}


@Dao
@RewriteQueriesToDropUnusedColumns
interface StoreDao {
    @Query(
        "WITH RECURSIVE tree AS (\n" +
        "    SELECT\n" +
        "        id,\n" +
        "        parent,\n" +
        "        coalesce(title, id) as title,\n" +
        "        description,\n" +
        "        drawable,\n" +
        "        origin,\n" +
        "        0 AS level,\n" +
        "        lower(id) AS sort_path       \n" +
        "    FROM items\n" +
        "    WHERE parent = 'ROOT'\n" +
        "    UNION ALL\n" +
        "    SELECT\n" +
        "        n.id,\n" +
        "        n.parent,\n" +
        "        coalesce(n.title, n.id) as title,\n" +
        "        n.description,\n" +
        "        n.drawable,\n" +
        "        n.origin,\n" +
        "        tree.level + 1 AS level,\n" +
        "        tree.sort_path || '>' || lower(n.id) AS sort_path\n" +
        "    FROM items AS n\n" +
        "    JOIN tree ON n.parent = tree.id\n" +
        ")\n" +
        "SELECT\n" +
        "    id,\n" +
        "    parent,\n" +
        "    title,\n" +
        "    description,\n" +
        "    origin,\n" +
        "    drawable,\n" +
        "    level\n" +
        "FROM tree\n" +
        "ORDER BY sort_path;"
    )
    fun tree(): List<ItemWithLevel>
    @Query(
        "WITH RECURSIVE tree AS (\n" +
        "    SELECT\n" +
        "        id,\n" +
        "        parent,\n" +
        "        coalesce(title, id) as title,\n" +
        "        description,\n" +
        "        drawable,\n" +
        "        origin,\n" +
        "        0 AS level,\n" +
        "        lower(id) AS sort_path       \n" +
        "    FROM items\n" +
        "    WHERE parent = 'ROOT'\n" +
        "    UNION ALL\n" +
        "    SELECT\n" +
        "        n.id,\n" +
        "        n.parent,\n" +
        "        coalesce(n.title, n.id) as title,\n" +
        "        n.description,\n" +
        "        n.drawable,\n" +
        "        n.origin,\n" +
        "        tree.level + 1 AS level,\n" +
        "        tree.sort_path || '>' || lower(n.id) AS sort_path\n" +
        "    FROM items AS n\n" +
        "    JOIN tree ON n.parent = tree.id\n" +
        ")\n" +
        "SELECT\n" +
        "    id,\n" +
        "    parent,\n" +
        "    title,\n" +
        "    description,\n" +
        "    origin,\n" +
        "    drawable,\n" +
        "    level\n" +
        "FROM tree\n" +
        "WHERE id in\n" +
        "\t (\n" +
        "\t WITH RECURSIVE ancestors AS (\n" +
        "\t\tSELECT\n" +
        "\t\t\tid,\n" +
        "\t\t\tparent\n" +
        "\t\tFROM items\n" +
        "\t\tWHERE id in (select item from shop_items where shop = :id)\n" +
        "\t\tUNION ALL\n" +
        "\t\tSELECT\n" +
        "\t\t\tn.id,\n" +
        "\t\t\tn.parent\n" +
        "\t\tFROM items AS n\n" +
        "\t\tJOIN ancestors AS a ON n.id = a.parent\n" +
        "\t)\n" +
        "\tSELECT id\n" +
        "    FROM ancestors\n" +
        ")\n" +
        "ORDER BY sort_path;"
    )
    fun tree(id: String): List<ItemWithLevel>
    @Query("UPDATE items SET drawable = :drawable WHERE pic = :pic")
    fun updateItem(drawable: Int, pic: String)
    @Query("UPDATE shops SET drawable = :drawable WHERE id = :id")
    fun updateShop(drawable: Int, id: String)
    @Query("SELECT * FROM items WHERE title IS NOT NULL ORDER BY title")
    fun items(): List<Item>
    @Query("SELECT * FROM items WHERE id IN (SELECT item from shop_items where shop = :id) ORDER BY title")
    fun items(id: String): List<Item>
    @Query("SELECT * FROM shops ORDER BY title")
    fun shops(): List<Shop>
    @Query("SELECT * FROM shops WHERE id IN (SELECT shop from shop_items where item = :id) ORDER BY title")
    fun shops(id: String): List<Shop>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    fun itemDetails(id: String): Item?
    @Query("SELECT * FROM shops WHERE id = :id LIMIT 1")
    fun shopDetails(id: String): Shop?
}


@Database(entities = [Item::class, Shop::class, ShopItems::class], version = 1, exportSchema = false)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: StoreDatabase? = null

        fun getDatabase(context: Context): StoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        StoreDatabase::class.java,
                        "store_database"
                    )
                    .createFromAsset("store_database")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


fun Item.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val lat = null
        override val lng = null
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
        override val level = this@toInterface.level
    }
}


fun ItemWithLevel.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
        override val level = this@toInterface.level
        override val lat = null
        override val lng = null
    }
}

fun Shop.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
        override val level = 0
        override val lat = this@toInterface.lat
        override val lng = this@toInterface.lat
    }
}

data class Details(
    val title: String = "",
    val origin: String? = null,
    val drawable: Int? = null,
    val level: Int = 0
)