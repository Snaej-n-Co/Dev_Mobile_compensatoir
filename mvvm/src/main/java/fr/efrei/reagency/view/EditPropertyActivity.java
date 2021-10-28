package fr.efrei.reagency.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.efrei.reagency.R;
import fr.efrei.reagency.bo.Property;
import fr.efrei.reagency.viewmodel.AddPropertyActivityViewModel;
import fr.efrei.reagency.viewmodel.EditPropertyActivityViewModel;
import fr.efrei.reagency.viewmodel.EditPropertyActivityViewModel;

import static fr.efrei.reagency.adapter.PropertiesAdapter.PropertyViewHolder.curSelectedProperty;
import static fr.efrei.reagency.tools.Utils.imageViewToByte;

/**
 * Class EditPropertyActivity
 * This class allows the modification of each property
 */
public class EditPropertyActivity
        extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //The tag used into this screen for the logs
    public static final String TAG = EditPropertyActivity.class.getSimpleName();

    private DatePickerDialog.OnDateSetListener mDateCreationSetListener;
    private DatePickerDialog.OnDateSetListener mDateUpdateSetListener;

    private static int RESULT_LOAD_IMG = 1;

    private boolean isFirstType = true;
    private boolean isFirstStatus = true;

    private ImageView propImage;
    private Spinner propType;
    private Spinner propStatus;
    private EditText propPrice;
    private EditText propSurface;
    private EditText propRoom;
    private EditText propDescription;
    private EditText propAddress;
    private TextView propLatitude;
    private TextView propLongitude;
    private TextView propDateCreation;
    private TextView propDateUpdate;
    private TextView propDateSold;
    private TextView propAgentName;
    private Button propSave;
    private Button getGPS;

    private EditPropertyActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_edit_property);


        propImage = findViewById(R.id.propImage);
        propType = findViewById(R.id.propType);
        propStatus = findViewById(R.id.propStatus);
        propPrice = findViewById(R.id.propPrice);
        propSurface = findViewById(R.id.propSurface);
        propRoom = findViewById(R.id.propRoom);
        propDescription = findViewById(R.id.propDescription);

        propAddress = findViewById(R.id.propAddress);
        propLatitude = findViewById(R.id.propLatitude);
        propLongitude = findViewById(R.id.propLongitude);
        propDateCreation = findViewById(R.id.propDateCreation);
        propDateUpdate = findViewById(R.id.propDateUpdate);
        propDateSold = findViewById(R.id.propDateSold);
        propAgentName = findViewById(R.id.propAgentName);
        propSave = findViewById(R.id.propSave);
        getGPS = findViewById(R.id.btnGetGpsCoordinate);

        setOnClickListenerGPS();
        setOnClickListenerAvatarImage();

        addItemsOnSpinnerType();
        propType.setOnItemSelectedListener(this);
        addItemsOnSpinnerStatus();
        propStatus.setOnItemSelectedListener(this);

        setOnClickListenerDateCreation();
        setOnClickListenerDateUpdate();

        propSave.setOnClickListener(this);
        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(getApplication(),
                this, getIntent().getExtras())).get(EditPropertyActivityViewModel.class);

        fillProperty();
        observeEvent();
    }

    /**
     * Function fillProperty
     */
    private void fillProperty() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(curSelectedProperty.imageBlob, 0, curSelectedProperty.imageBlob.length);
        propImage.setImageBitmap(bitmap);

        int i;
        String[] list = getResources().getStringArray(R.array.spinner_type);
        for (i = 0; i < list.length; i++) {
            if (list[i].equals(curSelectedProperty.propertyType)) {
                propType.setSelection(i);
                break;
            }
        }
        list = getResources().getStringArray(R.array.spinner_status);
        for (i = 0; i < list.length; i++) {
            if (list[i].equals(curSelectedProperty.propertyStatus)) {
                propStatus.setSelection(i);
                break;
            }
        }

        propPrice.setText(String.valueOf(curSelectedProperty.propertyPrice));
        propSurface.setText(String.valueOf(curSelectedProperty.propertySurface));
        propRoom.setText(String.valueOf(curSelectedProperty.propertyRoom));
        propDescription.setText(curSelectedProperty.propertyDescription);

        propAddress.setText(curSelectedProperty.propertyAddress);
        propLatitude.setText(curSelectedProperty.propertyLatitude);
        propLongitude.setText(curSelectedProperty.propertyLongitude);
        propDateCreation.setText(curSelectedProperty.propertyDateCreation);
        propDateUpdate.setText(curSelectedProperty.propertyDateUpdate);
        propDateSold.setText(curSelectedProperty.propertyDateSold);
        propAgentName.setText(curSelectedProperty.propertyAgentName);
        propDescription.setText(curSelectedProperty.propertyDescription);


    }

    /**
     * Function observeEvent
     */
    private void observeEvent() {
        viewModel.event.observe(this, new Observer<EditPropertyActivityViewModel.Event>() {
            @Override
            public void onChanged(EditPropertyActivityViewModel.Event event) {
                if (event == EditPropertyActivityViewModel.Event.ResetForm) {
                    resetForm();
                } else if (event == EditPropertyActivityViewModel.Event.DisplayError) {
                    displayError();
                }
            }
        });
    }

    /**
     * Function loadImagefromGallery
     * @param view
     */
    private void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    /**
     * Function onActivityResult
     * This function is called when we selected an image from the Gallery
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);

                    int width = propImage.getWidth();
                    int height = propImage.getHeight();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    propImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Function displayError
     */
    private void displayError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.cannot_add_property)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Function resetForm
     */
    private void resetForm() {
        this.finish();
    }

    /**
     * Function addItemsOnSpinnerType
     * Initialize SpinnerType and add items into spinner dynamically
     */
    private void addItemsOnSpinnerType() {
        String[] list = getResources().getStringArray(R.array.spinner_type);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propType.setAdapter(dataAdapter);
    }

    /**
     * Function addItemsOnSpinnerStatus
     * Initialize SpinnerStatus and add items into spinner dynamically
     */
    private void addItemsOnSpinnerStatus() {
        String[] list = getResources().getStringArray(R.array.spinner_status);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propStatus.setAdapter(dataAdapter);
    }

    /**
     * Function getDateToday
     * @return
     */
    private String getDateToday() {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String sToday = formatter.format(today);
        return sToday;
    }

    /**
     * Function onClick
     * @param v
     */
    @Override
    public void onClick(View v) {
        final String propertyType = propType.getSelectedItem().toString();
        final String propertyStatus = propStatus.getSelectedItem().toString();
        final int propertyPrice = Integer.parseInt(propPrice.getEditableText().toString());
        final int propertySurface = Integer.parseInt(propSurface.getEditableText().toString());
        final int propertyRoom = Integer.parseInt(propRoom.getEditableText().toString());
        final String propertyDescription = propDescription.getEditableText().toString();
        final String propertyAddress = propAddress.getEditableText().toString();
        final String propertyLatitude = propLatitude.getText().toString();
        final String propertyLongitude = propLongitude.getText().toString();

        if (curSelectedProperty.propertyPrice != propertyPrice ||
                curSelectedProperty.propertySurface != propertySurface ||
                curSelectedProperty.propertyRoom != propertyRoom ||
                !curSelectedProperty.propertyDescription.equals(propertyDescription) ||
                !curSelectedProperty.propertyAddress.equals(propertyAddress) ||
                !curSelectedProperty.propertyLatitude.equals(propertyLatitude) ||
                !curSelectedProperty.propertyLongitude.equals(propertyLongitude))
            propDateUpdate.setText(getDateToday());

        final String propertyDateCreation = propDateCreation.getText().toString();
        final String propertyDateUpdate = propDateUpdate.getText().toString();
        final String propertyDateSold = propDateSold.getText().toString();
        final String propertyAgentName = propAgentName.getText().toString();
        final byte[] imageBlob = imageViewToByte(propImage);

        viewModel.saveProperty(propertyType, propertyPrice, propertySurface, propertyRoom,
                propertyDescription, propertyAddress, propertyLatitude, propertyLongitude,
                propertyDateCreation, propertyDateUpdate, propertyDateSold, propertyStatus, propertyAgentName,
                imageBlob);
    }

    /**
     * Function onItemSelected
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(@NotNull AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.propType:
                if (isFirstType) {
                    isFirstType = false;
                } else {
                    String selection = propType.getItemAtPosition(position).toString();
                    Log.d(TAG, selection);
                    String propertyType = propType.getSelectedItem().toString();
                    Log.d(TAG, propertyType);
                    if (!curSelectedProperty.propertyType.equals(propertyType))
                        propDateUpdate.setText(getDateToday());
                }
                propType.setSelection(position);
                break;
            case R.id.propStatus:
                if (isFirstStatus) {
                    isFirstStatus = false;
                } else {
                    String selection = propStatus.getItemAtPosition(position).toString();
                    Log.d(TAG, selection);
                    String propertyStatus = propStatus.getSelectedItem().toString();
                    if (!curSelectedProperty.propertyStatus.equals(propertyStatus))
                        propDateSold.setText(getDateToday());
                    Log.d(TAG, propertyStatus);
                }
                propStatus.setSelection(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Function setOnClickListenerDateCreation
     */
    private void setOnClickListenerDateCreation() {
        propDateCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        EditPropertyActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateCreationSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateCreationSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateCreationSet: yyyy-mm-dd: " + year + "-" + month + "-" + day);
                String date = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
                propDateCreation.setText(date);
            }
        };
    }

    /**
     * Function setOnClickListenerDateUpdate
     */
    private void setOnClickListenerDateUpdate() {
        propDateUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        EditPropertyActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateUpdateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateUpdateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateCreationSet: yyyy-mm-dd: " + year + "-" + month + "-" + day);
                String date = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
                propDateUpdate.setText(date);
            }
        };
    }

    /**
     * Function setOnClickListenerGPS
     */
    private void setOnClickListenerGPS() {
        getGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String add = propAddress.getText().toString();
                if (!add.equals("")) {
                    Geocoder geocoder = new Geocoder(EditPropertyActivity.this);
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(add, 1);
                        if (addresses.size() > 0) {
                            double latitude = addresses.get(0).getLatitude();
                            double longitude = addresses.get(0).getLongitude();
                            propLatitude.setText(String.valueOf(latitude));
                            propLongitude.setText(String.valueOf(longitude));
                        } else {
                            new AlertDialog.Builder(EditPropertyActivity.this)
                                    .setTitle(R.string.warning_title)
                                    .setMessage(R.string.cannot_get_gps)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(EditPropertyActivity.this, R.string.cannot_get_gps_add_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Function setOnClickListenerAvatarImage
     */
    private void setOnClickListenerAvatarImage() {
        propImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectImage();
                loadImagefromGallery(v);
            }
        });
    }
}
