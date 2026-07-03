package com.jvmapp.panaderialautaro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Cliente::class,
        TipoPrecio::class,
        Venta::class,
        Produccion::class,
        Pago::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun tipoPrecioDao(): TipoPrecioDao
    abstract fun ventaDao(): VentaDao
    abstract fun produccionDao(): ProduccionDao
    abstract fun pagoDao(): PagoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "panaderia_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.tipoPrecioDao()?.let { dao ->
                                    dao.insertar(TipoPrecio(nombre = "Normal",      precioVarilla = 2350.0, precioBollo = 2350.0, precioCriollo = 4400.0))
                                    dao.insertar(TipoPrecio(nombre = "Mismo pueblo",precioVarilla = 2000.0, precioBollo = 2000.0, precioCriollo = 3800.0))
                                    dao.insertar(TipoPrecio(nombre = "Especial",    precioVarilla = 1800.0, precioBollo = 1800.0, precioCriollo = 3500.0))
                                    dao.insertar(TipoPrecio(nombre = "Jefa",        precioVarilla = 2350.0, precioBollo = 2350.0, precioCriollo = 4400.0))
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}