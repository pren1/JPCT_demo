package com.example.renpeng.jpct_demo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Date;

import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Z;

/**
 * Created by renpeng on 2017/11/25.
 * To use this class, your phone must support the TYPE_GAME_ROTATION_VECTOR sensor.
 * For instance, the google pixel support such sensor.
 */

public class Easy_Sensor implements SensorEventListener {
    Context My_Context; // Get MainActivity context
    private SensorManager mSensorManager; // Sensor manager
    private Sensor mAccel; // TYPE_ACCELEROMETER
    private Sensor my_game_vector; // TYPE_GAME_ROTATION_VECTOR
    float[] mGame_Rotation; // Record the result returned by the TYPE_GAME_ROTATION_VECTOR sensor
    float[] mGravity; // Record result returned by the TYPE_ACCELEROMETER sensor
    public int pitch_angle;
    public int roll_angle;
    private float[] mRotationMatrix = new float[9]; // accelerometer and magnetometer based rotation matrix
    float azimuth = 0; // the azimuth angle
    //ToDo: change the comments here.
    float vertical_pitch = 0; // The pitch angle used when the phone is verticle?

    float pitch = 0; // the pitch angle
    float roll = 0; // the roll angle
    float[] Re = new float[9];;
    /**
     * Initialize here please~
     */
    public Easy_Sensor(Context context){
        //get context here
        My_Context=context;
        //Sensor management
        mSensorManager =(SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        my_game_vector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            Log.i("MyCV","Unreliable sensor result");
            return;
        }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            //Log.i("MyCV","get gravity！");
            mGravity = event.values;
        }
        if(event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR)
        {
            //Log.i("MyCV","get vector！");
            mGame_Rotation = event.values;
        }
        if(mGravity!=null)
        {
            //Get pitch angle and roll angle
            //get_rotation_without_magnetic(mRotationMatrix,mGravity);
        }
        if(mGravity!=null&&mGame_Rotation!=null)
        {
            //first of all, get our azimuth angle!
            SensorManager.getRotationMatrixFromVector(Re,mGame_Rotation);
            float my_game_orientation[] = new float[3];
            float my_remaped_game_orientation[] = new float[3];
            float remapOut[] = new float[9];

            // To get the right azimuth angle, we need to remap the coordinate system first
            SensorManager.remapCoordinateSystem(Re, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remapOut);
            // Calculate rotation from the output
            // The azimuth and vertical_pitch angle will be used when the mobile phone is placed vertical
            SensorManager.getOrientation(remapOut,my_remaped_game_orientation);
            azimuth = my_remaped_game_orientation[0];
            vertical_pitch = my_remaped_game_orientation[2];

            // The pitch and roll angle will be used when the mobile phone is placed horizontally
            SensorManager.getOrientation(Re,my_game_orientation);
            pitch = my_game_orientation[1];
            roll = my_game_orientation[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Do nothing here~
    }
    public void register(){
        //register our sensors
        mSensorManager.registerListener(this,mAccel,100000);
        mSensorManager.registerListener(this,my_game_vector,200000);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    //unregister our sensors
    public void unregister(){
        mSensorManager.unregisterListener(this);
    }
    public float return_angle_azimuth()
    {
        float azimuth_angle = (float) (azimuth* 180 / Math.PI);
        if(azimuth_angle<0)
        {
            //change our angle from [-180,180] to [0,360]
            azimuth_angle += 360;
        }
        return azimuth_angle;
    }
    public float return_angle_vertical_pitch()
    {
        float pitch_angle = (float) (vertical_pitch* 180 / Math.PI);
        if(pitch_angle<0)
        {
            //change our angle from [-180,180] to [0,360]
            pitch_angle += 360;
        }
        return pitch_angle;
    }


    public float return_angle_pitch()
    {
        return pitch;
    }
    public float return_angle_roll()
    {
        return roll;
    }
    /**
     * This function is used to determine the position of our mobile phone
     * @return
     * 2: The phone is vertical
     * 1: The phone is placed horizontally
     * 0: Cannot decide the position of the phone
     */
    public int phone_is_vertical()
    {
        // Determine whether the phone is hold vertical or not.
        if(mGravity!=null)
        {
            if(mGravity[1]>7.6f&&mGravity[2]<7.2f)
            {
                return 2;
            }
            else if(mGravity[1]<7.2f&&mGravity[2]>7.6f)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        return 0;
    }
}
