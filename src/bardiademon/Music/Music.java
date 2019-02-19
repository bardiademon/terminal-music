package bardiademon.Music;

import bardiademon.Music.Database.Database;
import bardiademon.Music.Database.InfoDb;
import bardiademon.Music.Interface.bardiademon;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@bardiademon
public class Music
{
    static Music _Music;

    static boolean activeControl;

    private static boolean repeat;

    static boolean getInput;

    private static File nowMusic;

    private static boolean isPlay, isPause;

    static int indexNowMusic = 0;
    private static int indexPreMusic;
    private static int repeatMusic = 0;

    static final String[] TYPE_MUSIC = {"mp3"};

    static OpenMusic openMusic;

    private static Player player;
    private static FileInputStream fileInputStream;

    @bardiademon
    public static Database ConnectionDbFile;

    private static final int PLAY_LAST_MUSIC = 1, PLAY_LAST_LIST = 2,
            OPEN_MUSIC = 3, YOUR_LIST = 4,
            CREATE_LIST = 5, GO_TO_PROGRESS = 6, CONTROL = 7;

    private static int allAvailable, availablePause;

    @bardiademon
    Music ()
    {
        ConnectionDbFile = (new Database (InfoDb.DFile.NAME_DATABASE));
        if (ConnectionDbFile.isConnected ())
        {
            new About ();
            titleAndNumber ();
        }
        else System.err.println ("Connection Database Error");
    }

    @bardiademon
    static void titleAndNumber ()
    {
        Progress.startThread = false;
        activeControl = false;
        new CreateOption ().create (new String[]{"Play Last Music" , "Play Last List" , "Open Music" , "Your List" , "Create List" , "Progress" , "Control"} , true);
        GetNumber getNumber = new GetNumber ("\rInput Number: ");
        try
        {
            if (getNumber.isOk ())
            {
                if (getNumber.getNumber () == CreateOption.OPTION_EXIT)
                {
                    Music.ConnectionDbFile.close ();
                    System.exit (0);
                }
                switch (getNumber.getNumber ())
                {
                    case PLAY_LAST_MUSIC:
                        if (!Last.LastMusic ())
                        {
                            System.err.println ("Not Found Last Music");
                            titleAndNumber ();
                        }
                        break;
                    case PLAY_LAST_LIST:
                        if (!Last.LastList ())
                        {
                            System.err.println ("Not Found Last List");
                            titleAndNumber ();
                        }
                        break;
                    case OPEN_MUSIC:
                        openMusic = new OpenMusic ();
                        setIndexNowMusic (0);
                        playMusic (0);
                        isPlay = true;
                        break;
                    case YOUR_LIST:
                        playList ();
                        break;
                    case CREATE_LIST:
                        new CreateList ();
                        break;
                    case GO_TO_PROGRESS:
                    {
                        if (isPlay)
                        {
                            Progress.startThread = true;
                            new Progress ();
                        }
                        else
                        {
                            System.err.println ("\nMusic Not Play!");
                            if (!Music.getInput) titleAndNumber ();
                        }
                        break;
                    }
                    case CONTROL:
                        Progress.startThread = false;
                        control ();
                        break;
                }
            }
            else throw new Exception ("Error Input");
        }
        catch (Exception e)
        {
            new ClearTerminal ();
            if (!Music.getInput) titleAndNumber ();
        }

    }

    private static void control ()
    {
        activeControl = true;
        Progress.startThread = false;
        final int PRE = 1, PLAY_PAUSE = 2, NEXT = 3, STOP = 4, REPEAT = 5;
        String pathMusic = nowMusic.getPath ();
        Mp3Info mp3Info = new Mp3Info (pathMusic);
        println ("-----------------------------------------------------------------------------------------------");
        println ("Name: " + FilenameUtils.getName (pathMusic));
        println ("Title: " + mp3Info.getTitle ());
        println ("Artist: " + mp3Info.getArtist ());
        println ("Track: " + mp3Info.getTrack ());
        println ("Album: " + mp3Info.getAlbum ());
        println ("Year: " + mp3Info.getYear ());
        println ("Path: " + pathMusic);
        println ("-----------------------------------------------------------------------------------------------");
        new CreateOption ().create (new String[]{"Pre" , "Play Or Pause" , "Next" , "Stop" , "Repeat"} , true , false);
        GetNumber getNumber = new GetNumber ();
        if (getNumber.isOk ())
        {
            if (getNumber.getNumber () == CreateOption.OPTION_EXIT)
            {
                activeControl = false;
                titleAndNumber ();
                return;
            }
            switch (getNumber.getNumber ())
            {
                case PRE:
                    preMusic ();
                    break;
                case PLAY_PAUSE:
                    playPauseMusic ();
                    break;
                case NEXT:
                    nextMusic ();
                    break;
                case STOP:
                    player.close ();
                    isPlay = false;
                    isPause = false;
                    break;
                case REPEAT:
                    repeat = !repeat;
                    break;
            }
        }
        control ();
    }

