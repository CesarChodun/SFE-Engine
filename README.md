# SFE - Sierra Fastigium Engine

A small engine based on Vulkan(LWJGL3) and Java. With a goal to provide a highly multithreaded rendering engine that works natively on Vulkan API.

<h2>Relevant links</h2>

 * Trello board for the project: https://trello.com/b/ghMSQq99
 
<h2>Installation with ANT(Recomended)</h2>

  <h3>Install folowing:</h3>
  
   - Java 8(JDK)
   - Apache ANT
   - Vulkan SDK [version >= 1.2]
   - Vulkan runtime(only on windows) [version >= 1.2]
   
   Note: If some of these programs are already installed on your machine or are already included in your GPU driver (Vulkan runtime), you can ommit some of this steps.
   
   <h3>Engine installation</h3>
   
   - Clone or download the repository.
   - Copy the glslangValidator(.exe) from the Vulkan SDK folder(/Bin/glslangValidator...) to the repository folder.
   - Either open the repository in an editor that has tools for ANT and invoke the "build-all" task. Or run <code>ant build-all</code>.
   - DONE! If everything went well you should be able to go to the folder: build/demos and run the demonstration programs(see "running the demos" section).
   
   <h3>Working with the engine</h3>
   
   If you want to experiment with the engine I recomend setting up a project in IDE like Eclipse or IntelliJ. To do so you should first follow the instructions above. When you are done and at least the HardwareInit demo works(see "running the demos" section). You can move on to prepare your workspace.
   
   - Add the build/eclipse/lib/dependencies.jar jar file to the classpath of your project(in the IDE).
   - Attach the source build/eclipse/lib/dependencies-sources.jar and the javadoc build/eclipse/lib/dependencies-javadoc.jar
   - Add following VM arguments:<br/>
      <code>-Dorg.lwjgl.util.DebugAllocator=true</code><br/>
      <code>-Dorg.lwjgl.util.DebugLoader=true</code><br/>
      <code>-Dorg.lwjgl.vulkan.libname=[Your path to the vulkan library eg. "C:\Windows\System32\vulkan-1.dll"]</code><br/>
  - And if you are on MAC add also:<br/>
  <code>-XstartOnFirstThread</code>
  
<h2>Running the demos</h2>

After the installation(with ANT) you should be able to run some(or all) of the demos.<br/>
The demos can be run simply by double-click but it's not the best way to go especially if you are running them for the first time.<br/>
A much better way of running the demos is to go to the repository folder and run the following command in the terminal: <code>java -jar demoName.jar</code>(where "demoName" is the name of the program that you want to run).<br/>
This way you should be able to see the demo output. Also if you are running it on MAC you might want to run the following command instead:<code>java -jar -XstartOnFirstThread demoName.jar</code>.

<h2>Troubleshooting</h2>

Try running the HardwareInit.jar demo in the console and check if the demo outputs information about Validation Layers and available Vulkan extensions. If not you have some problem with your Vulkan runtime installation. And you might want to try reinstalling it.

If you have experienced any other problems running the demos. Please report them in the issues section.
   
<h2>Installation without ANT(Not Recomended) </h2>

 <h3>Prepare your IDE</h3>
 
  * Download and install Vulkan SDK from <a href="https://vulkan.lunarg.com/sdk/home">this</a> site.
  * Download required libraries(see dependecies) and add them to the build path.
  * Create a new project in your IDE.
  * Add the Vulkan libraries to the project.
  * Add glslangValidator executable to the project folder(from installed Vulkan SDK).
  * Add <b>SFE Engine</b> package contents to the project and make sure that your IDE recognizes the source("src") package.
  * If you are using Mac OS you might have to add "-XstartOnTheFirstThread" to the VM arguments and then run the demo again.
  * You are ready to go!
  
  
<h3>Dependencies</h3>

   <h4>LWJGL3 and JOML</h4>

   <p> You need "Minimal Vulkan" preset with JOML selected. You can find it on <a href="https://www.lwjgl.org/download">this</a> website.   </p>

   <h4> JSON </h4>

   <p> The engine uses JSON files and loads it with the following open source library: https://github.com/douglascrockford/JSON-java.git.
  It is required to add it to the build path.</p>

  <h4> Nullable anotations </h4>

  <p> The project uses Eclipse null anotations(org.eclipse.jdt.annotation.Nullable). If you are using any other IDEs you might need to remove @Nullable anotation from a few files. </p>

<h2>Contribute!</h2>
<p>Developing fully functional game engine is challenging enough for a team of full time developers. Not to say for a guy that is pursuing bechelor's degree at the same time(and tries to sleep 8 hours a day). So any amount of help is more then welcome!</p>

<h3>Help with documentation!</h3>

  We all make mistakes and it is quite normall that when the program exceeds a few thousands lines of code (and its documentation) there will be some typos. So if you found any typos please report it through the issues page. I will really aprecieate it.
  
 <h3>Help with demos!</h3>
 There is no such thing as too much demos. If you understand some part of the engine and want to contribute to the project you can create a demo which uses this part of the engine. It doesn't have to be sophisticated! Just experiment if you wish and if you make something interesting please share it with others! 
 
 <h3>Help with configuration!</h3>
  There are quite a few configuration files with the project. And as there are a lot of hardware configurations. It is very likely that some problems with the engine would be possible to solve using just a few small changes in the configuration files. So if you found a issue with the engine that you think can be solve with the configuration files. Feel free to experiment and If you happen to fix the problem share your thoughts and tips with others!
 
 <h3> Help with code! </h3>
  Of course there is the usual way to help with the project. And this is by adding new features to it! So if you found yourself quite comfortable with the project feel free to add some tweaks to it!
  
 <h2>Special thanks to:</h2>
 
 * <a href="https://github.com/Spasi">Ioannis Tsakpinis(Spasi)</a>
 * <a href="https://www.youtube.com/channel/UCo8zkw_12vD_G-we_fWrBDA/featured">Phoenix</a>
