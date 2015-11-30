package sandstorm.com.thenotebook.data_structures;

import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

import sandstorm.com.thenotebook.functions.Functions;

public class DataBundle
{
    private ArrayList<Notebook> notebookBundle = new ArrayList();
    private ArrayList<Note> noteBundle = new ArrayList();

    public DataBundle(){}

    public DataBundle(ArrayList<Note> notes, ArrayList<Notebook> notebooks)
    {
        try {
            this.noteBundle = notes;
            this.notebookBundle = notebooks;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<Notebook> getNotebookBundle() {
        try {
            return notebookBundle;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Note> getNoteBundle() {
        try {
            return noteBundle;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void setNotebookBundle(ArrayList<Notebook> notebookBundle) {
        try {
            this.notebookBundle = notebookBundle;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void setNoteBundle(ArrayList<Note> noteBundle) {
        try {
            this.noteBundle = noteBundle;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized Note getNote(int index)
    {
        try {
            return this.noteBundle.get(index);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized Notebook getNotebook(int index)
    {
        try {
            return this.notebookBundle.get(index);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void setNote(int index, Note note)
    {
        try {
            this.noteBundle.set(index, note);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void setNotebook(int index, Notebook notebook)
    {
        try {
            this.notebookBundle.set(index, notebook);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void addNote(Note note)
    {
        try {
            this.noteBundle.add(note);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void addNotebook(Notebook notebook)
    {
        try {
            this.notebookBundle.add(notebook);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void removeNote(int index)
    {
        try {
            this.noteBundle.remove(index);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void removeNotebook(int index)
    {
        try {
            this.notebookBundle.remove(index);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<Note> getNotesByNotebookId(long notebookId)
    {
        try {
            ArrayList<Note> output = new ArrayList();

            for (int i = 0; i < noteBundle.size(); i++) {
                if (noteBundle.get(i).getNotebookId() == notebookId) {
                    output.add(noteBundle.get(i));
                }
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Notebook> getNotebooksByNotebookId(long notebookId)
    {
        try {
            ArrayList<Notebook> output = new ArrayList();

            for (int i = 0; i < notebookBundle.size(); i++) {
                if (notebookBundle.get(i).getNotebookId() == notebookId) {
                    output.add(notebookBundle.get(i));
                }
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized Note getNoteById(long id)
    {
        try {
            for (int i = 0; i < noteBundle.size(); i++) {
                if (noteBundle.get(i).getId() == id) return noteBundle.get(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public synchronized Notebook getNotebookById(long id)
    {
        try {
            for (int i = 0; i < notebookBundle.size(); i++) {
                if (notebookBundle.get(i).getId() == id) return notebookBundle.get(i);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public synchronized ArrayList<Note> getNotesByStatus(int status)
    {
        try {
            ArrayList<Note> output = new ArrayList();

            for (int i = 0; i < noteBundle.size(); i++) {
                if (noteBundle.get(i).getStatus() == status) output.add(noteBundle.get(i));
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Notebook> getNotebooksByStatus(int status)
    {
        try {
            ArrayList<Notebook> output = new ArrayList();

            for (int i = 0; i < notebookBundle.size(); i++) {
                if (notebookBundle.get(i).getStatus() == status) output.add(notebookBundle.get(i));
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Note> getNotesByNotebookId(long notebookId, int status)
    {
        try {
            ArrayList<Note> output = new ArrayList();

            for (int i = 0; i < noteBundle.size(); i++) {
                if (noteBundle.get(i).getNotebookId() == notebookId && noteBundle.get(i).getStatus() == status) {
                    output.add(noteBundle.get(i));
                }
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Notebook> getNotebooksByNotebookId(long notebookId, int status)
    {
        try {
            ArrayList<Notebook> output = new ArrayList();

            for (int i = 0; i < notebookBundle.size(); i++) {
                if (notebookBundle.get(i).getNotebookId() == notebookId && notebookBundle.get(i).getStatus() == status) {
                    output.add(notebookBundle.get(i));
                }
            }

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<Vector<Long>> getDataMap()
    {
        try{
            ArrayList<Vector<Long>> dataMap = new ArrayList();

            for(int i = 0; i < noteBundle.size(); i++)
            {
                Vector<Long> v = new Vector();
                v.add(noteBundle.get(i).getId());
                v.add(noteBundle.get(i).getVersionCode());
                dataMap.add(v);
            }

            for(int i = 0; i < notebookBundle.size(); i++)
            {
                Vector<Long> v = new Vector();
                v.add(notebookBundle.get(i).getId());
                v.add(notebookBundle.get(i).getVersionCode());
                dataMap.add(v);
            }

            return dataMap;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

//    public synchronized String getRequestedAbstractNotesEncoded(ArrayList<Long> ids)
//    {
//        try{
//            String output = "";
//            boolean a = false;
//
//            for(int i = 0; i < ids.size(); i++)
//            {
//                for(int j = 0; j < noteBundle.size(); j++) {
//                    if(a) output+="#";
//                    if (noteBundle.get(j).getId() == ids.get(i)) {
//                        output += getNoteAsString(noteBundle.get(j));
//                    }
//                    a = true;
//                }
//
//                for(int j = 0; j < notebookBundle.size(); j++)
//                {
//                    if(a) output+="#";
//                    if (noteBundle.get(j).getId() == ids.get(i)) {
//                        output += getNotebookAsString(notebookBundle.get(j));
//                    }
//                    a = true;
//                }
//            }
//
//            return Base64.encodeToString(output.getBytes("UTF-8"),0);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public synchronized void insertOrUpdateDataFromEncodedServerAnswer(String encodedServerData)
//    {
//        try{
//            String decoded = new String(Base64.decode(encodedServerData.getBytes("UTF-8"), 0),"UTF-8");
//
//            Log.d("myData","when we split : "+decoded);
//
//            String[] AbstractNoteStrings = decoded.split("#");
//
//            ArrayList<AbstractNoteNotebook> newData = new ArrayList();
//
//            for(int i = 0; i < AbstractNoteStrings.length; i++)
//            {
//                String[] temp = AbstractNoteStrings[i].split("\n");
//
//                if(temp[4].equals(Integer.toString(AbstractNoteNotebook.TYPE_NOTE)))
//                {
//                    Note note = new Note(Long.parseLong(temp[0]),Long.parseLong(temp[1]),Long.parseLong(temp[2]),Integer.parseInt(temp[3]));
//                    note.setContent(Functions.convertJsonStringToHeavyNoteContent(new String(Base64.decode(temp[5], 0),"UTF-8")));
//                    newData.add(note);
//                }else if(temp[4].equals(Integer.toString(AbstractNoteNotebook.TYPE_NOTEBOOK)))
//                {
//                    Notebook notebook = new Notebook(Long.parseLong(temp[0]),Long.parseLong(temp[1]),Long.parseLong(temp[2]),Integer.parseInt(temp[3]));
//                    notebook.setName(Functions.convertJsonStringToNotebookName(new String(Base64.decode(temp[5], 0),"UTF-8")));
//                    newData.add(notebook);
//                }
//            }
//
//            for(int i = 0; i < newData.size(); i++)
//            {
//                if(newData.get(i).getType() == AbstractNoteNotebook.TYPE_NOTE)
//                {
//                    if(getNoteById(newData.get(i).getId()) == null){
//                        noteBundle.add((Note)newData.get(i));
//                    }else{
//                        updateNoteById((Note)newData.get(i));
//                    }
//                }else{
//                    if(getNotebookById(newData.get(i).getId()) == null){
//                        notebookBundle.add((Notebook)newData.get(i));
//                    }else{
//                        updateNotebookById((Notebook)newData.get(i));
//                    }
//                }
//            }
//
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }

//    private synchronized String getNoteAsString(Note note)
//    {
//        try{
//            String output = "";
//
//            output += Long.toString(note.getId())+"\n";
//            output += Long.toString(note.getNotebookId())+"\n";
//            output += Long.toString(note.getVersionCode())+"\n";
//            output += Integer.toString(note.getStatus())+"\n";
//            output += Integer.toString(note.getType())+"\n\'";
//            output += Functions.convertNoteContentToJsonString(note.getContent())+"\'";
//
//            return output;
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    private synchronized String getNotebookAsString(Notebook notebook)
    {
        try{
            String output = "";

            output += Long.toString(notebook.getId())+"\n";
            output += Long.toString(notebook.getNotebookId())+"\n";
            output += Long.toString(notebook.getVersionCode())+"\n";
            output += Integer.toString(notebook.getStatus())+"\n";
            output += Integer.toString(notebook.getType())+"\n\'";
            output += Functions.convertNotebookNameToJsonString(notebook.getName())+"\'";

            return output;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void updateNoteById(Note note)
    {
        try{
            for(int i = 0; i < noteBundle.size(); i++)
            {
                if(noteBundle.get(i).getId() == note.getId())
                {
                    noteBundle.get(i).setNotebookId(note.getNotebookId());
                    noteBundle.get(i).setVersionCode(note.getVersionCode());
                    noteBundle.get(i).setStatus(note.getStatus());
                    noteBundle.get(i).setContent(note.getContent());
                    noteBundle.get(i).setIsSynced(note.getIsSynced());
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void updateNotebookById(Notebook notebook)
    {
        try{
            for(int i = 0; i < notebookBundle.size(); i++)
            {
                if(notebookBundle.get(i).getId() == notebook.getId())
                {
                    notebookBundle.get(i).setNotebookId(notebook.getNotebookId());
                    notebookBundle.get(i).setVersionCode(notebook.getVersionCode());
                    notebookBundle.get(i).setStatus(notebook.getStatus());
                    notebookBundle.get(i).setIsSynced(notebook.getIsSynced());
                    notebookBundle.get(i).setName(notebook.getName());
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void setNoteById(Note note)
    {
        for(int i = 0; i < noteBundle.size(); i++)
        {
            if(noteBundle.get(i).getId() == note.getId())
            {
                noteBundle.set(i,note);
                return;
            }
        }
    }

    public synchronized void setNotebookById(Notebook notebook)
    {
        for(int i = 0; i < notebookBundle.size(); i++)
        {
            if(notebookBundle.get(i).getId() == notebook.getId())
            {
                notebookBundle.set(i,notebook);
                return;
            }
        }
    }

    public synchronized void removeNoteById(long id)
    {
        try{
            for(int i = 0; i < noteBundle.size(); i++)
            {
                if(noteBundle.get(i).getId() == id)
                {
                    noteBundle.remove(i);
                    return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void removeNotebookById(long id)
    {
        try{
            for(int i = 0; i < notebookBundle.size(); i++)
            {
                if(notebookBundle.get(i).getId() == id)
                {
                    notebookBundle.remove(i);
                    return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void removeNoteOrNotebookById(long id)
    {
        try{
            for(int i = 0; i < notebookBundle.size(); i++)
            {
                if(notebookBundle.get(i).getId() == id)
                {
                    notebookBundle.remove(i);
                    return;
                }
            }

            for(int i = 0; i < noteBundle.size(); i++)
            {
                if(noteBundle.get(i).getId() == id)
                {
                    noteBundle.remove(i);
                    Log.d("myData","removed : "+Long.toString(id));
                    return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized AbstractNoteNotebook getNoteOrNotebookById(long id)
    {
        try{
            for(int i = 0; i < noteBundle.size(); i++)
            {
                if(noteBundle.get(i).getId() == id)
                {
                    return noteBundle.get(i);
                }
            }

            for(int i = 0; i < notebookBundle.size(); i++)
            {
                if(notebookBundle.get(i).getId() == id)
                {
                    return notebookBundle.get(i);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void setAllSynced()
    {
        try{
            for(int i = 0; i < noteBundle.size(); i++)
            {
                noteBundle.get(i).setIsSynced(AbstractNoteNotebook.IS_SYNCED_TRUE);
            }

            for(int i = 0; i < notebookBundle.size(); i++)
            {
                notebookBundle.get(i).setIsSynced(AbstractNoteNotebook.IS_SYNCED_TRUE);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