    private static void playPauseMusic ()
    {
        if (isPlay)
        {
            availablePause = GetAvailable ();
            isPause = true;
            isPlay = false;
            player.close ();
        }
        else if (isPause) playMusic (availablePause);
    }

    private static void println (String str)
    {
        System.out.println (str);
    }

    private static void setIndexNowMusic (int indexNowMusic)
    {
        Music.indexPreMusic = Music.indexNowMusic;
        Music.indexNowMusic = indexNowMusic;
    }

    private static void playList ()
    {
        new YourList ();
    }

    @bardiademon
    static void playMusic (int available)
    {
        try
        {
            if (!isPause && !openMusic.isOk ())
            {
                System.out.println ("Not Selected!");
                titleAndNumber ();
                return;
            }
            if (isPlay && !isPause)
            {
                if (player != null)
                {
                    player.close ();
                    player = null;
                }
                isPlay = false;
            }
            if (!isPause)
            {
                fileInputStream = new FileInputStream (openMusic.getListFile ().get (indexNowMusic));
                allAvailable = fileInputStream.available ();
                player = new Player (fileInputStream);
            }
            new Thread (() ->
            {
                if (!isPause)
                {
                    nowMusic = openMusic.getListFile ().get (indexNowMusic);
                    String pathMusic = nowMusic.getPath ();
                    if (indexNowMusic == indexPreMusic) repeatMusic++;
                    else repeatMusic = 1;

                    Last.SetLastMusic (pathMusic);
                    if (!getInput && !activeControl)
                    {
                        String str = FilenameUtils.getName (pathMusic);
                        if (repeatMusic > 1) str += " :::: " + repeatMusic;
                        System.out.println (str);
                    }
                    if (!activeControl)
                    {
                        if (!getInput) Progress.startThread = true;
                        new Progress ();
                    }
                }
                try
                {
                    isPlay = true;
                    if (isPause && available > 0)
                    {
                        isPause = false;
                        player.play (available);
                    }
                    else player.play ();
                    if (isPlay)
                    {
                        if (!repeat) nextMusic ();
                        else playMusic (0);
                    }
                    else Progress.startThread = false;
                }
                catch (JavaLayerException e)
                {
                    isPlay = false;
                    System.err.print ("\nError: " + e.getMessage ());
                }
            }).start ();
        }
        catch (JavaLayerException | IOException e)
        {
            isPlay = false;
            System.err.print ("\nError: " + e.getMessage ());
        }
    }

    @bardiademon
    private static void nextMusic ()
    {
        if (openMusic == null) return;
        if (indexNowMusic + 1 < openMusic.getListFile ().size ())
        {
            setIndexNowMusic (indexNowMusic + 1);
            playMusic (0);
        }
        else
        {
            if (openMusic.getListFile ().size () > 0)
            {
                setIndexNowMusic (0);
                playMusic (0);
            }
        }
    }

    @bardiademon
    private static void preMusic ()
    {
        if (openMusic == null) return;
        if (indexNowMusic - 1 > 0)
        {
            setIndexNowMusic (indexNowMusic + 1);
            playMusic (0);
        }
        else
        {
            if (openMusic.getListFile ().size () > 0)
            {
                setIndexNowMusic (indexNowMusic = 0);
                indexNowMusic = 0;
                playMusic (0);
            }
        }
    }


    @bardiademon
    static int GetAvailable ()
    {
        if (player.isComplete ()) return 0;
        else
        {
            try
            {
                return (fileInputStream.available ());
            }
            catch (IOException e)
            {
                return 0;
            }
        }
    }

    @bardiademon
    static int GetAllAvailable ()
    {
        return allAvailable;
    }

    @bardiademon
    static boolean IsComplete ()
    {
        return (player != null && player.isComplete ());
    }

    static void ResetDatabase ()
    {
        Music.ConnectionDbFile.close ();
        Music.ConnectionDbFile = null;
        Music.ConnectionDbFile = new Database (InfoDb.DFile.NAME_DATABASE);
        System.gc ();
    }
}
