package bardiademon.Music;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

class Mp3Info extends Mp3File
{
    private String album, artist, title, track, year;

    Mp3Info (String Path)
    {
        try
        {
            Mp3File mp3File = new Mp3File (Path);
            ID3v1 id3v1Tag = mp3File.getId3v1Tag ();
            album = id3v1Tag.getAlbum ();
            artist = id3v1Tag.getArtist ();
            title = id3v1Tag.getTitle ();
            track = id3v1Tag.getTrack ();
            year = id3v1Tag.getYear ();
        }
        catch (IOException | UnsupportedTagException | InvalidDataException e)
        {
            e.printStackTrace ();
        }
    }

    String getAlbum ()
    {
        return album;
    }

    String getArtist ()
    {
        return artist;
    }

    String getTitle ()
    {
        return title;
    }

    String getTrack ()
    {
        return track;
    }

    String getYear ()
    {
        return year;
    }
}
