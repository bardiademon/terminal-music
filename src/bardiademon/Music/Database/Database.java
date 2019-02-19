package bardiademon.Music.Database;

import bardiademon.Music.Music;
import bardiademon.Music.Other.Str;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

public class Database
{
    private Connection connection;

    private String nameDatabase;

    public Database (String NameDatabase)
    {
        this.nameDatabase = NameDatabase;
        connect ();
    }

    private void connect ()
    {
        try
        {
            Class.forName ("org.sqlite.JDBC");
            connection = DriverManager.getConnection ("jdbc:sqlite:" + InfoDb.ADDRESS_DATABASE + nameDatabase);
        }
        catch (SQLException | ClassNotFoundException e)
        {
            System.out.print (e.getMessage ());
            connection = null;
        }
    }

    public boolean isConnected ()
    {
        try
        {
            return (connection != null && !connection.isClosed ());
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public Connection get ()
    {
        return connection;
    }

    public void close ()
    {
        if (connection != null)
        {
            try
            {
                connection.close ();
                connection = null;
            }
            catch (SQLException ignored)
            {
            }
        }
    }

    public static Integer GetCountSelectedRow (Connection _Connection , String QuerySelect)
    {
        try
        {
            return (_Connection.createStatement ().executeQuery (Str.MakeQueryCount (QuerySelect)).getInt (1));
        }
        catch (SQLException e)
        {
            return 0;
        }
    }


    public static void CloseSR (Statement _Statement , ResultSet _ResultSet)
    {
        try
        {
            if (_Statement != null) _Statement.close ();
            if (_ResultSet != null) _ResultSet.close ();
        }
        catch (SQLException ignored)
        {
        }
    }

    public static class Query
    {
        public static Object GetValue (String NameTable , String NameRowGet , String NameRowTest , Object ValueTest , boolean ValueTestIsInt , Object TypeValueGet)
        {
            Object result = null;
            String query = String.format ("SELECT \"%s\" FROM \"%s\" WHERE \"%s\"=" , NameRowGet , NameTable , NameRowTest);
            if (ValueTestIsInt) query += ValueTest.toString ();
            else query += String.format ("'%s'" , ValueTest.toString ());
            try
            {
                Statement statement = Music.ConnectionDbFile.get ().createStatement ();
                ResultSet resultSet = statement.executeQuery (query);
                if (Database.GetCountSelectedRow (Music.ConnectionDbFile.get () , query) > 0)
                {
                    if (TypeValueGet instanceof Boolean) result = resultSet.getBoolean (NameRowGet);
                    else if (TypeValueGet instanceof Integer) result = resultSet.getInt (NameRowGet);
                    else if (TypeValueGet instanceof Long) result = resultSet.getLong (NameRowGet);
                    else if (TypeValueGet instanceof Short) result = resultSet.getShort (NameRowGet);
                    else result = resultSet.getString (NameRowGet);
                }
            }
            catch (SQLException ignored)
            {
            }
            return result;
        }
    }
}
