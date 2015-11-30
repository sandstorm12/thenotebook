package sandstorm.com.thenotebook.ui.newNote;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.micro_content.AbstractContent;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentFile;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentImage;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentSound;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentVideo;
import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;

public class NewNoteListAdapter extends BaseAdapter
{
    NoteContent data;
    Dialog d;

    public void setContent(NoteContent data)
    {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.getContentSize();
    }

    @Override
    public Object getItem(int position) {
        return data.getContent(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        try {
//            if(convertView != null) return convertView;
            LayoutInflater li = LayoutInflater.from(parent.getContext());
            switch (data.getContent(position).getType()) {
                case AbstractContent.TYPE_TEXT:
                    convertView = li.inflate(R.layout.note_text, parent, false);
                    final EditText tv = (EditText) convertView.findViewById(R.id.note_text);
                    tv.setText(((ContentText) data.getContent(position)).getText());
                    convertView.setVisibility(View.GONE);
                    break;
                case AbstractContent.TYPE_IMAGE:
                    convertView = li.inflate(R.layout.note_image, parent, false);
                    ImageView iv = (ImageView)convertView.findViewById(R.id.note_image_image);
                    iv.setImageBitmap(((ContentImage)data.getContent(position)).getThumbnail(500,500));
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                i.setDataAndType(((ContentImage)data.getContent(position)).getImageUri(),"image/*");

                                parent.getContext().startActivity(i);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case AbstractContent.TYPE_SOUND:
                    convertView = li.inflate(R.layout.note_sound, parent, false);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                i.setDataAndType(((ContentSound)data.getContent(position)).getSoundUri(),"audio/*");

                                parent.getContext().startActivity(i);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case AbstractContent.TYPE_VIDEO:
                    convertView = li.inflate(R.layout.note_image, parent, false);
                    ImageView iv2 = (ImageView)convertView.findViewById(R.id.note_image_image);
                    iv2.setImageBitmap(((ContentVideo)data.getContent(position)).getThumbnail());
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                i.setDataAndType(((ContentVideo)data.getContent(position)).getVideoUri(),"video/*");

                                parent.getContext().startActivity(i);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case AbstractContent.TYPE_FILE:
                    convertView = li.inflate(R.layout.note_file, parent, false);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                i.setDataAndType(((ContentFile) data.getContent(position)).getFileUri(), "*/*");

                                parent.getContext().startActivity(i);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(parent.getContext(), parent.getContext().getString(R.string.no_suitable_app), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    break;
            }

        convertView.setLongClickable(true);
        }catch(Exception e){
            e.printStackTrace();
        }

        return convertView;
    }
}
