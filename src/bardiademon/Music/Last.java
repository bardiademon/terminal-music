package bardiademon.Music;

import bardiademon.Music.Database.Database;
import bardiademon.Music.Database.Db;
import bardiademon.Music.Database.InfoDb.DFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract class Last
{
    static boolean LastMusic ()
    {
        class GetLastMusic implements Db
        {

            private GetLastMusic ()
            {
                GetInsertUpdate ();
            }

            private String pathMusic;
            private boolean found;

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
                        pathMusic = resultSet.getString (DFile.Last.PATH_LAST_MUSIC);
                        found = true;
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace ();
                }
                finally
                {
                    Database.CloseSR (statement , resultSet);
                }
            }

            @Override
            public String Query ()
            {
                return String.format ("SELECT \"%s\" FROM \"%s\"" , DFile.Last.PATH_LAST_MUSIC , DFile.Last.NAME_TABLE);
            }
        }

        GetLastMusic getLastMusic = new GetLastMusic ();
        if (getLastMusic.found)
        {
            Music.openMusic = new OpenMusic (false);
            Music.openMusic.setOneMusic (getLastMusic.pathMusic);
            Music.playMusic (0);
            return true;
        }
        else return false;
    }

    static boolean LastList ()
    {
        class GetLastList implements Db
        {
            private GetLastList ()
            {
                GetInsertUpdate ();
            }

            private int id;
            private boolean found;

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
                        id = resultSet.getInt (DFile.Last.ID_LAST_LIST);
                        found = true;
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace ();
                }
                finally
                {
                    Database.CloseSR (statement , resultSet);
                }
            }

            @Override
            public String Query ()
            {
                return String.format ("SELECT \"%s\" FROM \"%s\"" , DFile.Last.ID_LAST_LIST , DFile.Last.NAME_TABLE);
            }
        }

        GetLastList getLastList = new GetLastList ();
        if (getLastList.found)
        {
            YourList.playList (getLastList.id);
            return true;
        }
        else return false;
    }

    static void SetLastMusic (String Path)
    {
        try
        {
            Statement statement = Music.ConnectionDbFile.get ().createStatement ();
            statement.execute (String.format ("UPDATE \"%s\" SET \"%s\"='%s';" , DFile.Last.NAME_TABLE , DFile.Last.PATH_LAST_MUSIC , Path));
            Database.CloseSR (statement , null);
            Music.ResetDatabase ();
        }
        catch (SQLException ignored)
        {
        }
    }

    static void SetLastList (int IdNameMusic)
    {
        try
        {
            Statement statement = Music.ConnectionDbFile.get ().createStatement ();
            statement.execute (String.format ("UPDATE \"%s\" SET \"%s\"=%d;" , DFile.Last.NAME_TABLE , DFile.Last.ID_LAST_LIST , IdNameMusic));
            Database.CloseSR (statement , null);
            Music.ResetDatabase ();
        }
        catch (SQLException ignored)
        {
        }
    }
}
