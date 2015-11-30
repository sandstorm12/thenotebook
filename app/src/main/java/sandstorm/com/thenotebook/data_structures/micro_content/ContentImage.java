package sandstorm.com.thenotebook.data_structures.micro_content;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class ContentImage extends AbstractContent
{
    File cache;

    public ContentImage(String data, File internalCacheDirectory)
    {
        try{
            Gson g = new Gson();

            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache");

            cache.mkdirs();

            cache = new File(internalCacheDirectory.toString()+"//NewEditNoteCache//"+Long.toString(System.currentTimeMillis()));

            FileOutputStream fos = new FileOutputStream(cache);

            Log.d("myData","bad data : "+data);

            data = data.replaceAll(" ","+");
            fos.write(Base64.decode(data,0));

            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ContentImage(byte[] data, File internalCacheDirectory)
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
        return AbstractContent.TYPE_IMAGE;
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

    public Uri getImageUri()
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

    public Bitmap getThumbnail(int width, int height)
    {
        try{

            File f = new File("/storage/emulated/0/The Notebook");

            f.mkdirs();

            f = new File("/storage/emulated/0/The Notebook"+"/temp.png");

            if(f.exists()){
                f.delete();
            }
            f.createNewFile();

            FileOutputStream fos = new FileOutputStream(f);

            fos.write(getDataBytes());

            fos.close();

            BitmapFactory.Options bf = new BitmapFactory.Options();
            bf.inSampleSize = 4;

            return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(f.toString(),bf),width,height);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getString()
    {
        try{
            return Base64.encodeToString(getDataBytes(),Base64.NO_WRAP);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Class getTypeClass()
    {
        try{
            return ContentImage.class;
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
