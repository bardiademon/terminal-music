package bardiademon.Music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Progress
{
    static boolean startThread;
    private static Thread thread;
    private static boolean threadStart;
    private static boolean isBreak;

    Progress ()
    {
        if (thread == null && startThread && !Music.activeControl)
        {
            thread = new Thread (() ->
            {
                try
                {
                    threadStart = true;
                    new BufferedReader (new InputStreamReader (System.in)).readLine ();
                }
                catch (IOException ignored)
                {
                }
                finally
                {
                    thread = null;
                    threadStart = false;
                    isBreak = true;
                }
            });
        }
        if (thread != null && !threadStart) thread.start ();
        if (Music.activeControl) return;
        new Thread (() ->
        {
            while (!isBreak)
            {
                float d = ((float) Music.GetAvailable () / Music.GetAllAvailable ()) * 100;
                int d0 = (int) (100 - d);
                System.out.printf ("\r%d%%" , d0);
                if (Music.IsComplete () || Music.activeControl) break;
            }
            if (isBreak && !Music.getInput) Music._Music.titleAndNumber ();
            isBreak = false;
        }).start ();
    }
}
