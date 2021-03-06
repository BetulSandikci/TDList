package com.sk.tdlist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements CalendarDialogBoxFragment.NoticeDialogListener{


    private List<TaskItem> mainList=new ArrayList<TaskItem>();
    private ListView mainListView=null;
    private ArrayAdapter ad=null;
    private EditText addTaskEditText=null;
    private FloatingActionButton addTaskButton=null;
    private SQLiteDatabase sqlDB=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        sqlDB=DBHandle.createDBTables(this);

        //Toast.makeText(this,"Click on task to delete it!",Toast.LENGTH_SHORT).show();

        /**
         * This section retrieves data from DB and adds it to ArrayList @existingTasks
         */
        Cursor existingTasks=sqlDB.rawQuery("SELECT * FROM ToDoList",null);
        if(existingTasks!=null){
            if(existingTasks.moveToFirst()){
                do{
                    if(existingTasks.isNull(existingTasks.getColumnIndex("DeadlineDate"))){
                        mainList.add( new TaskItem( existingTasks.getString( existingTasks.getColumnIndex("Task")) , Boolean.valueOf( existingTasks.getString(existingTasks.getColumnIndex("Status")) ) , null ) );
                    }
                    else{
                        mainList.add( new TaskItem( existingTasks.getString( existingTasks.getColumnIndex("Task")) , Boolean.valueOf( existingTasks.getString(existingTasks.getColumnIndex("Status")) ) , existingTasks.getString( existingTasks.getColumnIndex("DeadlineDate")) ) );
                    }

                }while (existingTasks.moveToNext());
            }
        }

        Toast.makeText(this,mainList.size()+" tasks present",Toast.LENGTH_SHORT).show();

        mainListView=(ListView) findViewById(R.id.MainListView);
        ad=new TaskAdapter((ArrayList) mainList,getApplicationContext(),sqlDB,getSupportFragmentManager());

        if(mainListView!=null){
            mainListView.setAdapter(ad);
        }



        addTaskEditText=(EditText) findViewById(R.id.AddTaskEditText);
        addTaskButton=(FloatingActionButton) findViewById(R.id.AddTaskButton);

        addTaskButton.setBackgroundResource(R.color.colorPrimaryDarkComplementAlpha);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!addTaskEditText.getText().toString().equals("")){
                    try{
                        sqlDB.execSQL("INSERT INTO ToDoList VALUES('"+addTaskEditText.getText().toString().trim()+"','false',NULL)");
                        mainList.add( new TaskItem( addTaskEditText.getText().toString().trim() , false , null) );
                        addTaskEditText.setText("");
                        ad.notifyDataSetChanged();
                    }
                    catch (SQLiteException e){
                        Toast.makeText(getApplicationContext(),e+"",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, TaskItem dataModel) {
        int position=TaskItem.findTaskPosition((ArrayList) mainList,dataModel.getTask());
        Log.d("MainActivity",mainList.get(position).getDeadlineDate()+", new: "+dataModel.getDeadlineDate());
        mainList.get(position).setDeadlineDate(dataModel.getDeadlineDate());
        ad.notifyDataSetChanged();
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog, TaskItem dataModel) {
        if(dataModel.getDeadlineDate()!=null){
            Toast.makeText(getApplicationContext(),"Non null deadline date passed through onDialogNeutralClick",Toast.LENGTH_LONG).show();
            return;
        }
        int position=TaskItem.findTaskPosition((ArrayList) mainList,dataModel.getTask());
        Log.d("MainActivity",mainList.get(position).getDeadlineDate()+", new: "+dataModel.getDeadlineDate());
        mainList.get(position).setDeadlineDate(null);
        ad.notifyDataSetChanged();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, TaskItem dataModel) {
        int position=TaskItem.findTaskPosition((ArrayList) mainList,dataModel.getTask());
        Log.d("MainActivity",mainList.get(position).getDeadlineDate()+", new: "+dataModel.getDeadlineDate());
        mainList.get(position).setDeadlineDate(dataModel.getDeadlineDate());
        ad.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }
}
