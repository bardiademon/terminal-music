package bardiademon.Music.Database;

public interface Db
{
    void GetInsertUpdate ();

    String Query ();

    interface Callback
    {
        void AfterOk ();
    }
}
