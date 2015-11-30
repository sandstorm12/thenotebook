package sandstorm.com.thenotebook.data_structures;

import android.util.Log;

import java.util.ArrayList;

import sandstorm.com.thenotebook.data_structures.micro_content.InfoItem;

public class InfoBundle
{
    private ArrayList<InfoItem> data;

    public InfoBundle()
    {
        data = new ArrayList();
    }

    public InfoBundle(ArrayList<InfoItem> data) {
        this.data = data;
    }

    public ArrayList<InfoItem> getData() {
        return data;
    }

    public void setData(ArrayList<InfoItem> data) {
        this.data = data;
    }

    public InfoItem getInfoItem(int index)
    {
        return this.data.get(index);
    }

    public void setInfoItem(int index, InfoItem infoItem)
    {
        this.data.set(index,infoItem);
    }

    public void removeInfoItem(int index)
    {
        this.data.remove(index);
    }

    public String getContentByName(String name)
    {
        try {
            for (int i = 0; i < data.size(); i++)
            {
                if(data.get(i).getName().equalsIgnoreCase(name)){
                    return data.get(i).getContent();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getSize()
    {
        try{
            return this.data.size();
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public void setContentByName(String name, String content)
    {
        try{
            for(int i = 0; i < data.size(); i++)
            {
                if(data.get(i).getName().equals(name))
                {
                    data.get(i).setContent(content);
                    return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public InfoItem getInfoItemByName(String name)
    {
        try{
            for (int i = 0; i < data.size(); i++)
            {
                if(data.get(i).getName().equalsIgnoreCase(name)){
                    return data.get(i);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
