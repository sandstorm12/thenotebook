package sandstorm.com.thenotebook.functions;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.AbstractNoteNotebook;
import sandstorm.com.thenotebook.data_structures.HeavyNote;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.Notebook;
import sandstorm.com.thenotebook.data_structures.micro_content.AbstractContent;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentFile;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentImage;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentSound;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentVideo;
import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;
import util.IabHelper;
import util.IabResult;

import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeWriter;

public class Functions
{
    static boolean output;

//    public static String convertNoteContentToJsonString(NoteContent content)
//    {
//        try{
//            JSONObject root = new JSONObject();
//            JSONArray noteArray = new JSONArray();
//
//            root.put("reminder",content.getReminder().getTimeInMillis());
//            root.put("repeat",content.getRepeat());
//            root.put("title",content.getTitle());
//
//            for(int i = 0; i < content.getContentSize(); i++)
//            {
//                Runtime.getRuntime().gc();
//                switch(content.getContent(i).getType())
//                {
//                    case AbstractContent.TYPE_TEXT :
//                        JSONObject temp = new JSONObject();
//                        temp.put("type",AbstractContent.TYPE_TEXT);
//                        temp.put("content",((ContentText)content.getContent(i)).getText());
//                        noteArray.put(i,temp);
//                        break;
//                    case AbstractContent.TYPE_FILE :
//                    case AbstractContent.TYPE_IMAGE :
//                        JSONObject tempI = new JSONObject();
//                        tempI.put("type",AbstractContent.TYPE_IMAGE);
//                        tempI.put("content",((ContentImage)content.getContent(i)).toString());
//                        noteArray.put(i,tempI);
//                        break;
//                    case AbstractContent.TYPE_SOUND :
//                    case AbstractContent.TYPE_VIDEO :
//                }
//            }
//
//            root.put("data",noteArray);
//
//            return root.toString();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static File convertHeavyNoteContentToJsonFile(NoteContent content, Context context)
    {
        try{
            Log.d("myData","going to save image 1");
            File f = new File(context.getCacheDir()+"//"+Long.toString(System.currentTimeMillis()));
            FileWriter fw = new FileWriter(f);

            com.google.gson.stream.JsonWriter jw = new com.google.gson.stream.JsonWriter(fw);

            jw.beginObject();

            jw.name("reminder").value(content.getReminder().getTimeInMillis());
            jw.name("repeat").value(content.getRepeat());
            jw.name("title").value(Base64.encodeToString(content.getTitle().getBytes("UTF-8"), 0));

            jw.name("data").beginArray();

            for(int i = 0; i < content.getContentSize(); i++)
            {
                jw.beginObject();
                switch(content.getContent(i).getType())
                {
                    case AbstractContent.TYPE_TEXT :
                        jw.name("type").value(AbstractContent.TYPE_TEXT);
                        jw.name("content").value(((ContentText)content.getContent(i)).getTextEncoded());
                        break;
                    case AbstractContent.TYPE_IMAGE :
                        jw.name("type").value(AbstractContent.TYPE_IMAGE);
                        jw.name("content").value(((ContentImage)content.getContent(i)).getString());
                        break;
                    case AbstractContent.TYPE_SOUND :
                        jw.name("type").value(AbstractContent.TYPE_SOUND);
                        jw.name("content").value(((ContentSound)content.getContent(i)).getString());
                        break;
                    case AbstractContent.TYPE_VIDEO :
                        jw.name("type").value(AbstractContent.TYPE_VIDEO);
                        jw.name("content").value(((ContentVideo)content.getContent(i)).getString());
                        break;
                    case AbstractContent.TYPE_FILE :
                        jw.name("type").value(AbstractContent.TYPE_FILE);
                        jw.name("content").value(((ContentFile)content.getContent(i)).getString());
                        break;
                }
                jw.endObject();
            }

            jw.endArray();
            jw.endObject();

            jw.close();

            return f;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static NoteContent convertJsonStringToHeavyNoteContent(String fileAddress,Context context)
    {
        try{
            File source = new File(fileAddress);
            FileInputStream fis = new FileInputStream(source);

            File cache = new File(context.getCacheDir()+"//"+Long.toString(System.currentTimeMillis()));
            cache.createNewFile();
            FileOutputStream fos = new FileOutputStream(cache);

            byte[] buffer = new byte[1000];
            int read = 0;

            while((read = fis.read(buffer)) != -1)
            {
                fos.write(buffer,0,read);
            }

            fis.close();
            fos.close();

            FileReader fr = new FileReader(cache);

            com.google.gson.stream.JsonReader jr = new com.google.gson.stream.JsonReader(fr);

            NoteContent nc = new NoteContent();

            jr.beginObject();

            while(jr.hasNext()) {
                switch (jr.nextName()) {
                    case "reminder":
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(jr.nextLong());
                        nc.setReminder(c);
                        break;
                    case "repeat":
                        nc.setRepeat(jr.nextLong());
                        break;
                    case "title":
                        nc.setTitle(new String(Base64.decode(jr.nextString(),0),"UTF-8"));
                        break;
                    case "data":
                        jr.beginArray();
                        for (int i = 0; jr.hasNext(); i++) {
                            jr.beginObject();
                            jr.nextName();
                            switch(jr.nextString())
                            {
                                case AbstractContent.TYPE_TEXT :
                                    jr.nextName();
                                    ContentText ct = new ContentText(new String(Base64.decode(jr.nextString().replaceAll(" ","+"),0)));
                                    nc.addContent(ct);
                                    break;
                                case AbstractContent.TYPE_IMAGE :
                                    jr.nextName();
                                    ContentImage ci = new ContentImage(jr.nextString(),context.getCacheDir());
                                    nc.addContent(ci);
                                    break;
                                case AbstractContent.TYPE_SOUND :
                                    jr.nextName();
                                    ContentSound cs = new ContentSound(jr.nextString(),context.getCacheDir());
                                    nc.addContent(cs);
                                    break;
                                case AbstractContent.TYPE_VIDEO :
                                    jr.nextName();
                                    ContentVideo cv = new ContentVideo(jr.nextString(),context.getCacheDir());
                                    nc.addContent(cv);
                                    break;
                                case AbstractContent.TYPE_FILE :
                                    jr.nextName();
                                    ContentFile cf = new ContentFile(jr.nextString(),context.getCacheDir());
                                    nc.addContent(cf);
                                    break;
                            }
                            jr.endObject();
                        }
                        break;
                }
            }

            jr.endArray();
            jr.endObject();

            cache.delete();

            return nc;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static NoteContent convertJsonStringToNoteContent(String fileAddress, Context context)
    {
        try{
            File source = new File(fileAddress);
            FileInputStream fis = new FileInputStream(source);

            File cache = new File(context.getCacheDir()+"//"+Long.toString(System.currentTimeMillis()));
            cache.createNewFile();
            FileOutputStream fos = new FileOutputStream(cache);

            byte[] buffer = new byte[1000];
            int read = 0;

            while((read = fis.read(buffer)) != -1)
            {
                fos.write(buffer);
            }

            fis.close();
            fos.close();

            FileReader fr = new FileReader(cache);

            com.google.gson.stream.JsonReader jr = new com.google.gson.stream.JsonReader(fr);

            NoteContent nc = new NoteContent();

            jr.beginObject();

            while(jr.hasNext()) {
                switch (jr.nextName()) {
                    case "reminder":
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(jr.nextLong());
                        nc.setReminder(c);
                        break;
                    case "repeat":
                        nc.setRepeat(jr.nextLong());
                        break;
                    case "title":
                        nc.setTitle(new String(Base64.decode(jr.nextString(),0),"UTF-8"));
                        break;
                    case "data":
                        jr.beginArray();
                        for (int i = 0; jr.hasNext(); i++) {
                            jr.beginObject();
                            jr.nextName();
                            if (jr.nextString().equalsIgnoreCase(AbstractContent.TYPE_TEXT)) {
                                jr.nextName();
                                ContentText ct = new ContentText(new String(Base64.decode(jr.nextString().replaceAll(" ","+"),0)));
                                nc.addContent(ct);
                            }else{
                                jr.nextName();
                                jr.nextString();
                            }
                            jr.endObject();
                        }
                        break;
                }
            }

            jr.endArray();
            jr.endObject();

            cache.delete();

            return nc;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String convertNotebookNameToJsonString(String name)
    {
        try {
            JSONObject json = new JSONObject();
            json.put("name",name);

            return json.toString();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String convertJsonStringToNotebookName(String jsonString)
    {
        try{
            JSONObject json = new JSONObject(jsonString);

            return json.getString("name");
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteFilesInDirectory(File directory)
    {
        try{
            File[] f = directory.listFiles();

            if(f == null) return;

            for(int i = 0; i < f.length; i++)
            {
                f[i].delete();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static AbstractNoteNotebook getAbstractNoteNotebookServerData(File cache,Context context)
    {
        try{
            FileReader fr = new FileReader(cache);
            BufferedReader br = new BufferedReader(fr);

            AbstractNoteNotebook ann = new AbstractNoteNotebook();
            ann.setId(Long.parseLong(br.readLine()));
            ann.setNotebookId(Long.parseLong(br.readLine()));
            ann.setVersionCode(Long.parseLong(br.readLine()));
            ann.setStatus(Integer.parseInt(br.readLine()));

            Log.d("myData","note info : notebook ==> "+Long.toString(ann.getNotebookId())+" status ==> "+Integer.toString(ann.getStatus()));

            if(Integer.parseInt(br.readLine()) == AbstractNoteNotebook.TYPE_NOTEBOOK)
            {
                Notebook n = new Notebook(ann.getId(),ann.getNotebookId(),ann.getVersionCode(),ann.getStatus());

                n.setName(convertJsonStringToNotebookName(br.readLine()));
                n.setIsSynced(1);

                return n;
            }else{
                ann.setType(AbstractNoteNotebook.TYPE_NOTE);
                HeavyNote n = new HeavyNote();

                n.setId(ann.getId());
                n.setNotebookId(ann.getNotebookId());
                n.setVersionCode(ann.getVersionCode());
                n.setStatus(ann.getStatus());

                File f = new File(context.getCacheDir()+"//sync_content_cache");
                if(f.exists()){
                    f.delete();
                }f.createNewFile();
                FileWriter fw = new FileWriter(f);
                fw.write(br.readLine());
                fw.close();

                n.setContent(convertJsonStringToHeavyNoteContent(f.toString(),context));
                n.setIsSynced(1);

                Log.d("myData","before return status : "+Integer.toString(n.getStatus()));

                return n;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isConnectedToInternet(Context context)
    {
        try{
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() == null){
                return false;
            }else{
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void copyFile(File origin, File destination)
    {
        try{
            FileInputStream fis = new FileInputStream(origin);
            FileOutputStream fos = new FileOutputStream(destination);

            byte[] buffer = new byte[1000];
            int read = 0;

            while((read = fis.read(buffer)) != -1){
                fos.write(buffer,0,read);
            }

            fis.close();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
