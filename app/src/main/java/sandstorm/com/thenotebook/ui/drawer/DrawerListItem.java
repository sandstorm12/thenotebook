package sandstorm.com.thenotebook.ui.drawer;

import android.graphics.drawable.Drawable;

public class DrawerListItem
{
    private Drawable icon;
    private String label;
    private int type;

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SEPARATOR = 1;
    public static final int TYPE_ACCOUNT = 2;

    public DrawerListItem(){}

    public DrawerListItem(String Label, Drawable icon, int type)
    {
        this.label = Label;
        this.icon = icon;
        this.type = type;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
