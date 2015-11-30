package sandstorm.com.thenotebook.data_structures.micro_content;

import android.net.Uri;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ContentSound extends AbstractContent
{
    File cache;

    public ContentSound(String data, File internalCacheDirectory)
    {
        try{
            Gson g = new Gson();

            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache");

            cache.mkdirs();

            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache//"+Long.toString(System.currentTimeMillis()));

            FileOutputStream fos = new FileOutputStream(cache);

            data = data.replaceAll(" ","+");
            fos.write(Base64.decode(data,0));

            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ContentSound(byte[] data, File internalCacheDirectory)
    {
        try{
            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache");

            cache.mkdirs();

            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache//"+Long.toString(System.currentTimeMillis()));

            FileOutputStream fos = new FileOutputStream(cache);

            fos.write(data);

            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getSize() {
        try{
            return (int)cache.length();
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public String getType() {
        return AbstractContent.TYPE_SOUND;
    }

    public byte[] getDataBytes()
    {
        try{
            FileInputStream fis = new FileInputStream(cache);

            byte[] data = new byte[fis.available()];

            fis.read(data);

            return data;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Uri getSoundUri()
    {
        try{
            File f = new File("/storage/emulated/0/The Notebook");

            f.mkdirs();

            f = new File("/storage/emulated/0/The Notebook/cache.tn");

            if(f.exists()){
                f.delete();
            }
            f.createNewFile();

            FileOutputStream fos = new FileOutputStream(f);

            fos.write(getDataBytes());

            fos.close();

            return Uri.fromFile(f);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getString()
    {
        try{
            return Base64.encodeToString(getDataBytes(), 0);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Class getTypeClass()
    {
        try{
            return ContentSound.class;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public File getFile()
    {
        return this.cache;
    }
}
