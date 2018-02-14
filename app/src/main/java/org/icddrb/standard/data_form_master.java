package org.icddrb.standard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Common.Connection;
import Common.Global;
import DataSync.Log;
import Utility.MySharedPreferences;
import form_design.module_data_DataModel;
import form_design.module_variable_DataModel;

/**
 * Created by TanvirHossain on 05/12/2016.
 */

public class data_form_master extends AppCompatActivity {
    //Disabled Back/Home key
    //--------------------------------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int iKeyCode, KeyEvent event)
    {
        if(iKeyCode == KeyEvent.KEYCODE_BACK || iKeyCode == KeyEvent.KEYCODE_HOME)
        { return false; }
        else { return true;  }
    }
    String VariableID;
    private int hour;
    private int minute;
    private int mDay;
    private int mMonth;
    private int mYear;
    static final int DATE_DIALOG = 1;
    static final int TIME_DIALOG = 2;

    private List<module_variable_DataModel> variableList = new ArrayList<>();
    private RecyclerView recyclerView;
    private VariableListAdapter mAdapter;

    static String DEVICEID  = "";
    static String ENTRYUSER = "";

    MySharedPreferences sp;
    TextView lblCountVariable;
    LinearLayout secStatusSearch;
    Spinner spnStatusSearch;
    EditText txtSearch;
    Bundle IDbundle;
    static String LOCATION = "";
    static String ROLE     = "";

    LinearLayout secRegisType;
    LinearLayout secTitle;
    TextView lblTitle;


    static String MODULEID = "";
    static String DATAID  = "";
    static String VARIABLENAME = "";

    Global g;
    Connection C;
    private Boolean spinnerTouched = false;
    GridLayoutManager manager;
    LinearLayout secChildSl;
    public Spinner spnChildSl;
    RadioGroup rdogrpChildSl;
    RadioButton rdoChildSl1;
    RadioButton rdoChildSl2;
    RadioButton rdoChildSl3;

