package sandstorm.com.thenotebook.data_structures;

import sandstorm.com.thenotebook.data_structures.DataBundle;

public class AbstractNoteNotebook
{
    public static final int TYPE_NOTE = 0;
    public static final int TYPE_NOTEBOOK = 1;
    public static final int IS_SYNCED_TRUE = 1;
    public static final int IS_SYNCED_FALSE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DELETED = 0;
    public static final int STATUS_DELETED_PERMANENTLY = -1;

    private long versionCode, notebookId, id;
    private int status, type, isSynced = IS_SYNCED_FALSE;

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

    public void setType(int type) {
        this.type = type;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}
