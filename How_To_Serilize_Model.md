# How to serilize model
This is a tutorial of the JPCT-AE Mesh Serializer:
- https://sourceforge.net/p/meshserializer/home/Home/

Download Eclipse from here:
- https://www.eclipse.org/home/index.php
And install it on your computer, then open it and create a new java project:
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/1.png)
Enter the project name as you want, it doesn't matter:
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/2.png)
Here is what you should get:
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/3.png)
Then right click the project name (My_Java in my case), choose New->Folder. 
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/4.png)
And name the new folder "input".
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/5.png)
And create another folder, name it "output". Then you should see these two folders.
Put all your model files, such as the .obj file and .mtl file under the input folder:
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/6.png)
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/7.png)
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/8.png)

Then right click on your project name, choose Properties:
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/9.png)
Then, in the open window, choose Mesh Serializer. And check the Enable serializer box, type the "input"
in the Source folder input box, and "output" in the Target folder.
![image](https://github.com/pren1/JPCT_demo/raw/master/Image/10.png)
Click "Apply" on the right bottom of the window, then click "Apply and Close".
Finally, you should see a file whose extension is .ser under the output folder:

![image](https://github.com/pren1/JPCT_demo/raw/master/Image/11.png)
