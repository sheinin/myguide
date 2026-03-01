package android.myguide

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.jvm.java

@Entity(tableName = "items", primaryKeys = ["id","parent"])
data class Item(
    val parent: String,
    val id: String,
    val pic: String?,
    val title: String?,
    val origin: String?,
    val description: String?,
    val drawable: Int?,
)


@Entity(tableName = "shops", primaryKeys = ["id"])
data class Shop(
    val id: String,
    val title: String,
    val description: String,
    val origin: String,
    val lat: Double,
    val lng: Double,
    val drawable: Int?,
)

@Entity(tableName = "shop_items", primaryKeys = ["shop", "item"])
data class ShopItems(
    val shop: String,
    val item: String
)

interface ListInterface {
    val description: String?
    val drawable: Int?
    val id: String?
    val origin: String?
    val title: String?
}

class Repository(private val storeDao: StoreDao) {
    fun getItems(callback: (List<Item>) -> Unit) {
        Thread {
            callback(storeDao.items())
        }.start()
    }
    fun getItems(id: String, callback: (List<Item>) -> Unit) {
        Thread {
            callback(storeDao.items(id))
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
    @Query("UPDATE items SET drawable = :drawable WHERE pic = :pic")
    fun updateItem(drawable: Int, pic: String)
    @Query("UPDATE shops SET drawable = :drawable WHERE id = :id")
    fun updateShop(drawable: Int, id: String)
    @Query("SELECT * FROM items WHERE title IS NOT NULL ORDER BY title DESC")
    fun items(): List<Item>
    @Query("SELECT * FROM items WHERE id IN (SELECT item from shop_items where shop = :id) ORDER BY title DESC")
    fun items(id: String): List<Item>
    @Query("SELECT * FROM shops ORDER BY title DESC")
    fun shops(): List<Shop>
    @Query("SELECT * FROM shops WHERE id IN (SELECT shop from shop_items where item = :id) ORDER BY title DESC")
    fun shops(id: String): List<Shop>
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
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
    }
}


fun Shop.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
    }
}