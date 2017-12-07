# LeelaWatcher
A GUI for watching Leela Zero self train

Please note that this is a fairly quick hack, and I resurected code I wrote 15 years ago when I first learned java, so there's a bunch of warts in the code, but with that caveat LeelaWatcher provides the following features:

1. Executes [Leela Zero](https://github.com/gcp/leela-zero)'s autogtp cooperative training mode
1. Parses the standard output from the training sesion to extract the moves
1. Displays the moves on a graphical board
1. When the game ends, writes it out to an SGF file named for the timestamp of when the game ended.

# Running
You can run the disributed jar file like this on Unix

    java -jar LeelaWatcher-1.0.1.jar /home/gus/leelaz/leela-zero/autogtp/
    
The directory passed in must be the location of your leela-zero autogtp build. For windows adding the name of the binary like this should make it work.

    D:\My Folders\Downloads\leela-zero-0.4-windows>java -jar LeelaWatcher-1.0.1.jar . autogtp.exe
