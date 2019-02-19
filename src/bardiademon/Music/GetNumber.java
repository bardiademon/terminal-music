package bardiademon.Music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class GetNumber
{
    private String message;

    private int number = 0;
    private boolean ok;

    GetNumber ()
    {
        this ("Input: ");
    }

    GetNumber (String Message)
    {
        this.message = Message;
        get ();
    }

    private void get ()
    {
        try
        {
            BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));
            System.out.print (message);
            Music.getInput = true;
            String line = reader.readLine ();
            Music.getInput = false;
            if (line != null && !line.equals ("") && line.matches ("[0-9]*"))
            {
                number = Integer.parseInt (line);
                ok = true;
            }
            else ok = false;
        }
        catch (IOException e)
        {
            ok = false;
        }
    }

    boolean isOk ()
    {
        return ok;
    }

    int getNumber ()
    {
        return number;
    }
}
