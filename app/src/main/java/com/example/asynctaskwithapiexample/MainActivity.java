package com.example.asynctaskwithapiexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asynctaskwithapiexample.utilities.ApiDataReader;
import com.example.asynctaskwithapiexample.utilities.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvItems;
    private TextView tvStatus;
    private ArrayAdapter listAdapter;
    private List<String> listOfCurrencies; // Retained the original list of currencies
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.lvItems = findViewById(R.id.lv_items);
        this.tvStatus = findViewById(R.id.tv_status);
        this.listOfCurrencies = new ArrayList<>();

        this.listAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        this.lvItems.setAdapter(this.listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission (if needed)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text change
                filterCurrencyList(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void onBtnGetDataClick(View view) {
        fetchData();
    }

    private void fetchData() {
        fetchDataByThread();
        Toast.makeText(this, R.string.msg_using_thread, Toast.LENGTH_LONG).show();
    }

    public void fetchDataByThread() {
        this.tvStatus.setText(R.string.loading_data);
        Runnable fetchDataAndDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = ApiDataReader.getValuesFromApi(Constants.FLOATRATES_API_URL);
                    Runnable updateUIRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // Updated: Load all data initially, but do not filter here
                            listOfCurrencies = parseCurrencyData(result);
                            filterCurrencyList(searchView.getQuery().toString());
                        }
                    };
                    runOnUiThread(updateUIRunnable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(fetchDataAndDisplayRunnable);
        thread.start();
        // Removed: Setting status text here; it will be set after data is loaded
    }

    private List<String> parseCurrencyData(String data) {
        List<String> currencyList = new ArrayList<>();

        String[] currencyArray = data.split("\n");

        for (String currency : currencyArray) {
            String[] currencyParts = currency.split(",");
            if (currencyParts.length == 2) {
                String currencyName = currencyParts[0].trim();
                String rate = currencyParts[1].trim();

                currencyList.add(currency);
            }
        }
        return currencyList;
    }



    private void filterCurrencyList(String filter) {
        List<String> filteredList = new ArrayList<>();
        for (String currency : listOfCurrencies) {
            if (currency.toLowerCase().contains(filter.toLowerCase())) {
                filteredList.add(currency);
            }
        }
        updateUI(filteredList);
    }



    private void updateUI(List<String> dataList) {
        listAdapter.clear();
        listAdapter.addAll(dataList);
        listAdapter.notifyDataSetChanged();
        this.tvStatus.setText(R.string.data_loaded); // Moved setting status text here
    }
}
