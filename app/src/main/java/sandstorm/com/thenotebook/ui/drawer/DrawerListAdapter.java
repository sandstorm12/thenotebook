package sandstorm.com.thenotebook.ui.drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import sandstorm.com.thenotebook.R;

public class DrawerListAdapter extends BaseAdapter
{
    ArrayList<DrawerListItem> data = new ArrayList();

    public ArrayList<DrawerListItem> getData() {
        return data;
    }

    public void setData(ArrayList<DrawerListItem> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        if(data.get(position).getType() == DrawerListItem.TYPE_SEPARATOR){
            convertView = li.inflate(R.layout.drawer_separator,parent,false);

        }else if(data.get(position).getType() == DrawerListItem.TYPE_ITEM){
            convertView = li.inflate(R.layout.drawer_item,parent,false);
            TextView tv = (TextView)convertView.findViewById(R.id.drawer_item_label);

            tv.setText(data.get(position).getLabel());
        }else{
            convertView = li.inflate(R.layout.drawer_account,parent,false);
            TextView tv = (TextView)convertView.findViewById(R.id.drawer_account_text);
//            ImageView iv = (ImageView)convertView.findViewById(R.id.drawer_account_image);

            tv.setText(data.get(position).getLabel());
//            iv.setBackgroundDrawable(data.get(position).getIcon());
        }

        return convertView;
    }
}