    Boolean video_flag=true;
    private static boolean audio_flag=true;
    private static MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_form_master);
        g = Global.getInstance();
        C = new Connection(this);

        DEVICEID    = sp.getValue(this, "deviceid");
        ENTRYUSER   = sp.getValue(this, "userid");

        IDbundle    = getIntent().getExtras();
        MODULEID    = IDbundle.getString("moduleid");;
        DATAID      = IDbundle.getString("dataid");

        VARIABLENAME = "";

        mp = new MediaPlayer();

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new VariableListAdapter(variableList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                try {
                    module_variable_DataModel variable = variableList.get(position);
                }catch(Exception ex){

                }
                //Toast.makeText(getApplicationContext(), variable.getDescription() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        manager = new GridLayoutManager(this, 1);
        //dynamically changing the total number of column
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
                //Labour and Delivery
                /*if(TABLEID.equals("1")) {
                    if (position == 16) {
                        return 2;
                    } else if (position == 17) {
                        return 2;
                    }else {
                        return 1;
                    }
                }
                //Newborn
                else if(TABLEID.equals("2")) {
                    return 1;
                }
                //Resus
                else if(TABLEID.equals("3")) {
                    if (position == 2|position == 3|position == 4|position == 5|position == 6) {
                        return 2;
                    }else {
                        return 1;
                    }
                }
                //PPHx
                else if(TABLEID.equals("4")) {
                    if (position == 0|position == 17|position == 20|position == 31) {
                        return 2;
                    }else {
                        return 1;
                    }
                }else {
                    return 1;
                }*/

            }
        });
        recyclerView.setLayoutManager(manager);


        prepareVariableListData(MODULEID, DATAID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.stop();
        mp.reset();
        audio_flag=true;
    }

    public void refreshAdapter(){
        mAdapter.notifyDataSetChanged();
    }

    public void prepareVariableListData(String Module_Id, String Data_Id) {

        String SQL = "";

        //Populate data for update
        SQL ="Insert into module_data(module_id, variable_name, data_id, variable_data, data_desc, status, entry_date, first_entry_time, final_entry_time, DeviceId, EntryUser, Upload, modifyDate)\n" +
                " select module_id, variable_name, '"+ Data_Id +"' data_id, '' variable_data, '' data_desc, 1 status, null entry_date, null first_entry_time, null final_entry_time, '"+ DEVICEID +"' DeviceId, '"+ ENTRYUSER +"' EntryUser, 2 Upload, getdate() modifyDate\n" +
                " from module_variable v where module_id='"+ Module_Id +"' and not exists(select * from module_data where module_id=v.module_id and variable_name=v.variable_name and " +
                " data_id='"+ Data_Id +"')\n" +
                " order by variable_seq";

        C.Save(SQL);

        //Populate data for form generate
        SQL = "Select v.module_id,v. variable_name,v. variable_desc,v. variable_seq,v. control_type,\n" +
                " v. variable_option,v. variable_length,v. data_type,v. skip_rule,v. color,v.active,v. variable_image,v. variable_audio,v. variable_video,\n" +
                " ifnull(d.variable_data,'')variable_data,ifnull(d.data_desc,'')data_desc, ifnull(d.data_id,'')data_id, ifnull(d.status,'')status\n" +
                " from module_variable v\n" +
                " left outer join module_data d on v.module_id=d.module_id and v.variable_name=d.variable_name \n" +
                " and d.data_id='"+ Data_Id +"'\n" +
                " where v.module_id='"+ Module_Id +"'\n" +
                " order by v.variable_seq";


        module_variable_DataModel d = new module_variable_DataModel();
        List<module_variable_DataModel> data = d.SelectAll_WithVariableData(this, SQL);

        variableList.clear();

        variableList.addAll(data);
        try {
            mAdapter.notifyDataSetChanged();
        }catch ( Exception ex){
            Connection.MessageBox(data_form_master.this,ex.getMessage());
        }
    }

    public String millsToDateFormat(long mills) {

        Date date = new Date(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateFormatted = formatter.format(date);
        return dateFormatted; //note that it will give you the time in GMT+0
    }

    //RadioButton[] rb;
    RadioButton rdo_butt;
    String[] item_list;

    public class VariableListAdapter extends RecyclerView.Adapter<VariableListAdapter.MyViewHolder> {
        private List<module_variable_DataModel> variableList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView objDescription, dataDescription,rdoData_Value;
            public LinearLayout objCheckList;
            public RelativeLayout resource,secVideo;
            public Spinner spnDataList;
            public EditText txtData;
            public RadioGroup rdoData;
            public CheckBox chkData;
            public ImageButton btnData;
            public View lineImportant;

            Button btnAudio,btnVideo;
            ImageView ivImage;

            VideoView secVideoView;
            Bitmap thumb;
            BitmapDrawable bitmapDrawable = null;




            public MyViewHolder(View view) {
                super(view);
                objDescription = (TextView) view.findViewById(R.id.objDescription);
                objCheckList = (LinearLayout) view.findViewById(R.id.objCheckList);
                resource = (RelativeLayout) view.findViewById(R.id.resource);

                dataDescription = (TextView) view.findViewById(R.id.dataDescription);
                spnDataList = (Spinner) view.findViewById(R.id.spnDataList);
                txtData = (EditText) view.findViewById(R.id.txtData);
                rdoData = (RadioGroup) view.findViewById(R.id.rdoData);
                rdoData_Value = (TextView) view.findViewById(R.id.rdoData_Value);
                chkData = (CheckBox) view.findViewById(R.id.chkData);

                btnData = (ImageButton) view.findViewById(R.id.btnData);
                txtData.setInputType(InputType.TYPE_CLASS_NUMBER);
                txtData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                lineImportant = (View) view.findViewById(R.id.lineImportant);


                //********************* sakib start *********************

                btnAudio= (Button) view.findViewById(R.id.btnAudio);
                ivImage= (ImageView) view.findViewById(R.id.ivImage);
                btnVideo= (Button) view.findViewById(R.id.btnVideo);

                secVideoView= (VideoView) view.findViewById(R.id.secVideoView);
                secVideo=(RelativeLayout) view.findViewById(R.id.secVideo);

                //********************* sakib end *********************

            }
        }


        public VariableListAdapter(List<module_variable_DataModel> varList) {
            this.variableList = varList;
        }

        @Override
        public VariableListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.module_variable_view_item_sakib, parent, false);

            return new VariableListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final VariableListAdapter.MyViewHolder holder, final int position) {
            final module_variable_DataModel varlist = variableList.get(position);

            /*if(varlist.getImportant().equals("1"))
                holder.objDescription.setText(Html.fromHtml("<font color=\"#FF0000\">*</font>"+ varlist.getDescription()));
            else
                holder.objDescription.setText(varlist.getDescription());*/

            holder.objDescription.setText(
                    varlist.getvariable_seq() + ". " +
                    varlist.getvariable_desc());

            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            });

            recyclerView.setLayoutManager(manager);

            holder.objDescription.setVisibility(View.VISIBLE);
            holder.dataDescription.setVisibility(View.GONE);
            holder.spnDataList.setVisibility(View.GONE);
            holder.txtData.setVisibility(View.GONE);
            holder.rdoData.setVisibility(View.GONE);
            holder.chkData.setVisibility(View.GONE);
            holder.btnData.setVisibility(View.GONE);

            holder.resource.setVisibility(View.GONE);
            holder.ivImage.setVisibility(View.GONE);
            holder.btnAudio.setVisibility(View.GONE);
            holder.btnVideo.setVisibility(View.GONE);
            holder.secVideoView.setVisibility(View.GONE);
            holder.secVideo.setVisibility(View.GONE);


            //********************* sakib start *********************
            if(varlist.get_image().length()!=0) {
                holder.resource.setVisibility(View.VISIBLE);
                holder.ivImage.setVisibility(View.VISIBLE);
                holder.ivImage.setBackground(Drawable.createFromPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_image()));
            }
            if(varlist.get_audio().length()!=0) {
                holder.resource.setVisibility(View.VISIBLE);
                holder.btnAudio.setVisibility(View.VISIBLE);
            }if(varlist.get_video().length()!=0) {
                holder.resource.setVisibility(View.VISIBLE);
                holder.btnVideo.setVisibility(View.VISIBLE);



                holder.secVideoView.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_video()));
                holder.thumb = ThumbnailUtils.createVideoThumbnail(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_video(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                holder.bitmapDrawable = new BitmapDrawable(holder.secVideoView.getResources(), holder.thumb);
                holder.secVideoView.setBackground(holder.bitmapDrawable);

                MediaController mediaController = new MediaController(holder.secVideoView.getContext());
                holder.secVideoView.setMediaController(mediaController);
                mediaController.setMediaPlayer(holder.secVideoView);
                mediaController.setAnchorView(holder.secVideoView);
                holder.secVideoView.requestFocus();

            }

            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Connection.MessageBox(data_form_master.this,"Image Clicked !!!");
                    ImageView image = new ImageView(data_form_master.this);
                    image.setBackground(Drawable.createFromPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_image()));

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(data_form_master.this).
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).
                                    setView(image);
                    builder.create().show();

                }
            });

            holder.btnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Connection.MessageBox(data_form_master.this,"Button Audio Clicked !!!: "+varlist.get_audio());


                    if(audio_flag)
                    {
                        try {
                            mp.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_audio());
                            mp.setVolume(100,100);
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mp.start();
                        holder.btnAudio.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.mute));
                        audio_flag=false;
                    }else
                    {
                        mp.stop();
                        mp.reset();
                        audio_flag=true;
                        holder.btnAudio.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.speaker));
                    }

                }
            });

            holder.btnVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Connection.MessageBox(data_form_master.this,"Button Video Clicked !!!: "+varlist.get_video());

                    if(video_flag)
                    {
                        holder.secVideoView.setVisibility(View.VISIBLE);
                        holder.secVideo.setVisibility(View.VISIBLE);
                        holder.secVideoView.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Global.DatabaseFolder + File.separator + varlist.get_video()));
                        holder.secVideoView.start();
                        holder.btnVideo.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.pause));
                        holder.secVideoView.setBackground(null);
                        video_flag=false;
                    }else
                    {

                        holder.secVideoView.stopPlayback();
                        holder.secVideoView.setBackground(holder.bitmapDrawable);
                        holder.btnVideo.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.play));
                        holder.secVideoView.setVisibility(View.GONE);
                        holder.secVideo.setVisibility(View.GONE);

                        video_flag=true;
                    }
                }
            });



            //********************* sakib end *********************

            //EditText
            //**************************************************************************************
            if(varlist.getcontrol_type().equals("1"))
            {
                holder.txtData.setVisibility(View.VISIBLE);

                holder.txtData.setText(varlist.getvariable_data());
                Log.logError(varlist.getvariable_data());

                //Length
                if (varlist.getvariable_length().length() != 0)
                    holder.txtData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.valueOf(varlist.getvariable_length()))});

                //Data Type
                if (varlist.getdata_type().equals("1"))
                    holder.txtData.setInputType(InputType.TYPE_CLASS_TEXT);
                else if (varlist.getdata_type().equals("2"))
                    holder.txtData.setInputType(InputType.TYPE_CLASS_NUMBER);
                else
                    holder.txtData.setInputType(InputType.TYPE_CLASS_TEXT);

                holder.txtData.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus & holder.txtData.getText().length()!=0) {
//                            Connection.MessageBox(data_form_master.this, v.getTag() + "");

                            saveData(varlist,holder.txtData.getText().toString());
                        }
                    }
                });
            }

            //Radio Button
            //**************************************************************************************
            else if(varlist.getcontrol_type().equals("2"))
            {
                holder.rdoData.setVisibility(View.VISIBLE);
                holder.rdoData.removeAllViews();
                item_list = varlist.getvariable_option().split(",");
                RadioButton[] rb = new RadioButton[item_list.length];

                holder.rdoData.setOrientation(RadioGroup.VERTICAL);
                for(int i=0; i<rb.length; i++){
                    rb[i]  = new RadioButton(data_form_master.this);
                    holder.rdoData.addView(rb[i]); //the RadioButtons are added to the radioGroup instead of the layout
                    try {
                        rb[i].setText(item_list[i].split("-").length==1?item_list[i].split("-")[0].trim():item_list[i].split("-")[1].trim());
                    }catch (Exception ex){

                    }
                }

                if(varlist.getvariable_data().length()>0) {

                    for (int i = 0; i < holder.rdoData.getChildCount(); i++) {
                        rdo_butt = (RadioButton) holder.rdoData.getChildAt(i);
                        if (varlist.getvariable_data().equals(varlist.getvariable_option().split(",")[i].split("-")[0])) {
                            holder.rdoData.check(rdo_butt.getId());
                        }
                    }
                }

                holder.rdoData.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        for (int i = 0; i < holder.rdoData.getChildCount(); i++)
                        {
                            rdo_butt = (RadioButton)holder.rdoData.getChildAt(i);
                            if (rdo_butt.isChecked()) {

                                Connection.MessageBox(data_form_master.this, varlist.getvariable_option().split(",")[i].split("-")[0]);

                                saveData(varlist,varlist.getvariable_option().split(",")[i].split("-")[0]);

                            }
                        }

                        switch (checkedId) {

                        }
                    }

                });


            }

            //Spinner
            //**************************************************************************************
            else if(varlist.getcontrol_type().equals("3"))
            {
                holder.spnDataList.setVisibility(View.VISIBLE);


                //populate data
                int pos = 0;
                String[] Opn = varlist.getvariable_option().split(",");
                List<String> listSpinnerItem = new ArrayList<String>();
                listSpinnerItem.add("");

                for (int i = 0; i < Opn.length; i++) {
                    listSpinnerItem.add(Opn[i].toString().trim());
                }
                ArrayAdapter<String> adptrMotEthnicity = new ArrayAdapter<String>(data_form_master.this, android.R.layout.simple_spinner_item, listSpinnerItem);
                holder.spnDataList.setAdapter(adptrMotEthnicity);



                if (varlist.getvariable_data().length() > 0) {
                    for (int i = 0; i < holder.spnDataList.getCount(); i++) {
                        if ((holder.spnDataList.getItemAtPosition(i).equals(varlist.getvariable_data()))) {
                            pos = i;
                        }
                    }
                    holder.spnDataList.setSelection(pos);
                }
//                if (varlist.getvariable_data()!= null ) {
//                    for (int i = 0; i < holder.spnDataList.getCount(); i++) {
//                        if ((holder.spnDataList.getItemAtPosition(i).equals(varlist.getvariable_data()))) {
//                            pos = i;
//                        }
//                    }
//                    holder.spnDataList.setSelection(pos);
//                }

                holder.spnDataList.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        spinnerTouched = true;
                        return false;
                    }
                });

                holder.spnDataList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int p, long id) {
                        if (spinnerTouched == true) {
//                            Connection.MessageBox(data_form_master.this,holder.spnDataList.getItemAtPosition(p).toString());


                            saveData(varlist,holder.spnDataList.getItemAtPosition(p).toString());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }
                });
            }

            //CheckBox
            //**************************************************************************************
            else if(varlist.getcontrol_type().equals("4")) {
                holder.chkData.setVisibility(View.VISIBLE);

                if(varlist.getvariable_data().equals("1"))
                    holder.chkData.setChecked(true);
                else
                    holder.chkData.setChecked(false);


                holder.chkData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int data;

                        if(isChecked)
                            data=1;
                        else
                            data=2;

                        saveData(varlist,""+data);

                    }
                });
            }



            //**************************************************************************************
            //module_variable_DataModel varlist1 = variableList.get(position + i);
            //varlist1.setactive("1");

