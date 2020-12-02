package com.example.project;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Edit extends AppCompatActivity {

    private long start;
    private long end;
    private Uri selectedVideo;
    private VideoView videoView;
    StorageReference storageRefernce;


    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    private String filePath;
    private int choice;
    private int size;
    private TextView minR;
    private TextView label;
    private int REQUEST_STICKER = 1;
    private int REQUEST_VIDEO = 100;
    float x = 0;
    float y = 0;
    private int width;
    private int height;
    private TextView maxR;
    private TextView saveUpload;
    private Button saveText;
    private TextView upload;
    private TextView trim;
    private TextView text;

    private Uri selectStickerUri;
    private TextView sticker;
    private ProgressDialog progressDialog;
    private EditText editText;
    private ImageView stickerimageView;
    private TextView stickerupload;
    private Button saveSticker;


    private int videoLen;

    private String path;
    private MediaController mediaController;
    private CrystalRangeSeekbar rangeSeekbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        videoView = findViewById(R.id.editVideo);
        mediaController = new MediaController(this);
        rangeSeekbar = findViewById(R.id.range);
        rangeSeekbar.setMaxValue(100);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        saveSticker = findViewById(R.id.saveSticker);
        stickerupload = findViewById(R.id.uploadSticker);
        stickerimageView = findViewById(R.id.imlabel);
        storageRefernce = FirebaseStorage.getInstance().getReference("Video");
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        rangeSeekbar.setMinValue(0);
        rangeSeekbar.setEnabled(false);
        minR = findViewById(R.id.minR);
        maxR = findViewById(R.id.maxR);
        upload = findViewById(R.id.uploadv);
        trim = findViewById(R.id.trim);
        text = findViewById(R.id.text);
        sticker = findViewById(R.id.sticker);
      //  saveUpload = findViewById(R.id.save);
        label = findViewById(R.id.label);
        saveText = findViewById(R.id.saveT);


        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                long minSec = Long.parseLong(String.valueOf(minValue)) / 1000;
                long maxSec = Long.parseLong(String.valueOf(maxValue)) / 1000;
                start = (Long) minValue;
                end = (Long) maxValue;
                long minMin = minSec / 60;
                long minS = minSec % 60;
                long maxMin = maxSec / 60;
                long maxS = maxSec % 60;
                minR.setText(minMin + ":" + minS);
                maxR.setText(maxMin + ":" + maxS);
                if (selectedVideo != null) {

                    videoView.seekTo(Integer.parseInt(String.valueOf(minValue)));
                }
            }
        });
        label.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float newx = event.getX();
                        float newy = event.getY();
                        float disx = newx - x;
                        float disy = newy - y;
                        float MAXY = videoView.getY() + videoView.getHeight() / 1.05F;
                        label.setX(label.getX() + disx);
                        Log.e("Label x", "Get X"+label.getX() + " " + "Get Left "+label.getLeft());
                        Log.e("Label y", "Get Y "+label.getY() + " Get Top" + label.getTop());

                        //  Toast.makeText(Edit.this,MAXY+" "+(label.getX()+disx),Toast.LENGTH_SHORT).show();
                        label.setY(Math.max(Math.min(label.getY() + disy, MAXY),0));
                }
                return true;
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                saveText.setVisibility(View.VISIBLE);
                stickerimageView.setVisibility(View.INVISIBLE);
                stickerupload.setVisibility(View.INVISIBLE);
                saveSticker.setVisibility(View.INVISIBLE);


            }
        });
        stickerimageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float newx = event.getX();
                        float newy = event.getY();
                        float disx = newx - x;
                        float disy = newy - y;
                        float MAXY = videoView.getY() + videoView.getHeight() / 1.05F;
                        stickerimageView.setX(stickerimageView.getX() + disx);
                        Log.e("Sticker x", stickerimageView.getX() + " " + stickerimageView.getTop());
                        Log.e("Sticker y", stickerimageView.getY() + " " + stickerimageView.getLeft());

                        //  Toast.makeText(Edit.this,MAXY+" "+(label.getX()+disx),Toast.LENGTH_SHORT).show();
                        stickerimageView.setY(Math.min(stickerimageView.getY() + disy, MAXY));
                }
                return true;
            }
        });
        editText = findViewById(R.id.message);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                label.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = videoView.getDuration();
                int sec = duration / 1000;
                int min = sec / 60;
                int secs = sec % 60;
                width = mp.getVideoWidth();
                height = mp.getVideoHeight();
                Log.e("Video", mp.getVideoHeight() + " " + mp.getVideoWidth() + " " + videoView.getHeight() + " " + videoView.getWidth());
                start = 0;
                rangeSeekbar.setEnabled(true);
                rangeSeekbar.setMinValue(0);
                rangeSeekbar.setMaxValue(duration);
                end = duration;
                maxR.setText(min + ":" + secs);
                rangeSeekbar.setMinValue(0);
                rangeSeekbar.setMaxValue(duration);
                Log.e("TAG", "onPrepared: " + duration + " format " + min + ":" + secs);

            }
        });
        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedVideo==null)
                    return;
                File moviesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES
                );
                String filePrefix = "text";
                String fileExtn = ".mp4";
                File dest = new File(moviesDir, filePrefix + fileExtn);
                filePath = dest.getAbsolutePath();
                int fileNo = 0;
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                }
                filePath = dest.getAbsolutePath();
                String message = String.valueOf(label.getText());
                float x = label.getX();
                float y = label.getY();
                Log.e("X & Y ",x+" "+y);
                Log.e("Videview ht and wd ",videoView.getHeight()+" "+videoView.getWidth());
                y /= videoView.getHeight();
                x /= videoView.getWidth();
                Log.e("X & Y aftter div ",x+" "+y);

                x *= width;
                y *= height;
                Log.e("Final X & Y after mul ",x+" "+y);
                String[] cmd = new String[]{"-i", getPath(Edit.this, selectedVideo), "-vf", "drawtext=fontfile=/system/fonts/DroidSans.ttf:text='" + message + "':fontsize=40:fontcolor=black:x=" + x + ":y=" + y + ":enable='between(t," + start / 1000 + "," + end / 1000 + ")'", "-acodec", "copy", "-y", filePath};
                String[] c = {"-i", getPath(Edit.this, selectedVideo), "-vf", "drawtext=fontfile=/system/fonts/DroidSans.ttf:text=sdassssss:fontsize=70:fontcolor=black:enable='between(t,5,10)':x=" + label.getX() + ":y=" + label.getY(), "-acodec", "copy", "-y", filePath};
                String[] complexCommand = {"-ss", "" + start / 1000, "-y", "-i", getPath(Edit.this, selectedVideo), "-t", "" + (end - start) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
                execFFmpegBinary(cmd);
            }
        });
        sticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.INVISIBLE);
                saveText.setVisibility(View.INVISIBLE);
                //stickerimageView.setVisibility(View.VISIBLE);
                stickerupload.setVisibility(View.VISIBLE);
                saveSticker.setVisibility(View.VISIBLE);

            }
        });
        saveSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedVideo==null){
                    return;
                }
                File moviesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES
                );
                String filePrefix = "sticker_video";
                String fileExtn = ".mp4";
                File dest = new File(moviesDir, filePrefix + fileExtn);
                int fileNo=0;
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                }
                filePath=dest.getAbsolutePath();
                String[] cmd = {"-i", getPath(Edit.this,selectedVideo) ,"-i", getPath(Edit.this,selectStickerUri),"-filter_complex","[1]scale=75:75[b];[0][b] overlay=100:25:enable='between(t,0,20)':format=rgb", "-c:a" ,"copy", filePath};
                //String[] complexCommand = {"-ss", "" + start / 1000, "-y", "-i", getPath(MainActivity.this,selectedVideoUri), "-t", "" + (end - start) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
                execFFmpegBinary(cmd);
            }
        });
        stickerupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedVideo == null) {
                    return;
                }
                File moviesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES
                );
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                        REQUEST_STICKER);
            }
        });
        trim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedVideo == null)
                    return;
                saveText.setVisibility(View.INVISIBLE);

                File moviesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES
                );
                choice = 1;
                String filePrefix = "cut_video";
                String fileExtn = ".mp4";
                String yourRealPath = getPath(Edit.this, selectedVideo);
                File dest = new File(moviesDir, filePrefix + fileExtn);
                int fileNo = 0;
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                }

                filePath = dest.getAbsolutePath();
                //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
                String[] complexCommand = {"-ss", "" + start / 1000, "-y", "-i", yourRealPath, "-t", "" + (end - start) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};

                execFFmpegBinary(complexCommand);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23)
                    getPermission();
                else
                    uploadVideo();
            }
        });
    }


    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(Edit.this,
                    params,
                    100);
        } else
            uploadVideo();
    }

    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_VIDEO);

        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(selectedVideo!=null&&videoView!=null){
            videoView.setVideoURI(selectedVideo);
            videoView.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Log.e("Tag", String.valueOf(requestCode));
            if (requestCode == REQUEST_VIDEO) {
                selectedVideo = data.getData();
                path = getPath(Edit.this, selectedVideo);
                Log.e("Tag", path.toString());
                videoView.setVideoURI(selectedVideo);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                rangeSeekbar.setRight(videoView.getDuration());
                rangeSeekbar.setLeft(0);
                videoView.start();


            } else {
                if (requestCode == REQUEST_STICKER) {
                    selectStickerUri = data.getData();
                    stickerimageView.setVisibility(View.VISIBLE);
                    stickerimageView.setImageURI(null);
                    stickerimageView.setImageURI(selectStickerUri);
                }
            }
        }

    }

    private String getExt(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void execFFmpegBinary(final String[] command) {

        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.e(Config.TAG, message.getText());
            }
        });
        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {
                Log.e(Config.TAG, String.format("frame: %d, time: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
                Log.d("TAG", "Started command : ffmpeg " + Arrays.toString(command));
                if (choice == 1)
                    progressDialog.setMessage("progress : splitting video " + newStatistics.toString());
                else if (choice == 2)
                    progressDialog.setMessage("progress : Editing Text video " + newStatistics.toString());
                else if (choice == 2)
                    progressDialog.setMessage("progress :Sticker Edit video " + newStatistics.toString());
                else

                    Log.d("TAG", "progress : " + newStatistics.toString());
            }
        });
        Log.d("TAG", "Started command : ffmpeg " + Arrays.toString(command));
        progressDialog.setMessage("Processing...");
        progressDialog.show();

        long executionId = com.arthenica.mobileffmpeg.FFmpeg.execute(command);
        if (executionId == Config.RETURN_CODE_SUCCESS) {
            Log.d("TAG", "Finished command : ffmpeg " + Arrays.toString(command));


            final StorageReference reference = storageRefernce.child(System.currentTimeMillis() + "." + getExt(selectedVideo));
            Log.e("uri", filePath);

            UploadTask uploadTask = reference.putFile(Uri.fromFile(new File(filePath)));
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Toast.makeText(Edit.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Log.e("Here", "her");
                    Uri uri = task.getResult();
                    String note = firebaseAuth.getCurrentUser().getEmail().toString() + "#" + System.currentTimeMillis();
                    Toast.makeText(Edit.this, note, Toast.LENGTH_SHORT).show();
                    DocumentReference documentReference = FirebaseFirestore.getInstance().document("Videos/" + note);
                    Map<String, Object> list = new HashMap<String, Object>();
                    list.put("Username", note);
                    list.put("video", uri.toString());
                    documentReference.set(list);

                    Log.e("Tag", uri.toString());
                    Toast.makeText(Edit.this, "Data upload", Toast.LENGTH_SHORT).show();

                }
            });
            progressDialog.dismiss();


        } else if (executionId == Config.RETURN_CODE_CANCEL) {
            Log.e("TAG", "Async command execution cancelled by user.");
            if (progressDialog != null)
                progressDialog.dismiss();
        } else {
            Log.e("TAG", String.format("Async command execution failed with returnCode=%d.", executionId));
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.feed:
                Intent intent = new Intent(com.example.project.Edit.this, com.example.project.List.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.signOutFeed:
                Intent intent1 = new Intent(com.example.project.Edit.this, com.example.project.MainActivity.class);
                FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                finish();
                startActivity(intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}




