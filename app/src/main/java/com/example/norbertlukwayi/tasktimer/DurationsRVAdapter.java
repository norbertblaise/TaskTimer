package com.example.norbertlukwayi.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class DurationsRVAdapter extends RecyclerView.Adapter<DurationsRVAdapter.ViewHolder> {

    private Cursor mCursor;

    private final java.text.DateFormat mDateFormat; //module level  so we don't keep instatiating in bindView.

    public DurationsRVAdapter(Context context, Cursor cursor) {
        this.mCursor = cursor;
        mDateFormat = DateFormat.getDateFormat(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_durations_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if ((mCursor == null) || (mCursor.getCount() == 0)) {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldnt move cursor to position " + position);
            }
            String name = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_NAME));
            String description = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DESCRIPTION));
            Long startTime = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_START_TIME));
            long totalDuration = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DURATION));

            holder.name.setText(name);
            if (holder.description != null) { // description is not present in portrait
                holder.description.setText(description);
            }

            String userDate = mDateFormat.format(startTime * 1000); //the databse stores seconds, we need milliseconds
            String totalTime = formatDuration(totalDuration);

            holder.startDate.setText(userDate);
            holder.duration.setText(totalTime);
        }
    }

//    @Override
//    public int getItemCount() {
//
//        return mCursor != null ? mCursor.getCount() : 0;
//    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }


    private String formatDuration(long duration) {
        //duration is in seconds, convert to hours:mins:secs
        //allowing for greater than 24 hours so we can't use a time data type
        long hours = duration / 3600;
        long remainder = duration - (hours * 3600);
        long minutes = remainder / 60;
        long seconds = remainder - (minutes * 60);

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is <em>not</em> closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is also returned.
     */
    Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        int numItems = getItemCount(); //store olditem count
        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            //notify the observers about the new Cursor
            notifyDataSetChanged();
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems); // use the old count
        }
        return oldCursor;

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        TextView startDate;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.td_name);
            this.description = itemView.findViewById(R.id.td_description);
            this.startDate = itemView.findViewById(R.id.td_start);
            this.duration = itemView.findViewById(R.id.td_duration);
        }
    }
}
