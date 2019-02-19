package bardiademon.Music;

class CreateOption
{
    static final int OPTION_EXIT = 0;

    CreateOption ()
    {

    }

    CreateOption (String... Options)
    {
        create (Options);
    }

    void create (String[] options)
    {
        create (options , false);
    }

    void create (String[] options , boolean optionExit)
    {
        create (options , optionExit , true);
    }

    void create (String[] options , boolean optionExit , boolean breakNewLine)
    {
        System.out.println ();
        String str;
        if (optionExit) System.out.println (0 + ".Exit");
        for (int i = 0, len = options.length; i < len; i++)
        {
            str = (i + 1) + "." + options[i];
            if (breakNewLine) System.out.println (str);
            else
            {
                System.out.print (str);
                if (i + 1 < len) System.out.print (" ::: ");
            }
        }
        System.out.println ();
    }
}
