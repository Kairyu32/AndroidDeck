package com.kairyu.remotelauncher;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> implements Filterable {

    private List<AppItem> appsFull; // Keep a copy of the full list
    private List<AppItem> appsDisplayed; // The list currently on screen
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AppItem item);
    }

    public AppAdapter(List<AppItem> apps, OnItemClickListener listener) {
        this.appsFull = new ArrayList<>(apps);
        this.appsDisplayed = apps;
        this.listener = listener;
    }

    // Call this to update data instead of accessing list directly
    public void updateList(List<AppItem> newApps) {
        this.appsFull = new ArrayList<>(newApps);
        this.appsDisplayed = new ArrayList<>(newApps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use our new Card Layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = appsDisplayed.get(position);

        holder.nameText.setText(app.name);

        // Generate "Icon"
        String initial = app.name.isEmpty() ? "?" : app.name.substring(0, 1).toUpperCase();
        holder.iconText.setText(initial);

        // Generate a random-looking but consistent color based on the name
        int color = generateColor(app.name);
        holder.iconBackground.setCardBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(app));
    }

    @Override
    public int getItemCount() { return appsDisplayed.size(); }

    // --- Search Logic ---
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<AppItem> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(appsFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (AppItem item : appsFull) {
                        if (item.name.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                appsDisplayed.clear();
                appsDisplayed.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private int generateColor(String name) {
        // Use hash code to make sure "Chrome" is always the same color
        Random rnd = new Random(name.hashCode());
        return Color.argb(255, rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, iconText;
        CardView iconBackground;

        public ViewHolder(View v) {
            super(v);
            nameText = v.findViewById(R.id.appName);
            iconText = v.findViewById(R.id.iconText);
            iconBackground = (CardView) iconText.getParent();
        }
    }
}