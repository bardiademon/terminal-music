package bardiademon.Music;

import java.io.IOException;

public class ClearTerminal extends Thread
{
    ClearTerminal ()
    {
//        start ();
    }

    @Override
    public void run ()
    {
        String code;
        String osName = System.getProperty ("os.name");
        if (osName.equals ("Windows")) code = "cls";
        else code = "clear";
        try
        {
            Runtime.getRuntime ().exec (code);
        }
        catch (IOException ignored)
        {
        }
    }
}
