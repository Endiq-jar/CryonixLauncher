package net.kdt.pojavlaunch.instances;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import git.artdeell.mojo.R;

import net.kdt.pojavlaunch.Tools;

import java.util.Collections;
import java.util.List;

/**
 * Shows every installed instance as a card with its own Play/Manage buttons,
 * plus a trailing "add instance" card - replaces the single mc_version_spinner
 * row on the main menu.
 */
public class InstanceCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_INSTANCE = 0;
    private static final int TYPE_ADD = 1;

    public interface Listener {
        /** The user tapped Play on a specific instance */
        void onPlayInstance(DisplayInstance instance);
        /** The user tapped Manage on a specific instance */
        void onManageInstance(DisplayInstance instance);
        /** The user tapped the trailing "add instance" card */
        void onAddInstance();
    }

    private final Listener mListener;
    private List<DisplayInstance> mInstances = Collections.emptyList();

    public InstanceCardAdapter(Listener listener) {
        mListener = listener;
    }

    /** Swap in a freshly-loaded instance list (see Instances.loadDisplay()) */
    public void submitInstances(List<DisplayInstance> instances) {
        mInstances = instances;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        // +1 for the trailing "add instance" card
        return mInstances.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < mInstances.size() ? TYPE_INSTANCE : TYPE_ADD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADD) {
            View view = inflater.inflate(R.layout.item_instance_card_add, parent, false);
            return new AddViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_instance_card, parent, false);
        return new InstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InstanceViewHolder) {
            ((InstanceViewHolder) holder).bind(mInstances.get(position));
        }
    }

    private class InstanceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mIcon;
        private final TextView mName;
        private final TextView mVersion;
        private final Button mManageButton;
        private final Button mPlayButton;

        InstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.instance_card_icon);
            mName = itemView.findViewById(R.id.instance_card_name);
            mVersion = itemView.findViewById(R.id.instance_card_version);
            mManageButton = itemView.findViewById(R.id.instance_card_manage_button);
            mPlayButton = itemView.findViewById(R.id.instance_card_play_button);
        }

        void bind(DisplayInstance instance) {
            mIcon.setImageDrawable(InstanceIconProvider.fetchIcon(itemView.getResources(), instance));

            String profileName = Tools.validOrNullString(instance.name);
            String versionName = Tools.validOrNullString(instance.versionId);
            if (Instance.VERSION_LATEST_RELEASE.equalsIgnoreCase(versionName))
                versionName = itemView.getContext().getString(R.string.profiles_latest_release);
            else if (Instance.VERSION_LATEST_SNAPSHOT.equalsIgnoreCase(versionName))
                versionName = itemView.getContext().getString(R.string.profiles_latest_snapshot);

            mName.setText(profileName != null ? profileName : versionName);
            mVersion.setText(versionName != null ? versionName : "");
            mVersion.setVisibility(profileName != null && versionName != null ? View.VISIBLE : View.GONE);

            mPlayButton.setOnClickListener(v -> mListener.onPlayInstance(instance));
            mManageButton.setOnClickListener(v -> mListener.onManageInstance(instance));
        }
    }

    private class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> mListener.onAddInstance());
        }
    }
}
