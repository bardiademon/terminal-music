package bardiademon.Music.Database;

import bardiademon.Music.Interface.bardiademon;

import java.io.File;

@bardiademon
public class InfoDb
{
    private static final String TYPE = ".db";
    static final String ADDRESS_DATABASE = System.getProperty ("user.dir") + File.separator + "Database" + File.separator;

    @bardiademon
    public static class PublicRow
    {
        public static final String ID = "id";
    }

    @bardiademon
    public static class DFile
    {
        public static final String NAME_DATABASE = "File" + TYPE;

        @bardiademon
        public static class Last extends PublicRow
        {
            public static final String NAME_TABLE = "last",
                    LAST_CHOOSER_FILE = "last_chooser_file", ID_LAST_LIST = "id_last_list", PATH_LAST_MUSIC = "path_last_music";
        }

        @bardiademon
        public static class ListMusic extends PublicRow
        {
            public static final String NAME_TABLE = "list_music", PATH = "path", ID_NAME = "id_name";
        }

        @bardiademon
        public static class NameListMusic extends ListMusic
        {
            public static final String NAME_TABLE = "name_" + ListMusic.NAME_TABLE, NAME = "name";
        }
    }


}
