package sandstorm.com.thenotebook.ui.noteList;

import android.graphics.drawable.Drawable;

public class NoteListData
{
    private int type;
    private String label, preview;
    private long id;
    private boolean isSynced, isReminder;

    public static final int TYPE_NOTEBOOK = 0;
    public static final int TYPE_NOTE = 1;

    public NoteListData(long id,int type, String label, String preview, boolean isSynced, boolean isReminder) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.preview = preview;
        this.isSynced = isSynced;
        this.isReminder = isReminder;
    }

    public NoteListData(long id,int type, String label, boolean isSynced) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.isSynced = isSynced;
    }

    public NoteListData(long id,int type, String label, boolean isSynced, boolean isReminder) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.isSynced = isSynced;
        this.isReminder = isReminder;
    }

    public boolean isReminder() {
        return isReminder;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public String getPreview() {
        return preview;
    }

    public String getLabel() {
        return label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public void setReminder(boolean isReminder) {
        this.isReminder = isReminder;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
