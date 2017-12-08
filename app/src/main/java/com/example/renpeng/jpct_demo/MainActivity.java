package com.example.renpeng.jpct_demo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import static android.R.attr.bitmap;


public class MainActivity extends Activity {
    private static final int PICKFILE_REQUEST_CODE=66666;
    private static final int PICKTEXTURE_REQUEST_CODE=99999;
    // Used to handle pause and resume...
    private static MainActivity master = null;

    private GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null; // Frame buffer
    private World world = null; // World instance in JPCT
    public int color_background=210;
    private RGBColor back = new RGBColor(color_background, color_background, color_background); // The background color
    private Light sun = null; // Light source
    public int light_intensity=250;
    public Object3D my_load_ser = null; // Load .obj or .ser
    private Camera cam; // Camera instance

    private Easy_Sensor mEasy_To_Use_Sensor = null; // My class that uses the sensor
    // Flags that are used together with sensor to control the rotation of our model
    public float pre_roll_value;
    public float pre_pitch_value;
    public boolean first_time_use_sensor=true;
    public boolean first_time_use_sensor_pitch=true;
    public boolean first_time_verticle=true;
    public boolean first_time_verticle_pitch=true;
    public float begin_angle_value=0;
    public float pre_azimuth_value=0;
    public float begin_angle_value_vertical=0;
    public float pre_pitch_value_vertical=0;

    SimpleVector sv; // Position of our light source
    float currX, currY; // Variables that are used to record the touch position
    float finalWidth, finalHeight; // The width and height of the view / framebuffer
    // Menu Setting
    public static final int MENU_Choose_Obj = Menu.FIRST; // Choose a model
    public static final int MENU_Choose_TextureMap = Menu.FIRST + 1; // Choose a texture map
    public static final int MENU_Setting = Menu.FIRST + 2; // Set the intensity and color of light
    public static final int MENU_Light_Setting = Menu.FIRST + 3; // Set the intensity and color of light
    public static final int MENU_Back_Color_Setting = Menu.FIRST + 4; // Set the intensity and color of light
    public static final int MENU_TRY_OBJ = Menu.FIRST + 5;
    public static final int MENU_Create_Gif = Menu.FIRST + 6; // Create an gif
    // If this is set to true, we load .obj. It is rather slow.
    // Otherwise, we load .ser, which is rather fast.
    public boolean Use_Obj_Slow=false;
    public String My_Pick_Path=null; // Save the path to the model we picked
    public String My_Texture_Pick_Path=null; // Save the path to the texture map we picked
    public  int[] tmpPixels=null; // record the pixels from the framebuffer

    public boolean first_ondraw=true;

    public int Frame_Number=0; // Record the numbers of images that are used to create gif. Just for debug.
    public boolean Do_Gif_Caption=false; // It will be set to true when the user enter the gif generation mode
    public boolean Finish_Gif_Generation=false; // It will be set to true when the gif generation mode is finished
    public boolean Normal_Zoom=true; // It will be set to false when we enter the gif generation mode
    public boolean Change_Light_Intensity=false; // It will be set to true when we use the volume button to change the intensity of light source
    public boolean Change_Background_Brightness=false; // It will be set to true when we use the volume button to change the intensity of background brightness
    public AnimatedGIFWriter writer; // Third-party class that are used to generate gif from a set of images
    public OutputStream os = null; // OutputStream
    public boolean refresh_model=true; // If this is true, then we reload the texture and model in onDrawFrame function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Logger.log("onCreate");
        // Set our app to full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Use the Easy_Sensor class to monitor the orientation of the mobile phone.
        mEasy_To_Use_Sensor = new Easy_Sensor(this);
        if (master != null) {
            copy(master);
        }
        mGLView = new GLSurfaceView(getApplication());

        mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                // Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
                // back to Pixelflinger on some device (read: Samsung I7500)
                int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
                EGLConfig[] configs = new EGLConfig[1];
                int[] result = new int[1];
                egl.eglChooseConfig(display, attributes, configs, 1, result);
                return configs[0];
            }
        });
        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);
        // Get the width and height of my screen
        ViewTreeObserver viewTree = mGLView.getViewTreeObserver();
        viewTree.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                // Note that the width and height are inversed here
                finalWidth = mGLView.getMeasuredHeight();
                finalHeight = mGLView.getMeasuredWidth();

                // Initialize here
                currX = finalHeight/2;
                currY = finalWidth/2;
                Log.i("My_JPCT"," finalHeight = "+ finalHeight);
                Log.i("My_JPCT"," finalWidth = "+ finalWidth);
                return true;
            }
        });

        setContentView(mGLView);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
        mEasy_To_Use_Sensor.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        mEasy_To_Use_Sensor.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void copy(Object src) {
        try {
            Logger.log("Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(this, f.get(src));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overide this function and add items to the Menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_Choose_Obj, Menu.NONE, "Choose Obj");
        menu.add(Menu.NONE, MENU_Choose_TextureMap, Menu.NONE, "Choose Tex");
        SubMenu sub=menu.addSubMenu(Menu.NONE, MENU_Setting, Menu.NONE, "Setting");
        sub.add(Menu.NONE, MENU_Light_Setting, Menu.NONE, "light intensity");
        sub.add(Menu.NONE, MENU_Back_Color_Setting, Menu.NONE, "Background intensity");
        sub.add(Menu.NONE, MENU_TRY_OBJ, Menu.NONE, "LOAD SLOW OBJECT");
        menu.add(Menu.NONE, MENU_Create_Gif, Menu.NONE, "Create GIF");
        return true;
    }

    /**
     * Overide this function and set event to each item in the Menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case MENU_Choose_Obj:
                Log.i("My_JPCT"," Choose Other Obj");
                Use_Obj_Slow=false;// Make sure that we load .ser instead of .obj
                openFolder();
                return true;
            case MENU_Choose_TextureMap:
                Log.i("My_JPCT"," Choose new Tex");
                Use_Obj_Slow=false;
                openFolder_ForTexture();
                return true;
            case MENU_Light_Setting:
                Log.i("My_JPCT"," Start MENU_Light_Setting intent ");
                Use_Obj_Slow=false;
                // Change the light intensity of the light source.
                Normal_Zoom=false;
                Change_Light_Intensity=true;
                Change_Background_Brightness=false;
                return true;
            case MENU_Back_Color_Setting:
                Log.i("My_JPCT"," Start MENU_Back_Color_Setting intent ");
                Use_Obj_Slow=false;
                // Change the brightness of the background
                Normal_Zoom=false;
                Change_Light_Intensity=false;
                Change_Background_Brightness=true;
                return true;
            case MENU_TRY_OBJ:
                Log.i("My_JPCT"," Start Try Obj intent ");
                // This is only used to show that it is rather slow to load an obj.
                Use_Obj_Slow=true;
                refresh_model=true;
                return true;
            case MENU_Create_Gif:
                Log.i("My_JPCT"," Start Creating Gif ");
                Use_Obj_Slow=false;
                // Enter the gif generation mode
                Normal_Zoom=false;
                Change_Light_Intensity=false;
                Change_Background_Brightness=false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            // When the screen is touched, just escape the light intensity or background intensity change modes
            if(Change_Light_Intensity||Change_Background_Brightness)
            {
                Normal_Zoom=true;
            }
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            // Record current position so that we can know
            // Where we should set the light
            currX = me.getRawX();
            currY = me.getRawY();
            return true;
        }
        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return super.onTouchEvent(me);
    }

    protected boolean isFullscreenOpaque() {
        return true;
    }
    class MyRenderer implements GLSurfaceView.Renderer {

        private long time = System.currentTimeMillis();

        public MyRenderer() {
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) {
                fb.dispose();
            }
            fb = new FrameBuffer(gl, w, h);


        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        /**
         * This function is called many times every second. Please be careful.
         * @param gl
         */
        public void onDrawFrame(GL10 gl) {
            if(refresh_model)
            {
                // Render here, instead of rendering at onSurfaceChanged
                // Log.i("My_JPCT"," Here is the inside part! ");
                if(my_load_ser!=null)
                {
                    // Remove all prior models
                    world.removeAll();
                    Log.i("My_JPCT"," Removed prior model! ");
                }
                // Create a new world
                world = new World();
                world.setAmbientLight(20, 20, 20);
                // Set light
                sun = new Light(world);
                sun.setIntensity(250, 250, 250);

                // Start record the loading time of our model
                long start=new Date().getTime();
                Texture texture=null;
                if(My_Texture_Pick_Path==null)
                {
                    // If My_Texture_Pick_Path is null, it means that we execute this program for the
                    // first time, or we didn't select any path.
                    // Use default texture~
                    File sdCard = Environment.getExternalStorageDirectory();
                    //File dir = new File (sdCard.getAbsolutePath() + "/My_JPCT/"+"merge3d_hat.jpg");
                    //FileInputStream fileInputStream = new FileInputStream(dir);
                    Bitmap mybitmap = BitmapFactory.decodeFile(sdCard.getAbsolutePath() + "/My_JPCT/"+"merge3d_hat.jpg");
                    // Resize the texture map. Be careful about the size. It must be the power of 2
                    // For instance, 2048 = 2^11 and 1024 = 2^10
                    texture = new Texture(BitmapHelper.rescale(mybitmap, 2048, 1024));
                }
                else
                {
                    //Use picked path here~
                    Log.i("My_JPCT"," Use picked texture path! ");
                    Bitmap mybitmap = BitmapFactory.decodeFile(My_Texture_Pick_Path);
                    texture = new Texture(BitmapHelper.rescale(mybitmap, 2048, 1024));
                }
                if(texture==null)
                {
                    // The texture should not be null.
                    // Please contact me if you really see the log below
                    Log.i("My_JPCT"," Unexpected Error About texture loading. Please contact me for further information :) ");
                }


                //Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.merge3d)), 2048, 1024));
                //Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.merge3d_hat)), 2048, 1024));
                if(TextureManager.getInstance().containsTexture("texture"))
                {
                    // If the texture named "texture" has existed, just remove it and create a new texture
                    // whose name is "texture"
                    TextureManager.getInstance().removeTexture("texture");
                    TextureManager.getInstance().addTexture("texture", texture);
                }
                else
                {
                    // This line should be executed once, only at the beginning.
                    // Because at first time, there are no texture whose name is "texture"
                    TextureManager.getInstance().addTexture("texture", texture);
                }
                // Choose whether we want to use Obj or Ser.
                if(Use_Obj_Slow)
                {
                    // We can load the obj and mtl by this way.
                    // But it is rather slow.
                    // So, I decided to not use it.
                    Object3D[] objectsArray2 = new Object3D[0];
                    try {
                        objectsArray2 = Loader.loadOBJ(MainActivity.this.getResources().getAssets().open("try_jame2.obj"), MainActivity.this.getResources()
                                .getAssets().open("try_jame2_mtl.mtl"), 1.0f);

                        // Just for debug
                        //    objectsArray2 = Loader.loadOBJ(MainActivity.this.getResources().getAssets().open("no_hat.obj"), MainActivity.this.getResources()
                        //            .getAssets().open("no_hat_mtl.mtl"), 1.0f);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    my_load_ser = Object3D.mergeAll(objectsArray2);
                    // Set the texture map of this object
                    my_load_ser.setTexture("texture");
                    my_load_ser.build();
                    // Rotate the model so that it is in the right position (just in my case).
                    // Maybe you don't need to rotate your model.
                    my_load_ser.rotateZ(3.1415927f);
                    my_load_ser.rotateY(3.1415927f);
                }
                else
                {
                    // Instead of using .obj file, we use .ser here.
                    // It is much faster.
                    // To convert an .obj file to a .ser file, please follow the instruction at here:
                    // https://sourceforge.net/p/meshserializer/home/Home/
                    // Though you need to install the eclipse first, it is very easy to use.
                    try {
                        //load no hat
                        //my_load_ser = Loader.loadSerializedObject(MainActivity.this.getResources().getAssets().open("merge3d.ser"));
                        //load with hat
                        //my_load_ser = Loader.loadSerializedObject(MainActivity.this.getResources().getAssets().open("try_jame2.ser"));
                        //File file = new File(My_Pick_Path);
                        if(My_Pick_Path==null)
                        {
                            // If My_Pick_Path is null, it means that we execute this program for the
                            // first time, or we didn't select any path.
                            File sdCard = Environment.getExternalStorageDirectory();
                            File dir = new File (sdCard.getAbsolutePath() + "/My_JPCT/"+"try_jame2.ser");
                            FileInputStream fileInputStream = new FileInputStream(dir);
                            // Load .ser model from the default file path
                            my_load_ser = Loader.loadSerializedObject(fileInputStream);
                        }
                        else
                        {
                            //Use picked path here~
                            Log.i("My_JPCT"," Use picked path! ");
                            File dir = new File (My_Pick_Path);
                            FileInputStream fileInputStream = new FileInputStream(dir);
                            // Load .ser model from the file path you chose
                            my_load_ser = Loader.loadSerializedObject(fileInputStream);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Set texture we selected.
                    my_load_ser.setTexture("texture");
                    my_load_ser.build();
                    // Rotate the model so that it is in the right position (just in my case).
                    // Maybe you don't need to rotate your model.
                    my_load_ser.rotateZ(3.1415927f);
                    my_load_ser.rotateY(3.1415927f);
                }
                // Record the loading time here.
                long end=new Date().getTime();
                final long run_time=end-start;
                Log.i("My_JPCT","run_time = "+run_time);
                // Show the time used to load the model using toast.
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this.getBaseContext(), "file load time =  "+run_time+" ms", Toast.LENGTH_LONG).show();
                    }
                });

                // Add the object to the world we created
                world.addObject(my_load_ser);

                // Set the position of our camera.
                cam = world.getCamera();
                cam.moveCamera(Camera.CAMERA_MOVEOUT, 5);
                cam.lookAt(my_load_ser.getTransformedCenter());


                // Set the initial position of the light
                // It is at the center of our model.
                sv = new SimpleVector();
                sv.set(my_load_ser.getTransformedCenter());
                sv.z -= 100;
                sun.setPosition(sv);
                // Log the memory usage
                MemoryHelper.compact();

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    master = MainActivity.this;
                }

                refresh_model=false;
            }
            // Set the position of the light
            // First and foremost, set position of the light
            // Current x interval: [0,ScreenHeight] -> [0,300] -> [-150,150]
            sv.x=300*currX/finalHeight-150;
            // Current y interval: [0,ScreenWidth] -> [0,400] -> [-200,200]
            sv.y=400*currY/finalWidth-200;
            sun.setPosition(sv);

            // Rotate our model refer to the data from sensor
            Rotate_Object_According_To_Sensor_Data();

            // Handle gif creation event
            Handle_Gif_Creation();

            //refresh back ground color at here
            back = new RGBColor(color_background, color_background, color_background);
            fb.clear(back);

            world.renderScene(fb);
            world.draw(fb);
            fb.display();
        }
    }

    /**
     * In this function, the data from sensor is used to rotate the object
     */
    public void Rotate_Object_According_To_Sensor_Data()
    {

        float roll_angle=mEasy_To_Use_Sensor.return_angle_roll();
        float pitch_angle=mEasy_To_Use_Sensor.return_angle_pitch();
        //Log.i("My_JPCT","g = "+mEasy_To_Use_Sensor.return_gravity_zero());
        // Determine the position of our mobile phone by the data from our sensor.
        int phone_is_vertical = mEasy_To_Use_Sensor.phone_is_vertical();
        //Log.i("JPCT","vertical = "+phone_is_vertical);
        if(phone_is_vertical==2)
        {
            // The phone is verticle
            // Use azimuth value
            float azimuth_angle=mEasy_To_Use_Sensor.return_angle_azimuth();
            // This value is set to 50 to avoid reset of the
            // object rotation when the phone is verticle at first time
            float angle_diff=50;
            float angle_diff_pitch=50;

            if(first_time_verticle)
            {
                //record angle here
                begin_angle_value=azimuth_angle;
                pre_azimuth_value=0;

                my_load_ser.clearRotation();
                my_load_ser.rotateZ(3.1415927f);
                my_load_ser.rotateY(3.1415927f);

                first_time_verticle=false;
            }
            else
            {

                angle_diff = azimuth_angle - begin_angle_value;
                // Make sure the angle_diff is within [0,360]
                if(angle_diff<-180)
                {
                    angle_diff += 360;
                }
                if(angle_diff>180)
                {
                    angle_diff -= 360;
                }
                //Log.i("My_JPCT","angle_diff = "+angle_diff);

                float sensor_value_azimuth_diff = pre_azimuth_value-angle_diff;

                //ToDo: Use a function to do this.
                //Todo: Maybe should use double when we use Math.PI
                float my_radius= (float) (Math.PI*sensor_value_azimuth_diff/180);
                //Log.i("My_JPCT","angle_diff = "+angle_diff);
                if(Math.abs(my_radius)>0.03f)
                {
                    pre_azimuth_value=angle_diff;
                    //Turn value sensor_value_azimuth_diff to radius here
                    my_load_ser.rotateY(my_radius);
                }
            }

            // Use vertical pitch angle
            float vertical_pitch_angle=mEasy_To_Use_Sensor.return_angle_vertical_pitch();

            if(first_time_verticle_pitch)
            {
                begin_angle_value_vertical=90;
                pre_pitch_value_vertical=0;
                first_time_verticle_pitch=false;
            }
            else
            {
                angle_diff_pitch = vertical_pitch_angle - begin_angle_value_vertical;
                if(angle_diff_pitch<-180)
                {
                    angle_diff_pitch += 360;
                }
                if(angle_diff_pitch>180)
                {
                    angle_diff_pitch -= 360;
                }
                float sensor_value_pitch_diff_verticle = -pre_pitch_value_vertical+angle_diff_pitch;
                //Log.i("My_JPCT","angle_diff_pitch = "+angle_diff_pitch);
                //ToDo: Use a function to do this.
                //Todo: Maybe should use double when we use Math.PI
                float my_radius_pitch= (float) (Math.PI*sensor_value_pitch_diff_verticle/180);
                if(Math.abs(my_radius_pitch)>0.03f)
                {
                    pre_pitch_value_vertical=angle_diff_pitch;
                    my_load_ser.rotateX(my_radius_pitch);
                }

            }
            // Parameters maybe tuned for better performance here.
            if(Math.abs(angle_diff)<6&&Math.abs(angle_diff_pitch)<4.5)
            {
                //clear rotation when the phone is flat
                my_load_ser.clearRotation();
                my_load_ser.rotateZ(3.1415927f);
                my_load_ser.rotateY(3.1415927f);
            }
        }
        else if(phone_is_vertical==1)
        {
            // If the phone is placed horizontally
            first_time_verticle = true;
            first_time_verticle_pitch = true;
            if(first_time_use_sensor)
            {
                float my_roll_angle=mEasy_To_Use_Sensor.return_angle_roll();
                pre_roll_value=my_roll_angle;
                first_time_use_sensor=false;
            }
            else
            {
                float my_roll_angle=mEasy_To_Use_Sensor.return_angle_roll();
                float sensor_value_roll_diff = -pre_roll_value+my_roll_angle;
                if(Math.abs(sensor_value_roll_diff)>0.03f)
                {
                    pre_roll_value=my_roll_angle;
                    //Log.i("My_JPCT","my_roll_angle = "+my_roll_angle);
                    my_load_ser.rotateY(sensor_value_roll_diff);
                }
            }

            if(first_time_use_sensor_pitch)
            {
                float my_pitch_angle=mEasy_To_Use_Sensor.return_angle_pitch();
                pre_pitch_value=my_pitch_angle;
                first_time_use_sensor_pitch=false;
            }
            else
            {
                float my_pitch_angle=mEasy_To_Use_Sensor.return_angle_pitch();
                float sensor_value_pitch_diff = pre_pitch_value-my_pitch_angle;
                if(Math.abs(sensor_value_pitch_diff)>0.03f)
                {
                    pre_pitch_value=my_pitch_angle;
                    //Log.i("My_JPCT","my_pitch_angle = "+my_pitch_angle);
                    my_load_ser.rotateX(sensor_value_pitch_diff);
                }
            }
        }
        else
        {
            // Do nothing when the data is unreliable.
        }
        // Parameters maybe tuned for better performance here.
        if(Math.abs(roll_angle)<0.10f&&Math.abs(pitch_angle)<0.08f)
        {
            //clear rotation when the phone is flat
            my_load_ser.clearRotation();
            my_load_ser.rotateZ(3.1415927f);
            my_load_ser.rotateY(3.1415927f);
        }
        //Log.i("My_JPCT","roll_angle = "+roll_angle);
    }
    public void Handle_Gif_Creation()
    {
        String Output_Dir=null;
        if(first_ondraw)
        {
            // Only execute this part when the program is executed for the first time.
            // This gif writer is from here:
            // https://github.com/dragon66/animated-gif-writer
            // Initialize here!
            writer = new AnimatedGIFWriter(true);

            try {
                // Set the output directory of our gif
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/my_gif_test");
                Log.i("JPCT","My File path" + sdCard.getAbsolutePath() + "/my_gif_test");
                Output_Dir=sdCard.getAbsolutePath() + "/my_gif_test";
                dir.mkdirs();
                // Set the file name
                String fileName = String.format("Generated_Result.gif");
                File outFile = new File(dir, fileName);

                os = new FileOutputStream(outFile);
                writer.prepareForWrite(os, -1, -1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            first_ondraw=false;
        }
        else
        {
            // Exectue this part if we are in the gif generation mode (Do_Gif_Caption = true)
            if(Do_Gif_Caption)
            {
                try {
                    // Copy image pixels from the frame buffer to an int array: tmpPixels
                    tmpPixels = fb.getPixels();
                    // Log.i("JPCT","Refresh");
                    if(tmpPixels!=null)
                    {
                        // Create a bitmap from the int array: tmpPixels
                        Bitmap lastImage = Bitmap.createBitmap(fb.getWidth(), fb.getHeight(), Bitmap.Config.ARGB_4444);
                        lastImage.copyPixelsFromBuffer(IntBuffer.wrap(tmpPixels));
                        try {
                            Do_Gif_Caption=false;
                            if(Finish_Gif_Generation)
                            {
                                writer.finishWrite(os);
                                Log.i("JPCT","Done");
                                // Use toast to show the output path of our gif
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        File sdCard = Environment.getExternalStorageDirectory();
                                        Toast.makeText(MainActivity.this.getBaseContext(), "Gif Generated at "+sdCard.getAbsolutePath() +
                                                "/my_gif_test/Generated_Result.gif", Toast.LENGTH_LONG).show();
                                    }
                                });
                                Normal_Zoom=true;
                            }
                            else
                            {
                                // Keep capturing images and write them to the frame
                                writer.writeFrame(os, lastImage);
                                Frame_Number++;
                            }
                            // And you are done!!!
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    /**
     * Listen to the key event
     * @param event
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            // Push the volume up button
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.i("JPCT","keyup!!！");
                    // Decide whether we are in the gif generation mode or not according to the
                    // value of Normal_Zoom.
                    if(Normal_Zoom)
                    {
                        // Simply zoom the camera
                        if(cam.getFOV()>0.3f)
                        {
                            cam.setFOV(cam.getFOV()-0.1f);
                        }
                        else
                        {
                            // Limit the fov of the camera
                            cam.setFOV(0.3f);
                        }
                        /*refresh_model=true;
                        color_background-=50;
                        Use_Obj_Slow=false;*/
                    }
                    else if(Change_Light_Intensity)
                    {
                        // Decrease light intensity
                        light_intensity-=30;
                        sun.setIntensity(light_intensity, light_intensity, light_intensity);
                    }
                    else if(Change_Background_Brightness)
                    {
                        // Decrease background brightness
                        color_background-=30;
                    }
                    else
                    {
                        // Capture an image that can be used to create a gif
                        Do_Gif_Caption=true;
                    }


                }
                return true;
            // Push the volume down button
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.i("JPCT","keydown!!！");
                    // Decide whether we are in the gif generation mode or not according to the
                    // value of Normal_Zoom.
                    if(Normal_Zoom)
                    {
                        // Simply zoom the camera
                        if(cam.getFOV()<5.4f)
                        {
                            cam.setFOV(cam.getFOV()+0.1f);
                        }
                        else
                        {
                            // Limit the fov of the camera
                            cam.setFOV(5.4f);
                        }
                    }
                    else if(Change_Light_Intensity)
                    {
                        // Increase light intensity
                        light_intensity+=30;
                        sun.setIntensity(light_intensity, light_intensity, light_intensity);
                    }
                    else if(Change_Background_Brightness)
                    {
                        // Increase background brightness
                        color_background+=30;
                    }
                    else
                    {
                        // Finish capturing images and start generate a gif image.
                        // Note that we don't capture an image here. Though the
                        // Do_Gif_Caption flag is set to true.
                        Do_Gif_Caption=true;
                        Finish_Gif_Generation=true;
                    }
                }
                return true;
            // Push the back button
            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.i("JPCT","Call the menu");
                    // Call the Menu
                    openOptionsMenu();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                String type = data.getType();
                Log.i("JPCT","Pick completed: "+ uri + " "+type);
                if (uri != null)
                {
                    // Get model uri and change it to the file path
                    My_Pick_Path = getRealPathFromURI(MainActivity.this,uri);
                    Log.i("JPCT","My Path: "+ My_Pick_Path);
                }
                // Change this flag to true so that we can refresh the object
                // This flag is used in onDrawFrame function.
                refresh_model=true;
            }
        }
        if (requestCode == PICKTEXTURE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                String type = data.getType();
                Log.i("JPCT","Pick texture completed: "+ uri + " "+type);
                if (uri != null)
                {
                    // Get texture uri and change it to the file path
                    My_Texture_Pick_Path = getRealPathFromURI(MainActivity.this,uri);
                    Log.i("JPCT","My Texture Path: "+ My_Texture_Pick_Path);
                }
                // Change this flag to true so that we can refresh the object
                // This flag is used in onDrawFrame function.
                refresh_model=true;
            }
        }
    }

    /**
     * https://stackoverflow.com/a/17173655/7783462
     * Open the My_JPCT folder via Intent and show its content in a file browser.
     * Used to get the uri path of the model
     */
    public void openFolder()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                + "/My_JPCT/");
        intent.setDataAndType(uri, "*/*");
        startActivityForResult(Intent.createChooser(intent, "Open folder"),PICKFILE_REQUEST_CODE);
    }

    /**
     * Open the My_JPCT folder via Intent and show its content in a file browser
     * Used to get the uri path of the texture of a model
     */
    public void openFolder_ForTexture()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                + "/My_JPCT/");
        intent.setDataAndType(uri, "*/*");
        startActivityForResult(Intent.createChooser(intent, "Open folder"),PICKTEXTURE_REQUEST_CODE);
    }
    /**
     * Source: https://stackoverflow.com/a/17546561/7783462
     * @param context
     * @param contentUri: The input uri
     * @return The file path
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}