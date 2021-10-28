package fr.efrei.reagency.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import fr.efrei.reagency.R;
import fr.efrei.reagency.bo.Property;
import fr.efrei.reagency.viewmodel.PropertyDetailActivityViewModel;

import static fr.efrei.reagency.adapter.PropertiesAdapter.PropertyViewHolder.curSelectedProperty;

/**
 * Class PropertyDetailActivity
 * This class allows the view of each selected property and allows the calculation of loan
 */
public class PropertyDetailActivity
        extends AppCompatActivity {
    public static final String PROPERTY_EXTRA = "propertyExtra";

    //The tag used into this screen for the logs
    public static final String TAG = PropertyDetailActivity.class.getSimpleName();

    private boolean isEuro = true;

    private ImageView propImage;
    private TextView propType;
    private TextView propStatus;
    private TextView propPrice;
    private TextView propSurface;
    private TextView propRoom;
    private TextView propDescription;
    private TextView propAddress;
    private TextView propLatitude;
    private TextView propLongitude;
    private TextView propDateCreation;
    private TextView propDateUpdate;
    private TextView propDateSold;
    private TextView propAgentName;
    private Button btnConvertPrice;
    private Button btnLoanSimulate;

    private String euroString;
    private String euro;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_property_detail);

        //Then we retrieved the widget we will need to manipulate into the
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


        btnConvertPrice = findViewById(R.id.btnConvertPrice);
        btnConvertPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!propPrice.getText().toString().isEmpty()) {
                  //  Toast.makeText(PropertyDetailActivity.this, propPrice.getText().toString(), Toast.LENGTH_SHORT).show();
                    if (isEuro == true) {
                        euroString = propPrice.getText().toString();

                        euro = euroString.substring(0, euroString.length() - 2);
                        String toCurr = "USD";

                        double euroValue = Double.valueOf(euro);
                        Toast.makeText(PropertyDetailActivity.this, "Conversion done.", Toast.LENGTH_SHORT).show();
                        try {
                            convertCurrency(toCurr, euroValue);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(PropertyDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(PropertyDetailActivity.this, "Conversion done.", Toast.LENGTH_SHORT).show();
                        propPrice.setText(euro + " €");
                    }
                    //change Euro to Dollar or conversely
                    isEuro = !isEuro;
                    if (btnConvertPrice.getText().toString() == "€")
                        btnConvertPrice.setText("$");
                    else
                        btnConvertPrice.setText("€");
                } else {
                    Toast.makeText(PropertyDetailActivity.this, "Please Enter a Value to Convert..", Toast.LENGTH_SHORT).show();
                }

            }
        });
        findViewById(R.id.btnLoanSimulate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String currency;
                String price = propPrice.getText().toString();
                if (price.contains("€")) currency = "€";
                else currency = "$";
                String price1 = price.substring(0, price.indexOf(" " + currency));
                String data = price1 + ";" + currency;
                Intent intent = new Intent(PropertyDetailActivity.this, SimulatorActivity.class);
                intent.putExtra("priceParameter", data);
                startActivity(intent);
            }
        });

        displayProperty();
        //Then we update the title into the actionBar
        getSupportActionBar().setTitle(R.string.property_detail);
    }

    /**
     * Function displayProperty
     */
    private void displayProperty() {
        Property property = curSelectedProperty;
        Bitmap bitmap = BitmapFactory.decodeByteArray(property.imageBlob, 0, property.imageBlob.length);
//        propImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));
        propImage.setImageBitmap(bitmap);

        propType.setText(property.propertyType);
        if (property.propertyStatus.equals("Sold")) {
            propStatus.setBackgroundResource(R.color.solid_red);
        } else {
            propStatus.setBackgroundResource(R.color.solid_green);
        }
        propStatus.setTextColor(Color.WHITE);
        //propStatus.setTextColor(getResources().getColor(R.color.solid_white));
        propStatus.setText(property.propertyStatus);
        propPrice.setText(String.valueOf(property.propertyPrice) + " €");
        propSurface.setText(String.valueOf(property.propertySurface) + " m2");
        propRoom.setText(String.valueOf(property.propertyRoom));
        propDescription.setText(property.propertyDescription);

        propAddress.setText(property.propertyAddress);
        propLatitude.setText(property.propertyLatitude);
        propLongitude.setText(property.propertyLongitude);
        propDateCreation.setText(property.propertyDateCreation);
        propDateUpdate.setText(property.propertyDateUpdate);
        propDateSold.setText(property.propertyDateSold);
        propAgentName.setText(property.propertyAgentName);
        propDescription.setText(property.propertyDescription);
    }

    /**
     * Function convertCurrency
     * @param toCurr
     * @param euroValue
     * @throws IOException
     */
    public void convertCurrency(final String toCurr, final double euroValue) throws IOException {

        String url = "https://api.exchangeratesapi.io/latest";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                Toast.makeText(PropertyDetailActivity.this, mMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String mMessage = response.body().string();
                PropertyDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainActivity.this, mMessage, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject obj = new JSONObject(mMessage);
                            JSONObject b = obj.getJSONObject("rates");

                            String val = b.getString(toCurr);
                            double output = euroValue * Double.valueOf(val);
                            String v = String.valueOf(output);
                            String v1 = v.substring(0, v.indexOf("."));
                            propPrice.setText(v1 + " $");
                            //int i = Integer.valueOf(output.intValue());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}