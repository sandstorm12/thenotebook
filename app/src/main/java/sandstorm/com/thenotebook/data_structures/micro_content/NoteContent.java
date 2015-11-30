package sandstorm.com.thenotebook.data_structures.micro_content;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.*;

public class NoteContent
{
    private Calendar reminder;
    private long repeat;
    private String title = "";
    private ArrayList<AbstractContent> data = new ArrayList();

    public NoteContent()
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        this.reminder = c;
    }

    public NoteContent(String title, long reminderMillis, long repeat)
    {
        this.title = title;

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(reminderMillis);
        this.reminder = c;

        this.repeat = repeat;
    }

    public Calendar getReminder() {
        return reminder;
    }

    public ArrayList<AbstractContent> getData() {
        return data;
    }

    public void setReminder(Calendar reminder) {
        this.reminder = reminder;
    }

    public void setData(ArrayList<AbstractContent> data) {
        this.data = data;
    }

    public AbstractContent getContent(int index)
    {
        return data.get(index);
    }

    public void setContent(int index, AbstractContent content)
    {
        this.data.set(index,content);
    }

    public void addContent(AbstractContent content)
    {
        this.data.add(content);
    }

    public void removeContent(int index)
    {
        this.data.remove(index);
    }

    public int getContentSize()
    {
        try{
            return data.size();
        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getRepeat() {
        return repeat;
    }

    public void setRepeat(long repeat) {
        this.repeat = repeat;
    }

    public int getDataSize()
    {
        try{
            int temp = 0;
            for(int i = 0; i < data.size(); i++)
            {
                temp += data.get(i).getSize();
            }

            Log.d("myData","DataSize : "+Integer.toString(data.size())+"Data Volume : "+Integer.toString(temp));

            return temp;
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}