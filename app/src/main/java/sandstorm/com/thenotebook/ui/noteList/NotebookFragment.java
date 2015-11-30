package sandstorm.com.thenotebook.ui.noteList;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Notation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import sandstorm.com.thenotebook.R;
import sandstorm.com.thenotebook.data_structures.AbstractNoteNotebook;
import sandstorm.com.thenotebook.data_structures.DataBundle;
import sandstorm.com.thenotebook.data_structures.InfoBundle;
import sandstorm.com.thenotebook.data_structures.Note;
import sandstorm.com.thenotebook.data_structures.Notebook;
import sandstorm.com.thenotebook.data_structures.micro_content.AbstractContent;
import sandstorm.com.thenotebook.data_structures.micro_content.ContentText;
import sandstorm.com.thenotebook.data_structures.micro_content.NoteContent;
import sandstorm.com.thenotebook.functions.Functions;
import sandstorm.com.thenotebook.services.DataService;
import sandstorm.com.thenotebook.services.InfoService;
import sandstorm.com.thenotebook.ui.MainActivity;
import sandstorm.com.thenotebook.ui.newNote.NewEditNote;


public class NotebookFragment extends Fragment
{
    private ListView lv;
    private View view;
    private DataService ds;
    private InfoService is;
    private InfoBundle ib;
    private NoteListAdapter nla;
    private Activity root;
    private AlertDialog ad;
    private int mode;
    private Button fab, image, text, video, sound, file;
    private Toolbar tb;

