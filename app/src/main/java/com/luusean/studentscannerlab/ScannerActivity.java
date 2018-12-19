package com.luusean.studentscannerlab;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.database.EventObjectDao;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.database.StudentObject;
import com.luusean.studentscannerlab.database.StudentObjectDao;
import com.luusean.studentscannerlab.event.EventShowActivity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventObjectDao eventObjectDao;//sql access object
    private EventStudentObject eventStudentObject;
    private EventStudentObjectDao eventStudentObjectDao;
    private StudentObjectDao studentObjectDao;

    private List<StudentObject> ls_students;
    private List<StudentObject> ls_stored_students;
    private RecyclerView recyclerView;
    private TextView txtEmpty;
    //to save scanned student to this event id
    private Long event_id;
    //to save excel file
    private String pathToSaveExcelFile;
    private String excelFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //mapping & initiating
        recyclerView = findViewById(R.id.rev_stored_students);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtEmpty = findViewById(R.id.empty_view);

        //[GreenDAO] initiate
        eventObjectDao = initEventObjectDb();
        eventStudentObjectDao = initEventStudentDb();
        studentObjectDao = initStudentObjectDb();

        event_id = getMaxEventId(eventObjectDao).getId();
        ls_students = studentObjectDao.queryBuilder().orderDesc(StudentObjectDao.Properties.Lname).build().list();
        ls_stored_students = new ArrayList<>();

        if(ls_stored_students.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void initCameraScan(){
        IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan Student ID");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                boolean isFound = false;//check if found student
                for (StudentObject s : ls_students) {
                    if (s.getId().equals(intentResult.getContents())) {
                        ls_stored_students.add(new StudentObject(
                                null,
                                s.getId(),
                                s.getFname(),
                                s.getLname(),
                                s.getClassroom()
                        ));

                        //save EventStudent to DB
                        eventStudentObject = new EventStudentObject(null, event_id, s.getId());
                        eventStudentObjectDao.insert(eventStudentObject);

                        //remove student from origin list
                        ls_students.remove(s);
                        //set found to check if found student
                        isFound = true;

                        //set VISIBLE to show recyclerView
                        if(!ls_stored_students.isEmpty()) {
                            recyclerView.setVisibility(View.VISIBLE);
                            txtEmpty.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "Found: " + intentResult.getContents(), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (!isFound) {
                    if(isLsStudentsContains(ls_stored_students, intentResult.getContents())){
                        Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "Not found: " + intentResult.getContents(), Toast.LENGTH_SHORT).show();
                    }
                }

                Collections.sort(ls_stored_students, StudentsAscComparator);
                StudentAdapter adapter = new StudentAdapter(ScannerActivity.this, ls_stored_students);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    // Comparator for Ascending Order
    public static Comparator<StudentObject> StudentsAscComparator = new Comparator<StudentObject>() {
        public int compare(StudentObject s1, StudentObject s2) {
            String sub1 = s1.getLname();
            String sub2 = s2.getLname();
            return sub1.compareToIgnoreCase(sub2);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    public void onExportAction(MenuItem mi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export .xlsx");
        builder.setMessage("Enter name to export(.xlsx)");
        builder.setCancelable(false);

        final EditText edtNameExcel = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtNameExcel.setLayoutParams(lp);
        builder.setView(edtNameExcel);
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
                    if(saveExcelFile(excelFileName, pathToSaveExcelFile, ls_stored_students)) {
                        Toast.makeText(ScannerActivity.this,
                                "Lưu thành công " + excelFileName +".xls",
                                Toast.LENGTH_SHORT).show();
                    }
                    //dismiss dialog
                    alertDialog.dismiss();
                }
            }
        });
    }

    public void onContinuousScanAction(MenuItem mi) {
        initCameraScan();
    }

    private boolean isLsStudentsContains(List<StudentObject> ls_students, String stuid) {
        for (StudentObject s : ls_students) {
            if(s.getId().equals(stuid)) return true;
        }
        return false;
    }
    //detect click back button
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard Scanned");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //send Result_ok to reload list events in MainActivity
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
        builder.setMessage("Do you wanna discard this scanned list?");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get list of students of event id
                List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                        .where(EventStudentObjectDao.Properties.Event_id.eq(event_id))
                        .list();
                //delete all of list
                eventStudentObjectDao.deleteInTx(ls_es);
                //delete created event
                eventObjectDao.deleteByKey(event_id);

                //send Result_ok to reload list events in MainActivity
                setResult(RESULT_OK, new Intent());

                //finish & back to MainActivity
                finish();
            }
        });
    }

    //initiate EventObject DB
    private EventObjectDao initEventObjectDb() {
        //create db file if not exist
        String DB_NAME = "event_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventObjectDao();
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

    //get max event id
    private EventObject getMaxEventId(EventObjectDao eventObjectDao){
        List<EventObject> ls_es = eventObjectDao.queryBuilder().orderDesc(EventObjectDao.Properties.Id).limit(1).list();
        return ls_es.get(0);
    }

    //initiate StudentObject DB
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

    //method to save excel
    private boolean saveExcelFile(String fileName, String filePath, List<StudentObject> lsToShow){
        if (!isExternalStorageAvailable() && isExternalStorageReadOnly()) {
            Toast.makeText(ScannerActivity.this, getString(R.string.storage_not_available_or_readonly), Toast.LENGTH_SHORT).show();
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
