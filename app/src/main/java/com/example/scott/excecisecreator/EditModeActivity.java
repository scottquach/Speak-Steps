package com.example.scott.excecisecreator;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditModeActivity extends AppCompatActivity {

    private String exerciseName;

    @BindView(R.id.editExerciseRecycleView) RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private ArrayList<String> entries = new ArrayList<>();

    //0 represents a task, and 1 represents a break
    private ArrayList<Integer> entryType = new ArrayList<Integer>();

    private DataBaseHelper dbHelper;

    @BindView(R.id.addTaskButton) FloatingActionButton addTaskButton;
    @BindView(R.id.addBreakButton) FloatingActionButton addBreakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mode);

        ButterKnife.bind(this);

        //initialization
        dbHelper = new DataBaseHelper(this);

        //Get exercise name to be edited
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            exerciseName = extras.getString("name");
            loadData();
        }else{
            Toast.makeText(this, "Error creating new exercise", Toast.LENGTH_SHORT).show();
            Intent exitToHome = new Intent(EditModeActivity.this, StartMenuActivity.class);
            startActivity(exitToHome);
        }

        //changes action bar name based on exerciseName
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(exerciseName);

        loadData();
        setUpRecycleView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void setUpRecycleView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(entries, this);
        recyclerView.setAdapter(adapter);
    }

    private void updateRecycleView(){
        adapter.notifyDataSetChanged();
    }

    /*Retrieves the specified exercises data from the db and
    indexes them into arrayLists, with the entryType list discerning
    between tasks and breaks
     */
    private void loadData(){
        Cursor dataCursor = dbHelper.getExercise(exerciseName);

        if (dataCursor.moveToFirst()){

            String entry = "";
            int type = 0;

            do{
                entry = dataCursor.getString(1);
                type = dataCursor.getInt(2);
                entries.add(entry);
                entryType.add(type);
            }while (dataCursor.moveToNext());

        }else{
            Log.d("debug", "error loading cursor data");
        }
    }

    /*Saves the edited data back into
    the db
     */
    private void saveData(){
        dbHelper.saveExerciseEdits(exerciseName, entries, entryType);
    }

    private void createTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Task");

        final EditText input = new EditText(this);
        input.setHint("Task Name");
        builder.setView(input);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String task = input.getText().toString();
                Toast.makeText(EditModeActivity.this, task, Toast.LENGTH_SHORT).show();
                entries.add(task);
                entryType.add(0);
                updateRecycleView();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void createBreak(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Break");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_break, null);

        builder.setView(view);

        final NumberPicker minuteNP = (NumberPicker) view.findViewById(R.id.minutePicker);
        final NumberPicker secondNP = (NumberPicker) view.findViewById(R.id.secondPicker);
        minuteNP.setMinValue(0);
        minuteNP.setMaxValue(60);
        secondNP.setMinValue(0);
        secondNP.setMaxValue(59);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int minutes = minuteNP.getValue();
                int seconds = secondNP.getValue();
                int totalSeconds = convertToSeconds(minutes, seconds);
                Toast.makeText(EditModeActivity.this, "Minutes :" + String.valueOf(minutes) + " Seconds: " + String.valueOf(seconds), Toast.LENGTH_SHORT).show();
                entries.add(String.valueOf(totalSeconds));
                entryType.add(1);
                updateRecycleView();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private int convertToSeconds(int minute, int seconds){
        int totalSeconds;
        if (minute == 0){
            return seconds;
        }else{
            totalSeconds = (minute * 60);
            totalSeconds += seconds;
            return totalSeconds;
        }
    }

    public void createBreakClicked(View view) {
        createBreak();
    }

    public void createTaskClicked(View view) {
        createTask();
    }
}