    private boolean fabAccess;
    private Long currentNotebookId = Notebook.ROOT_NOTEBOOK_ID;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                ds = ((DataService.MyBinder)service).getService();
                loadContent(Notebook.ROOT_NOTEBOOK_ID);
                ds.addConstantOnSaveCompleteListener(new DataService.OnSaveCompleteListener() {
                    @Override
                    public void onSaveCompleted() {
                        root.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reload();
                                Log.d("myData","reload called from constant listener");
                            }
                        });
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceConnection Isc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try{
                is = ((InfoService.MyBinder)service).getService();
                is.addOnInfoLoadedListener(new InfoService.OnInfoLoaded() {
                    @Override
                    public void onInfoLoaded() {
                        ib = is.getCacheData();
                        is.addConstantOnInfoSavedListener(new InfoService.OnInfoSaved() {
                            @Override
                            public void onInfoSaved() {
                                is.addOnInfoLoadedListener(new InfoService.OnInfoLoaded() {
                                    @Override
                                    public void onInfoLoaded() {
                                        ib = is.getCacheData();

                                        root.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                reload();
                                            }
                                        });

                                    }
                                });
                                is.loadData();
                            }
                        });
                    }
                });
                is.loadData();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private final int FLAG_BY_CREATION_TIME = 0;
    private final int FLAG_BY_EDIT_TIME = 1;
    private final int FLAG_ASCENDING = 0;
    private final int FLAG_DESCENDING = 1;

    public static final int MODE_NOTEBOOK = 0;
    public static final int MODE_ALL_NOTES = 1;
    public static final int MODE_REMINDER = 2;
    public static final int MODE_TRASH = 3;
    public static final int MODE_SEARCH = 4;

    public static final int REQUEST_NEW_TEXT = 0;
    public static final int REQUEST_NEW_SOUND = 2;
    public static final int REQUEST_NEW_VIDEO = 3;
    public static final int REQUEST_NEW_IMAGE = 4;
    public static final int REQUEST_NEW_FILE = 5;
    public static final int REQUEST_EDIT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.note_show_fragment, container, false);

            lv = (ListView) view.findViewById(R.id.note_show_fragment_listView);
            tb = (Toolbar)view.findViewById(R.id.main_toolbar);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    try{
                        if(mode != MODE_TRASH) {
                            if (nla.data.get(position).getType() == NoteListData.TYPE_NOTE) {
                                Intent i = new Intent(view.getContext(), NewEditNote.class);
                                i.putExtra("request",REQUEST_EDIT);
                                i.putExtra("notebookId",currentNotebookId);
                                i.putExtra("id",nla.data.get(position).getId());
                                if(currentNotebookId == 0){
                                    i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                                }else{
                                    i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                                }
                                startActivity(i);
                                root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                            } else {
                                currentNotebookId = nla.data.get(position).getId();
                                loadContent(nla.data.get(position).getId());
                                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(ds.getCachedData().getNotebookById(currentNotebookId).getName());
                            }
                        }else{

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        final Dialog d = new Dialog(view.getContext());
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.raw_option_dialog);

                        if(mode == MODE_TRASH){
                            d.show();

                            String[] list = {getString(R.string.restore),getString(R.string.delete_permanently)};
                            ArrayAdapter aa = new ArrayAdapter(view.getContext(),android.R.layout.simple_list_item_1,list);

                            ListView lv = (ListView)d.findViewById(R.id.raw_dialog_listView);
                            lv.setAdapter(aa);

                            final int pos = position;
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    switch (position)
                                    {
                                        case 0 :
                                            Note n = ds.getCachedData().getNoteById(nla.getData().get(pos).getId());
                                            n.setNotebookId(Notebook.ROOT_NOTEBOOK_ID);
                                            n.setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                                            n.setStatus(AbstractNoteNotebook.STATUS_ACTIVE);
                                            ds.getCachedData().updateNoteById(n);
                                            ds.updateLightNoteOrNotebook(n);
                                            break;
                                        case 1 :
                                            final AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                            adb.setMessage(getString(R.string.wanna_delete_permanently));
                                            adb.setTitle(getString(R.string.caution));
                                            adb.setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ds.deleteHeavyNotePermanently(nla.getData().get(pos).getId());
                                                    ad.dismiss();
                                                }
                                            });
                                            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ad.dismiss();
                                                }
                                            });
                                            ad = adb.create();
                                            ad.show();
                                    }
                                    d.dismiss();
                                }
                            });
                        }else {
                            d.show();
                            if(nla.data.get(position).getType() == NoteListData.TYPE_NOTE) {
                                String[] cache = {getString(R.string.delete),getString(R.string.delete_permanently),getString(R.string.move_to_notebook),getString(R.string.add_shortcut_to_home_screen)};
                                ArrayAdapter aa = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, cache);

                                ListView lv2 = (ListView) d.findViewById(R.id.raw_dialog_listView);
                                lv2.setAdapter(aa);

                                final int pos = position;

                                lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        try {
                                            switch (position) {
                                                case 0:
                                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                                    adb.setTitle(getString(R.string.caution));
                                                    adb.setMessage(getString(R.string.wanna_delete));
                                                    adb.setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            try{
                                                                ds.getCachedData().getNoteOrNotebookById(nla.data.get(pos).getId()).setStatus(AbstractNoteNotebook.STATUS_DELETED);
                                                                ds.getCachedData().getNoteOrNotebookById(nla.data.get(pos).getId()).setVersionCode((Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTimeInMillis());
                                                                ds.updateLightNoteOrNotebook(ds.getCachedData().getNoteOrNotebookById(nla.data.get(pos).getId()));
                                                                ad.dismiss();
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    adb.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            try{
                                                                ad.dismiss();
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    ad = adb.create();
                                                    ad.show();
                                                    break;
                                                case 3:
                                                    Intent i = new Intent(view.getContext(), NewEditNote.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    i.putExtra("request",REQUEST_EDIT);
                                                    i.putExtra("new","new");
                                                    i.putExtra("notebookId",currentNotebookId);
                                                    i.putExtra("id",nla.data.get(pos).getId());
                                                    if(currentNotebookId == 0){
                                                        i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                                                    }else{
                                                        i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                                                    }
                                                    Intent i2 = new Intent();
                                                    i2.putExtra(Intent.EXTRA_SHORTCUT_INTENT,i);
                                                    i2.putExtra(Intent.EXTRA_SHORTCUT_NAME,nla.getData().get(pos).getLabel());
                                                    i2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(view.getContext(),R.drawable.icon));
                                                    i2.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                                    view.getContext().sendBroadcast(i2);
                                                    d.dismiss();
                                                    break;
                                                case 1 :
                                                    final AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
                                                    adb2.setMessage(getString(R.string.wanna_delete_permanently));
                                                    adb2.setTitle(getString(R.string.caution));
                                                    adb2.setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ds.deleteHeavyNotePermanently(nla.getData().get(pos).getId());
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    adb2.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ad.dismiss();
                                                        }
                                                    });
                                                    ad = adb2.create();
                                                    ad.show();
                                                    break;
                                                case 2 :
                                                    d.dismiss();
                                                    final Dialog d2 = new Dialog(view.getContext());
                                                    d2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                                                    d2.setContentView(R.layout.raw_option_dialog);
                                                    d2.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
                                                    d2.show();

                                                    ListView lv2 = (ListView)d2.findViewById(R.id.raw_dialog_listView);

                                                    final ArrayList<Notebook> temp = ds.getCachedData().getNotebookBundle();
                                                    String[] data = new String[temp.size()];
                                                    for(int j = 0; j < temp.size(); j++)
                                                    {
                                                        data[j] = temp.get(j).getName();
                                                    }

                                                    ArrayAdapter aa = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, data);

                                                    lv2.setAdapter(aa);

                                                    lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                            try{
                                                                ds.getCachedData().getNoteById(nla.data.get(pos).getId()).setNotebookId(temp.get(position).getId());
                                                                ds.getCachedData().getNoteById(nla.data.get(pos).getId()).setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                                                                ds.getCachedData().getNoteById(nla.data.get(pos).getId()).setIsSynced(AbstractNoteNotebook.IS_SYNCED_FALSE);
                                                                ds.updateLightNoteOrNotebook(ds.getCachedData().getNoteById(nla.data.get(pos).getId()));
                                                                d2.dismiss();
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });

                                                    break;
                                            }
                                            d.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }else{
                                String[] cache = {getString(R.string.rename),getString(R.string.delete)};
                                ArrayAdapter aa = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, cache);

                                ListView lv2 = (ListView) d.findViewById(R.id.raw_dialog_listView);
                                lv2.setAdapter(aa);

                                final int pos = position;

                                lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        try {
                                            switch (position) {
                                                case 1:
                                                    deleteNotebook(nla.getData().get(pos).getId());
                                                    ds.addOnSaveCompleteListener(new DataService.OnSaveCompleteListener() {
                                                        @Override
                                                        public void onSaveCompleted() {
                                                            reload();
                                                        }
                                                    });
                                                    ds.saveData(ds.getCachedData());
                                                    break;
                                                case 0:
                                                    d.dismiss();
                                                    final Dialog d2 = new Dialog(view.getContext());
                                                    d2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                                                    d2.setContentView(R.layout.rename);
                                                    d2.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels*90/100, WindowManager.LayoutParams.WRAP_CONTENT);
                                                    d2.show();

                                                    final EditText name = (EditText)d2.findViewById(R.id.rename_name);

                                                    final Context c = view.getContext();
                                                    Button rename = (Button)d2.findViewById(R.id.rename_rename);
                                                    rename.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            try{
                                                                if(name.getText().toString().isEmpty()){
                                                                    name.startAnimation(AnimationUtils.loadAnimation(c, R.anim.abc_fade_in));
                                                                }else {
                                                                    ds.getCachedData().getNotebookById(nla.data.get(pos).getId()).setName(name.getText().toString());
                                                                    ds.getCachedData().getNotebookById(nla.data.get(pos).getId()).setIsSynced(AbstractNoteNotebook.IS_SYNCED_FALSE);
                                                                    ds.getCachedData().getNotebookById(nla.data.get(pos).getId()).setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                                                                    ds.updateLightNoteOrNotebook(ds.getCachedData().getNotebookById(nla.data.get(pos).getId()));
                                                                    d2.dismiss();
                                                                }
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    break;
                                            }
                                            d.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            nla = new NoteListAdapter(ds,lv);

            initializeNotebooks();

            initializeFloatingButton();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            root = activity;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeNotebooks()
    {
        try{
            Intent i = new Intent(view.getContext(), InfoService.class);
            view.getContext().startService(i);
            view.getContext().bindService(i,Isc,0);

            i = new Intent(view.getContext(), DataService.class);
            view.getContext().bindService(i,sc,0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadContent(Long notebookId)
    {
        switch(mode)
        {
            case MODE_REMINDER :
            case MODE_NOTEBOOK :
                if (Integer.parseInt(ib.getContentByName("sortBy")) == FLAG_BY_CREATION_TIME) {
                    if (Integer.parseInt(ib.getContentByName("sortDirection")) == FLAG_ASCENDING) {
                        loadContentByCreationAscending(notebookId);
                    } else {

                    }
                } else {
                    if (Integer.parseInt(ib.getContentByName("sortDirection")) == FLAG_ASCENDING) {

                    } else {

                    }
                }
            break;

            case MODE_ALL_NOTES :
                break;
            case MODE_TRASH :
                break;
        }
    }

    private void loadAllNotes()
    {
        try{
            nla = new NoteListAdapter(ds,lv);

            ArrayList<NoteListData> data = new ArrayList();

            ArrayList<Note> notes = ds.getCachedData().getNoteBundle();
            for(int i = 0; i < notes.size(); i++)
            {
                if(notes.get(i).getStatus() != Note.STATUS_ACTIVE) continue;
                NoteListData nld = new NoteListData(notes.get(i).getId(),NoteListData.TYPE_NOTE, notes.get(i).getContent().getTitle(),notes.get(i).isSynced(),notes.get(i).isReminder());

                String preview = "";
                for(int j = 0; j < notes.get(i).getContent().getContentSize(); j++)
                {
                    if(notes.get(i).getContent().getContent(j).getType() == AbstractContent.TYPE_TEXT)
                    {
                        preview = ((ContentText)notes.get(i).getContent().getContent(j)).getText();
                    }
                }

                nld.setPreview(preview);
                data.add(nld);
            }

            nla.setData(data);

            Animation an = AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
            lv.setLayoutAnimation(new LayoutAnimationController(an));

            lv.setAdapter(nla);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadAllNotes(String filter)
    {
        try{
            if(filter.isEmpty()){
                loadAllNotes();
            }else{
                nla = new NoteListAdapter(ds,lv);

                ArrayList<NoteListData> data = new ArrayList();

                ArrayList<Note> notes = ds.getCachedData().getNoteBundle();
                for(int i = 0; i < notes.size(); i++)
                {
                    if(notes.get(i).getStatus() != Note.STATUS_ACTIVE) continue;
                    NoteListData nld = new NoteListData(notes.get(i).getId(),NoteListData.TYPE_NOTE, notes.get(i).getContent().getTitle(),notes.get(i).isSynced(),notes.get(i).isReminder());

                    String preview = "";
                    for(int j = 0; j < notes.get(i).getContent().getContentSize(); j++)
                    {
                        if(notes.get(i).getContent().getContent(j).getType() == AbstractContent.TYPE_TEXT)
                        {
                            preview = ((ContentText)notes.get(i).getContent().getContent(j)).getText();
                        }
                    }

                    nld.setPreview(preview);
                    if(nld.getLabel().contains(filter) || nld.getPreview().contains(filter)) {
                        data.add(nld);
                    }
                }

                nla.setData(data);

                Animation an = AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
                lv.setLayoutAnimation(new LayoutAnimationController(an));

                lv.setAdapter(nla);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadDeleted()
    {
        try{
            nla = new NoteListAdapter(ds,lv);

            ArrayList<NoteListData> data = new ArrayList();

            ArrayList<Notebook> notebooks = ds.getCachedData().getNotebookBundle();
            for(int i = 0; i < notebooks.size(); i++)
            {
                if(notebooks.get(i).getStatus() != Notebook.STATUS_DELETED) continue;
                NoteListData nld = new NoteListData(notebooks.get(i).getId(),NoteListData.TYPE_NOTEBOOK,notebooks.get(i).getName(),notebooks.get(i).isSynced());
                data.add(nld);
            }

            ArrayList<Note> notes = ds.getCachedData().getNoteBundle();
            for(int i = 0; i < notes.size(); i++)
            {
                if(notes.get(i).getStatus() != Note.STATUS_DELETED) continue;
                NoteListData nld = new NoteListData(notes.get(i).getId(),NoteListData.TYPE_NOTE, notes.get(i).getContent().getTitle(),notes.get(i).isSynced(),notes.get(i).isReminder());

                String preview = "";
                for(int j = 0; j < notes.get(i).getContent().getContentSize(); j++)
                {
                    if(notes.get(i).getContent().getContent(j).getType() == AbstractContent.TYPE_TEXT)
                    {
                        preview = ((ContentText)notes.get(i).getContent().getContent(j)).getText();
                    }
                }

                nld.setPreview(preview);
                data.add(nld);
            }

            nla.setData(data);

            Animation an = AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
            lv.setLayoutAnimation(new LayoutAnimationController(an));

            lv.setAdapter(nla);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadContentByCreationAscending(Long NotebookId)
    {
        try{
            nla = new NoteListAdapter(ds,lv);

            ArrayList<NoteListData> data = new ArrayList();

            ArrayList<Notebook> notebooks = ds.getCachedData().getNotebooksByNotebookId(NotebookId);
            for(int i = 0; i < notebooks.size(); i++)
            {
                if(notebooks.get(i).getStatus() != Notebook.STATUS_ACTIVE) continue;
                NoteListData nld = new NoteListData(notebooks.get(i).getId(),NoteListData.TYPE_NOTEBOOK,notebooks.get(i).getName(),notebooks.get(i).isSynced());
                data.add(nld);
            }

            ArrayList<Note> notes = ds.getCachedData().getNotesByNotebookId(NotebookId);
            for(int i = 0; i < notes.size(); i++)
            {
                if(notes.get(i).getStatus() != Note.STATUS_ACTIVE) continue;
                NoteListData nld = new NoteListData(notes.get(i).getId(),NoteListData.TYPE_NOTE, notes.get(i).getContent().getTitle(),notes.get(i).isSynced(),notes.get(i).isReminder());

                Log.d("myData","in reload VersionCode : "+Long.toString(notes.get(i).getVersionCode()));

                String preview = "";
                for(int j = 0; j < notes.get(i).getContent().getContentSize(); j++)
                {
                    if(notes.get(i).getContent().getContent(j).getType() == AbstractContent.TYPE_TEXT)
                    {
                        preview = ((ContentText)notes.get(i).getContent().getContent(j)).getText();
                    }
                }

                nld.setPreview(preview);
                data.add(nld);
            }

            nla.setData(data);

            Animation an = AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
            lv.setLayoutAnimation(new LayoutAnimationController(an));

            lv.setAdapter(nla);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void reload()
    {
        try{
            switch(mode) {
                case MODE_NOTEBOOK:
                    loadContent(currentNotebookId);
                    break;
                case MODE_REMINDER:
                    loadReminders();
                    break;
                case MODE_TRASH:
                    loadDeleted();
                    break;
                case MODE_ALL_NOTES:
                    loadAllNotes();
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy()
    {
        try{
            view.getContext().unbindService(sc);
            view.getContext().unbindService(Isc);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void initializeFloatingButton()
    {
        try{
            fab = (Button)view.findViewById(R.id.note_show_fragment_fab);
            text = (Button)view.findViewById(R.id.note_show_fragment_fab_text);
            image = (Button)view.findViewById(R.id.note_show_fragment_fab_image);
            sound = (Button)view.findViewById(R.id.note_show_fragment_fab_sound);
            video = (Button)view.findViewById(R.id.note_show_fragment_fab_video);
            file = (Button)view.findViewById(R.id.note_show_fragment_fab_file);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        dispatchFabClick();

                        Intent i = new Intent(view.getContext(), NewEditNote.class);
                        i.putExtra("request",REQUEST_NEW_TEXT);
                        i.putExtra("notebookId",currentNotebookId);
                        if(currentNotebookId == 0){
                            i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                        }else{
                            i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                        }
                        startActivity(i);
                        root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchFabClick();

                    Intent i = new Intent(view.getContext(), NewEditNote.class);
                    i.putExtra("request",REQUEST_NEW_IMAGE);
                    i.putExtra("notebookId",currentNotebookId);
                    if(currentNotebookId == 0){
                        i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                    }else{
                        i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                    }
                    startActivity(i);
                    root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            sound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchFabClick();

                    Intent i = new Intent(view.getContext(), NewEditNote.class);
                    i.putExtra("request",REQUEST_NEW_SOUND);
                    i.putExtra("notebookId",currentNotebookId);
                    if(currentNotebookId == 0){
                        i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                    }else{
                        i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                    }
                    startActivity(i);
                    root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchFabClick();

                    Intent i = new Intent(view.getContext(), NewEditNote.class);
                    i.putExtra("request",REQUEST_NEW_VIDEO);
                    i.putExtra("notebookId",currentNotebookId);
                    if(currentNotebookId == 0){
                        i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                    }else{
                        i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                    }
                    startActivity(i);
                    root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchFabClick();

                    Intent i = new Intent(view.getContext(), NewEditNote.class);
                    i.putExtra("request",REQUEST_NEW_FILE);
                    i.putExtra("notebookId",currentNotebookId);
                    if(currentNotebookId == 0){
                        i.putExtra("notebookName",Notebook.ROOT_NOTEBOOK_NAME);
                    }else{
                        i.putExtra("notebookName", ds.getCachedData().getNotebookById(currentNotebookId).getName());
                    }
                    startActivity(i);
                    root.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchFabClick();
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void dispatchFabClick()
    {
        try{
            if(!fabAccess) {
                Animation an = AnimationUtils.loadAnimation(view.getContext(), R.anim.to_cross);
                an.setFillAfter(true);
                fab.startAnimation(an);


                an = AnimationUtils.loadAnimation(view.getContext(),R.anim.slide_out_bottom);
                text.startAnimation(an);
                image.startAnimation(an);
                sound.startAnimation(an);
                video.startAnimation(an);
                file.startAnimation(an);

                text.setVisibility(View.VISIBLE);
                image.setVisibility(View.VISIBLE);
                sound.setVisibility(View.VISIBLE);
                video.setVisibility(View.VISIBLE);
                file.setVisibility(View.VISIBLE);

                fabAccess = true;
            }else{
                Animation an = AnimationUtils.loadAnimation(view.getContext(), R.anim.to_add);
                an.setFillAfter(true);
                fab.startAnimation(an);

                an = AnimationUtils.loadAnimation(view.getContext(),R.anim.slide_in_bottom);
                text.startAnimation(an);
                image.startAnimation(an);
                sound.startAnimation(an);
                video.startAnimation(an);
                file.startAnimation(an);

                an.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        text.setVisibility(View.INVISIBLE);
                        image.setVisibility(View.INVISIBLE);
                        sound.setVisibility(View.INVISIBLE);
                        video.setVisibility(View.INVISIBLE);
                        file.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                fabAccess = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public long getCurrentNotebookId() {
        return currentNotebookId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode, String filter) {
        this.mode = mode;
        currentNotebookId = Notebook.ROOT_NOTEBOOK_ID;
        switch(mode)
        {
            case MODE_NOTEBOOK :
                loadContent(Notebook.ROOT_NOTEBOOK_ID);
                fab.setVisibility(View.VISIBLE);
                break;
            case MODE_ALL_NOTES :
                fab.setVisibility(View.VISIBLE);
                loadAllNotes();
                break;
            case MODE_REMINDER :
                fab.setVisibility(View.VISIBLE);
                loadReminders();
                break;
            case MODE_TRASH :
                fab.setVisibility(View.INVISIBLE);
                loadDeleted();
                break;
            case MODE_SEARCH :
                fab.setVisibility(View.INVISIBLE);
                loadAllNotes(filter);
                break;
        }
    }

    public void goOneLevelUp()
    {
        try{
            currentNotebookId = ds.getCachedData().getNotebookById(currentNotebookId).getNotebookId();
            loadContent(currentNotebookId);
            if(currentNotebookId == 0){
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.notebooks);
            }else {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(ds.getCachedData().getNotebookById(currentNotebookId).getName());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deleteNotebook(long id)
    {
        try{
            Notebook n = ds.getCachedData().getNotebookById(id);
            n.setStatus(AbstractNoteNotebook.STATUS_DELETED_PERMANENTLY);
            n.setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            ds.getCachedData().updateNotebookById(n);

            ArrayList<Note> notes = ds.getCachedData().getNotesByNotebookId(id);
            ArrayList<Notebook> notebooks = ds.getCachedData().getNotebooksByNotebookId(id);

            for(int i = 0; i < notes.size(); i++)
            {
                notes.get(i).setStatus(AbstractNoteNotebook.STATUS_DELETED);
                notes.get(i).setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                ds.getCachedData().updateNoteById(notes.get(i));
            }

            for(int i = 0; i < notebooks.size(); i++)
            {
                notebooks.get(i).setStatus(AbstractNoteNotebook.STATUS_DELETED_PERMANENTLY);
                notebooks.get(i).setVersionCode(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                deleteNotebook(notebooks.get(i).getId());
                ds.getCachedData().updateNotebookById(notebooks.get(i));
            }

            ds.saveData(ds.getCachedData());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setCurrentNotebookId(long id)
    {
        try{

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadReminders()
    {
        try{
            nla = new NoteListAdapter(ds,lv);

            ArrayList<NoteListData> data = new ArrayList();

            ArrayList<Note> notes = ds.getCachedData().getNoteBundle();
            for(int i = 0; i < notes.size(); i++)
            {
                if(!notes.get(i).isReminder()) continue;

                NoteListData nld = new NoteListData(notes.get(i).getId(),NoteListData.TYPE_NOTE,notes.get(i).getContent().getTitle(),notes.get(i).isSynced(),true);

                nld.setPreview(((ContentText)notes.get(i).getContent().getContent(0)).getText());

                data.add(nld);
            }

            nla.setData(data);

            Animation an = AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
            lv.setLayoutAnimation(new LayoutAnimationController(an));

            lv.setAdapter(nla);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isFabOpen()
    {
        try{
            return fabAccess;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
