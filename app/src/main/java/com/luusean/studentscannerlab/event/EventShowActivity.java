package com.luusean.studentscannerlab.event;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.database.StudentObject;
import com.luusean.studentscannerlab.database.StudentObjectDao;
import com.luusean.studentscannerlab.student.StudentAdapter;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventShowActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventStudentObjectDao eventStudentObjectDao;
    private StudentObjectDao studentObjectDao;

    private String pathToSaveExcelFile;
    private String excelFileName;

    //list to show students of the event
    List<StudentObject> lsToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_show);

        //mapping recyclerView
        RecyclerView recyclerView = findViewById(R.id.rev_show_stu_of_event);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView txtEmpty = findViewById(R.id.empty_view);

        //Initialise DAO
        eventStudentObjectDao = initEventStudentDb();
        studentObjectDao = initStudentObjectDb();

        //get event id from intent
        Long event_id = Objects.requireNonNull(getIntent().getExtras()).getLong("event_id");

        //list id of student to lockup on listStudents
        List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                .where(EventStudentObjectDao.Properties.Event_id.eq(event_id))
                .list();

        //list of all students
        List<StudentObject> ls_so = studentObjectDao.queryBuilder().build().list();

        //list students after match 2 lists above
        lsToShow = new ArrayList<>();
        for(EventStudentObject es : ls_es){
            for(StudentObject so : ls_so){
                if(so.getId().equals(es.getStu_id())){
                    lsToShow.add(so);
                    break;
                }
            }
        }

        //check if empty to show "No Data" or Content of list
        if(!lsToShow.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);

            //set adapter
            StudentAdapter adapter = new StudentAdapter(this, lsToShow);
            recyclerView.setAdapter(adapter);
        }else{
            recyclerView.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_show_menu, menu);
        return true;
    }

    //initiate EventStudentObject DB
    private EventStudentObjectDao initEventStudentDb() {
        //create db file if not exist
        String DB_NAME = "event_student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventStudentObjectDao();
    }

    //initiate Student DB
    private StudentObjectDao initStudentObjectDb() {
        //create db file if not exist
        String DB_NAME = "student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getStudentObjectDao();
    }

    //MenuItem - export to excel
    public void onExportAction(MenuItem mi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Excel File");
        builder.setMessage("Enter name to export(*.xlsx)");

        final EditText edtNameExcel = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtNameExcel.setLayoutParams(lp);
        builder.setView(edtNameExcel);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_export_excel_black);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //check if null input event name
                if(TextUtils.isEmpty(edtNameExcel.getText().toString())){
                    edtNameExcel.setError(getString(R.string.invalid_input));
                    edtNameExcel.requestFocus();
                }else{
                    wantToCloseDialog = true;
                }
                //dismiss dialog
                if(wantToCloseDialog){
                    excelFileName = edtNameExcel.getText().toString();
                    pathToSaveExcelFile = Environment.getExternalStorageDirectory().toString();
                    if(saveExcelFile(excelFileName, pathToSaveExcelFile, lsToShow)) {
                        Toast.makeText(EventShowActivity.this,
                                "Lưu thành công " + excelFileName +".xls",
                                Toast.LENGTH_SHORT).show();
                    }
                    //dismiss dialog
                    alertDialog.dismiss();
                }
            }
        });
    }

    //method to save excel
    private boolean saveExcelFile(String fileName, String filePath, List<StudentObject> lsToShow){
        if (!isExternalStorageAvailable() && isExternalStorageReadOnly()) {
            Toast.makeText(EventShowActivity.this, getString(R.string.storage_not_available_or_readonly), Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean success = false;

        //New Workbook - .xls file
        Workbook wb = new HSSFWorkbook();
        //create font
        Font titleFont = wb.createFont();
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setFontHeightInPoints((short) 16);

        Font headFont = wb.createFont();
        headFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headFont.setFontHeightInPoints((short) 14);

        Font dataFont = wb.createFont();
        dataFont.setFontHeightInPoints((short) 11);

        //cell style for title
        CellStyle style_title = wb.createCellStyle();
        style_title.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style_title.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style_title.setAlignment(CellStyle.ALIGN_CENTER);
        style_title.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style_title.setFont(titleFont);
        style_title.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style_title.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style_title.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style_title.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style_title.setWrapText(true);

        //cell style for header row
        CellStyle style_field = wb.createCellStyle();
//        style_field.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
        style_field.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        style_field.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style_field.setAlignment(CellStyle.ALIGN_CENTER);
        style_field.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style_field.setFont(headFont);
        style_field.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style_field.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style_field.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style_field.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style_field.setWrapText(true);

        //cell style for header row
        CellStyle style_data = wb.createCellStyle();
        style_data.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style_data.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style_data.setFont(dataFont);
        style_data.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style_data.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style_data.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style_data.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style_data.setWrapText(true);

        //new sheet
        Sheet new_sheet;
        new_sheet = wb.createSheet("Participants");
        //generate column heading
        //this is code for enter value into the 0th Row and the 0th Cell
        Row row0 = new_sheet.createRow(0);
        row0.setHeight((short) 500);
        Cell c;
        //set title
        new_sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        c = row0.createCell(0);
        c.setCellValue(fileName);
        c.setCellStyle(style_title);
        //======================================================================
        Row row1 = new_sheet.createRow(1);
        row1.setHeight((short) 400);

        c = row1.createCell(0);
        c.setCellValue("MSSV");
        c.setCellStyle(style_field);

        c = row1.createCell(1);
        c.setCellValue("Họ Tên");
        c.setCellStyle(style_field);

        c = row1.createCell(2);
        c.setCellValue("Lớp");
        c.setCellStyle(style_field);

        //create sheet
        new_sheet.setColumnWidth(0, 5000);
        new_sheet.setColumnWidth(1, 5000);
        new_sheet.setColumnWidth(2, 5000);

        //insert data from lsToShow to excel file
        short count = 1;
        for(StudentObject so : lsToShow){
            Row rowToAdd = new_sheet.createRow(++count);
            rowToAdd.setHeight((short) 250);

            c = rowToAdd.createCell(0);
            c.setCellValue(so.getId());
            c.setCellStyle(style_data);

            c = rowToAdd.createCell(1);
            c.setCellValue(so.getFname() + " " + so.getLname());
            c.setCellStyle(style_data);

            c = rowToAdd.createCell(2);
            c.setCellValue(so.getClassroom());
            c.setCellStyle(style_data);
        }

        //Create file path for saving & assign extension ".xls"
        pathToSaveExcelFile = filePath;
        File file = new File(pathToSaveExcelFile, fileName + ".xls");
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            wb.write(fileOutputStream);
            Log.d("FileUtils", "Writing file: " + file);
            success = true;
        } catch (IOException e) {
            Log.d("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.d("FileUtils", "Failed to save file " + file, e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }
}