//            mAdapter.variableList.set(position, varlist);
//            //recyclerView.invalidate();
//            //mAdapter.notifyItemChanged(position);
//            mAdapter.notifyDataSetChanged();

        }

        public void saveData(module_variable_DataModel varlist,String data)
        {
            module_data_DataModel module_data_dataModel=new module_data_DataModel();
            module_data_dataModel.setmodule_id(MODULEID);
            module_data_dataModel.setvariable_name(varlist.getvariable_name());
            module_data_dataModel.setdata_id(DATAID);
            module_data_dataModel.setvariable_data(data);
            module_data_dataModel.setdata_desc(varlist.getdata_desc());
            module_data_dataModel.setstatus(varlist.getstatus());
            module_data_dataModel.setentry_date(Global.DateNowYMD());
            module_data_dataModel.setfirst_entry_time(millsToDateFormat(new Date().getTime()));
            module_data_dataModel.setfinal_entry_time(millsToDateFormat(new Date().getTime()));

            module_data_dataModel.setDeviceID(DEVICEID);
            module_data_dataModel.setEntryUser(ENTRYUSER);

//                                module_data_dataModel.setUp(Connection.De);

            module_data_dataModel.setmodifyDate(Global.DateTimeNowYMDHMS());

            module_data_dataModel.SaveUpdateData(data_form_master.this);

        }



        @Override
        public int getItemCount() {
            return variableList.size();
        }
    }




    public class module_variable_DataModel_old {
        private String _TableId = "";
        public String getTableId(){
            return _TableId;
        }
        public void setTableId(String newValue){
            _TableId = newValue;
        }

        private String _VarName = "";
        public String getVarName(){
            return _VarName;
        }
        public void setVarName(String newValue){
            _VarName = newValue;
        }

        private String _SL = "";
        public String getSL(){
            return _SL;
        }
        public void setSL(String newValue){
            _SL = newValue;
        }

        private String _Description = "";
        public String getDescription(){
            return _Description;
        }
        public void setDescription(String newValue){
            _Description = newValue;
        }

        private String _ObjSeq1 = "";
        public String getObjSeq1(){
            return _ObjSeq1;
        }
        public void setObjSeq1(String newValue){
            _ObjSeq1 = newValue;
        }

        private String _ObjSeq2 = "";
        public String getObjSeq2(){
            return _ObjSeq2;
        }
        public void setObjSeq2(String newValue){
            _ObjSeq2 = newValue;
        }

        private String _Status = "";
        public String getStatus(){
            return _Status;
        }
        public void setStatus(String newValue){
            _Status = newValue;
        }

        private String _VarData = "";
        public String getVarData(){
            return _VarData;
        }
        public void setVarData(String newValue){
            _VarData = newValue;
        }

        private String _ControlType = "";
        public String getControlType(){
            return _ControlType;
        }
        public void setControlType(String newValue){
            _ControlType = newValue;
        }

        private String _VarOption = "";
        public String getVarOption(){
            return _VarOption;
        }
        public void setVarOption(String newValue){
            _VarOption = newValue;
        }

        private String _VarLength = "";
        public String getVarLength(){
            return _VarLength;
        }
        public void setVarLength(String newValue){
            _VarLength = newValue;
        }

        private String _VarDataType = "";
        public String getVarDataType(){
            return _VarDataType;
        }
        public void setVarDataType(String newValue){
            _VarDataType = newValue;
        }

        private String _Color = "";
        public String getColor(){
            return _Color;
        }
        public void setColor(String newValue){
            _Color = newValue;
        }

        private String _Active = "";
        public String getActive(){
            return _Active;
        }
        public void setActive(String newValue){
            _Active = newValue;
        }

        private String _Important = "";
        public String getImportant(){
            return _Important;
        }
        public void setImportant(String newValue){
            _Important = newValue;
        }

        private String _ForceVar = "";
        public String getForceVar(){
            return _ForceVar;
        }
        public void setForceVar(String newValue){
            _ForceVar = newValue;
        }
    }


    public class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == VERTICAL_LIST) {
                //drawVertical(c, parent);
            } else {
                //drawHorizontal(c, parent);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }



    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private RecyclerTouchListener.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final RecyclerTouchListener.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            try {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                    clickListener.onClick(child, rv.getChildPosition(child));
                }
            }
            catch(Exception ex){

            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        interface ClickListener {
            void onClick(View view, int position);
            void onLongClick(View view, int position);
        }
    }
}