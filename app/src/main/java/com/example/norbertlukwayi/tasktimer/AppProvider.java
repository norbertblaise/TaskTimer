package com.example.norbertlukwayi.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Provider for the TaskTimer app. This is the only app that knows about {@link AppDatabase}
 */
public class AppProvider  extends ContentProvider {
    private static final String TAG = "AppProvider";

    private AppDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final String CONTENT_AUTHORITY = "com.example.norbertlukwayi.tasktimer.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int TASKS = 100;
    private static final int TASKS_ID = 101;

    private static final int TIMINGS = 200;
    private static final int TIMINGS_ID = 201;

    /*
    private static final int TASK_TIMINGS = 300;
    private static final int TASK_TIMINGS_ID = 301;
     */

    private static final int TASK_DURATIONS = 400;
    private static final int TASK_DURATIONS_ID = 401;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //eg. content://com.example.norbertlukwayi.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
        //eg. content://com.examle.norbertlukwayi.tasktimer.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME +"/#", TASKS_ID);

        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS);
        matcher.addURI(CONTENT_AUTHORITY,TimingsContract.TABLE_NAME + "/#", TIMINGS_ID);

        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS);
        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME + "/#", TASK_DURATIONS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,  String[] projection,  String selection,  String[] selectionArgs,  String sortOrder) {
        Log.d(TAG, "query: called with URi " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query: match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match){
            case TASKS:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;
            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
                break;
            case TIMINGS:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                break;
            case TIMINGS_ID:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                long timingId = TimingsContract.getTimingId(uri);
                queryBuilder.appendWhere(TimingsContract.Columns._ID + " = " + timingId);
                break;

            case TASK_DURATIONS:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                break;
            case TASK_DURATIONS_ID:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                long durationId = DurationsContract.getDurationId(uri);
                queryBuilder.appendWhere(DurationsContract.Columns._ID + " = " + durationId);
                break;

                default:
                    throw new IllegalArgumentException("unknown URI: " + uri);

        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//        return queryBuilder.query(db, projection, selection, selectionArgs,null, null, sortOrder);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs,null, null, sortOrder);
        Log.d(TAG, "query: rows in returned cursor = " + cursor.getCount()); //TODO remove this line

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }


    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TASKS:
                return  TasksContract.CONTENT_TYPE;
            case TASKS_ID:
                return  TasksContract.CONTENT_ITEM_TYPE;

                case TIMINGS:
                return TimingsContract.CONTENT_TYPE;
            case TIMINGS_ID:
                 return TimingsContract.CONTENT_ITEM_TYPE;

            case TASK_DURATIONS:
                return DurationsContract.CONTENT_TYPE;

            case TASK_DURATIONS_ID:
                return DurationsContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("unknown URI: " + uri);
        }
    }


    @Override
    public Uri insert( @NonNull Uri uri,  ContentValues values) {
        Log.d(TAG, "Entering insert, called with uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "insert: " + match);

        final SQLiteDatabase db;

        Uri returnUri;
        long recordId;

        switch (match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME, null, values);
                if (recordId>=0){
                    returnUri = TasksContract.buildTaskUri(recordId);
                }else{
                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
                }
                break;
            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values);
                if (recordId>=0){
                    returnUri = TimingsContract.buildTimingUri(recordId);
                }else{
                    throw  new android.database.SQLException("Failed to insert into " + uri.toString());
                }
                break;
            default:
                    throw  new IllegalArgumentException("Unkown uri " + uri);
        }

        if (recordId >= 0){
            //something was inserted
            Log.d(TAG, "insert: setting notifychange with " + uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }else{
            Log.d(TAG, "insert: nothing inserted");
        }

        Log.d(TAG, "Exiting insert returning: " + returnUri);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection,  String[] selectionArgs) {
        Log.d(TAG, "update called with uri: " + uri);
        final  int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is " + match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch (match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME, selection,selectionArgs);
                break;
            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;
                if (selection != null && selection.length() >0){
                    selectionCriteria +=  " AND (" + selection + ")";
                }
                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TimingsContract.TABLE_NAME, selection,selectionArgs);
                break;
            case TIMINGS_ID:
                db = mOpenHelper.getWritableDatabase();
                long timingsId = TimingsContract.getTimingId(uri);
                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
                if (selection != null && selection.length() >0){
                    selectionCriteria +=  " AND (" + selection + ")";
                }
                count = db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;

            default:
                throw  new IllegalArgumentException("unknown uri: " + uri);
        }
        if (count >0){
            //something was deleted
            Log.d(TAG, "delete: setting notifyChange with " + uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri,null);
        }else{
            Log.d(TAG, "delete: nothing deleted");
        }

        Log.d(TAG, "exiting update returning: " + count);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update called with uri: " + uri);
        final  int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is " + match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch (match){
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TasksContract.TABLE_NAME, values, selection,selectionArgs);
                break;
            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;
                if (selection != null && selection.length() >0){
                    selectionCriteria +=  " AND (" + selection + ")";
                }
                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TimingsContract.TABLE_NAME, values, selection,selectionArgs);
                break;
            case TIMINGS_ID:
                db = mOpenHelper.getWritableDatabase();
                long timingsId = TimingsContract.getTimingId(uri);
                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
                if (selection != null && selection.length() >0){
                    selectionCriteria +=  " AND (" + selection + ")";
                }
                count = db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;

                default:
                    throw  new IllegalArgumentException("unknown uri: " + uri);
        }
        if (count >0){
            //something was updated
            Log.d(TAG, "update: setting notifyChange with " + uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri,null);
        }else{
            Log.d(TAG, "update: nothing deleted");
        }

        Log.d(TAG, "exiting update returning: " + count);
        return count;
    }
}
