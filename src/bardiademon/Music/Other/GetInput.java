package bardiademon.Music.Other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetInput
{
    public static String GetString ()
    {
        return GetString ("Get Str: ");
    }

    public static String GetString (String Message)
    {
        try
        {
            System.out.print (Message);
            return ((new BufferedReader (new InputStreamReader (System.in))).readLine ());
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
