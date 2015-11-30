package sandstorm.com.thenotebook.data_structures;

public class Notebook extends AbstractNoteNotebook
{
    private long id;
    private long notebookId;
    private long versionCode;
    private String name;
    private int status, type = AbstractNoteNotebook.TYPE_NOTEBOOK, isSynced;

    public static final long ROOT_NOTEBOOK_ID = 0;
    public static final String ROOT_NOTEBOOK_NAME = "Home";
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DELETED = 0;
    public static final int STATUS_DELETED_PERMANENTLY = -1;

    public Notebook(){}

    public Notebook(long id, long notebookId, long versionCode, int status)
    {
        try{
            this.id = id;
            this.notebookId = notebookId;
            this.versionCode = versionCode;
            this.status = status;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Notebook(long id, long notebookId, long versionCode, int status, int isSynced)
    {
        try{
            this.id = id;
            this.notebookId = notebookId;
            this.versionCode = versionCode;
            this.status = status;
            this.isSynced = isSynced;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Notebook(long id, long notebookId, long versionCode, int status, String name)
    {
        try{
            this.id = id;
            this.notebookId = notebookId;
            this.versionCode = versionCode;
            this.status = status;
            this.name = name;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Notebook(long id, long notebookId, long versionCode, int status, String name, int isSynced)
    {
        try{
            this.id = id;
            this.notebookId = notebookId;
            this.versionCode = versionCode;
            this.status = status;
            this.name = name;
            this.isSynced = isSynced;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public long getNotebookId() {
        return notebookId;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNotebookId(long notebookId) {
        this.notebookId = notebookId;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public void setName(String name) {
        this.name = name;
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
}
