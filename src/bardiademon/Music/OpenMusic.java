package bardiademon.Music;

import bardiademon.Music.Database.Database;
import bardiademon.Music.Database.Db;
import bardiademon.Music.Database.InfoDb;
import bardiademon.Music.Interface.bardiademon;

import javax.swing.JFileChooser;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@bardiademon
class OpenMusic
{
    private List<File> listFile;

    private GetLastChooserFile getLastChooserFile;

    private boolean ok;

    private File[] files;
    private File file;

    @bardiademon
    OpenMusic ()
    {
        this (true);
    }

    @bardiademon
    OpenMusic (boolean Open)
    {
        if (Open) getLastChooserFile = new GetLastChooserFile (this::choose);
    }

    @bardiademon
    private void choose ()
    {
        JFileChooser chooser;
        try
        {
            chooser = new JFileChooser (getLastChooserFile.lastFile);
        }
        catch (NullPointerException e)
        {
            chooser = new JFileChooser ();
        }
        chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog (null) == JFileChooser.OPEN_DIALOG)
        {
            if ((file = chooser.getSelectedFile ()) == null) files = chooser.getSelectedFiles ();
            listFile = new ArrayList<> ();
            afterOk ();
            ok = true;
        }
    }

    @bardiademon
    public class GetLastChooserFile implements Db
    {

        private File lastFile = null;

        private Db.Callback callback;

        @bardiademon
        GetLastChooserFile (Db.Callback Callback)
        {
            this.callback = Callback;
            GetInsertUpdate ();
        }

        @bardiademon
        @Override
        public void GetInsertUpdate ()
        {
            Statement statement = null;
            ResultSet resultSet = null;
            try
            {
                statement = Music.ConnectionDbFile.get ().createStatement ();
                String query = Query ();
                resultSet = statement.executeQuery (query);
                if (Database.GetCountSelectedRow (Music.ConnectionDbFile.get () , query) > 0)
                {
                    lastFile = new File (resultSet.getString (InfoDb.DFile.Last.LAST_CHOOSER_FILE));
                    if (!lastFile.exists ()) lastFile = null;
                }
            }
            catch (SQLException ignored)
            {
            }
            finally
            {
                Database.CloseSR (statement , resultSet);
                callback.AfterOk ();
            }
        }

        @bardiademon
        @Override
        public String Query ()
        {
            return String.format ("SELECT \"%s\" FROM `%s`" , InfoDb.DFile.Last.LAST_CHOOSER_FILE , InfoDb.DFile.Last.NAME_TABLE);
        }
    }

    private void afterOk ()
    {
        System.out.println ("\nWaiting For Found All Music");
        FindAllFile.CallBack callBack = new FindAllFile.CallBack ()
        {
            @Override
            public void FindTimeFile (long NumberOfFileFind , FindAllFile.Find FileFind)
            {
                for (String type : Music.TYPE_MUSIC)
                {
                    if (FileFind.typeFile.equals (type))
                    {
                        listFile.add (FileFind.file);
                        System.out.print ("\rFound File Music: " + NumberOfFileFind);
                        break;
                    }
                }
            }

            @Override
            public void FindTimeDir (long NumberOfDirFind , File DirFind)
            {

            }

            @Override
            public void FindTimeFileOrDir (long NumberOfFileFind , long NumberOfDirFind , String FindFileOrDir , FindAllFile.Find FileFind , File DirFind)
            {

            }

            @Override
            public void AfterFind (long NumberOfFileFind , long NumberOfDirFind , List<FindAllFile.Find> FindList)
            {

            }

            @Override
            public void Error (int ErrorType)
            {

            }
        };
        if (file != null) new FindAllFile (callBack , file);
        else new FindAllFile (callBack , files);
    }

    void setListFile (List<File> listFile)
    {
        ok = true;
        this.listFile = listFile;
    }

    void setOneMusic (String pathMusic)
    {
        listFile = new ArrayList<> ();
        listFile.add (new File (pathMusic));
        setListFile (listFile);
    }

    List<File> getListFile ()
    {
        return listFile;
    }

    boolean isOk ()
    {
        return ok;
    }
}
