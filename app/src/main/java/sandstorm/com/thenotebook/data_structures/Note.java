package sandstorm.com.thenotebook.data_structures;

import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;

public class Note extends AbstractNoteNotebook
{
    private NoteContent content;
    private long versionCode, notebookId, id;
    private int status, type = AbstractNoteNotebook.TYPE_NOTE, isSynced = 0;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DELETED = 0;
    public static final int STATUS_DELETED_PERMANENTLY = -1;

    public Note(){}

    public Note(long id, long notebookId, long versionCode, int status)
    {
        this.id = id;
        this.notebookId = notebookId;
        this.versionCode = versionCode;
        this.status = status;
    }

    public Note(long id, long notebookId, long versionCode, int status, int isSynced)
    {
        this.id = id;
        this.notebookId = notebookId;
        this.versionCode = versionCode;
        this.status = status;
        this.isSynced = isSynced;
    }

    public Note(long id, long notebookId, long versionCode, int status, NoteContent content)
    {
        this.id = id;
        this.notebookId = notebookId;
        this.versionCode = versionCode;
        this.status = status;
        this.content = content;
    }

    public Note(long id, long notebookId, long versionCode, int status, NoteContent content, int isSynced)
    {
        this.id = id;
        this.notebookId = notebookId;
        this.versionCode = versionCode;
        this.status = status;
        this.content = content;
        this.isSynced = isSynced;
    }

    public NoteContent getContent() {
        return content;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public long getNotebookId() {
        return notebookId;
    }

    public long getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public void setContent(NoteContent content) {
        this.content = content;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public void setNotebookId(long notebookId) {
        this.notebookId = notebookId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isSynced()
    {
        try{
            if(isSynced == 0){
                return false;
            }else{
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isReminder()
    {
        try{
            if(content.getReminder().getTimeInMillis() == 0) return false;
            else return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void setHeavyContent()
    {
        try{

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
