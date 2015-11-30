package sandstorm.com.thenotebook.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.*;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.Buffer;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import sandstorm.com.thenotebook.data_structures.*;
import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;
import sandstorm.com.thenotebook.functions.Functions;

public class DataService extends Service
{
    //DEFINITIONS
    private static DataBundle dataBundle;
    private static SQLiteDatabase myDatabase;
    private Cursor c;
    private final int RESULT_EXIST_BUT_UPDATE = -1;
    private final int RESULT_NOT_EXISTS = -2;

//    CLASSES AND INTERFACES
    public class MyBinder extends Binder
    {
        public DataService getService()
        {
            return DataService.this;
        }
    }
    public interface OnLoadCompleteListener{
        public abstract void onLoadCompleted();
    }
    public interface OnSaveCompleteListener{
        public abstract void onSaveCompleted();
    }
    private static ArrayList<OnLoadCompleteListener> loadListener = new ArrayList();
    private static ArrayList<OnSaveCompleteListener> saveListener = new ArrayList();
    private static ArrayList<OnLoadCompleteListener> constantLoadListener = new ArrayList();
    private static ArrayList<OnSaveCompleteListener> constantSaveListener = new ArrayList();

//    METHODS
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.d("myData","data service started due to install app");
            if(myDatabase == null ) myDatabase = openOrCreateDatabase("DATABASE", MODE_PRIVATE, null);
            if (!myDatabase.isOpen()) myDatabase = openOrCreateDatabase("DATABASE", MODE_PRIVATE, null);
            if(dataBundle == null)
            {
                Log.d("myData","data bundle is null due to reinstall");
                loadData();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public synchronized void addOnLoadCompleteListener(OnLoadCompleteListener loadListener)
    {
        this.loadListener.add(loadListener);
    }

    public synchronized void addOnSaveCompleteListener(OnSaveCompleteListener saveListener)
    {
        this.saveListener.add(saveListener);
    }

    public synchronized void addConstantOnLoadCompleteListener(OnLoadCompleteListener loadListener)
    {
        this.constantLoadListener.add(loadListener);
    }

    public synchronized void addConstantOnSaveCompleteListener(OnSaveCompleteListener saveListener)
    {
        this.constantSaveListener.add(saveListener);
    }

    public synchronized DataBundle getCachedData()
    {
        return dataBundle;
    }

    /**
     * @serialData listeners not working if you add them after calling saveData()
     */
    public synchronized void saveData(DataBundle dataBundle)
    {
        try{
            this.dataBundle = dataBundle;
            saveDataInternal();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void saveDataInternal() {
        try {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS data(id INTEGER AUTO_INCREMENT, notebookId INTEGER AUTO_INCREMENT, versionCode INTEGER AUTO_INCREMENT, status INTEGER(1), type INTEGER(1), content TEXT AUTO_INCREMENT, isSynced INTEGER(1))");

            ArrayList<Notebook> notebookData = dataBundle.getNotebookBundle();
            for (int i = 0; i < notebookData.size(); i++) {
                int result = recordStatus(notebookData.get(i).getId(), notebookData.get(i).getVersionCode());

                if (result == RESULT_NOT_EXISTS) {
                    insertNotebookIntoDatabase(notebookData.get(i));
                } else if (result >= 0) {
                    updateNotebookIntoDatabase(notebookData.get(i));
                }
            }

            ArrayList<Note> noteData = dataBundle.getNoteBundle();
            for (int i = 0; i < noteData.size(); i++) {
                updateLightNote(noteData.get(i));
            }

            dispatchSave();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveHeavyNote(HeavyNote heavyNote)
    {
        try{
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS data(id INTEGER AUTO_INCREMENT, notebookId INTEGER AUTO_INCREMENT, versionCode INTEGER AUTO_INCREMENT, status INTEGER(1), type INTEGER(1), content TEXT AUTO_INCREMENT, isSynced INTEGER(1))");

            int result = recordStatus(heavyNote.getId(), heavyNote.getVersionCode());

            if (result == RESULT_NOT_EXISTS) {
                insertNoteIntoDatabase(heavyNote);
            } else if (result >= 0) {
                updateNoteIntoDatabase(heavyNote);
            }

            dispatchHeavySave(heavyNote.getId());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @serialData  listener not working if you add them after calling saveData()
     */
    public synchronized void loadData()
    {
        try{
            loadDataInternal();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void loadDataInternal() {
        try {
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS data(id INTEGER AUTO_INCREMENT, notebookId INTEGER AUTO_INCREMENT, versionCode INTEGER AUTO_INCREMENT, status INTEGER(1), type INTEGER(1), content TEXT AUTO_INCREMENT, isSynced INTEGER(1))");

                DataBundle temp = new DataBundle();

                temp.setNoteBundle(getNotes());
                temp.setNotebookBundle(getNotebooks());

                dataBundle = temp;
                dispatchLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized int recordStatus(long id, long versionCode)
    {
        try{
            c = myDatabase.rawQuery("SELECT id, versionCode FROM data WHERE id=\""+Long.toString(id)+"\"",null);
            c.moveToFirst();
            if(c.getCount() == 0) return this.RESULT_NOT_EXISTS;
            for(int i = 0; i < c.getCount(); i++)
            {
                if(c.getLong(1) != versionCode){
                    int result = c.getPosition();

                    c.close();

                    return result;
                }

                c.moveToNext();
            }
            c.close();
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return RESULT_EXIST_BUT_UPDATE;
    }

    private synchronized void insertNoteIntoDatabase(HeavyNote note)
   {
        try{
            FileInputStream fis = new FileInputStream(Functions.convertHeavyNoteContentToJsonFile(note.getContent(),getApplicationContext()));

            File content = new File(getFilesDir()+"//content//");
            content.mkdirs();
            content = new File(getFilesDir()+"//content//"+Long.toString(note.getId()));
            content.createNewFile();
            FileOutputStream fos = new FileOutputStream(content);

            byte[] temp = new byte[1000];
            int read = 0;

            while((read = fis.read(temp))!= -1)
            {
                fos.write(temp);
            }

            fis.close();
            fos.close();

            ContentValues cv = new ContentValues();
            cv.put("id",note.getId());
            cv.put("notebookId",note.getNotebookId());
            cv.put("versionCode",note.getVersionCode());
            cv.put("status",note.getStatus());
            cv.put("type", note.getType());
            cv.put("isSynced",note.getIsSynced());
            cv.put("content",content.toString());
            myDatabase.insert("data",null,cv);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void updateNotebookIntoDatabase(Notebook notebook)
    {
        try{
            myDatabase.execSQL("UPDATE data SET id="+Long.toString(notebook.getId())
                                                +",notebookId="+Long.toString(notebook.getNotebookId())
                                                +",versionCode="+Long.toString(notebook.getVersionCode())
                                                +",status="+Integer.toString(notebook.getStatus())
                                                +",type="+Integer.toString(notebook.getType())
                                                +",content=\'"+Functions.convertNotebookNameToJsonString(notebook.getName())
                                                +"\',isSynced="+Integer.toString(notebook.getIsSynced())
                                                +" WHERE id="+Long.toString(notebook.getId()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void insertNotebookIntoDatabase(Notebook notebook)
    {
        try{
            myDatabase.execSQL("INSERT INTO data(id,notebookId,versionCode,status,type,content,isSynced) VALUES("+NotebookToString(notebook)+")");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void updateNoteIntoDatabase(HeavyNote note)
    {
        try{
            FileInputStream fis = new FileInputStream(Functions.convertHeavyNoteContentToJsonFile(note.getContent(),getApplicationContext()));

            File content = new File(getFilesDir()+"//content//"+Long.toString(note.getId()));
            FileOutputStream fos = new FileOutputStream(content);

            byte[] temp = new byte[1000];
            int read = 0;

            while((read = fis.read(temp))!= -1)
            {
                fos.write(temp);
            }

            fis.close();
            fos.close();

            ContentValues cv = new ContentValues();
            cv.put("id", note.getId());
            cv.put("notebookId",note.getNotebookId());
            cv.put("versionCode",note.getVersionCode());
            cv.put("status",note.getStatus());
            cv.put("type",note.getType());
            cv.put("isSynced",note.getIsSynced());
            cv.put("content",content.toString());
            myDatabase.update("data",cv,"id="+note.getId(),null);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void updateLightNote(Note note)
    {
        try{
            ContentValues cv = new ContentValues();
            cv.put("id", note.getId());
            cv.put("notebookId",note.getNotebookId());
            cv.put("versionCode",note.getVersionCode());
            cv.put("status",note.getStatus());
            cv.put("isSynced",note.getIsSynced());
            cv.put("type",note.getType());
            myDatabase.update("data",cv,"id="+note.getId(),null);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void updateLightNoteOrNotebook(AbstractNoteNotebook ann)
    {
        try{
            ContentValues cv = new ContentValues();
            cv.put("id", ann.getId());
            cv.put("notebookId",ann.getNotebookId());
            cv.put("versionCode",ann.getVersionCode());
            cv.put("status",ann.getStatus());
            cv.put("isSynced",ann.getIsSynced());
            cv.put("type",ann.getType());
            myDatabase.update("data",cv,"id="+ann.getId(),null);

            dispatchSave();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

//    private synchronized String heavyNoteToString(HeavyNote note)
//    {
//        try{
//            String output = "";
//
//            output += Long.toString(note.getId())+",";
//            output += Long.toString(note.getNotebookId())+",";
//            output += Long.toString(note.getVersionCode())+",";
//            output += Integer.toString(note.getStatus())+",";
//            output += Integer.toString(note.getType())+",\'";
//            output += Functions.convertNoteContentToJsonString(note.getContent())+"\',";
//            output += Integer.toString(note.getIsSynced());
//
//            return output;
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    private synchronized String NotebookToString(Notebook notebook)
    {
        try{
            String output = "";

            output += Long.toString(notebook.getId())+",";
            output += Long.toString(notebook.getNotebookId())+",";
            output += Long.toString(notebook.getVersionCode())+",";
            output += Integer.toString(notebook.getStatus())+",";
            output += Integer.toString(notebook.getType())+",\'";
            output += Functions.convertNotebookNameToJsonString(notebook.getName())+"\',";
            output += Integer.toString(notebook.getIsSynced());

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private synchronized Note getNoteFromDatabase(long id)
    {
        try{
            c = myDatabase.rawQuery("SELECT notebookId,versionCode,status,type,content,isSynced FROM data WHERE id="+Long.toString(id),null);
            c.moveToFirst();

            if(Integer.parseInt(c.getString(3)) ==  AbstractNoteNotebook.TYPE_NOTEBOOK) return null;

            Note note = new Note();
            note.setId(id);
            note.setNotebookId(c.getLong(0));
            note.setVersionCode(c.getLong(1));
            note.setStatus(c.getInt(2));
            note.setContent(Functions.convertJsonStringToNoteContent(c.getString(4),getApplicationContext()));
            note.setIsSynced(c.getInt(5));

            c.close();
            return note;
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return null;
    }

    public synchronized HeavyNote getHeavyNoteFromDatabase(long id)
    {
        try{
            c = myDatabase.rawQuery("SELECT notebookId,versionCode,status,content,isSynced FROM data WHERE id="+Long.toString(id),null);
            c.moveToFirst();

            HeavyNote note = new HeavyNote();
            note.setId(id);
            note.setNotebookId(c.getLong(0));
            note.setVersionCode(c.getLong(1));
            note.setStatus(c.getInt(2));
            note.setContent(Functions.convertJsonStringToHeavyNoteContent(c.getString(3),getApplicationContext()));
            note.setIsSynced(c.getInt(4));

            c.close();

            return note;
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return null;
    }

    private synchronized Notebook getNotebookFromDatabase(long id)
    {
        try{
            c = myDatabase.rawQuery("SELECT notebookId,versionCode,status,type,content,isSynced FROM data WHERE id="+Long.toString(id),null);
            c.moveToFirst();

            if(Integer.parseInt(c.getString(3)) ==  AbstractNoteNotebook.TYPE_NOTE) return null;

            Notebook notebook = new Notebook();
            notebook.setId(id);
            notebook.setNotebookId(c.getLong(0));
            notebook.setVersionCode(c.getLong(1));
            notebook.setStatus(c.getInt(2));
            notebook.setName(Functions.convertJsonStringToNotebookName(c.getString(4)));
            notebook.setIsSynced(c.getInt(5));

            c.close();

            return notebook;
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return null;
    }

    private synchronized ArrayList<Note> getNotes()
    {
        try{
            c = myDatabase.rawQuery("SELECT id,notebookId,versionCode,status,content,isSynced FROM data WHERE type="+ AbstractNoteNotebook.TYPE_NOTE,null);
            c.moveToFirst();

            ArrayList<Note> notes = new ArrayList();

            for(int i = 0; i < c.getCount(); i++)
            {
                Note note = new Note(c.getLong(0),c.getLong(1),c.getLong(2),c.getInt(3),Functions.convertJsonStringToNoteContent(c.getString(4),getApplicationContext()),c.getInt(5));
                notes.add(note);
                Log.d("myData","in loadDataInternal is synced : "+Boolean.toString(note.isSynced())+" real saved parameter : "+Integer.toString(c.getInt(5)));
                c.moveToNext();
            }

            c.close();

            return notes;
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return null;
    }

    private synchronized void updateNotes(long newNoteId)
    {
        try{
            c = myDatabase.rawQuery("SELECT id,notebookId,versionCode,status,content,isSynced FROM data WHERE (type="+ AbstractNoteNotebook.TYPE_NOTE+" AND id = "+Long.toString(newNoteId)+")",null);
            c.moveToFirst();

            Note note = new Note(c.getLong(0),c.getLong(1),c.getLong(2),c.getInt(3),Functions.convertJsonStringToNoteContent(c.getString(4),getApplicationContext()),c.getInt(5));
            if(dataBundle.getNoteById(note.getId()) == null){
                dataBundle.addNote(note);
            }else {
                dataBundle.updateNoteById(note);
            }
            Log.d("myData","on save is synced : "+Boolean.toString(note.isSynced()));

            c.close();
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
    }

    private synchronized ArrayList<Notebook> getNotebooks()
    {
        try{
            c = myDatabase.rawQuery("SELECT id,notebookId,versionCode,status,content,isSynced FROM data WHERE type="+AbstractNoteNotebook.TYPE_NOTEBOOK,null);
            c.moveToFirst();

            ArrayList<Notebook> notebooks = new ArrayList();

            for(int i = 0; i < c.getCount(); i++)
            {
                Notebook notebook = new Notebook(c.getLong(0),c.getLong(1),c.getLong(2),c.getInt(3),Functions.convertJsonStringToNotebookName(c.getString(4)),c.getInt(5));
                notebooks.add(notebook);
                c.moveToNext();
            }

            c.close();

            return notebooks;
        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return null;
    }

    private synchronized void dispatchHeavySave(long newNoteId)
    {
        try{
            updateNotes(newNoteId);

            for(int i = 0; i < saveListener.size(); i++)
            {
                saveListener.get(i).onSaveCompleted();
            }

            saveListener = new ArrayList();

            for(int i = 0; i < constantSaveListener.size(); i++)
            {
                constantSaveListener.get(i).onSaveCompleted();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void dispatchSave()
    {
        try{
            for(int i = 0; i < saveListener.size(); i++)
            {
                saveListener.get(i).onSaveCompleted();
            }

            saveListener = new ArrayList();

            for(int i = 0; i < constantSaveListener.size(); i++)
            {
                constantSaveListener.get(i).onSaveCompleted();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void dispatchLoad()
    {
        try{
            for(int i = 0; i < loadListener.size(); i++)
            {
                loadListener.get(i).onLoadCompleted();
            }

            loadListener = new ArrayList();

            for(int i = 0; i < constantLoadListener.size(); i++)
            {
                constantLoadListener.get(i).onLoadCompleted();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized int getDatabaseSize()
    {
        try{
            c = myDatabase.rawQuery("SELECT id FROM data WHERE 1",null);

            int result = c.getCount();

            c.close();

            return result;

        }catch(Exception e){
            e.printStackTrace();
            if(c != null){
                if(!c.isClosed()){
                    c.close();
                }
            }
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        try{
            myDatabase.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public File getNoteNotebookEncodedById(Long id)
    {
        try{
            File f = new File(getCacheDir()+"//send_cache_file");
            if(f.exists()){
                f.delete();
            }f.createNewFile();

            FileWriter fr = new FileWriter(f);
            BufferedWriter fos = new BufferedWriter(fr);

            Notebook nb = getNotebookFromDatabase(id);
            if(nb!=null){
                fos.write(Long.toString(nb.getId()));
                fos.newLine();
                fos.write(Long.toString(nb.getNotebookId()));
                fos.newLine();
                fos.write(Long.toString(nb.getVersionCode()));
                fos.newLine();
                fos.write(Integer.toString(nb.getStatus()));
                fos.newLine();
                fos.write(Integer.toString(nb.getType()));
                fos.newLine();
                fos.write(Functions.convertNotebookNameToJsonString(nb.getName()));
                fos.close();
            }else {
                HeavyNote hn = getHeavyNoteFromDatabase(id);
                fos.write(Long.toString(hn.getId()));
                fos.newLine();
                fos.write(Long.toString(hn.getNotebookId()));
                fos.newLine();
                fos.write((Long.toString(hn.getVersionCode())));
                fos.newLine();
                fos.write((Integer.toString(hn.getStatus())));
                fos.newLine();
                fos.write((Integer.toString(hn.getType())));
                fos.newLine();

                File temp = Functions.convertHeavyNoteContentToJsonFile(getHeavyNoteFromDatabase(id).getContent(), getApplicationContext());
                FileReader fr3 = new FileReader(temp);
                BufferedReader br3 = new BufferedReader(fr3);

                fos.write(br3.readLine());

                br3.close();
                fos.close();

                FileReader fr2 = new FileReader(f);
                BufferedReader br = new BufferedReader(fr2);

                br.close();
            }
                return f;

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void deleteHeavyNotePermanently(long id)
    {
        try{
            c = myDatabase.rawQuery("SELECT notebookId,versionCode,status,content,isSynced FROM data WHERE id="+Long.toString(id),null);
            c.moveToFirst();

            HeavyNote note = new HeavyNote();
            note.setId(id);
            note.setNotebookId(0);
            note.setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            note.setStatus(AbstractNoteNotebook.STATUS_DELETED_PERMANENTLY);
            note.setContent(new NoteContent("",0,0));
            note.setIsSynced(AbstractNoteNotebook.IS_SYNCED_FALSE);

            c.close();

            saveHeavyNote(note);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void eraseAllData()
    {
        try{
            deleteDatabase("DATABASE");
            Functions.deleteFilesInDirectory(getFilesDir());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setAllSynced()
    {
        try{
            ContentValues cv = new ContentValues();
            cv.put("isSynced",AbstractNoteNotebook.IS_SYNCED_TRUE);
            myDatabase.update("data",cv,"1",null);

            dispatchSave();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
