# JPCT_demo
This is a class project of CMPS 260 directed by James Davis at UCSC.
- https://courses.soe.ucsc.edu/courses/cmps260/Fall17/01
## Project name
A well documented face model render app on Android platform
## Features
1. The serialized object is loaded instead of obj and mtl files for speed up (about 20X speed-up in a google pixel)
2. The model is rotated when people rotate their mobile phone
3. The light position is moved according to the touch position
4. People can select the models and their texture maps and load them.
5. The light intensity and the background brightness can be changed
6. People can create gif images using this app
7. The program is well documented
## Libraries Used
JPCT-ae for object loading
http://www.jpct.net

Standalone Animated GIF Writer for gif creation
https://github.com/dragon66/animated-gif-writer
## How to use
1. Install Android Studio on your computer
2. Download this project, and open it with Android Studio.
3. Create a folder named My_JPCT on your phone. Its directory should be:

storage/emulated/0/My_JPCT

4. Then copy the files under JPCT_Demo_Showcase_File to the My_JPCT folder. (From PC to your mobile phone) You can find the JPCT_Demo_Showcase_File folder under the home directory of this project.

   Note that these are just serialized models and texture maps. You can also serialize your own model and put it together with your texture map into the MyCV folder on your phone.
   Please also be careful if you are going to use your own model, you should load your model use software such as blender first and see if its center is at the corrdinate origin. Otherwise, you may not see your model on the phone screen even if you successfully load it.
   To serialize your model, please refer to this:
[How_To_Serialize_Model](How_To_Serialize_Model.md)
## Things to be done
- Simply the comments 
## Notice
This app has only been tested on a google pixel phone! However, if your phone support the TYPE_GAME_ROTATION_VECTOR sensor, I think this app will run properly on it.
## Contact
Please contact me if you meet any difficulties when comprehending this app via email:

![image](https://github.com/pren1/JPCT_demo/raw/master/output2.png)
