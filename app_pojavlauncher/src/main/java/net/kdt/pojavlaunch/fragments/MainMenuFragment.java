package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import git.artdeell.mojo.R;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.instances.DisplayInstance;
import net.kdt.pojavlaunch.instances.Instances;
import net.kdt.pojavlaunch.instances.InstanceCardAdapter;

import java.io.IOException;

/**
 * Custom controls / execute-jar / share logs / open game directory used to be
 * plain buttons in this fragment's list; they now live as icon buttons in the
 * right side bar (see LauncherActivity), wired there since they need to stay
 * reachable from the whole activity, not just this fragment.
 *
 * The single mc_version_spinner row was replaced by instance_card_recycler: a
 * horizontal row of cards, one per installed instance, each with its own
 * Play/Manage buttons - see InstanceCardAdapter.
 */
public class MainMenuFragment extends Fragment implements InstanceCardAdapter.Listener {
    public static final String TAG = "MainMenuFragment";

    private final InstanceCardAdapter mInstanceCardAdapter = new InstanceCardAdapter(this);

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mNewsButton = view.findViewById(R.id.news_button);
        Button mDiscordButton = view.findViewById(R.id.social_media_button);
        RecyclerView instanceCardRecycler = view.findViewById(R.id.instance_card_recycler);
        instanceCardRecycler.setAdapter(mInstanceCardAdapter);

        mNewsButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));
        mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.social_media_invite)));

        mNewsButton.setOnLongClickListener((v)->{
            Tools.swapFragment(requireActivity(), GamepadMapperFragment.class, GamepadMapperFragment.TAG, null);
            return true;
        });

        reloadInstances();
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
        reloadInstances();
    }

    private void reloadInstances() {
        PojavApplication.sExecutorService.execute(()->{
            try {
                Instances instances = Instances.loadDisplay();
                Tools.runOnUiThread(()->mInstanceCardAdapter.submitInstances(instances.list));
            } catch (final IOException e) {
                Tools.runOnUiThread(()->Tools.showError(requireContext(), e));
            }
        });
    }

    @Override
    public void onPlayInstance(DisplayInstance instance) {
        Instances.setSelectedInstance(instance);
        ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
    }

    @Override
    public void onManageInstance(DisplayInstance instance) {
        Instances.setSelectedInstance(instance);
        Tools.swapFragment(requireActivity(), InstanceEditorFragment.class, InstanceEditorFragment.TAG, null);
    }

    @Override
    public void onAddInstance() {
        Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment.class, ProfileTypeSelectFragment.TAG, null);
    }
}
