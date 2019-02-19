package bardiademon.Music.Other;

public abstract class Str
{
    public static String MakeQueryCount (String Query)
    {
        int indexSelect = Query.indexOf ("SELECT");
        int indexFrom = Query.indexOf ("FROM");
        int indexStart = indexSelect + 6;
        int indexEnd = indexFrom - 1;
        String center = Query.substring (indexStart , indexEnd).trim ();
        return Query.substring (indexSelect , indexStart) + " COUNT(" + center + ") " + Query.substring (indexFrom);
    }
}
