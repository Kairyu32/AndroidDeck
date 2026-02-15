package com.kairyu.remotelauncher;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;


public class MainActivity extends AppCompatActivity {

    private EditText ipInput;
    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private List<AppItem> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipInput = findViewById(R.id.ipInput);
        recyclerView = findViewById(R.id.recyclerView);
        EditText searchBar = findViewById(R.id.searchBar); // New Search Bar

        // 1. SWITCH TO GRID LAYOUT (3 Columns)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new AppAdapter(appList, item -> {
            sendCommand(item.path);
        });
        recyclerView.setAdapter(adapter);

        // 2. SEARCH LISTENER
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the adapter
                adapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load saved custom apps
        loadCustomApps();

        // Buttons
        findViewById(R.id.btnScan).setOnClickListener(v -> {
            String ip = ipInput.getText().toString();
            if(!ip.isEmpty()) scanPC(ip);
        });

        findViewById(R.id.btnAddCustom).setOnClickListener(v -> showAddDialog());

        // Logic for saving computers
        findViewById(R.id.btnSaveIp).setOnClickListener(v -> {
            String currentIp = ipInput.getText().toString();
            if (currentIp.isEmpty()) {
                Toast.makeText(this, "Enter an IP first!", Toast.LENGTH_SHORT).show();
                return;
            }
            showSaveComputerDialog(currentIp);
        });

        findViewById(R.id.btnLoadIp).setOnClickListener(v -> {
            showSelectComputerDialog();
        });
    }

    private void showSaveComputerDialog(String ipToSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Computer");

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Nickname (e.g. My PC)");
        builder.setView(nameInput);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString();
            if (!name.isEmpty()) {
                StorageManager.saveComputer(this, new SavedComputer(name, ipToSave));
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Dialog to pick from saved PCs
    private void showSelectComputerDialog() {
        List<SavedComputer> savedList = StorageManager.getSavedComputers(this);

        if (savedList.isEmpty()) {
            Toast.makeText(this, "No saved computers yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the list to string array for dialog
        String[] names = new String[savedList.size()];
        for (int i = 0; i < savedList.size(); i++) {
            names[i] = savedList.get(i).name + "\n" + savedList.get(i).ip;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Computer");
        builder.setItems(names, (dialog, which) -> {
            // User clicks item at index 'which'
            SavedComputer selected = savedList.get(which);
            ipInput.setText(selected.ip);
            Toast.makeText(this, "Selected: " + selected.name, Toast.LENGTH_SHORT).show();

            // After user selects, scan the selected PC
            scanPC(selected.ip);
        });
        builder.show();
    }

    private void scanPC(String ip) {
        new Thread(() -> {
            try {
                // 1. Connect to Server
                Socket socket = new Socket(ip, 5000);
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                // 2. Send Command
                out.print("SCAN");
                out.flush();

                // 3. Read Response (The Big JSON String)
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();

                // Close connection immediately after reading
                socket.close();

                // 4. Process Data on UI Thread
                if(response != null) {
                    JSONArray json = new JSONArray(response);

                    runOnUiThread(() -> {
                        // Create a BRAND NEW list to hold the updated data
                        List<AppItem> newFullList = new ArrayList<>();

                        // Step A: Keep your existing "Custom" apps
                        // We look at the old list and copy over only the custom ones
                        for (AppItem item : appList) {
                            if (item.isCustom) {
                                newFullList.add(item);
                            }
                        }

                        // Step B: Add the fresh "Scanned" apps from the server
                        for(int i=0; i<json.length(); i++) {
                            JSONObject obj = json.optJSONObject(i);
                            String name = obj.optString("name");
                            String path = obj.optString("path");

                            // Create new item (isCustom = false)
                            newFullList.add(new AppItem(name, path, false));
                        }

                        // Step C: Update the Master List reference
                        appList = newFullList;

                        // Step D: Tell Adapter to update
                        // This uses the special method we wrote to handle the search filter correctly
                        if (adapter != null) {
                            adapter.updateList(appList);
                        }

                        Toast.makeText(this, "Found " + json.length() + " apps", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void sendCommand(String path) {
        String ip = ipInput.getText().toString();
        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, 5000);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.print("EXEC|" + path);
                out.flush();
                socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom App");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameBox = new EditText(this);
        nameBox.setHint("Name (e.g. My Game)");
        layout.addView(nameBox);

        final EditText pathBox = new EditText(this);
        pathBox.setHint("Full Path (C:\\Games\\game.exe)");
        layout.addView(pathBox);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            AppItem newItem = new AppItem(nameBox.getText().toString(), pathBox.getText().toString(), true);
            saveCustomApp(newItem);
            appList.add(newItem);
            adapter.notifyDataSetChanged();
        });
        builder.show();
    }

    // --- Simple Storage Logic ---
    private void saveCustomApp(AppItem item) {
        SharedPreferences prefs = getSharedPreferences("MyLauncher", Context.MODE_PRIVATE);
        String currentJson = prefs.getString("custom_apps", "[]");
        try {
            JSONArray arr = new JSONArray(currentJson);
            JSONObject obj = new JSONObject();
            obj.put("name", item.name);
            obj.put("path", item.path);
            arr.put(obj);
            prefs.edit().putString("custom_apps", arr.toString()).apply();
        } catch(Exception e) {}
    }

    private void loadCustomApps() {
        SharedPreferences prefs = getSharedPreferences("MyLauncher", Context.MODE_PRIVATE);
        String currentJson = prefs.getString("custom_apps", "[]");
        try {
            JSONArray arr = new JSONArray(currentJson);
            for(int i=0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                appList.add(new AppItem(obj.getString("name"), obj.getString("path"), true));
            }
            adapter.notifyDataSetChanged();
        } catch(Exception e) {}
    }
}