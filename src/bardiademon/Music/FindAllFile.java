package bardiademon.Music;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindAllFile
{
    private List<Find> findList;
    private boolean error;
    private int typeError;

    private long numberOfFileFind, numberOrDirFind;

    private CallBack callBack;

    public FindAllFile (CallBack CallBack , File Address)
    {
        setCallBack(CallBack);
        numberOrDirFind = 0;
        numberOfFileFind = 0;
        findList = new ArrayList<> ();
        runClass(Address);
        callBack.AfterFind(numberOfFileFind , numberOrDirFind , findList);
    }

    public FindAllFile (CallBack CallBack , File[] Address)
    {
        numberOrDirFind = 0;
        numberOfFileFind = 0;
        findList = new ArrayList<> ();
        setCallBack(CallBack);
        for (File address : Address) runClass(address);
        callBack.AfterFind(numberOfFileFind , numberOrDirFind , findList);
    }

    private void runClass (File Address)
    {
        if (Address.isFile())
        {
            Find find = new Find(Address);
            findList.add(find);
            numberOfFileFind++;
            callBack.FindTimeFile(numberOfFileFind , find);
            return;
        }
        find(Address);
    }

    public void setCallBack (CallBack callBack)
    {
        this.callBack = callBack;
    }

    private void find (File dir)
    {
        File[] files = dir.listFiles();
        if (files != null && files.length > 0)
        {
            for (File fileOrDir : files)
            {
                if (fileOrDir.isDirectory())
                {
                    numberOrDirFind++;
                    callBack.FindTimeDir(numberOrDirFind , fileOrDir);
                    find(fileOrDir);
                    callBack.FindTimeFileOrDir(numberOfFileFind , numberOrDirFind , CallBack.FindDir , null , fileOrDir);
                }
                else
                {
                    numberOfFileFind++;
                    Find find = new Find(fileOrDir);
                    findList.add(find);
                    callBack.FindTimeFile(numberOfFileFind , find);
                    callBack.FindTimeFileOrDir(numberOfFileFind , numberOrDirFind , CallBack.FindFile , find , null);
                }
            }
        }

    }

    private void setError (int error)
    {
        this.error = true;
        typeError = error;
    }

    public static class Find
    {
        public final String nameFile, typeFile, nameAndType;
        public final long size;

        public final File file;
        public final File dir;

        private Find (File file)
        {
            nameFile = FilenameUtils.getBaseName(file.getPath());
            typeFile = FilenameUtils.getExtension(file.getPath());
            nameAndType = FilenameUtils.getName(file.getPath());
            this.file = file;
            dir = file.getParentFile().getParentFile();
            size = file.getTotalSpace();
        }
    }

    public static abstract class ERROR
    {
        private static final int NOT_EXISTS = 0;
    }

    public interface CallBack
    {
        void FindTimeFile (long NumberOfFileFind , Find FileFind);

        void FindTimeDir (long NumberOfDirFind , File DirFind);

        void FindTimeFileOrDir (long NumberOfFileFind , long NumberOfDirFind , String FindFileOrDir , Find FileFind , File DirFind);

        String FindFile = "f_file", FindDir = "f_dir";

        void AfterFind (long NumberOfFileFind , long NumberOfDirFind , List<Find> FindList);

        void Error (int ErrorType);
    }
}
