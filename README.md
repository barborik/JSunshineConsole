# JSunshineConsole
*A Java library for simple ASCII output/input*

*this is a port of [this](https://github.com/derrickcreamer/SunshineConsole) C# library*

![Hello World](https://i.imgur.com/hplC1Vk.png)

JSunshineConsole is intended to be very friendly to beginners that just want to put something on the screen (while still being powerful enough for larger ASCII projects). So, here are the very basics:

First, you'll need to add JSunshineConsole to your project, you can download it [here](https://github.com/Barbo24/JSunshineConsole/releases/).
```java
//Create a window, 37 columns across and 12 rows high.
//I swapped rows and columns for easier conversion to standard X,Y coord system
ConsoleWindow console = new ConsoleWindow(37, 12, "JSunshineConsole");

//Write to the window at column 11, row 5
console.write(11, 5, "Hello, World!", Color.CYAN);
```

That's all you need to get off the ground!
