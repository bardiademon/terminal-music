package bardiademon.Music;

import bardiademon.Music.Database.Database;
import bardiademon.Music.Database.Db;
import bardiademon.Music.Database.InfoDb;
import bardiademon.Music.Database.InfoDb.DFile.NameListMusic;
import bardiademon.Music.Interface.bardiademon;
import bardiademon.Music.Other.GetInput;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@bardiademon
class CreateList
{

    private static final int NEW = 1, BACK = 2;

    @bardiademon
    CreateList ()
    {
        runClass ();
    }

    @bardiademon
    private void runClass ()
    {
        print ("\n----------------------");
        print ("\n-------- List --------");
        print ("\n----------------------\n");
        options ();
    }

    @bardiademon
    private void options ()
    {
        new CreateOption ("New" , "Back");
        GetNumber getNumber = new GetNumber ();
        if (getNumber.isOk ())
        {
            switch (getNumber.getNumber ())
            {
                case NEW:
                    newList ();
                    break;
                default:
                case BACK:
                    Music._Music.titleAndNumber ();
                    break;
            }
        }
        else Music._Music.titleAndNumber ();
    }

    @bardiademon
    private void newList ()
    {
        String nameList;
        while (true)
        {
            nameList = GetInput.GetString ("Please Enter Name List: ");
            if (new CheckExistsName (nameList).isExists ())
            {
                GetNumber getNumber = new GetNumber ("This Name Is Exists <1.TryAgain || 2.Exit> ? 1 Or 2");
                if (!getNumber.isOk () || getNumber.getNumber () != 1)
                {
                    runClass ();
                    return;
                }
            }
            else break;
        }
        System.out.print ("Open Music: ");
        OpenMusic openMusic = new OpenMusic ();
        if (openMusic.isOk ())
        {
            new SaveNew (nameList , openMusic.getListFile ());
        }
        else print ("\nError\n");
        runClass ();

    }

    @bardiademon
    private void print (String str)
    {
        System.out.print (str);
    }


    @bardiademon
    private class SaveNew implements Db
    {

        private List<File> files;
        private String nameList;

        @bardiademon
        SaveNew (String NameList , List<File> Files)
        {
            this.files = Files;
            this.nameList = NameList;
            GetInsertUpdate ();
        }

        @bardiademon
        @Override
        public void GetInsertUpdate ()
        {
            Statement statement = null;
            try
            {
                statement = Music.ConnectionDbFile.get ().createStatement ();
                /* Save Name */
                statement.execute (Query ());
                statement.close ();

                Music.ResetDatabase ();
                Object resultGetValue = Database.Query.GetValue (NameListMusic.NAME_TABLE , NameListMusic.ID , NameListMusic.NAME , nameList , false , 0);
                if (resultGetValue == null)
                {
                    System.err.println ("\nError\n");
                    return;
                }
                int idNameList = (int) resultGetValue;
                if (idNameList == 0) throw new SQLException ("No Save Name");
                else
                {
                    statement = Music.ConnectionDbFile.get ().createStatement ();
                    for (File file : files) statement.execute (querySavePath (idNameList , file));

                    print ("\nSave New List <" + nameList + ">.\n");
                }
            }
            catch (SQLException e)
            {
                print ("\nError <" + e.getMessage () + ">.\n");
            }
            finally
            {
                Database.CloseSR (statement , null);
            }
        }

        @bardiademon
        @Override
        public String Query ()
        {
            return String.format ("INSERT INTO \"%s\"(\"%s\") VALUES ('%s')" , NameListMusic.NAME_TABLE , NameListMusic.NAME , nameList);
        }

        @bardiademon
        private String querySavePath (int idName , File file)
        {
            StringBuilder query;
            query = new StringBuilder (String.format ("INSERT INTO \"%s\"(\"%s\",\"%s\") VALUES "
                    , InfoDb.DFile.ListMusic.NAME_TABLE , InfoDb.DFile.ListMusic.PATH , InfoDb.DFile.ListMusic.ID_NAME));

            query.append (String.format ("('%s',%d)" , file , idName));
            return query.toString ();
        }
    }

    @bardiademon
    private class CheckExistsName implements Db
    {

        private String name;
        private boolean exists;

        @bardiademon
        CheckExistsName (String Name)
        {
            this.name = Name;
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
                exists = (Database.GetCountSelectedRow (Music.ConnectionDbFile.get () , query) > 0);
            }
            catch (SQLException e)
            {
                exists = false;
            }
            finally
            {
                Database.CloseSR (statement , resultSet);
            }
        }

        @bardiademon
        @Override
        public String Query ()
        {
            return String.format ("SELECT \"%s\" FROM \"%s\" WHERE \"%s\"='%s'" , NameListMusic.ID , NameListMusic.NAME_TABLE , NameListMusic.NAME , name);
        }

        boolean isExists ()
        {
            return exists;
        }
    }

}
