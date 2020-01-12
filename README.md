# SFE - Sierra Fastigium Engine

A small engine based on Vulkan(LWJGL3) and Java. With a goal to provide a highly multithreaded rendering engine that works natively on Vulkan API.

<h2>Relevant links</h2>

 * Trello board for the project: https://trello.com/b/ghMSQq99

<h2>Instalation</h2>

 <h3>Prepare your IDE</h3>
 
  * Download and install Vulkan SDK from <a href="https://vulkan.lunarg.com/sdk/home">this</a> site.
  * Download required libraries(see dependecies) and add them to the build path.
  * Create a new project in your IDE.
  * Add the Vulkan libraries to the project.
  * Add glslangValidator executable to the project folder(from installed Vulkan SDK).
  * Add <b>SFE Engine</b> package contents to the project and make sure that your IDE recognizes the source("src") package.
  * If you are using Mac OS you might have to add "-XstartOnTheFirstThread" to the VM arguments and then run the demo again.
  * You are ready to go!
  
  
<h2>Dependencies</h2>

 <h3>LWJGL3 and JOML</h3>

 <p> You need "Minimal Vulkan" preset with JOML selected. You can find it on <a href="https://www.lwjgl.org/download">this</a> website. </p>
 
 <h3> JSON </h3>
 
 <p> The engine uses JSON files and loads it with the following open source library: https://github.com/douglascrockford/JSON-java.git.
It is required to add it to the build path.</p>

<h3> Nullable anotations </h3>

<p> The project uses Eclipse null anotations(org.eclipse.jdt.annotation.Nullable). If you are using any other IDEs you might need to remove @Nullable anotation from a few files. </p>

<h2>Contribute!</h2>
<p>Developing fully functional game engine is challenging enough for a team of full time developers. Not to say for a guy that is pursuing bechelor's degree at the same time(and tries to sleep 8 hours a day). So any amount of help is more then welcome!</p>

<h3>Help with documentation!</h3>

  We all make mistakes and it is quite normall that when the program exceeds a few thousands lines of code(and it's documentation) there will be some typos. So if you found any typos please report it through the issues page. I will really aprecieate it.
  
 <h3>Help with demos!</h3>
 There is no such thing as too much demos. If you understand some part of the engine and want to contribute to the project you can create a demo which uses this part of the engine. It doesn't have to be sophysticated! Just experiment if you wish and if you make something interesting please share it with others! 
 
 <h3>Help with configuration!</h3>
  There are quite a few configuration files with the project. And as there are a lot of hardware configurations. It is very likely that some problems with the engine would be possible to solve using just a few small changes in the configuration files. So if you found a issue with the engine that you think can be solve with the configuration files. Feel free to experiment and If you happen to fix the problem share your thoughts and tips with others!
 
 <h3> Help with code! </h3>
  Of course there is the usual way to help with the project. And this is by adding new features to it! So if you found yourself quite comfortable with the project feel free to add some tweeks to it!
  
 <h2>Special thanks to:</h2>
 
 * <a href="https://github.com/Spasi">Ioannis Tsakpinis(Spasi)</a>
 * <a href="https://www.youtube.com/channel/UCo8zkw_12vD_G-we_fWrBDA/featured">Phoenix</a>
