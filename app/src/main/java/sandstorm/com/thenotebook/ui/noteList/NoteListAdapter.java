package sandstorm.com.thenotebook.ui.noteList;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.DataBundle;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.services.DataService;

public class NoteListAdapter extends BaseAdapter
{
    ArrayList<NoteListData> data = new ArrayList();

    private float startX, startViewX, dx;
    private NoteListAdapter adapter;
    private DataService ds;
    private ListView lv;

    public NoteListAdapter(DataService ds, ListView lv)
    {
        adapter = this;
        this.ds = ds;
        this.lv = lv;
    }

    public ArrayList<NoteListData> getData() {
        return data;
    }

    public void setData(ArrayList<NoteListData> data) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        try{
            LayoutInflater li = LayoutInflater.from(parent.getContext());

            if(data.get(position).getType() == NoteListData.TYPE_NOTE){
                convertView = li.inflate(R.layout.note_list_note,parent,false);
                TextView title = (TextView)convertView.findViewById(R.id.note_title);
                TextView preview = (TextView)convertView.findViewById(R.id.note_preview);
                ImageView isSynced = (ImageView)convertView.findViewById(R.id.note_not_synced);
                ImageView isReminder = (ImageView)convertView.findViewById(R.id.note_is_reminder);

                title.setText(data.get(position).getLabel());
                preview.setText(data.get(position).getPreview());
                if(data.get(position).isSynced()) isSynced.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.not_synced));
                if(data.get(position).isReminder()) isReminder.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.set_alarm));
            }else{
                convertView = li.inflate(R.layout.note_list_notebook,parent,false);
                TextView title = (TextView)convertView.findViewById(R.id.notebook_label);
                ImageView isSynced = (ImageView)convertView.findViewById(R.id.notebook_is_synced);

                title.setText(data.get(position).getLabel());
                if(data.get(position).isSynced()) isSynced.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.not_synced));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return convertView;
    }
}
