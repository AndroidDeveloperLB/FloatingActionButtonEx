# FloatingActionButtonEx
A modified version of [**FloatingActionButton library**](https://github.com/Clans/FloatingActionButton), that better handles various issues.

![enter image description here](https://raw.githubusercontent.com/AndroidDeveloperLB/FloatingActionButtonEx/master/device-2016-03-25-113238.gif)


Advantages
----------

 1. Uses the Google Support library's FAB as the main FAB, so you can customize it the way you know it, and it uses a nice shadow for it when possible
 2. handles some animation issues, such as [**this one**](https://github.com/Clans/FloatingActionButton/issues/220#issuecomment-200958234)
3. You can edit the code and make the animation work in a different way, if you wish.

Disadvantages, known issues, and To-Dos
-------------

 1. Since it uses the support library's FAB, it has [**this issue**](https://code.google.com/p/android/issues/detail?id=204102), if you use **useCompatPadding="true"** : on certain Android APIs, the selection will not look well. Also, it can't be in small size at runtime, so you need to modify my code if you wish to have it small.
 2. Support library's FAB is only used on the main FAB. I hope to do it for the others too .
 3. If you open the menu and then try to hide it right away during the animation of showing the menu, it might disappear too fast. The toggling works fine as long as the Views have finished their animations. 
