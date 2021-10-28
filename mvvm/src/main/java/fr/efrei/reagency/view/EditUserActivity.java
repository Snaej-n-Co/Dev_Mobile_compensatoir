package fr.efrei.reagency.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;

import fr.efrei.reagency.R;
//import fr.efrei.reagency.viewmodel.AddUserActivityViewModel;
import fr.efrei.reagency.viewmodel.EditPropertyActivityViewModel;
import fr.efrei.reagency.viewmodel.EditUserActivityViewModel;

//import static fr.efrei.reagency.adapter.PropertiesAdapter.PropertyViewHolder.curSelectedProperty;
import static fr.efrei.reagency.adapter.UsersAdapter.UserViewHolder.curSelectedUser;
import static fr.efrei.reagency.tools.Utils.imageViewToByte;

/**
 * Class EditUserActivity
 * This class allows the modification of each user
 */
public class EditUserActivity
        extends AppCompatActivity
        implements View.OnClickListener
{


    public static final String TAG = EditUserActivity.class.getSimpleName();

    private static int RESULT_LOAD_IMG = 1;

    private ImageView avatarImage;
    private TextInputLayout name;
    private TextInputEditText nameText;
    private TextInputLayout phoneNumber;
    private TextInputEditText phoneNumberText;
    private TextInputLayout address;
    private TextInputEditText addressText;
    private TextInputLayout about;
    private TextInputEditText aboutText;

    private EditUserActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23)
            setContentView(R.layout.activity_edit_user);
        else
            setContentView(R.layout.activity_edit_user_loolipop);

        avatarImage = findViewById(R.id.avatar);
        name = findViewById(R.id.nameLayout);
        phoneNumber = findViewById(R.id.phoneLayout);
        address = findViewById(R.id.addressLayout);
        about = findViewById(R.id.aboutLayout);
        nameText = findViewById(R.id.name);
        phoneNumberText = findViewById(R.id.phone);
        addressText = findViewById(R.id.address);
        aboutText = findViewById(R.id.about);

        findViewById(R.id.btnSave).setOnClickListener(this);

        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImagefromGallery(v);
            }
        });

        viewModel = new ViewModelProvider(this).get(EditUserActivityViewModel.class);

        fillUser();
        observeEvent();
    }

    /**
     * Function fillUser
     */
    private void fillUser() {
        int width = avatarImage.getWidth();
        int height = avatarImage.getHeight();

        Bitmap bitmap = BitmapFactory.decodeByteArray(curSelectedUser.avatarBlob, 0, curSelectedUser.avatarBlob.length);
        if (width == 0 || height == 0)
            avatarImage.setImageBitmap(bitmap);
        else
            avatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));

        nameText.setText(curSelectedUser.name);
        phoneNumberText.setText(curSelectedUser.phoneNumber);
        addressText.setText(curSelectedUser.address);
        aboutText.setText(curSelectedUser.about);

        //Then we update the title into the actionBar
        //getSupportActionBar().setTitle(getResources().getString(R.string.property_edit));
    }

    /**
     * Function observeEvent
     */
    private void observeEvent() {
        viewModel.event.observe(this, new Observer<EditUserActivityViewModel.Event>() {
            @Override
            public void onChanged(EditUserActivityViewModel.Event event) {
                if (event == EditUserActivityViewModel.Event.ResetForm) {
                    resetForm();
                } else if (event == EditUserActivityViewModel.Event.DisplayError) {
                    displayError();
                }
            }
        });
    }

    /**
     * Function displayError
     */
    private void displayError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.cannot_modify_user)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Function onClick
     * @param v
     */
    @Override
    public void onClick(View v) {

        final String userName = name.getEditText().getText().toString();
        final String userPhoneNumber = phoneNumber.getEditText().getText().toString();
        final String userAddress = address.getEditText().getText().toString();
        final String aboutUser = about.getEditText().getText().toString();
        final byte[] avatarBlob = imageViewToByte(avatarImage);

        viewModel.saveUser(userName, userPhoneNumber, userAddress, aboutUser, avatarBlob);
    }

    /**
     * Function resetForm
     */
    private void resetForm() {
        this.finish();
    }

    /**
     * Function loadImagefromGallery
     * @param view
     */
    private void loadImagefromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    /**
     * Function onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);

                    int width = avatarImage.getWidth();
                    int height = avatarImage.getHeight();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    avatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));

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

}
