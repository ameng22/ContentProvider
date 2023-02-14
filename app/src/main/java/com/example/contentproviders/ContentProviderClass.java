package com.example.contentproviders;

import static com.example.contentproviders.DatabaseHelper.TABLE_NAME;
import static com.example.contentproviders.DatabaseHelper.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class ContentProviderClass extends ContentProvider {
    public ContentProviderClass(){
    }

    static final String PROVIDER_NAME = "com.demo.user.provider";

    static final String URL = "content://"+PROVIDER_NAME+"/users";

    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String name = "name";
    static final int uriCode = 1;
    static UriMatcher uriMatcher;
    private static HashMap<String,String> values;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"users",uriCode);
        uriMatcher.addURI(PROVIDER_NAME,"users/*",uriCode);
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        if (db!=null){
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sq= new SQLiteQueryBuilder();
        sq.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)){
            case uriCode:
                sq.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: "+ uri);
        }
        if (sortOrder == null || sortOrder == ""){
            sortOrder = id;
        }
        Cursor c = sq.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/users";
            default:
                throw new IllegalArgumentException("Unsupported Uri : "+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
       long rowId = db.insert(TABLE_NAME,"",contentValues);
       if (rowId>0){
           Uri _uri = ContentUris.withAppendedId(CONTENT_URI,rowId);
           getContext().getContentResolver().notifyChange(_uri,null);
           return _uri;
       }
       throw new SQLiteException("Failed to insert values "+uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case uriCode:
                count = db.delete(TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknwon uri "+ uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case uriCode:
                count = db.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknwon uri "+ uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
