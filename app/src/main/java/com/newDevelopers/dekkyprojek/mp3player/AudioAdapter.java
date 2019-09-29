package com.newDevelopers.dekkyprojek.mp3player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jean.jcplayer.model.JcAudio;
import com.newDevelopers.dekkyprojek.R;

import java.io.File;
import java.util.List;

/**
 * Created by jean on 27/06/16.
 */

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioAdapterViewHolder> {
    private static final String TAG = AudioAdapter.class.getSimpleName();
    private static OnItemClickListener mListener;
    private List<JcAudio> jcAudioList;
    private SparseArray<Float> progressMap = new SparseArray<>();
    private AdapterCallback adapterCallback;

    public AudioAdapter(Context context, List<JcAudio> jcAudioList) {
        this.jcAudioList = jcAudioList;
        try{
            adapterCallback = ((AdapterCallback) context);
        }catch (ClassCastException e){
            return;
        }
        setHasStableIds(true);
    }

    // Define the method that allows the parent activity or fragment to define the jcPlayerManagerListener
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public AudioAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jc_player, parent, false);
        return new AudioAdapterViewHolder(view);
//        audiosViewHolder.itemView.setOnClickListener(this);
//        return audiosViewHolder;
    }

    @Override
    public void onBindViewHolder(final AudioAdapterViewHolder holder, final int position) {
        String title = position + 1 + "    " + jcAudioList.get(position).getTitle();
        holder.audioTitle.setText(title);
        holder.itemView.setTag(jcAudioList.get(position));

        String folder = Environment.getExternalStorageDirectory() + File.separator + "Soyluna/"+jcAudioList.get(position).getTitle()+".mp3";
        File directory = new File(folder);

        if (!directory.exists()) {
            holder.btnDown.setEnabled(true);
            DrawableCompat.setTint(
                    holder.btnDown.getDrawable(),
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.abuabu2)
            );
            holder.btnDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int permissionCheck = ContextCompat.checkSelfPermission(view.getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                        // permission is not granted yet, let's ask for it
                        ActivityCompat.requestPermissions((Activity) view.getContext(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                200);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("Download");
                        builder.setMessage("Are you sure want to download "+jcAudioList.get(position).getTitle()+" ?");
                        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialogInterface, int id) {
                                try{
                                    adapterCallback.downloadSong(position);
                                }catch (ClassCastException e){
                                }
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                }
            });
        }else{
            DrawableCompat.setTint(
                    holder.btnDown.getDrawable(),
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.hijau)
            );
            holder.btnDown.setEnabled(false);

        }

        applyProgressPercentage(holder, progressMap.get(position, 0.0f));
    }
    public static interface AdapterCallback {
        void downloadSong(int position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Applying percentage to progress.
     *
     * @param holder     ViewHolder
     * @param percentage in float value. where 1 is equals as 100%
     */
    private void applyProgressPercentage(AudioAdapterViewHolder holder, float percentage) {
        Log.d(TAG, "applyProgressPercentage() with percentage = " + percentage);
        LinearLayout.LayoutParams progress = (LinearLayout.LayoutParams) holder.viewProgress.getLayoutParams();
        LinearLayout.LayoutParams antiProgress = (LinearLayout.LayoutParams) holder.viewAntiProgress.getLayoutParams();

        progress.weight = percentage;
        holder.viewProgress.setLayoutParams(progress);

        antiProgress.weight = 1.0f - percentage;
        holder.viewAntiProgress.setLayoutParams(antiProgress);
    }

    @Override
    public int getItemCount() {
        return jcAudioList == null ? 0 : jcAudioList.size();
    }

    public void updateProgress(JcAudio jcAudio, float progress) {
        int position = jcAudioList.indexOf(jcAudio);
        Log.d(TAG, "Progress = " + progress);


        progressMap.put(position, progress);
        if (progressMap.size() > 1) {
            for (int i = 0; i < progressMap.size(); i++) {
                if (progressMap.keyAt(i) != position) {
                    Log.d(TAG, "KeyAt(" + i + ") = " + progressMap.keyAt(i));
                    notifyItemChanged(progressMap.keyAt(i));
                    progressMap.delete(progressMap.keyAt(i));
                }
            }
        }
        notifyItemChanged(position);
    }

    // Define the mListener interface
    public interface OnItemClickListener {
        void onItemClick(int position);

        void onSongItemDeleteClicked(int position);
    }

    static class AudioAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView audioTitle;
        private ImageView btnDelete;
        private ImageView btnDown;
        private View viewProgress;
        private View viewAntiProgress;
        private String downloadAudioPath;

        public AudioAdapterViewHolder(View view) {
            super(view);
            this.audioTitle = view.findViewById(R.id.audio_titlejc);
            this.btnDelete = view.findViewById(R.id.btn_delete);
            this.btnDown = view.findViewById(R.id.btn_down);
            viewProgress = view.findViewById(R.id.song_progress_view);
            viewAntiProgress = view.findViewById(R.id.song_anti_progress_view);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSongItemDeleteClicked(getAdapterPosition());
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (mListener != null) mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
