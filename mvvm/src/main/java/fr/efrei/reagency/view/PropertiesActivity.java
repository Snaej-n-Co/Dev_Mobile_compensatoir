package fr.efrei.reagency.view;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import fr.efrei.reagency.R;
import fr.efrei.reagency.adapter.PropertiesAdapter;
import fr.efrei.reagency.bo.Property;
import fr.efrei.reagency.preferences.AppPreferences;
import fr.efrei.reagency.repository.PropertyRepository;
import fr.efrei.reagency.viewmodel.PropertiesActivityViewModel;
import fr.efrei.reagency.viewmodel.PropertiesActivityViewModel.OrderState;
import fr.efrei.reagency.viewmodel.UsersActivityViewModel;

import static fr.efrei.reagency.adapter.PropertiesAdapter.PropertyViewHolder.curSelectedProperty;
import static fr.efrei.reagency.adapter.PropertiesAdapter.PropertyViewHolder.viewPropertySelected;

/**
 * Class PropertiesActivity
 * This class lists all properties and handles the Google Maps
 */
final public class PropertiesActivity
        extends AppCompatActivity
{

    //The tag used into this screen for the logs
    public static final String TAG = PropertiesActivity.class.getSimpleName();
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    private RecyclerView recyclerView;

    private PropertiesActivityViewModel viewModel;
    public String sSearchCriteria = "";

    public String userLogin = "";
    private List<Property> memProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_properties);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        viewPropertySelected = false;

        userLogin = AppPreferences.getUserLogin(getApplication());

        findViewById(R.id.fabAddProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(PropertiesActivity.this, AddPropertyActivity.class));
            }
        });
        findViewById(R.id.fabDeleteProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewPropertySelected) {
                    userLogin = AppPreferences.getUserLogin(getApplication());
                    if (curSelectedProperty.propertyAgentName.equals(userLogin)) {
                        PropertyRepository.getInstance(getApplication()).deleteProperty(curSelectedProperty);
                        onResume();
                    } else {
                        new AlertDialog.Builder(PropertiesActivity.this)
                                .setTitle(R.string.info_title)
                                .setMessage(R.string.cannot_delete_property)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                } else
                    Toast.makeText(PropertiesActivity.this, R.string.property_not_selected, Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.fabEditProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewPropertySelected) {
                    userLogin = AppPreferences.getUserLogin(getApplication());
                    if (curSelectedProperty.propertyAgentName.equals(userLogin))
                        startActivity(new Intent(PropertiesActivity.this, EditPropertyActivity.class));
                    else {
                        new AlertDialog.Builder(PropertiesActivity.this)
                                .setTitle(R.string.info_title)
                                .setMessage(R.string.cannot_edit_property)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                } else
                    Toast.makeText(PropertiesActivity.this, R.string.property_not_selected, Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.fabViewProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewPropertySelected) {
                    try {
                        final Intent intent = new Intent(PropertiesActivity.this, PropertyDetailActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                } else
                    Toast.makeText(PropertiesActivity.this, R.string.property_not_selected, Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.fabSearchProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PropertiesActivity.this, PropertySearchActivity.class);
                startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
            }
        });
        findViewById(R.id.fabLocationProperty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PropertiesActivity.this, MapsActivity.class);
                String sList = "";
                String sListSold = "";
                for (int i = 0; i < memProperties.size(); i++) {
                    String s = memProperties.get(i).propertyLatitude + "," + memProperties.get(i).propertyLongitude + "@" + memProperties.get(i).propertyAddress;
                    sList += s;
                    sListSold += memProperties.get(i).propertyStatus;
                    if (i != memProperties.size()-1) {
                        sList += "#";
                        sListSold += "#";
                    }
                }
                String curPos;
                if (viewPropertySelected)
                    curPos = curSelectedProperty.propertyLatitude + "," + curSelectedProperty.propertyLongitude + "@" + curSelectedProperty.propertyAddress;
                else
                    curPos = memProperties.get(0).propertyLatitude + "," + memProperties.get(0).propertyLongitude + "@" + memProperties.get(0).propertyAddress;
                intent.putExtra("curProperty", curPos);
                intent.putExtra("listProperty", sList);
                intent.putExtra("listSoldProperty", sListSold);
                startActivity(intent);
            }
        });

        viewModel = new ViewModelProvider(this).get(PropertiesActivityViewModel.class);

        observeProperties();
    }

    /**
     * Function onActivityResult
     * This method is called when the second activity finishes
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("searchData");
                sSearchCriteria = returnString;
            }
        }
    }

    /**
     * Function onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        //so the list is updated each time the screen goes to foreground
        viewPropertySelected = false;
        if (sSearchCriteria != "") {
            viewModel.loadPropertiesSearch(sSearchCriteria);
            getSupportActionBar().setTitle(R.string.property_result_search);
        } else
            viewModel.loadProperties();
    }

    /**
     * Function onSupportNavigateUp
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (sSearchCriteria != "") {
            sSearchCriteria = "";
            getSupportActionBar().setTitle(getResources().getString(R.string.property_list));
            onResume();
        } else {
            viewPropertySelected = false;
            finish();
        }
        return true;
    }

    /**
     * Function onCreateOptionsMenu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_property, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Function onOptionsItemSelected
     * This function processes the menu item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortId:
                viewModel.updateOrder(OrderState.sortId);
                break;
            case R.id.sortIdAsc:
                viewModel.updateOrder(OrderState.sortIdAsc);
                break;
            case R.id.sortIdDes:
                viewModel.updateOrder(OrderState.sortIdDes);
                break;
            case R.id.sortHouse:
                viewModel.updateOrder(OrderState.sortHouse);
                break;
            case R.id.sortFlat:
                viewModel.updateOrder(OrderState.sortFlat);
                break;
            case R.id.sortOffice:
                viewModel.updateOrder(OrderState.sortOffice);
                break;
            case R.id.sortStudent:
                viewModel.updateOrder(OrderState.sortStudent);
                break;
            case R.id.sortPriceAsc:
                viewModel.updateOrder(OrderState.sortPriceAsc);
                break;
            case R.id.sortPriceDes:
                viewModel.updateOrder(OrderState.sortPriceDes);
                break;
            case R.id.sortSurfaceAsc:
                viewModel.updateOrder(OrderState.sortSurfaceAsc);
                break;
            case R.id.sortSurfaceDes:
                viewModel.updateOrder(OrderState.sortSurfaceDes);
                break;
            case R.id.profile:
                final Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function observeProperties
     */
    private void observeProperties() {
        viewModel.properties.observe(this, new Observer<List<Property>>() {
            @Override
            public void onChanged(List<Property> properties) {
                memProperties = properties;
                final PropertiesAdapter propertiesAdapter = new PropertiesAdapter(properties);
                recyclerView.setAdapter(propertiesAdapter);
            }
        });
    }

}