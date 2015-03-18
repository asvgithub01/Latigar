package com.gigigo.asv.latigar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener, MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener {

    private Sensor acce;
    private Button button;
    private boolean isLatigazo = false;
    MediaPlayer player;

    ImageView imgAnimVisor;
    ImageView imgJeta;
    AnimationDrawable frameAnimation;
    int  prevY;
    boolean bBegin = false;
    int Veces = 0;

    Boolean SoyUnTerminalRetarded=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**/
        //el mismo fuck q desde el manifest
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imgJeta = (ImageView) findViewById(R.id.imgJeta);
        button = (Button) findViewById(R.id.BtnSave);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                /*create instance of File with name img.jpg*/
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "img.jpg");
				/*put uri as extra in intent object*/
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				/*start activity for result pass intent as argument and request code */
                startActivityForResult(intent, 1);

            }
        });

    }

    protected void onResume() {
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sm.getSensorList(SensorManager.SENSOR_ACCELEROMETER);

        if (sensorList.size() > 0)
            sm.registerListener(this, acce = sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
        else
        {
            SoyUnTerminalRetarded=true;
            acce =sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, acce, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    protected void onPause() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this, acce);

        super.onPause();
    }

    protected void onStop() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this, acce);

        super.onStop();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event != null) {

            int currY = (int) event.values[SensorManager.AXIS_Y];

            if(SoyUnTerminalRetarded)
                currY = (int)( event.values[1] * -9.81);

            if (!bBegin) {
                if (currY < -25) {
                    bBegin = true;
                    prevY = currY;

                }
            } else {
                if (currY >= prevY - 5 && !isLatigazo) {

                    prevY = currY;
                    //asv va con buen rumbo
                    if (currY > -15) {
                        try {
                            player = MediaPlayer.create(this, R.raw.ini);
                            if (!player.isPlaying()) {

                                player.setOnPreparedListener(this);//asv hacemos el star en el evento para evitar error -19 y -38
                                player.setOnCompletionListener(this);
                            }
                        } catch (Exception e) {
                        }
                        isLatigazo = true;
                        /*Animacion*/
                        imgAnimVisor = (ImageView) findViewById(R.id.imgAnim);
                        imgAnimVisor.setImageDrawable(null);
                        imgAnimVisor.setBackgroundResource(R.drawable.latigar_anim);
                        frameAnimation = (AnimationDrawable) imgAnimVisor.getBackground();
                        frameAnimation.start();
                    }
                } else {

                    if (currY <= prevY + 15) {
                        if (!isLatigazo)
                            bBegin = false;

                    }
                    if (isLatigazo) {
                        if (currY < -20) {
//                            try {
//                               // player.stop();
//                            } catch (Exception e) {
//                            }
                                player = MediaPlayer.create(this, R.raw.finidos);

                            player.setOnPreparedListener(this); //asv hacemos el star en el evento para evitar error -19 y -38
                            player.setOnCompletionListener(this);

                            bBegin = false;
                            isLatigazo = false;
                            //  imgAnimVisor.setBackgroundResource(R.drawable.ic_animocho);
                             /*Animacion*/
                            imgAnimVisor.setImageDrawable(null);
                            imgAnimVisor.setBackgroundResource(R.drawable.latigar_anim2);
                            frameAnimation = (AnimationDrawable) imgAnimVisor.getBackground();
                            frameAnimation.start();


                            if (Veces == 0)
                                imgAnimVisor.setImageResource(R.drawable.ic_animoluno);
                            else
                                imgAnimVisor.setImageResource(R.drawable.ic_animoldos);

                            if (Veces > 5)
                                imgAnimVisor.setImageResource(R.drawable.ic_animoltres);

                            Veces = Veces + 1;
                            //android:src="@drawable/ic_animoldos"
                        }
                    }

                }


            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if request code is same we pass as argument in startActivityForResult
        if (requestCode == 1) {
            //create instance of File with same name we created before to get image from storage
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "img.jpg");
            //Crop the captured image using an other intent
            try {
				/*the user's device may not support cropping*/
                cropCapturedImage(Uri.fromFile(file));
            } catch (ActivityNotFoundException aNFE) {
                //display an error message if user device doesn't support
                String errorMessage = "Sorry - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if (requestCode == 2) {
            //Create an instance of bundle and get the returned data
            Bundle extras = data.getExtras();
            //get the cropped bitmap from extras
            Bitmap thePic = extras.getParcelable("data");
            //set image bitmap to image view


            imgJeta.setImageBitmap(GetBitmapClippedCircle(thePic));
        }
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        // final int width =(int)(bitmap.getWidth()*0.75);
        final int width = (int) (bitmap.getWidth());
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }


    public void cropCapturedImage(Uri picUri) {
        //call the standard crop action intent
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri of image
        cropIntent.setDataAndType(picUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 75);
        cropIntent.putExtra("aspectY", 100);
        cropIntent.putExtra("scale", true);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 192);
        cropIntent.putExtra("outputY", 256);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, 2);
    }

    /**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }
}
