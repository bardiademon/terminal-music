package bardiademon.Music;

import bardiademon.Music.Database.Database;
import bardiademon.Music.Database.Db;
import bardiademon.Music.Database.InfoDb;
import bardiademon.Music.Database.InfoDb.DFile.ListMusic;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class YourList
{

    private static final int PLAY = 1, DELETE = 2, ALL_LIST = 3;

    YourList ()
    {
        GetAllNameList getAllNameList = new GetAllNameList ();
        if (getAllNameList.isFound ())
        {
            new CreateOption ().create (getAllNameList.getJustName () , true);
            GetNumber getNumber = new GetNumber ();
            if (getNumber.isOk () && getNumber.getNumber () != CreateOption.OPTION_EXIT)
            {
                int idNameList = getAllNameList.getLists ().get (getNumber.getNumber () - 1).id;
                new CreateOption ().create (new String[]{"Play List" , "Delete" , "All List"} , true);
                getNumber = new GetNumber ();
                if (getNumber.isOk ())
                {
                    switch (getNumber.getNumber ())
                    {
                        case PLAY:
                            playList (idNameList);
                            return;
                        case DELETE:
                            deleteList (idNameList);
                            return;
                        case ALL_LIST:
                        case CreateOption.OPTION_EXIT:
                            new YourList ();
                            return;
                    }
                }
            }
            else System.err.println ("\nError");
        }
        else System.err.println ("\nList Not Found");
        Music.titleAndNumber ();
    }

    static void playList (int id)
    {
        GetList getList = new GetList (id);
        if (getList.isFound ())
        {
            Music.openMusic = new OpenMusic (false);
            Music.openMusic.setListFile (getList.getMusic ());
            System.out.println ("\nFound Music " + getList.getMusic ().size ());
            Music.indexNowMusic = 0;
            Music.playMusic (0);
            Last.SetLastList (id);
        }
        else System.err.println ("\rThis List Empty");
    }

    private void deleteList (int id)
    {
        if (new DeleteList (id).isDeleted ()) System.out.println ("\n==== Deleted ====");
        else System.out.println ("\nNot Deleted");
        new YourList ();
    }

    private class GetAllNameList implements Db
    {
        private boolean found;
        private List<GetAllNameList.YList> lists;

        GetAllNameList ()
        {
            GetInsertUpdate ();
        }

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
                    lists = new ArrayList<> ();
                    YList yList;
                    while (resultSet.next ())
                    {
                        yList = new YList ();
                        yList.id = resultSet.getInt (InfoDb.DFile.NameListMusic.ID);
                        yList.name = resultSet.getString (InfoDb.DFile.NameListMusic.NAME);
                        lists.add (yList);
                    }
                    found = true;
                }
            }
            catch (SQLException ignored)
            {
            }
            finally
            {
                Database.CloseSR (statement , resultSet);
            }
        }

        @Override
        public String Query ()
        {
            return String.format ("SELECT * FROM \"%s\"" , InfoDb.DFile.NameListMusic.NAME_TABLE);
        }

        boolean isFound ()
        {
            return found;
        }

        private class YList
        {
            int id;
            String name;
        }

        List<YList> getLists ()
        {
            return lists;
        }

        String[] getJustName ()
        {
            if (!isFound ()) return new String[]{};
            int len = lists.size ();
            String[] name = new String[len];
            for (int i = 0; i < len; i++) name[i] = lists.get (i).name;
            return name;
        }
    }

    private static class GetList implements Db
    {
        private int id;
        private boolean found;
        private List<File> music;

        GetList (int IdName)
        {
            id = IdName;
            GetInsertUpdate ();
        }

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
                    music = new ArrayList<> ();
                    while (resultSet.next ())
                        music.add (new File (resultSet.getString (ListMusic.PATH)));
                    found = true;
                }
            }
            catch (SQLException ignored)
            {
            }
            finally
            {
                Database.CloseSR (statement , resultSet);
            }
        }

        @Override
        public String Query ()
        {
            return String.format ("SELECT \"%s\" FROM \"%s\" WHERE \"%s\"=%d"
                    , ListMusic.PATH , ListMusic.NAME_TABLE , ListMusic.ID_NAME , id);
        }

        boolean isFound ()
        {
            return found;
        }

        public List<File> getMusic ()
        {
            return music;
        }
    }

    private class DeleteList implements Db
    {
        private int id;
        private boolean deleted;

        DeleteList (int IdName)
        {
            id = IdName;
            GetInsertUpdate ();
        }

        @Override
        public void GetInsertUpdate ()
        {
            Statement statement = null;
            try
            {
                statement = Music.ConnectionDbFile.get ().createStatement ();
                System.out.print ("Deleting...");
                statement.execute (Query ());
                statement.execute (query2 ());
                deleted = true;
            }
            catch (SQLException e)
            {
                e.printStackTrace ();
            }
            finally
            {
                Database.CloseSR (statement , null);
            }
        }

        @Override
        public String Query ()
        {
            return String.format ("DELETE FROM \"%s\" WHERE \"%s\"=%d" , ListMusic.NAME_TABLE , ListMusic.ID_NAME , id);
        }

        private String query2 ()
        {
            return String.format ("DELETE FROM \"%s\" WHERE \"%s\"=%d" , InfoDb.DFile.NameListMusic.NAME_TABLE , InfoDb.DFile.NameListMusic.ID , id);
        }

        boolean isDeleted ()
        {
            return deleted;
        }
    }
}
