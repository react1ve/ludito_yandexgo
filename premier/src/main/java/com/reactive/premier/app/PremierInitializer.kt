package com.reactive.premier.app

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.markodevcic.peko.PermissionRequester
import com.reactive.premier.di.premierModulesList
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import timber.log.Timber

class PremierInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        app = this
        context?.let {
            initKoin(it)
            initFirebase(it)
            initPermissions(it)
            initLogger()
        }
        return true
    }

    fun initKoin(context: Context) {
        try {
            stopKoin()
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(context)
                modules(premierModulesList)
            }
        } catch (e: Exception) {
            e.localizedMessage
        }
    }


    fun initFirebase(context: Context) {
        FirebaseApp.initializeApp(context)
    }

    private fun initPermissions(context: Context) {
        PermissionRequester.initialize(context)
    }

    private fun initLogger() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        lateinit var app: PremierInitializer
            private set
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null;
    }

    override fun getType(uri: Uri): String? {
        return null;
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null;
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}