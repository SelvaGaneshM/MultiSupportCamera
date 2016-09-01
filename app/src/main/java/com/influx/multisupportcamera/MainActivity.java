package com.influx.multisupportcamera;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.tutoshowcase.TutoShowcase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.ecotastic.android.camerautil.lib.CameraIntentHelper;
import de.ecotastic.android.camerautil.lib.CameraIntentHelperCallback;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    CameraIntentHelper mCameraIntentHelper;
    TextView messageView;
    private Uri mImageCaptureUri;
    private File outPutFile = null;
    private static final int CROPING_CODE = 301;
    ImageView imageView;

    private static final String TAG = "Touch";
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    private float[] lastEvent;
    float d, newRot;
    ImageView snapshort_screen, fixed_dress, btnNext_right, btnNext_left,
            cart_wishlist_screen_img_logo, backpress, startCameraButton,
            screenshort;

    final int PIC_CROP = 3;
    private Uri picUri;
    final int PICK_IMAGE_REQUEST = 2;
    static final int CAMERA_CAPTURE = 1;

    Bitmap mbitmap;

    int[] myImageList = new int[]{R.drawable.test1, R.drawable.test2, R.drawable.test3, R.drawable.test4, R.drawable.test5, R.drawable.test6,
            R.drawable.test7, R.drawable.test8, R.drawable.test9, R.drawable.test10};

    int postion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        displayTuto();

        outPutFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.png");

        messageView = (TextView) findViewById(R.id.activity_camera_intent_message);
        imageView = (ImageView) findViewById(R.id.activity_camera_intent_image_view);
        fixed_dress = (ImageView) findViewById(R.id.fixed_dress);
        btnNext_right = (ImageView) findViewById(R.id.btnNext_right);
        btnNext_left = (ImageView) findViewById(R.id.btnNext_left);
        cart_wishlist_screen_img_logo = (ImageView) findViewById(R.id.cart_wishlist_screen_img_logo);
        backpress = (ImageView) findViewById(R.id.backpress);
        startCameraButton = (ImageView) findViewById(R.id.camera_cap);
        screenshort = (ImageView) findViewById(R.id.screenshort);


        snapshort_screen = (ImageView) findViewById(R.id.snapshort_screen);
        snapshort_screen.setVisibility(View.GONE);
        snapshort_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takeScreenshot();
                Toast.makeText(MainActivity.this, "Not yet Ready...!", Toast.LENGTH_SHORT).show();

                screenShot(v);
            }
        });

        startCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        backpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cart_wishlist_screen_img_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        postion = 0;
        if (postion == 0) {
            fixed_dress.setImageResource(myImageList[postion]);
        } else {
            fixed_dress.setImageResource(R.drawable.sample_dress);
        }

        btnNext_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postion <= 9) {
                    btnNext_left.setVisibility(View.VISIBLE);
                    fixed_dress.setImageResource(myImageList[postion]);
                    postion = postion + 1;
                } else {
                    btnNext_right.setVisibility(View.GONE);
                }
            }
        });

        btnNext_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnNext_right.setVisibility(View.VISIBLE);
                if (postion <= 10) {
                    if (postion != 0) {
                        postion = postion - 1;
                        fixed_dress.setImageResource(myImageList[postion]);
                    } else {
                        btnNext_left.setVisibility(View.GONE);
                    }
                }
            }
        });

        imageView.setOnTouchListener(MainActivity.this);
        setupCameraIntentHelper();
    }

    private void displayTuto() {
        TutoShowcase.from(this)
                .setContentView(R.layout.tuto_sample)

                .on(R.id.camera_cap)
                .addCircle()
                .withBorder()
                .onClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    public void screenShot(View view) {
        mbitmap = getBitmapOFRootView(snapshort_screen);
        screenshort.setImageBitmap(mbitmap);
        createImage(mbitmap);

        screenshort.buildDrawingCache();
        Bitmap bmap = screenshort.getDrawingCache();
        SaveImage(bmap);
    }

    public Bitmap getBitmapOFRootView(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = rootview.getDrawingCache();
        return bitmap1;
    }

    public void createImage(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 80, bytes);
        File file = new File(Environment.getExternalStorageDirectory() +
                "/capturedscreenandroid.png");
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/palamsilks");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "palamsilks-" + n + ".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
            Toast.makeText(MainActivity.this, "Image Saved...!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (mCameraIntentHelper != null) {
                        mCameraIntentHelper.startCameraIntent();
                    } else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.png";
                        File imageFile = new File(imageFilePath);
                        picUri = Uri.fromFile(imageFile); // convert path to Uri
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                        startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    Toast.makeText(MainActivity.this, "Not yet Ready...!", Toast.LENGTH_SHORT).show();
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // Start the Intent
                    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void setupCameraIntentHelper() {
        mCameraIntentHelper = new CameraIntentHelper(this, new CameraIntentHelperCallback() {
            @Override
            public void onPhotoUriFound(Date dateCameraIntentStarted, Uri photoUri, int rotateXDegrees) {
                messageView.setText(getString(R.string.activity_camera_intent_photo_uri_found) + photoUri.toString());
                mImageCaptureUri = photoUri;
                Bitmap photo = BitmapHelper.readBitmap(MainActivity.this, photoUri);
                if (photo != null) {
                    photo = BitmapHelper.shrinkBitmap(photo, 300, rotateXDegrees);
                    imageView.setImageBitmap(photo);
                    CropingIMG();
                }
            }

            @Override
            public void deletePhotoWithUri(Uri photoUri) {
                BitmapHelper.deleteImageWithUriIfExists(photoUri, MainActivity.this);
            }

            @Override
            public void onSdCardNotMounted() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_sd_card_not_mounted), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(getApplicationContext(), getString(R.string.warning_camera_intent_canceled), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCouldNotTakePhoto() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_take_photo), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPhotoUriNotFound() {
                messageView.setText(getString(R.string.activity_camera_intent_photo_uri_not_found));
            }

            @Override
            public void logException(Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_sth_went_wrong), Toast.LENGTH_LONG).show();
                Log.d(getClass().getName(), e.getMessage());
            }
        });
    }

    private void CropingIMG() {

        final ArrayList<CropingOption> cropOptions = new ArrayList<CropingOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Cann't find image croping app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.setData(mImageCaptureUri);
            intent.putExtra("outputX", 512);
            intent.putExtra("outputY", 512);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            //TODO: don't use return-data tag because it's not return large image data and crash not given any message
            //intent.putExtra("return-data", true);

            //Create output file here
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = (ResolveInfo) list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROPING_CODE);
            } else {
                for (ResolveInfo res : list) {
                    final CropingOption co = new CropingOption();

                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropingOptionAdapter adapter = new CropingOptionAdapter(getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Croping App");
                builder.setCancelable(false);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROPING_CODE);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (mImageCaptureUri != null) {
                            getContentResolver().delete(mImageCaptureUri, null, null);
                            mImageCaptureUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mCameraIntentHelper.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraIntentHelper.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mCameraIntentHelper.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CROPING_CODE) {
            try {
                if (outPutFile.exists()) {
                    snapshort_screen.setVisibility(View.VISIBLE);
                    Bitmap photo = decodeFile(outPutFile);
                    imageView.setImageBitmap(photo);
                } else {
                    Toast.makeText(getApplicationContext(), "Error while save image", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            picUri = intent.getData();
            Log.d("uriGallery", picUri.toString());
            performCrop();
        } else if (requestCode == PIC_CROP) {
            Bundle extras = intent.getExtras();
            Bitmap thePic = (Bitmap) extras.get("data");
            snapshort_screen.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(thePic);
        } else if (requestCode == CAMERA_CAPTURE) {
            //get the Uri for the captured image
            Uri uri = picUri;
            //carry out the crop operation
            performCrop();
            Log.d("picUri", uri.toString());

        }
    }

    private void performCrop() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PIC_CROP);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        // Dump touch event to log
        dumpEvent(event);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;

            case MotionEvent.ACTION_UP: //first finger lifted
            case MotionEvent.ACTION_POINTER_UP: //second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;


            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM && event.getPointerCount() == 2) {
                    float newDist = spacing(event);
                    matrix.set(savedMatrix);
                    if (newDist > 10f) {
                        scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        matrix.postRotate(r, view.getMeasuredWidth() / 2,
                                view.getMeasuredHeight() / 2);
                    }
                }
                break;

        }
        // Perform the transformation
        view.setImageMatrix(matrix);

        return true; // indicate event was handled

    }

    private void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);

        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);

    }
}